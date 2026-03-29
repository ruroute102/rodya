"""API инструкций: каталог, поиск, создание, шаги."""

import math

from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import selectinload

from app.api.deps import get_current_user, get_current_user_optional, require_role
from app.db.session import get_db
from app.models.content import (
    Comment,
    CommentStatus,
    Component,
    ComponentCategory,
    ContentStatus,
    Difficulty,
    Guide,
    GuideConfiguration,
    GuideStep,
)
from app.models.user import User, UserRole
from app.schemas.content import (
    ComponentCategoryOut,
    ComponentCreate,
    ComponentOut,
    GuideCreate,
    GuideDetailOut,
    GuideListItem,
    GuideStepCreate,
    GuideStepOut,
    PaginatedResponse,
)

router = APIRouter(prefix="/guides", tags=["Инструкции"])


# ── Категории узлов ─────────────────────────────────────────────

@router.get(
    "/categories",
    response_model=list[ComponentCategoryOut],
    summary="Категории узлов",
)
async def list_categories(db: AsyncSession = Depends(get_db)):
    result = await db.execute(
        select(ComponentCategory)
        .where(ComponentCategory.is_active == True)  # noqa: E712
        .order_by(ComponentCategory.sort_order, ComponentCategory.name)
    )
    return result.scalars().all()


@router.get(
    "/categories/{category_id}/components",
    response_model=list[ComponentOut],
    summary="Узлы в категории",
)
async def list_components(category_id: int, db: AsyncSession = Depends(get_db)):
    result = await db.execute(
        select(Component)
        .where(Component.category_id == category_id, Component.is_active == True)  # noqa: E712
        .order_by(Component.name)
    )
    return result.scalars().all()


@router.post(
    "/components",
    response_model=ComponentOut,
    summary="Создать узел (admin/editor)",
)
async def create_component(
    data: ComponentCreate,
    db: AsyncSession = Depends(get_db),
    user: User = Depends(require_role(UserRole.ADMIN, UserRole.EDITOR)),
):
    comp = Component(**data.model_dump())
    db.add(comp)
    await db.flush()
    return comp


# ── Каталог инструкций ──────────────────────────────────────────

@router.get("/", response_model=PaginatedResponse, summary="Каталог инструкций")
async def list_guides(
    configuration_id: int | None = None,
    component_id: int | None = None,
    difficulty: Difficulty | None = None,
    search: str | None = None,
    page: int = Query(1, ge=1),
    page_size: int = Query(20, ge=1, le=100),
    db: AsyncSession = Depends(get_db),
):
    """Каталог инструкций с фильтрами и поиском."""
    query = select(Guide).where(Guide.status == ContentStatus.PUBLISHED)

    # Фильтр по конфигурации авто
    if configuration_id is not None:
        query = query.join(GuideConfiguration).where(
            GuideConfiguration.configuration_id == configuration_id
        )

    # Фильтр по узлу
    if component_id is not None:
        query = query.where(Guide.component_id == component_id)

    # Фильтр по сложности
    if difficulty is not None:
        query = query.where(Guide.difficulty == difficulty)

    # Полнотекстовый поиск
    if search:
        search_pattern = f"%{search}%"
        query = query.where(Guide.title.ilike(search_pattern))

    # Подсчёт общего количества
    count_query = select(func.count()).select_from(query.subquery())
    total_result = await db.execute(count_query)
    total = total_result.scalar() or 0

    # Пагинация
    query = (
        query
        .options(selectinload(Guide.component))
        .order_by(Guide.created_at.desc())
        .offset((page - 1) * page_size)
        .limit(page_size)
    )
    result = await db.execute(query)
    guides = result.scalars().all()

    # Преобразование в карточки
    items = []
    for g in guides:
        rating = g.rating_sum / g.rating_count if g.rating_count > 0 else 0.0
        items.append(
            GuideListItem(
                id=g.id,
                title=g.title,
                slug=g.slug,
                difficulty=g.difficulty,
                estimated_time_min=g.estimated_time_min,
                is_verified=g.is_verified,
                is_premium=g.is_premium,
                views_count=g.views_count,
                rating=round(rating, 1),
                rating_count=g.rating_count,
                component_name=g.component.name if g.component else "",
                thumbnail_url=None,  # TODO: из guide_media
                author_name="",  # TODO: из author
                created_at=g.created_at,
            )
        )

    return PaginatedResponse(
        items=items,
        total=total,
        page=page,
        page_size=page_size,
        pages=math.ceil(total / page_size) if total > 0 else 0,
    )


# ── Детальная инструкция ────────────────────────────────────────

@router.get("/{guide_id}", response_model=GuideDetailOut, summary="Полная инструкция")
async def get_guide(guide_id: int, db: AsyncSession = Depends(get_db)):
    """Возвращает инструкцию со всеми шагами, инструментами, предупреждениями."""
    result = await db.execute(
        select(Guide)
        .options(
            selectinload(Guide.component),
            selectinload(Guide.steps).selectinload(GuideStep.tools),
            selectinload(Guide.steps).selectinload(GuideStep.consumables),
            selectinload(Guide.steps).selectinload(GuideStep.warnings),
            selectinload(Guide.steps).selectinload(GuideStep.variations),
            selectinload(Guide.steps).selectinload(GuideStep.checks),
            selectinload(Guide.precautions),
            selectinload(Guide.media),
        )
        .where(Guide.id == guide_id)
    )
    guide = result.scalar_one_or_none()
    if guide is None:
        raise HTTPException(status_code=404, detail="Инструкция не найдена")

    # Инкремент просмотров
    guide.views_count += 1

    # Подсчёт комментариев по шагам
    for step in guide.steps:
        count_result = await db.execute(
            select(func.count())
            .select_from(Comment)
            .where(
                Comment.step_id == step.id,
                Comment.status == CommentStatus.APPROVED,
            )
        )
        step_comments_count = count_result.scalar() or 0
        # Устанавливаем как атрибут для сериализации
        step.__dict__["comments_count"] = step_comments_count

    rating = guide.rating_sum / guide.rating_count if guide.rating_count > 0 else 0.0

    return GuideDetailOut(
        id=guide.id,
        title=guide.title,
        slug=guide.slug,
        description=guide.description,
        difficulty=guide.difficulty,
        estimated_time_min=guide.estimated_time_min,
        status=guide.status,
        is_verified=guide.is_verified,
        is_premium=guide.is_premium,
        views_count=guide.views_count,
        rating=round(rating, 1),
        rating_count=guide.rating_count,
        author_name="",
        author_id=guide.author_id,
        component=guide.component,
        steps=guide.steps,
        precautions=guide.precautions,
        media=guide.media,
        created_at=guide.created_at,
        updated_at=guide.updated_at,
    )


# ── Создание инструкции ─────────────────────────────────────────

@router.post("/", response_model=GuideDetailOut, summary="Создать инструкцию")
async def create_guide(
    data: GuideCreate,
    db: AsyncSession = Depends(get_db),
    user: User = Depends(get_current_user),
):
    """Создаёт черновик инструкции. Доступно любому авторизованному пользователю."""
    guide = Guide(
        component_id=data.component_id,
        author_id=user.id,
        title=data.title,
        slug=data.slug,
        description=data.description,
        difficulty=data.difficulty,
        estimated_time_min=data.estimated_time_min,
        status=ContentStatus.DRAFT,
    )
    db.add(guide)
    await db.flush()

    # Привязка к конфигурациям
    for config_id in data.configuration_ids:
        gc = GuideConfiguration(guide_id=guide.id, configuration_id=config_id)
        db.add(gc)

    await db.flush()

    # Перечитываем с relationships
    result = await db.execute(
        select(Guide)
        .options(
            selectinload(Guide.component),
            selectinload(Guide.steps),
            selectinload(Guide.precautions),
            selectinload(Guide.media),
        )
        .where(Guide.id == guide.id)
    )
    guide = result.scalar_one()

    return GuideDetailOut(
        id=guide.id,
        title=guide.title,
        slug=guide.slug,
        description=guide.description,
        difficulty=guide.difficulty,
        estimated_time_min=guide.estimated_time_min,
        status=guide.status,
        is_verified=guide.is_verified,
        is_premium=guide.is_premium,
        views_count=guide.views_count,
        rating=0.0,
        rating_count=0,
        author_name=user.display_name,
        author_id=guide.author_id,
        component=guide.component,
        steps=[],
        precautions=[],
        media=[],
        created_at=guide.created_at,
        updated_at=guide.updated_at,
    )


# ── Добавление шага ─────────────────────────────────────────────

@router.post(
    "/{guide_id}/steps",
    response_model=GuideStepOut,
    summary="Добавить шаг к инструкции",
)
async def add_step(
    guide_id: int,
    data: GuideStepCreate,
    db: AsyncSession = Depends(get_db),
    user: User = Depends(get_current_user),
):
    """Добавляет шаг. Автор может добавлять шаги к своим черновикам."""
    result = await db.execute(select(Guide).where(Guide.id == guide_id))
    guide = result.scalar_one_or_none()
    if guide is None:
        raise HTTPException(status_code=404, detail="Инструкция не найдена")

    # Проверяем права: автор или editor/admin
    if guide.author_id != user.id and user.role not in (UserRole.EDITOR, UserRole.ADMIN):
        raise HTTPException(status_code=403, detail="Недостаточно прав")

    step = GuideStep(guide_id=guide_id, **data.model_dump())
    db.add(step)
    await db.flush()

    return step
