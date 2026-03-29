"""Роутер API v1: объединяет все эндпоинты."""

from fastapi import APIRouter

from app.api.v1.endpoints.auth import router as auth_router
from app.api.v1.endpoints.cars import router as cars_router
from app.api.v1.endpoints.comments import router as comments_router
from app.api.v1.endpoints.diagnostics import router as diagnostics_router
from app.api.v1.endpoints.guides import router as guides_router
from app.api.v1.endpoints.techspecs import router as techspecs_router
from app.api.v1.endpoints.users import router as users_router

api_router = APIRouter(prefix="/api/v1")

api_router.include_router(auth_router)
api_router.include_router(cars_router)
api_router.include_router(guides_router)
api_router.include_router(comments_router)
api_router.include_router(users_router)
api_router.include_router(techspecs_router)
api_router.include_router(diagnostics_router)
