"""API диагностики: симптомы, правила, результаты."""

from fastapi import APIRouter, Depends
from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import selectinload

from app.db.session import get_db
from app.models.content import (
    Component,
    DiagnosticRule,
    Guide,
    Symptom,
    SymptomCategory,
)
from app.schemas.content import DiagnosticRequest, DiagnosticResultOut, SymptomOut

router = APIRouter(prefix="/diagnostics", tags=["Диагностика"])


@router.get("/symptoms", response_model=list[SymptomOut], summary="Все симптомы")
async def list_symptoms(
    category_id: int | None = None,
    db: AsyncSession = Depends(get_db),
):
    query = select(Symptom)
    if category_id is not None:
        query = query.where(Symptom.category_id == category_id)
    query = query.order_by(Symptom.name)
    result = await db.execute(query)
    return result.scalars().all()


@router.get("/symptom-categories", summary="Категории симптомов")
async def list_symptom_categories(db: AsyncSession = Depends(get_db)):
    result = await db.execute(
        select(SymptomCategory).order_by(SymptomCategory.name)
    )
    categories = result.scalars().all()
    return [
        {"id": c.id, "name": c.name, "slug": c.slug, "icon": c.icon}
        for c in categories
    ]


@router.post(
    "/diagnose",
    response_model=list[DiagnosticResultOut],
    summary="Диагностика по симптомам",
)
async def diagnose(
    data: DiagnosticRequest,
    db: AsyncSession = Depends(get_db),
):
    """Принимает список симптомов и конфигурацию, возвращает вероятные неисправности
    с привязкой к узлам, инструкциям и пошаговым проверкам."""
    result = await db.execute(
        select(DiagnosticRule)
        .options(selectinload(DiagnosticRule.checks))
        .where(
            DiagnosticRule.symptom_id.in_(data.symptom_ids),
            # Универсальные правила (configuration_id IS NULL) или для конкретной конфигурации
            (DiagnosticRule.configuration_id == data.configuration_id)
            | (DiagnosticRule.configuration_id == None),  # noqa: E711
        )
        .order_by(DiagnosticRule.probability.desc())
    )
    rules = result.scalars().all()

    items = []
    for rule in rules:
        # Получаем имя компонента
        component_name = None
        if rule.related_component_id:
            comp_result = await db.execute(
                select(Component.name).where(Component.id == rule.related_component_id)
            )
            component_name = comp_result.scalar_one_or_none()

        # Получаем название инструкции
        guide_title = None
        if rule.related_guide_id:
            guide_result = await db.execute(
                select(Guide.title).where(Guide.id == rule.related_guide_id)
            )
            guide_title = guide_result.scalar_one_or_none()

        items.append(
            DiagnosticResultOut(
                id=rule.id,
                probable_cause=rule.probable_cause,
                probability=rule.probability,
                component_name=component_name,
                guide_id=rule.related_guide_id,
                guide_title=guide_title,
                check_description=rule.check_description,
                checks=rule.checks,
            )
        )

    return items
