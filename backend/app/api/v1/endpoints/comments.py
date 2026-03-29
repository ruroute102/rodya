"""API комментариев: создание, модерация, древовидный вывод."""

from fastapi import APIRouter, Depends, HTTPException, Query
from sqlalchemy import func, select
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import selectinload

from app.api.deps import get_current_user, require_role
from app.db.session import get_db
from app.models.content import Comment, CommentStatus
from app.models.user import User, UserCar, UserRole
from app.schemas.content import CommentCreate, CommentOut

router = APIRouter(prefix="/comments", tags=["Комментарии"])


async def _enrich_comment(comment: Comment, db: AsyncSession) -> CommentOut:
    """Обогащает комментарий данными пользователя и его автомобиля."""
    # Получаем основной автомобиль пользователя для отображения
    car_display = None
    result = await db.execute(
        select(UserCar)
        .where(UserCar.user_id == comment.user_id, UserCar.is_primary == True)  # noqa: E712
    )
    user_car = result.scalar_one_or_none()
    if user_car and user_car.display_name:
        car_display = f"Владелец {user_car.display_name}"

    replies = []
    if comment.replies:
        for reply in comment.replies:
            if reply.status == CommentStatus.APPROVED:
                replies.append(await _enrich_comment(reply, db))

    return CommentOut(
        id=comment.id,
        user_id=comment.user_id,
        user_name=comment.user.display_name if comment.user else "",
        user_avatar=comment.user.avatar_url if comment.user else None,
        user_car_display=car_display,
        guide_id=comment.guide_id,
        step_id=comment.step_id,
        parent_id=comment.parent_id,
        text=comment.text,
        status=comment.status,
        created_at=comment.created_at,
        replies=replies,
    )


@router.get(
    "/guide/{guide_id}",
    response_model=list[CommentOut],
    summary="Комментарии к инструкции",
)
async def list_guide_comments(
    guide_id: int,
    step_id: int | None = None,
    db: AsyncSession = Depends(get_db),
):
    """Возвращает древовидные комментарии. Если step_id указан — только для этого шага."""
    query = (
        select(Comment)
        .options(
            selectinload(Comment.user),
            selectinload(Comment.replies).selectinload(Comment.user),
        )
        .where(
            Comment.guide_id == guide_id,
            Comment.parent_id == None,  # noqa: E711 — только корневые
            Comment.status == CommentStatus.APPROVED,
        )
        .order_by(Comment.created_at.desc())
    )

    if step_id is not None:
        query = query.where(Comment.step_id == step_id)
    else:
        query = query.where(Comment.step_id == None)  # noqa: E711

    result = await db.execute(query)
    comments = result.scalars().all()

    enriched = []
    for c in comments:
        enriched.append(await _enrich_comment(c, db))
    return enriched


@router.get(
    "/guide/{guide_id}/counts",
    summary="Количество комментариев по шагам",
)
async def comment_counts(guide_id: int, db: AsyncSession = Depends(get_db)):
    """Возвращает {step_id: count} для всех шагов инструкции."""
    result = await db.execute(
        select(Comment.step_id, func.count(Comment.id))
        .where(
            Comment.guide_id == guide_id,
            Comment.status == CommentStatus.APPROVED,
            Comment.step_id != None,  # noqa: E711
        )
        .group_by(Comment.step_id)
    )
    counts = {row[0]: row[1] for row in result.all()}

    # Общие комментарии (без step_id)
    result_general = await db.execute(
        select(func.count(Comment.id)).where(
            Comment.guide_id == guide_id,
            Comment.status == CommentStatus.APPROVED,
            Comment.step_id == None,  # noqa: E711
        )
    )
    counts["general"] = result_general.scalar() or 0

    return counts


@router.post("/", response_model=CommentOut, summary="Добавить комментарий")
async def create_comment(
    data: CommentCreate,
    db: AsyncSession = Depends(get_db),
    user: User = Depends(get_current_user),
):
    """Создаёт комментарий. Статус — pending (на модерации)."""
    comment = Comment(
        user_id=user.id,
        guide_id=data.guide_id,
        step_id=data.step_id,
        parent_id=data.parent_id,
        text=data.text,
        status=CommentStatus.PENDING,
    )
    db.add(comment)
    await db.flush()

    # Перечитываем с user
    result = await db.execute(
        select(Comment)
        .options(selectinload(Comment.user))
        .where(Comment.id == comment.id)
    )
    comment = result.scalar_one()

    return await _enrich_comment(comment, db)


# ── Модерация ───────────────────────────────────────────────────

@router.get(
    "/pending",
    response_model=list[CommentOut],
    summary="Комментарии на модерации",
)
async def list_pending(
    page: int = Query(1, ge=1),
    page_size: int = Query(50, ge=1, le=200),
    db: AsyncSession = Depends(get_db),
    user: User = Depends(require_role(UserRole.MODERATOR, UserRole.EDITOR, UserRole.ADMIN)),
):
    result = await db.execute(
        select(Comment)
        .options(selectinload(Comment.user))
        .where(Comment.status == CommentStatus.PENDING)
        .order_by(Comment.created_at.asc())
        .offset((page - 1) * page_size)
        .limit(page_size)
    )
    comments = result.scalars().all()
    enriched = []
    for c in comments:
        enriched.append(await _enrich_comment(c, db))
    return enriched


@router.patch(
    "/{comment_id}/moderate",
    summary="Модерировать комментарий",
)
async def moderate_comment(
    comment_id: int,
    action: CommentStatus,
    db: AsyncSession = Depends(get_db),
    user: User = Depends(require_role(UserRole.MODERATOR, UserRole.EDITOR, UserRole.ADMIN)),
):
    result = await db.execute(select(Comment).where(Comment.id == comment_id))
    comment = result.scalar_one_or_none()
    if comment is None:
        raise HTTPException(status_code=404, detail="Комментарий не найден")

    comment.status = action
    return {"message": f"Статус изменён на {action.value}"}
