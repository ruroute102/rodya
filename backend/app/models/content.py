"""Модели контента: узлы, инструкции, шаги, комментарии, техданные, диагностика."""

import enum
from datetime import datetime, timezone

from sqlalchemy import (
    Boolean,
    DateTime,
    Enum,
    Float,
    ForeignKey,
    Integer,
    String,
    Text,
    UniqueConstraint,
)
from sqlalchemy.dialects.postgresql import JSONB
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db.base import Base


# ── Перечисления ────────────────────────────────────────────────

class Difficulty(str, enum.Enum):
    EASY = "easy"
    MEDIUM = "medium"
    HARD = "hard"
    EXPERT = "expert"


class ContentStatus(str, enum.Enum):
    DRAFT = "draft"
    PENDING = "pending"         # на модерации
    APPROVED = "approved"       # одобрено
    PUBLISHED = "published"     # опубликовано
    REJECTED = "rejected"       # отклонено
    ARCHIVED = "archived"       # в архиве


class CommentStatus(str, enum.Enum):
    PENDING = "pending"
    APPROVED = "approved"
    REJECTED = "rejected"
    HIDDEN = "hidden"


class WarningSeverity(str, enum.Enum):
    INFO = "info"
    CAUTION = "caution"
    WARNING = "warning"
    DANGER = "danger"


class WarningType(str, enum.Enum):
    SAFETY = "safety"
    CAUTION = "caution"
    INFO = "info"
    VARIATION = "variation"


class MediaType(str, enum.Enum):
    IMAGE = "image"
    VIDEO = "video"
    MODEL_3D = "model_3d"
    DOCUMENT = "document"


# ── Категории узлов ─────────────────────────────────────────────

class ComponentCategory(Base):
    """Иерархические категории узлов (Двигатель → Топливная система → ...)."""
    __tablename__ = "component_categories"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    parent_id: Mapped[int | None] = mapped_column(ForeignKey("component_categories.id"))
    name: Mapped[str] = mapped_column(String(200), nullable=False)
    slug: Mapped[str] = mapped_column(String(200), nullable=False, index=True)
    icon: Mapped[str | None] = mapped_column(String(100))
    description: Mapped[str | None] = mapped_column(Text)
    sort_order: Mapped[int] = mapped_column(Integer, default=0)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True)

    parent: Mapped["ComponentCategory | None"] = relationship(
        remote_side="ComponentCategory.id", lazy="joined"
    )
    components: Mapped[list["Component"]] = relationship(back_populates="category", lazy="selectin")


# ── Узлы (компоненты) ──────────────────────────────────────────

class Component(Base):
    """Узел автомобиля (бензонасос, масляный фильтр и т.д.)."""
    __tablename__ = "components"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    category_id: Mapped[int] = mapped_column(ForeignKey("component_categories.id"), nullable=False)
    name: Mapped[str] = mapped_column(String(300), nullable=False)
    slug: Mapped[str] = mapped_column(String(300), nullable=False, unique=True, index=True)
    description: Mapped[str | None] = mapped_column(Text)
    icon: Mapped[str | None] = mapped_column(String(100))
    is_active: Mapped[bool] = mapped_column(Boolean, default=True)

    category: Mapped["ComponentCategory"] = relationship(back_populates="components")
    guides: Mapped[list["Guide"]] = relationship(back_populates="component", lazy="noload")


# ── Позиция узла на конкретном автомобиле ───────────────────────

class ComponentPosition(Base):
    """Где именно находится узел на конкретной конфигурации автомобиля."""
    __tablename__ = "component_positions"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    component_id: Mapped[int] = mapped_column(ForeignKey("components.id"), nullable=False)
    configuration_id: Mapped[int] = mapped_column(
        ForeignKey("car_configurations.id"), nullable=False
    )
    zone: Mapped[str | None] = mapped_column(String(100))  # "под задним сиденьем"
    position_x: Mapped[float | None] = mapped_column(Float)
    position_y: Mapped[float | None] = mapped_column(Float)
    position_z: Mapped[float | None] = mapped_column(Float)
    access_notes: Mapped[str | None] = mapped_column(Text)
    mesh_name: Mapped[str | None] = mapped_column(String(200))  # имя меша в 3D-модели

    __table_args__ = (
        UniqueConstraint("component_id", "configuration_id", name="uq_component_position"),
    )


# ── Инструкции ──────────────────────────────────────────────────

class Guide(Base):
    """Инструкция по ремонту/обслуживанию конкретного узла."""
    __tablename__ = "guides"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    component_id: Mapped[int] = mapped_column(ForeignKey("components.id"), nullable=False)
    author_id: Mapped[int] = mapped_column(ForeignKey("users.id"), nullable=False)
    title: Mapped[str] = mapped_column(String(500), nullable=False)
    slug: Mapped[str] = mapped_column(String(500), nullable=False, unique=True, index=True)
    description: Mapped[str | None] = mapped_column(Text)
    difficulty: Mapped[Difficulty] = mapped_column(Enum(Difficulty), nullable=False)
    estimated_time_min: Mapped[int | None] = mapped_column(Integer)
    status: Mapped[ContentStatus] = mapped_column(
        Enum(ContentStatus), default=ContentStatus.DRAFT, nullable=False
    )
    is_verified: Mapped[bool] = mapped_column(Boolean, default=False)
    is_premium: Mapped[bool] = mapped_column(Boolean, default=False)
    views_count: Mapped[int] = mapped_column(Integer, default=0)
    rating_sum: Mapped[int] = mapped_column(Integer, default=0)
    rating_count: Mapped[int] = mapped_column(Integer, default=0)
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        default=lambda: datetime.now(timezone.utc),
        nullable=False,
    )
    updated_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        default=lambda: datetime.now(timezone.utc),
        onupdate=lambda: datetime.now(timezone.utc),
        nullable=False,
    )
    published_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))

    component: Mapped["Component"] = relationship(back_populates="guides")
    author: Mapped["User"] = relationship()
    steps: Mapped[list["GuideStep"]] = relationship(
        back_populates="guide", lazy="selectin", order_by="GuideStep.step_number"
    )
    configurations: Mapped[list["GuideConfiguration"]] = relationship(lazy="selectin")
    precautions: Mapped[list["GuidePrecaution"]] = relationship(lazy="selectin")
    media: Mapped[list["GuideMedia"]] = relationship(lazy="selectin")
    comments: Mapped[list["Comment"]] = relationship(back_populates="guide", lazy="noload")


class GuideConfiguration(Base):
    """Связь: какие конфигурации авто поддерживает инструкция."""
    __tablename__ = "guide_configurations"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    guide_id: Mapped[int] = mapped_column(ForeignKey("guides.id", ondelete="CASCADE"), nullable=False)
    configuration_id: Mapped[int] = mapped_column(
        ForeignKey("car_configurations.id"), nullable=False
    )

    __table_args__ = (
        UniqueConstraint("guide_id", "configuration_id", name="uq_guide_configuration"),
    )


class GuidePrecaution(Base):
    """Общие меры предосторожности для инструкции."""
    __tablename__ = "guide_precautions"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    guide_id: Mapped[int] = mapped_column(ForeignKey("guides.id", ondelete="CASCADE"), nullable=False)
    warning_type: Mapped[WarningType] = mapped_column(Enum(WarningType), nullable=False)
    severity: Mapped[WarningSeverity] = mapped_column(Enum(WarningSeverity), nullable=False)
    text: Mapped[str] = mapped_column(Text, nullable=False)
    sort_order: Mapped[int] = mapped_column(Integer, default=0)


class GuideMedia(Base):
    """Медиафайлы инструкции (фото, видео, 3D-модели)."""
    __tablename__ = "guide_media"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    guide_id: Mapped[int] = mapped_column(ForeignKey("guides.id", ondelete="CASCADE"), nullable=False)
    media_type: Mapped[MediaType] = mapped_column(Enum(MediaType), nullable=False)
    url: Mapped[str] = mapped_column(String(1000), nullable=False)
    thumbnail_url: Mapped[str | None] = mapped_column(String(1000))
    title: Mapped[str | None] = mapped_column(String(300))
    sort_order: Mapped[int] = mapped_column(Integer, default=0)
    is_watermarked: Mapped[bool] = mapped_column(Boolean, default=False)
    file_size_bytes: Mapped[int | None] = mapped_column(Integer)


# ── Шаги инструкции ─────────────────────────────────────────────

class GuideStep(Base):
    """Один шаг пошаговой инструкции."""
    __tablename__ = "guide_steps"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    guide_id: Mapped[int] = mapped_column(ForeignKey("guides.id", ondelete="CASCADE"), nullable=False)
    step_number: Mapped[int] = mapped_column(Integer, nullable=False)
    title: Mapped[str] = mapped_column(String(500), nullable=False)
    description: Mapped[str] = mapped_column(Text, nullable=False)
    image_url: Mapped[str | None] = mapped_column(String(1000))
    video_url: Mapped[str | None] = mapped_column(String(1000))
    model_3d_url: Mapped[str | None] = mapped_column(String(1000))
    camera_position: Mapped[dict | None] = mapped_column(JSONB)
    highlighted_parts: Mapped[list | None] = mapped_column(JSONB)  # ["mesh_name1", "mesh_name2"]
    hidden_parts: Mapped[list | None] = mapped_column(JSONB)       # ["seat_rear"]

    guide: Mapped["Guide"] = relationship(back_populates="steps")
    tools: Mapped[list["StepTool"]] = relationship(lazy="selectin")
    consumables: Mapped[list["StepConsumable"]] = relationship(lazy="selectin")
    warnings: Mapped[list["StepWarning"]] = relationship(lazy="selectin")
    variations: Mapped[list["StepVariation"]] = relationship(lazy="selectin")
    checks: Mapped[list["StepCheck"]] = relationship(lazy="selectin")
    comments: Mapped[list["Comment"]] = relationship(lazy="noload")

    __table_args__ = (
        UniqueConstraint("guide_id", "step_number", name="uq_guide_step_number"),
    )


class StepTool(Base):
    """Инструмент, необходимый для шага."""
    __tablename__ = "step_tools"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    step_id: Mapped[int] = mapped_column(ForeignKey("guide_steps.id", ondelete="CASCADE"), nullable=False)
    tool_name: Mapped[str] = mapped_column(String(200), nullable=False)
    tool_spec: Mapped[str | None] = mapped_column(String(200))  # "Torx T25"
    is_required: Mapped[bool] = mapped_column(Boolean, default=True)
    note: Mapped[str | None] = mapped_column(Text)


class StepConsumable(Base):
    """Расходник для шага."""
    __tablename__ = "step_consumables"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    step_id: Mapped[int] = mapped_column(ForeignKey("guide_steps.id", ondelete="CASCADE"), nullable=False)
    name: Mapped[str] = mapped_column(String(300), nullable=False)
    part_number: Mapped[str | None] = mapped_column(String(100))
    quantity: Mapped[str | None] = mapped_column(String(50))  # "1 шт", "200 мл"
    note: Mapped[str | None] = mapped_column(Text)


class StepWarning(Base):
    """Предупреждение на конкретном шаге."""
    __tablename__ = "step_warnings"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    step_id: Mapped[int] = mapped_column(ForeignKey("guide_steps.id", ondelete="CASCADE"), nullable=False)
    warning_type: Mapped[WarningType] = mapped_column(Enum(WarningType), nullable=False)
    severity: Mapped[WarningSeverity] = mapped_column(Enum(WarningSeverity), nullable=False)
    text: Mapped[str] = mapped_column(Text, nullable=False)


class StepVariation(Base):
    """Отличие шага для конкретной конфигурации автомобиля."""
    __tablename__ = "step_variations"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    step_id: Mapped[int] = mapped_column(ForeignKey("guide_steps.id", ondelete="CASCADE"), nullable=False)
    configuration_id: Mapped[int] = mapped_column(
        ForeignKey("car_configurations.id"), nullable=False
    )
    text: Mapped[str] = mapped_column(Text, nullable=False)
    image_url: Mapped[str | None] = mapped_column(String(1000))


class StepCheck(Base):
    """Проверка после шага или после завершения ремонта."""
    __tablename__ = "step_checks"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    step_id: Mapped[int] = mapped_column(ForeignKey("guide_steps.id", ondelete="CASCADE"), nullable=False)
    description: Mapped[str] = mapped_column(Text, nullable=False)
    is_post_repair: Mapped[bool] = mapped_column(Boolean, default=False)


# ── Комментарии ─────────────────────────────────────────────────

class Comment(Base):
    """Древовидные комментарии к инструкциям и шагам."""
    __tablename__ = "comments"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    user_id: Mapped[int] = mapped_column(ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    guide_id: Mapped[int] = mapped_column(ForeignKey("guides.id", ondelete="CASCADE"), nullable=False)
    step_id: Mapped[int | None] = mapped_column(ForeignKey("guide_steps.id", ondelete="CASCADE"))
    parent_id: Mapped[int | None] = mapped_column(ForeignKey("comments.id", ondelete="CASCADE"))
    text: Mapped[str] = mapped_column(Text, nullable=False)
    status: Mapped[CommentStatus] = mapped_column(
        Enum(CommentStatus), default=CommentStatus.PENDING, nullable=False
    )
    created_at: Mapped[datetime] = mapped_column(
        DateTime(timezone=True),
        default=lambda: datetime.now(timezone.utc),
        nullable=False,
    )
    updated_at: Mapped[datetime | None] = mapped_column(DateTime(timezone=True))

    user: Mapped["User"] = relationship(back_populates="comments")
    guide: Mapped["Guide"] = relationship(back_populates="comments")
    replies: Mapped[list["Comment"]] = relationship(lazy="selectin")


# ── Техданные ───────────────────────────────────────────────────

class TechSpecCategory(Base):
    """Категория техданных: давления, моменты затяжки, объёмы..."""
    __tablename__ = "tech_spec_categories"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    name: Mapped[str] = mapped_column(String(200), nullable=False)
    slug: Mapped[str] = mapped_column(String(200), nullable=False, unique=True)


class TechSpec(Base):
    """Конкретный технический параметр для конфигурации + узла."""
    __tablename__ = "tech_specs"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    category_id: Mapped[int | None] = mapped_column(ForeignKey("tech_spec_categories.id"))
    configuration_id: Mapped[int] = mapped_column(
        ForeignKey("car_configurations.id"), nullable=False
    )
    component_id: Mapped[int | None] = mapped_column(ForeignKey("components.id"))
    spec_key: Mapped[str] = mapped_column(String(300), nullable=False, index=True)
    spec_value: Mapped[str] = mapped_column(String(300), nullable=False)
    unit: Mapped[str | None] = mapped_column(String(50))
    min_value: Mapped[float | None] = mapped_column(Float)
    max_value: Mapped[float | None] = mapped_column(Float)
    notes: Mapped[str | None] = mapped_column(Text)


# ── Диагностика ─────────────────────────────────────────────────

class SymptomCategory(Base):
    __tablename__ = "symptom_categories"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    name: Mapped[str] = mapped_column(String(200), nullable=False)
    slug: Mapped[str] = mapped_column(String(200), nullable=False, unique=True)
    icon: Mapped[str | None] = mapped_column(String(100))


class Symptom(Base):
    """Симптом неисправности."""
    __tablename__ = "symptoms"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    category_id: Mapped[int] = mapped_column(ForeignKey("symptom_categories.id"), nullable=False)
    name: Mapped[str] = mapped_column(String(300), nullable=False)
    description: Mapped[str | None] = mapped_column(Text)

    rules: Mapped[list["DiagnosticRule"]] = relationship(back_populates="symptom", lazy="selectin")


class DiagnosticRule(Base):
    """Правило диагностики: симптом → вероятная причина."""
    __tablename__ = "diagnostic_rules"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    symptom_id: Mapped[int] = mapped_column(ForeignKey("symptoms.id"), nullable=False)
    configuration_id: Mapped[int | None] = mapped_column(
        ForeignKey("car_configurations.id")
    )
    probable_cause: Mapped[str] = mapped_column(String(500), nullable=False)
    probability: Mapped[float] = mapped_column(Float, default=0.5)
    related_component_id: Mapped[int | None] = mapped_column(ForeignKey("components.id"))
    related_guide_id: Mapped[int | None] = mapped_column(ForeignKey("guides.id"))
    check_description: Mapped[str | None] = mapped_column(Text)

    symptom: Mapped["Symptom"] = relationship(back_populates="rules")
    checks: Mapped[list["DiagnosticCheck"]] = relationship(lazy="selectin")


class DiagnosticCheck(Base):
    """Пошаговая проверка для диагностического правила."""
    __tablename__ = "diagnostic_checks"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    rule_id: Mapped[int] = mapped_column(ForeignKey("diagnostic_rules.id", ondelete="CASCADE"), nullable=False)
    step_number: Mapped[int] = mapped_column(Integer, nullable=False)
    description: Mapped[str] = mapped_column(Text, nullable=False)
    expected_result: Mapped[str | None] = mapped_column(Text)
    tool_needed: Mapped[str | None] = mapped_column(String(200))


# ── 3D-модели ───────────────────────────────────────────────────

class Model3D(Base):
    """3D-модель автомобиля для визуального представления."""
    __tablename__ = "models_3d"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    configuration_id: Mapped[int] = mapped_column(
        ForeignKey("car_configurations.id"), nullable=False
    )
    file_url: Mapped[str] = mapped_column(String(1000), nullable=False)
    format: Mapped[str] = mapped_column(String(20), default="glb")
    file_size_bytes: Mapped[int | None] = mapped_column(Integer)
    version: Mapped[int] = mapped_column(Integer, default=1)
    parts_metadata: Mapped[dict | None] = mapped_column(JSONB)

    parts: Mapped[list["Model3DPart"]] = relationship(lazy="selectin")


class Model3DPart(Base):
    """Отдельная деталь в 3D-модели."""
    __tablename__ = "model_3d_parts"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    model_3d_id: Mapped[int] = mapped_column(
        ForeignKey("models_3d.id", ondelete="CASCADE"), nullable=False
    )
    part_name: Mapped[str] = mapped_column(String(200), nullable=False)
    component_id: Mapped[int | None] = mapped_column(ForeignKey("components.id"))
    mesh_name: Mapped[str] = mapped_column(String(200), nullable=False)
    default_visible: Mapped[bool] = mapped_column(Boolean, default=True)
    selectable: Mapped[bool] = mapped_column(Boolean, default=True)
    highlight_color: Mapped[str | None] = mapped_column(String(20))  # "#FF6600"


# ── Подписки ────────────────────────────────────────────────────

class SubscriptionPlan(Base):
    __tablename__ = "subscription_plans"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    name: Mapped[str] = mapped_column(String(100), nullable=False)
    slug: Mapped[str] = mapped_column(String(100), nullable=False, unique=True)
    price_rub: Mapped[int] = mapped_column(Integer, nullable=False)
    duration_days: Mapped[int] = mapped_column(Integer, nullable=False)
    features: Mapped[dict] = mapped_column(JSONB, default=dict)
    max_offline_cars: Mapped[int] = mapped_column(Integer, default=0)
    has_3d_access: Mapped[bool] = mapped_column(Boolean, default=False)
    has_ads: Mapped[bool] = mapped_column(Boolean, default=True)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True)
    sort_order: Mapped[int] = mapped_column(Integer, default=0)


class UserSubscription(Base):
    __tablename__ = "user_subscriptions"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    user_id: Mapped[int] = mapped_column(ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    plan_id: Mapped[int] = mapped_column(ForeignKey("subscription_plans.id"), nullable=False)
    started_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
    expires_at: Mapped[datetime] = mapped_column(DateTime(timezone=True), nullable=False)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True)
    payment_provider: Mapped[str | None] = mapped_column(String(50))
    payment_id: Mapped[str | None] = mapped_column(String(200))


# Forward reference для User
from app.models.user import User  # noqa: E402
