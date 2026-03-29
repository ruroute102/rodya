"""Pydantic-схемы для пользователей и авторизации."""

from datetime import datetime

from pydantic import BaseModel, ConfigDict, EmailStr, Field

from app.models.user import UserRole


# ── Регистрация / Авторизация ───────────────────────────────────

class PhoneOTPRequest(BaseModel):
    """Запрос OTP-кода на телефон."""
    phone: str = Field(..., pattern=r"^\+7\d{10}$", examples=["+79001234567"])


class PhoneOTPVerify(BaseModel):
    """Верификация OTP и завершение регистрации."""
    phone: str = Field(..., pattern=r"^\+7\d{10}$")
    code: str = Field(..., min_length=6, max_length=6)
    display_name: str = Field(..., min_length=2, max_length=100)
    password: str = Field(..., min_length=8, max_length=128)


class LoginRequest(BaseModel):
    """Вход по телефону + пароль."""
    phone: str = Field(..., pattern=r"^\+7\d{10}$")
    password: str


class TokenPair(BaseModel):
    """Пара токенов: access + refresh."""
    access_token: str
    refresh_token: str
    token_type: str = "bearer"


class RefreshRequest(BaseModel):
    """Обновление access-токена через refresh-токен."""
    refresh_token: str


class EmailVerifyRequest(BaseModel):
    """Привязка и подтверждение email."""
    email: EmailStr


# ── Профиль ─────────────────────────────────────────────────────

class UserOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    phone: str
    email: str | None = None
    email_verified: bool = False
    display_name: str
    avatar_url: str | None = None
    role: UserRole
    is_verified_author: bool = False
    created_at: datetime


class UserUpdate(BaseModel):
    display_name: str | None = Field(None, min_length=2, max_length=100)
    avatar_url: str | None = None


class UserCarOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    configuration_id: int
    display_name: str | None = None
    is_primary: bool = False
    mileage_km: int | None = None


class UserCarCreate(BaseModel):
    configuration_id: int
    display_name: str | None = None
    is_primary: bool = False
    vin: str | None = Field(None, max_length=17)
    mileage_km: int | None = None
