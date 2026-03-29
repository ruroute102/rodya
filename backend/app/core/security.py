"""Утилиты безопасности: хэширование паролей, JWT-токены, OTP."""

import secrets
import string
from datetime import datetime, timedelta, timezone

from jose import JWTError, jwt
from passlib.context import CryptContext

from app.core.config import settings

# ── Хэширование паролей ────────────────────────────────────────

pwd_context = CryptContext(schemes=["bcrypt"], deprecated="auto")


def hash_password(password: str) -> str:
    """Хэширует пароль с bcrypt."""
    return pwd_context.hash(password)


def verify_password(plain_password: str, hashed_password: str) -> bool:
    """Проверяет пароль против хэша."""
    return pwd_context.verify(plain_password, hashed_password)


# ── JWT-токены ──────────────────────────────────────────────────

def create_access_token(subject: str, extra_claims: dict | None = None) -> str:
    """Создаёт access token (короткоживущий)."""
    now = datetime.now(timezone.utc)
    expire = now + timedelta(minutes=settings.access_token_expire_minutes)
    payload = {
        "sub": subject,
        "iat": now,
        "exp": expire,
        "type": "access",
    }
    if extra_claims:
        payload.update(extra_claims)
    return jwt.encode(payload, settings.jwt_secret_key, algorithm=settings.jwt_algorithm)


def create_refresh_token(subject: str) -> str:
    """Создаёт refresh token (долгоживущий)."""
    now = datetime.now(timezone.utc)
    expire = now + timedelta(days=settings.refresh_token_expire_days)
    payload = {
        "sub": subject,
        "iat": now,
        "exp": expire,
        "type": "refresh",
        "jti": secrets.token_hex(16),  # уникальный ID для отзыва
    }
    return jwt.encode(payload, settings.jwt_secret_key, algorithm=settings.jwt_algorithm)


def decode_token(token: str) -> dict | None:
    """Декодирует и валидирует JWT. Возвращает None при ошибке."""
    try:
        payload = jwt.decode(
            token,
            settings.jwt_secret_key,
            algorithms=[settings.jwt_algorithm],
        )
        return payload
    except JWTError:
        return None


# ── OTP ─────────────────────────────────────────────────────────

def generate_otp(length: int = 6) -> str:
    """Генерирует цифровой OTP-код заданной длины."""
    return "".join(secrets.choice(string.digits) for _ in range(length))


# ── Email-токен ─────────────────────────────────────────────────

def create_email_verification_token(email: str) -> str:
    """Создаёт токен подтверждения email (24 часа)."""
    now = datetime.now(timezone.utc)
    expire = now + timedelta(hours=24)
    payload = {
        "sub": email,
        "exp": expire,
        "type": "email_verify",
    }
    return jwt.encode(payload, settings.jwt_secret_key, algorithm=settings.jwt_algorithm)
