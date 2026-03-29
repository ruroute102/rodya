"""Pydantic-схемы для контента: инструкции, шаги, комментарии, техданные, диагностика."""

from datetime import datetime

from pydantic import BaseModel, ConfigDict, Field

from app.models.content import (
    CommentStatus,
    ContentStatus,
    Difficulty,
    MediaType,
    WarningSeverity,
    WarningType,
)


# ── Категории и узлы ────────────────────────────────────────────

class ComponentCategoryOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    parent_id: int | None = None
    name: str
    slug: str
    icon: str | None = None
    description: str | None = None


class ComponentOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    category_id: int
    name: str
    slug: str
    description: str | None = None
    icon: str | None = None


class ComponentCreate(BaseModel):
    category_id: int
    name: str
    slug: str
    description: str | None = None
    icon: str | None = None


# ── Инструменты, расходники, предупреждения шага ────────────────

class StepToolOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    tool_name: str
    tool_spec: str | None = None
    is_required: bool = True
    note: str | None = None


class StepConsumableOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    name: str
    part_number: str | None = None
    quantity: str | None = None
    note: str | None = None


class StepWarningOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    warning_type: WarningType
    severity: WarningSeverity
    text: str


class StepVariationOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    configuration_id: int
    text: str
    image_url: str | None = None


class StepCheckOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    description: str
    is_post_repair: bool = False


# ── Шаг инструкции ──────────────────────────────────────────────

class GuideStepOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    step_number: int
    title: str
    description: str
    image_url: str | None = None
    video_url: str | None = None
    model_3d_url: str | None = None
    camera_position: dict | None = None
    highlighted_parts: list[str] | None = None
    hidden_parts: list[str] | None = None
    tools: list[StepToolOut] = []
    consumables: list[StepConsumableOut] = []
    warnings: list[StepWarningOut] = []
    variations: list[StepVariationOut] = []
    checks: list[StepCheckOut] = []
    comments_count: int = 0


class GuideStepCreate(BaseModel):
    step_number: int
    title: str
    description: str
    image_url: str | None = None
    video_url: str | None = None
    model_3d_url: str | None = None
    camera_position: dict | None = None
    highlighted_parts: list[str] | None = None
    hidden_parts: list[str] | None = None


# ── Инструкция ──────────────────────────────────────────────────

class GuidePrecautionOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    warning_type: WarningType
    severity: WarningSeverity
    text: str


class GuideMediaOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    media_type: MediaType
    url: str
    thumbnail_url: str | None = None
    title: str | None = None


class GuideListItem(BaseModel):
    """Карточка инструкции в каталоге/поиске."""
    model_config = ConfigDict(from_attributes=True)

    id: int
    title: str
    slug: str
    difficulty: Difficulty
    estimated_time_min: int | None = None
    is_verified: bool = False
    is_premium: bool = False
    views_count: int = 0
    rating: float = 0.0
    rating_count: int = 0
    component_name: str = ""
    thumbnail_url: str | None = None
    author_name: str = ""
    created_at: datetime


class GuideDetailOut(BaseModel):
    """Полная инструкция с шагами."""
    model_config = ConfigDict(from_attributes=True)

    id: int
    title: str
    slug: str
    description: str | None = None
    difficulty: Difficulty
    estimated_time_min: int | None = None
    status: ContentStatus
    is_verified: bool = False
    is_premium: bool = False
    views_count: int = 0
    rating: float = 0.0
    rating_count: int = 0
    author_name: str = ""
    author_id: int
    component: ComponentOut
    steps: list[GuideStepOut] = []
    precautions: list[GuidePrecautionOut] = []
    media: list[GuideMediaOut] = []
    created_at: datetime
    updated_at: datetime


class GuideCreate(BaseModel):
    component_id: int
    title: str
    slug: str
    description: str | None = None
    difficulty: Difficulty
    estimated_time_min: int | None = None
    configuration_ids: list[int] = []


# ── Комментарии ─────────────────────────────────────────────────

class CommentOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    user_id: int
    user_name: str = ""
    user_avatar: str | None = None
    user_car_display: str | None = None  # "Владелец Audi Q3 2011"
    guide_id: int
    step_id: int | None = None
    parent_id: int | None = None
    text: str
    status: CommentStatus
    created_at: datetime
    replies: list["CommentOut"] = []


class CommentCreate(BaseModel):
    guide_id: int
    step_id: int | None = None
    parent_id: int | None = None
    text: str = Field(..., min_length=1, max_length=5000)


# ── Техданные ───────────────────────────────────────────────────

class TechSpecOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    spec_key: str
    spec_value: str
    unit: str | None = None
    min_value: float | None = None
    max_value: float | None = None
    notes: str | None = None
    component_name: str | None = None
    category_name: str | None = None


class TechSpecCreate(BaseModel):
    category_id: int | None = None
    configuration_id: int
    component_id: int | None = None
    spec_key: str
    spec_value: str
    unit: str | None = None
    min_value: float | None = None
    max_value: float | None = None
    notes: str | None = None


# ── Диагностика ─────────────────────────────────────────────────

class SymptomOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    category_id: int
    name: str
    description: str | None = None


class DiagnosticCheckOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    step_number: int
    description: str
    expected_result: str | None = None
    tool_needed: str | None = None


class DiagnosticResultOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    probable_cause: str
    probability: float
    component_name: str | None = None
    guide_id: int | None = None
    guide_title: str | None = None
    check_description: str | None = None
    checks: list[DiagnosticCheckOut] = []


class DiagnosticRequest(BaseModel):
    """Запрос диагностики: список симптомов + конфигурация."""
    configuration_id: int
    symptom_ids: list[int] = Field(..., min_length=1)


# ── Пагинация ──────────────────────────────────────────────────

class PaginatedResponse(BaseModel):
    items: list = []
    total: int = 0
    page: int = 1
    page_size: int = 20
    pages: int = 0
