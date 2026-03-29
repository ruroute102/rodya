"""Pydantic-схемы для автомобилей."""

from pydantic import BaseModel, ConfigDict

from app.models.car import BodyType, DriveType, FuelType, TransmissionType


# ── Марка ───────────────────────────────────────────────────────

class CarBrandOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    name: str
    slug: str
    logo_url: str | None = None
    country: str | None = None


class CarBrandCreate(BaseModel):
    name: str
    slug: str
    logo_url: str | None = None
    country: str | None = None
    sort_order: int = 0


# ── Модель ──────────────────────────────────────────────────────

class CarModelOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    brand_id: int
    name: str
    slug: str


class CarModelCreate(BaseModel):
    brand_id: int
    name: str
    slug: str
    sort_order: int = 0


# ── Поколение ───────────────────────────────────────────────────

class CarGenerationOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    model_id: int
    name: str
    slug: str
    chassis_code: str | None = None
    year_start: int
    year_end: int | None = None
    image_url: str | None = None


class CarGenerationCreate(BaseModel):
    model_id: int
    name: str
    slug: str
    chassis_code: str | None = None
    year_start: int
    year_end: int | None = None
    image_url: str | None = None


# ── Двигатель ───────────────────────────────────────────────────

class CarEngineOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    generation_id: int
    code: str
    name: str
    displacement_label: str | None = None
    fuel_type: FuelType
    power_hp: int | None = None
    torque_nm: int | None = None


class CarEngineCreate(BaseModel):
    generation_id: int
    code: str
    name: str
    displacement_cc: int | None = None
    displacement_label: str | None = None
    fuel_type: FuelType
    power_hp: int | None = None
    torque_nm: int | None = None


# ── Кузов ───────────────────────────────────────────────────────

class CarBodyOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    generation_id: int
    body_type: BodyType
    doors_count: int
    name: str | None = None


# ── Комплектация ────────────────────────────────────────────────

class CarTrimOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    generation_id: int
    name: str
    market: str | None = None


# ── Конфигурация ────────────────────────────────────────────────

class CarConfigurationOut(BaseModel):
    model_config = ConfigDict(from_attributes=True)

    id: int
    generation_id: int
    engine_id: int
    body_id: int | None = None
    trim_id: int | None = None
    transmission_type: TransmissionType | None = None
    drive_type: DriveType | None = None
    year_start: int | None = None
    year_end: int | None = None
    display_name: str | None = None


class CarConfigurationCreate(BaseModel):
    generation_id: int
    engine_id: int
    body_id: int | None = None
    trim_id: int | None = None
    transmission_type: TransmissionType | None = None
    drive_type: DriveType | None = None
    year_start: int | None = None
    year_end: int | None = None
    display_name: str | None = None


# ── Полная иерархия для выбора авто ─────────────────────────────

class CarBrandWithModels(CarBrandOut):
    models: list[CarModelOut] = []


class CarModelWithGenerations(CarModelOut):
    generations: list[CarGenerationOut] = []


class CarGenerationFull(CarGenerationOut):
    engines: list[CarEngineOut] = []
    bodies: list[CarBodyOut] = []
    trims: list[CarTrimOut] = []
