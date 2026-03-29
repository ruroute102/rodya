"""Точка входа FastAPI-приложения ТехГид."""

from contextlib import asynccontextmanager

from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware

from app.api.v1.router import api_router
from app.core.config import settings


@asynccontextmanager
async def lifespan(app: FastAPI):
    """Startup/shutdown: подключения к БД, Redis и т.д."""
    # Startup
    yield
    # Shutdown
    from app.db.session import engine
    await engine.dispose()


app = FastAPI(
    title=settings.app_name,
    version=settings.app_version,
    description="API для приложения ТехГид — инструкции по ремонту автомобилей",
    docs_url="/docs" if settings.debug else None,
    redoc_url="/redoc" if settings.debug else None,
    lifespan=lifespan,
)

# CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.allowed_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Подключаем API v1
app.include_router(api_router)


@app.get("/health", tags=["Система"])
async def health_check():
    return {"status": "ok", "version": settings.app_version}
