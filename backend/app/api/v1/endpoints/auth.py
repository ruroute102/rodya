"""API авторизации: регистрация, вход, OTP, refresh."""

from datetime import datetime, timedelta, timezone

from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.security import (
    create_access_token,
    create_email_verification_token,
    create_refresh_token,
    decode_token,
    generate_otp,
    hash_password,
    verify_password,
)
from app.db.session import get_db
from app.models.user import RefreshToken, User
from app.schemas.user import (
    EmailVerifyRequest,
    LoginRequest,
    PhoneOTPRequest,
    PhoneOTPVerify,
    RefreshRequest,
    TokenPair,
    UserOut,
)

router = APIRouter(prefix="/auth", tags=["Авторизация"])

# Временное хранилище OTP (в продакшене — Redis)
_otp_store: dict[str, tuple[str, datetime]] = {}


@router.post("/otp/request", summary="Запросить OTP-код на телефон")
async def request_otp(data: PhoneOTPRequest):
    """Отправляет 6-значный код на указанный номер телефона."""
    otp = generate_otp()
    _otp_store[data.phone] = (otp, datetime.now(timezone.utc) + timedelta(minutes=5))

    # TODO: Отправка SMS через SMS.ru / Twilio
    # В разработке просто возвращаем код (убрать в продакшене!)
    return {"message": "Код отправлен", "debug_code": otp}


@router.post("/register", response_model=TokenPair, summary="Регистрация по OTP")
async def register(data: PhoneOTPVerify, db: AsyncSession = Depends(get_db)):
    """Проверяет OTP и создаёт аккаунт."""
    # Проверяем OTP
    stored = _otp_store.get(data.phone)
    if stored is None:
        raise HTTPException(status_code=400, detail="Сначала запросите код")

    otp_code, expires = stored
    if datetime.now(timezone.utc) > expires:
        _otp_store.pop(data.phone, None)
        raise HTTPException(status_code=400, detail="Код истёк")

    if data.code != otp_code:
        raise HTTPException(status_code=400, detail="Неверный код")

    _otp_store.pop(data.phone, None)

    # Проверяем, не занят ли телефон
    existing = await db.execute(select(User).where(User.phone == data.phone))
    if existing.scalar_one_or_none():
        raise HTTPException(status_code=409, detail="Телефон уже зарегистрирован")

    # Создаём пользователя
    user = User(
        phone=data.phone,
        password_hash=hash_password(data.password),
        display_name=data.display_name,
    )
    db.add(user)
    await db.flush()

    # Создаём токены
    access = create_access_token(str(user.id), {"role": user.role.value})
    refresh = create_refresh_token(str(user.id))

    # Сохраняем refresh token
    payload = decode_token(refresh)
    rt = RefreshToken(
        user_id=user.id,
        jti=payload["jti"],
        expires_at=datetime.fromtimestamp(payload["exp"], tz=timezone.utc),
    )
    db.add(rt)

    return TokenPair(access_token=access, refresh_token=refresh)


@router.post("/login", response_model=TokenPair, summary="Вход по телефону и паролю")
async def login(data: LoginRequest, db: AsyncSession = Depends(get_db)):
    """Авторизация по номеру телефона и паролю."""
    result = await db.execute(select(User).where(User.phone == data.phone))
    user = result.scalar_one_or_none()

    if user is None or not verify_password(data.password, user.password_hash):
        raise HTTPException(status_code=401, detail="Неверный телефон или пароль")

    if not user.is_active or user.is_banned:
        raise HTTPException(status_code=403, detail="Аккаунт заблокирован")

    # Обновляем last_login
    user.last_login = datetime.now(timezone.utc)

    access = create_access_token(str(user.id), {"role": user.role.value})
    refresh = create_refresh_token(str(user.id))

    payload = decode_token(refresh)
    rt = RefreshToken(
        user_id=user.id,
        jti=payload["jti"],
        expires_at=datetime.fromtimestamp(payload["exp"], tz=timezone.utc),
    )
    db.add(rt)

    return TokenPair(access_token=access, refresh_token=refresh)


@router.post("/refresh", response_model=TokenPair, summary="Обновить access-токен")
async def refresh_tokens(data: RefreshRequest, db: AsyncSession = Depends(get_db)):
    """Обновляет пару токенов через refresh token (ротация)."""
    payload = decode_token(data.refresh_token)
    if payload is None or payload.get("type") != "refresh":
        raise HTTPException(status_code=401, detail="Недействительный refresh-токен")

    jti = payload.get("jti")
    user_id = int(payload["sub"])

    # Проверяем, не отозван ли токен
    result = await db.execute(
        select(RefreshToken).where(
            RefreshToken.jti == jti,
            RefreshToken.is_revoked == False,  # noqa: E712
        )
    )
    stored_rt = result.scalar_one_or_none()
    if stored_rt is None:
        raise HTTPException(status_code=401, detail="Токен отозван или не найден")

    # Отзываем старый refresh token
    stored_rt.is_revoked = True

    # Выпускаем новую пару
    result = await db.execute(select(User).where(User.id == user_id))
    user = result.scalar_one_or_none()
    if user is None or not user.is_active:
        raise HTTPException(status_code=401, detail="Пользователь не найден")

    access = create_access_token(str(user.id), {"role": user.role.value})
    refresh = create_refresh_token(str(user.id))

    new_payload = decode_token(refresh)
    new_rt = RefreshToken(
        user_id=user.id,
        jti=new_payload["jti"],
        expires_at=datetime.fromtimestamp(new_payload["exp"], tz=timezone.utc),
    )
    db.add(new_rt)

    return TokenPair(access_token=access, refresh_token=refresh)


@router.post("/logout", summary="Выход (отзыв refresh-токена)")
async def logout(data: RefreshRequest, db: AsyncSession = Depends(get_db)):
    """Отзывает refresh-токен."""
    payload = decode_token(data.refresh_token)
    if payload and payload.get("jti"):
        result = await db.execute(
            select(RefreshToken).where(RefreshToken.jti == payload["jti"])
        )
        rt = result.scalar_one_or_none()
        if rt:
            rt.is_revoked = True

    return {"message": "Вы вышли из системы"}
