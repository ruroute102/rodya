"""API пользователей: профиль, автомобили, активность."""

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.api.deps import get_current_user
from app.db.session import get_db
from app.models.user import User, UserCar
from app.schemas.user import UserCarCreate, UserCarOut, UserOut, UserUpdate

router = APIRouter(prefix="/users", tags=["Пользователи"])


@router.get("/me", response_model=UserOut, summary="Мой профиль")
async def get_me(user: User = Depends(get_current_user)):
    return user


@router.patch("/me", response_model=UserOut, summary="Обновить профиль")
async def update_me(
    data: UserUpdate,
    db: AsyncSession = Depends(get_db),
    user: User = Depends(get_current_user),
):
    if data.display_name is not None:
        user.display_name = data.display_name
    if data.avatar_url is not None:
        user.avatar_url = data.avatar_url
    return user


# ── Автомобили пользователя ─────────────────────────────────────

@router.get("/me/cars", response_model=list[UserCarOut], summary="Мои автомобили")
async def list_my_cars(
    db: AsyncSession = Depends(get_db),
    user: User = Depends(get_current_user),
):
    result = await db.execute(
        select(UserCar).where(UserCar.user_id == user.id)
    )
    return result.scalars().all()


@router.post("/me/cars", response_model=UserCarOut, summary="Добавить автомобиль")
async def add_car(
    data: UserCarCreate,
    db: AsyncSession = Depends(get_db),
    user: User = Depends(get_current_user),
):
    # Если первый авто или is_primary, сбрасываем primary у остальных
    if data.is_primary:
        result = await db.execute(
            select(UserCar).where(UserCar.user_id == user.id, UserCar.is_primary == True)  # noqa: E712
        )
        for car in result.scalars().all():
            car.is_primary = False

    car = UserCar(user_id=user.id, **data.model_dump())
    db.add(car)
    await db.flush()
    return car


@router.delete("/me/cars/{car_id}", summary="Удалить автомобиль")
async def remove_car(
    car_id: int,
    db: AsyncSession = Depends(get_db),
    user: User = Depends(get_current_user),
):
    result = await db.execute(
        select(UserCar).where(UserCar.id == car_id, UserCar.user_id == user.id)
    )
    car = result.scalar_one_or_none()
    if car is None:
        raise HTTPException(status_code=404, detail="Автомобиль не найден")

    await db.delete(car)
    return {"message": "Автомобиль удалён"}
