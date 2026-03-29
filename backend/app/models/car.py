"""Модели автомобилей: марки, модели, поколения, двигатели, кузова, комплектации."""

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
from sqlalchemy.orm import Mapped, mapped_column, relationship

from app.db.base import Base


# ── Перечисления ────────────────────────────────────────────────

class FuelType(str, enum.Enum):
    PETROL = "petrol"
    DIESEL = "diesel"
    HYBRID = "hybrid"
    ELECTRIC = "electric"
    GAS = "gas"


class TransmissionType(str, enum.Enum):
    MANUAL = "manual"
    AUTOMATIC = "automatic"
    CVT = "cvt"
    ROBOT = "robot"
    DCT = "dct"


class DriveType(str, enum.Enum):
    FWD = "fwd"
    RWD = "rwd"
    AWD = "awd"


class BodyType(str, enum.Enum):
    SEDAN = "sedan"
    HATCHBACK = "hatchback"
    WAGON = "wagon"
    SUV = "suv"
    CROSSOVER = "crossover"
    COUPE = "coupe"
    CONVERTIBLE = "convertible"
    MINIVAN = "minivan"
    PICKUP = "pickup"
    LIFTBACK = "liftback"


# ── Марка ───────────────────────────────────────────────────────

class CarBrand(Base):
    __tablename__ = "car_brands"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    name: Mapped[str] = mapped_column(String(100), nullable=False, unique=True)
    slug: Mapped[str] = mapped_column(String(100), nullable=False, unique=True, index=True)
    logo_url: Mapped[str | None] = mapped_column(String(500))
    country: Mapped[str | None] = mapped_column(String(100))
    sort_order: Mapped[int] = mapped_column(Integer, default=0)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True)

    models: Mapped[list["CarModel"]] = relationship(back_populates="brand", lazy="selectin")


# ── Модель ──────────────────────────────────────────────────────

class CarModel(Base):
    __tablename__ = "car_models"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    brand_id: Mapped[int] = mapped_column(ForeignKey("car_brands.id"), nullable=False)
    name: Mapped[str] = mapped_column(String(100), nullable=False)
    slug: Mapped[str] = mapped_column(String(100), nullable=False, index=True)
    sort_order: Mapped[int] = mapped_column(Integer, default=0)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True)

    brand: Mapped["CarBrand"] = relationship(back_populates="models")
    generations: Mapped[list["CarGeneration"]] = relationship(back_populates="model", lazy="selectin")

    __table_args__ = (
        UniqueConstraint("brand_id", "slug", name="uq_car_models_brand_slug"),
    )


# ── Поколение ───────────────────────────────────────────────────

class CarGeneration(Base):
    __tablename__ = "car_generations"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    model_id: Mapped[int] = mapped_column(ForeignKey("car_models.id"), nullable=False)
    name: Mapped[str] = mapped_column(String(100), nullable=False)
    slug: Mapped[str] = mapped_column(String(100), nullable=False, index=True)
    chassis_code: Mapped[str | None] = mapped_column(String(50))
    year_start: Mapped[int] = mapped_column(Integer, nullable=False)
    year_end: Mapped[int | None] = mapped_column(Integer)
    image_url: Mapped[str | None] = mapped_column(String(500))
    is_active: Mapped[bool] = mapped_column(Boolean, default=True)

    model: Mapped["CarModel"] = relationship(back_populates="generations")
    engines: Mapped[list["CarEngine"]] = relationship(back_populates="generation", lazy="selectin")
    bodies: Mapped[list["CarBody"]] = relationship(back_populates="generation", lazy="selectin")
    trims: Mapped[list["CarTrim"]] = relationship(back_populates="generation", lazy="selectin")
    configurations: Mapped[list["CarConfiguration"]] = relationship(back_populates="generation", lazy="selectin")

    __table_args__ = (
        UniqueConstraint("model_id", "slug", name="uq_car_generations_model_slug"),
    )


# ── Двигатель ───────────────────────────────────────────────────

class CarEngine(Base):
    __tablename__ = "car_engines"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    generation_id: Mapped[int] = mapped_column(ForeignKey("car_generations.id"), nullable=False)
    code: Mapped[str] = mapped_column(String(50), nullable=False)
    name: Mapped[str] = mapped_column(String(150), nullable=False)
    displacement_cc: Mapped[int | None] = mapped_column(Integer)
    displacement_label: Mapped[str | None] = mapped_column(String(20))  # "2.0 TFSI"
    fuel_type: Mapped[FuelType] = mapped_column(Enum(FuelType), nullable=False)
    power_hp: Mapped[int | None] = mapped_column(Integer)
    torque_nm: Mapped[int | None] = mapped_column(Integer)
    is_active: Mapped[bool] = mapped_column(Boolean, default=True)

    generation: Mapped["CarGeneration"] = relationship(back_populates="engines")


# ── Кузов ───────────────────────────────────────────────────────

class CarBody(Base):
    __tablename__ = "car_bodies"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    generation_id: Mapped[int] = mapped_column(ForeignKey("car_generations.id"), nullable=False)
    body_type: Mapped[BodyType] = mapped_column(Enum(BodyType), nullable=False)
    doors_count: Mapped[int] = mapped_column(Integer, default=4)
    name: Mapped[str | None] = mapped_column(String(100))
    is_active: Mapped[bool] = mapped_column(Boolean, default=True)

    generation: Mapped["CarGeneration"] = relationship(back_populates="bodies")


# ── Комплектация ────────────────────────────────────────────────

class CarTrim(Base):
    __tablename__ = "car_trims"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    generation_id: Mapped[int] = mapped_column(ForeignKey("car_generations.id"), nullable=False)
    name: Mapped[str] = mapped_column(String(150), nullable=False)
    market: Mapped[str | None] = mapped_column(String(50))  # "RU", "EU", "US"
    is_active: Mapped[bool] = mapped_column(Boolean, default=True)

    generation: Mapped["CarGeneration"] = relationship(back_populates="trims")


# ── Конфигурация (конкретная сборка) ────────────────────────────

class CarConfiguration(Base):
    """Конкретная сборка: поколение + двигатель + кузов + комплектация + трансмиссия.
    Это главная связующая сущность между автомобилем и контентом."""
    __tablename__ = "car_configurations"

    id: Mapped[int] = mapped_column(Integer, primary_key=True, autoincrement=True)
    generation_id: Mapped[int] = mapped_column(ForeignKey("car_generations.id"), nullable=False)
    engine_id: Mapped[int] = mapped_column(ForeignKey("car_engines.id"), nullable=False)
    body_id: Mapped[int | None] = mapped_column(ForeignKey("car_bodies.id"))
    trim_id: Mapped[int | None] = mapped_column(ForeignKey("car_trims.id"))
    transmission_type: Mapped[TransmissionType | None] = mapped_column(Enum(TransmissionType))
    drive_type: Mapped[DriveType | None] = mapped_column(Enum(DriveType))
    year_start: Mapped[int | None] = mapped_column(Integer)
    year_end: Mapped[int | None] = mapped_column(Integer)
    display_name: Mapped[str | None] = mapped_column(String(300))
    is_active: Mapped[bool] = mapped_column(Boolean, default=True)

    generation: Mapped["CarGeneration"] = relationship(back_populates="configurations")
    engine: Mapped["CarEngine"] = relationship()
    body: Mapped["CarBody | None"] = relationship()
    trim: Mapped["CarTrim | None"] = relationship()
