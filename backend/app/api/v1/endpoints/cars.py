"""API автомобилей: марки, модели, поколения, двигатели, конфигурации."""

from fastapi import APIRouter, Depends, HTTPException
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import selectinload

from app.api.deps import get_current_user, require_role
from app.db.session import get_db
from app.models.car import (
    CarBody,
    CarBrand,
    CarConfiguration,
    CarEngine,
    CarGeneration,
    CarModel,
    CarTrim,
)
from app.models.user import User, UserRole
from app.schemas.car import (
    CarBodyOut,
    CarBrandCreate,
    CarBrandOut,
    CarBrandWithModels,
    CarConfigurationCreate,
    CarConfigurationOut,
    CarEngineCreate,
    CarEngineOut,
    CarGenerationCreate,
    CarGenerationFull,
    CarGenerationOut,
    CarModelCreate,
    CarModelOut,
    CarModelWithGenerations,
    CarTrimOut,
)

router = APIRouter(prefix="/cars", tags=["Автомобили"])


# ── Марки ───────────────────────────────────────────────────────

@router.get("/brands", response_model=list[CarBrandOut], summary="Все марки")
async def list_brands(db: AsyncSession = Depends(get_db)):
    result = await db.execute(
        select(CarBrand)
        .where(CarBrand.is_active == True)  # noqa: E712
        .order_by(CarBrand.sort_order, CarBrand.name)
    )
    return result.scalars().all()


@router.post(
    "/brands",
    response_model=CarBrandOut,
    summary="Создать марку (admin/editor)",
)
async def create_brand(
    data: CarBrandCreate,
    db: AsyncSession = Depends(get_db),
    user: User = Depends(require_role(UserRole.ADMIN, UserRole.EDITOR)),
):
    brand = CarBrand(**data.model_dump())
    db.add(brand)
    await db.flush()
    return brand


# ── Модели ──────────────────────────────────────────────────────

@router.get(
    "/brands/{brand_id}/models",
    response_model=list[CarModelOut],
    summary="Модели по марке",
)
async def list_models(brand_id: int, db: AsyncSession = Depends(get_db)):
    result = await db.execute(
        select(CarModel)
        .where(CarModel.brand_id == brand_id, CarModel.is_active == True)  # noqa: E712
        .order_by(CarModel.sort_order, CarModel.name)
    )
    return result.scalars().all()


@router.post(
    "/models",
    response_model=CarModelOut,
    summary="Создать модель (admin/editor)",
)
async def create_model(
    data: CarModelCreate,
    db: AsyncSession = Depends(get_db),
    user: User = Depends(require_role(UserRole.ADMIN, UserRole.EDITOR)),
):
    model = CarModel(**data.model_dump())
    db.add(model)
    await db.flush()
    return model


# ── Поколения ───────────────────────────────────────────────────

@router.get(
    "/models/{model_id}/generations",
    response_model=list[CarGenerationOut],
    summary="Поколения по модели",
)
async def list_generations(model_id: int, db: AsyncSession = Depends(get_db)):
    result = await db.execute(
        select(CarGeneration)
        .where(CarGeneration.model_id == model_id, CarGeneration.is_active == True)  # noqa: E712
        .order_by(CarGeneration.year_start.desc())
    )
    return result.scalars().all()


@router.get(
    "/generations/{generation_id}",
    response_model=CarGenerationFull,
    summary="Полная информация о поколении (двигатели, кузова, комплектации)",
)
async def get_generation(generation_id: int, db: AsyncSession = Depends(get_db)):
    result = await db.execute(
        select(CarGeneration)
        .options(
            selectinload(CarGeneration.engines),
            selectinload(CarGeneration.bodies),
            selectinload(CarGeneration.trims),
        )
        .where(CarGeneration.id == generation_id)
    )
    gen = result.scalar_one_or_none()
    if gen is None:
        raise HTTPException(status_code=404, detail="Поколение не найдено")
    return gen


@router.post(
    "/generations",
    response_model=CarGenerationOut,
    summary="Создать поколение (admin/editor)",
)
async def create_generation(
    data: CarGenerationCreate,
    db: AsyncSession = Depends(get_db),
    user: User = Depends(require_role(UserRole.ADMIN, UserRole.EDITOR)),
):
    gen = CarGeneration(**data.model_dump())
    db.add(gen)
    await db.flush()
    return gen


# ── Двигатели ───────────────────────────────────────────────────

@router.get(
    "/generations/{generation_id}/engines",
    response_model=list[CarEngineOut],
    summary="Двигатели поколения",
)
async def list_engines(generation_id: int, db: AsyncSession = Depends(get_db)):
    result = await db.execute(
        select(CarEngine)
        .where(CarEngine.generation_id == generation_id, CarEngine.is_active == True)  # noqa: E712
    )
    return result.scalars().all()


@router.post(
    "/engines",
    response_model=CarEngineOut,
    summary="Создать двигатель (admin/editor)",
)
async def create_engine(
    data: CarEngineCreate,
    db: AsyncSession = Depends(get_db),
    user: User = Depends(require_role(UserRole.ADMIN, UserRole.EDITOR)),
):
    engine = CarEngine(**data.model_dump())
    db.add(engine)
    await db.flush()
    return engine


# ── Конфигурации ────────────────────────────────────────────────

@router.get(
    "/generations/{generation_id}/configurations",
    response_model=list[CarConfigurationOut],
    summary="Конфигурации поколения",
)
async def list_configurations(generation_id: int, db: AsyncSession = Depends(get_db)):
    result = await db.execute(
        select(CarConfiguration)
        .where(
            CarConfiguration.generation_id == generation_id,
            CarConfiguration.is_active == True,  # noqa: E712
        )
    )
    return result.scalars().all()


@router.post(
    "/configurations",
    response_model=CarConfigurationOut,
    summary="Создать конфигурацию (admin/editor)",
)
async def create_configuration(
    data: CarConfigurationCreate,
    db: AsyncSession = Depends(get_db),
    user: User = Depends(require_role(UserRole.ADMIN, UserRole.EDITOR)),
):
    config = CarConfiguration(**data.model_dump())
    db.add(config)
    await db.flush()
    return config
