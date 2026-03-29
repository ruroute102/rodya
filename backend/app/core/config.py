"""Конфигурация приложения. Все настройки загружаются из переменных окружения."""

from pydantic_settings import BaseSettings, SettingsConfigDict


class Settings(BaseSettings):
    model_config = SettingsConfigDict(
        env_file=".env",
        env_file_encoding="utf-8",
        case_sensitive=False,
    )

    # ── Приложение ──────────────────────────────────────────────
    app_name: str = "ТехГид"
    app_version: str = "0.1.0"
    debug: bool = False
    allowed_origins: list[str] = ["http://localhost:3000", "http://localhost:5173"]

    # ── База данных ─────────────────────────────────────────────
    database_url: str = "postgresql+asyncpg://techgid:techgid_secret@localhost:5432/techgid"
    database_url_sync: str = "postgresql://techgid:techgid_secret@localhost:5432/techgid"

    # ── Redis ───────────────────────────────────────────────────
    redis_url: str = "redis://localhost:6379/0"

    # ── JWT ─────────────────────────────────────────────────────
    jwt_secret_key: str = "CHANGE_ME_TO_RANDOM_64_CHAR_STRING"
    jwt_algorithm: str = "HS256"
    access_token_expire_minutes: int = 15
    refresh_token_expire_days: int = 30

    # ── S3 / MinIO ──────────────────────────────────────────────
    s3_endpoint: str = "http://localhost:9000"
    s3_access_key: str = "minioadmin"
    s3_secret_key: str = "minioadmin"
    s3_bucket_name: str = "techgid"
    s3_region: str = "us-east-1"

    # ── SMS ─────────────────────────────────────────────────────
    sms_api_key: str = ""
    sms_sender: str = "TechGid"

    # ── Email ───────────────────────────────────────────────────
    smtp_host: str = "smtp.yandex.ru"
    smtp_port: int = 465
    smtp_user: str = ""
    smtp_password: str = ""
    email_from: str = ""

    # ── Шифрование контента ─────────────────────────────────────
    content_encryption_key: str = "CHANGE_ME_TO_32_BYTE_HEX_STRING"

    # ── Rate limiting ───────────────────────────────────────────
    rate_limit_per_minute: int = 100
    auth_rate_limit_per_minute: int = 5


settings = Settings()
