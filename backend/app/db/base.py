"""Базовый класс для всех SQLAlchemy-моделей."""

from sqlalchemy.orm import DeclarativeBase, MappedAsDataclass


class Base(DeclarativeBase):
    """Базовый класс ORM-моделей."""
    pass
