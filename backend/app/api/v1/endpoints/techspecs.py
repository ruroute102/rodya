"""API техданных: поиск по ключевым словам, CRUD."""

from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy import or_, select
from sqlalchemy.ext.asyncio import AsyncSession

from app.api.deps import require_role
from app.db.session import get_db
from app.models.content import Component, TechSpec, TechSpecCategory
from app.models.user import User, UserRole
from app.schemas.content import TechSpecCreate, TechSpecOut

router = APIRouter(prefix="/techspecs", tags=["Техданные"])


@router.get("/search", response_model=list[TechSpecOut], summary="Поиск техданных")
async def search_techspecs(
    configuration_id: int,
    q: str = Query(..., min_length=2, description="Поисковый запрос"),
    db: AsyncSession = Depends(get_db),
):
    """Поиск техданных по ключевым словам для конкретной конфигурации.

    Пример: q='давление' → находит 'Давление в топливной рампе: 4-5 бар'.
    """
    search_pattern = f"%{q}%"
    result = await db.execute(
        select(TechSpec)
        .outerjoin(Component, TechSpec.component_id == Component.id)
        .outerjoin(TechSpecCategory, TechSpec.category_id == TechSpecCategory.id)
        .where(
            TechSpec.configuration_id == configuration_id,
            or_(
                TechSpec.spec_key.ilike(search_pattern),
                TechSpec.spec_value.ilike(search_pattern),
                TechSpec.notes.ilike(search_pattern),
                Component.name.ilike(search_pattern),
            ),
        )
    )
    specs = result.scalars().all()

    items = []
    for s in specs:
        items.append(
            TechSpecOut(
                id=s.id,
                spec_key=s.spec_key,
                spec_value=s.spec_value,
                unit=s.unit,
                min_value=s.min_value,
                max_value=s.max_value,
                notes=s.notes,
                component_name=None,  # TODO: join
                category_name=None,   # TODO: join
            )
        )
    return items


@router.get(
    "/configuration/{configuration_id}",
    response_model=list[TechSpecOut],
    summary="Все техданные для конфигурации",
)
async def list_techspecs(
    configuration_id: int,
    db: AsyncSession = Depends(get_db),
):
    result = await db.execute(
        select(TechSpec)
        .where(TechSpec.configuration_id == configuration_id)
        .order_by(TechSpec.spec_key)
    )
    specs = result.scalars().all()
    return [
        TechSpecOut(
            id=s.id,
            spec_key=s.spec_key,
            spec_value=s.spec_value,
            unit=s.unit,
            min_value=s.min_value,
            max_value=s.max_value,
            notes=s.notes,
            component_name=None,
            category_name=None,
        )
        for s in specs
    ]


@router.post("/", response_model=TechSpecOut, summary="Добавить техданные (editor/admin)")
async def create_techspec(
    data: TechSpecCreate,
    db: AsyncSession = Depends(get_db),
    user: User = Depends(require_role(UserRole.EDITOR, UserRole.ADMIN)),
):
    spec = TechSpec(**data.model_dump())
    db.add(spec)
    await db.flush()
    return TechSpecOut(
        id=spec.id,
        spec_key=spec.spec_key,
        spec_value=spec.spec_value,
        unit=spec.unit,
        min_value=spec.min_value,
        max_value=spec.max_value,
        notes=spec.notes,
        component_name=None,
        category_name=None,
    )
