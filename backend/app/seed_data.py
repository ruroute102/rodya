"""Скрипт заполнения БД начальными демо-данными.

Запуск:
    cd backend
    python -m app.seed_data
"""

import asyncio
from datetime import datetime, timezone

from sqlalchemy import select
from sqlalchemy.ext.asyncio import AsyncSession

from app.core.security import hash_password, generate_otp
from app.db.session import async_session_factory, engine
from app.models import Base
from app.models.car import (
    BodyType, CarBody, CarBrand, CarConfiguration, CarEngine, CarGeneration,
    CarModel, CarTrim, DriveType, FuelType, TransmissionType,
)
from app.models.content import (
    Comment, CommentStatus, Component, ComponentCategory, ContentStatus,
    Difficulty, DiagnosticCheck, DiagnosticRule, Guide, GuideConfiguration,
    GuidePrecaution, GuideStep, StepCheck, StepConsumable, StepTool,
    StepWarning, Symptom, SymptomCategory, TechSpec, TechSpecCategory,
    WarningSeverity, WarningType,
)
from app.models.user import User, UserCar, UserRole


async def seed_database():
    """Создаёт таблицы и заполняет демо-данными."""
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)

    async with async_session_factory() as session:
        # Check if already seeded
        result = await session.execute(select(CarBrand).limit(1))
        if result.scalar_one_or_none() is not None:
            print("БД уже заполнена. Пропускаем.")
            return

        print("Заполняем БД демо-данными...")

        # ── Users ──────────────────────────────────────────────
        admin = User(
            phone="+79001234567",
            email="admin@techgid.ru",
            email_verified=True,
            password_hash=hash_password("admin123"),
            display_name="Администратор",
            role=UserRole.ADMIN,
        )
        author = User(
            phone="+79001234568",
            email="alexey@techgid.ru",
            email_verified=True,
            password_hash=hash_password("author123"),
            display_name="Алексей",
            role=UserRole.AUTHOR,
            is_verified_author=True,
        )
        user1 = User(
            phone="+79001234569",
            password_hash=hash_password("user1234"),
            display_name="Дмитрий",
            role=UserRole.USER,
        )
        user2 = User(
            phone="+79001234570",
            password_hash=hash_password("user1234"),
            display_name="Эрик",
            role=UserRole.USER,
        )
        user3 = User(
            phone="+79001234571",
            password_hash=hash_password("user1234"),
            display_name="Ольга",
            role=UserRole.USER,
        )
        session.add_all([admin, author, user1, user2, user3])
        await session.flush()

        # ── Car Brands ─────────────────────────────────────────
        brands_data = [
            ("Audi", "audi", "Германия"),
            ("BMW", "bmw", "Германия"),
            ("Mercedes-Benz", "mercedes-benz", "Германия"),
            ("Toyota", "toyota", "Япония"),
            ("Volkswagen", "volkswagen", "Германия"),
            ("Kia", "kia", "Южная Корея"),
            ("Hyundai", "hyundai", "Южная Корея"),
            ("Lada", "lada", "Россия"),
        ]
        brands = []
        for i, (name, slug, country) in enumerate(brands_data, start=1):
            b = CarBrand(name=name, slug=slug, country=country, sort_order=i)
            session.add(b)
            brands.append(b)
        await session.flush()

        audi = brands[0]

        # ── Car Models (Audi) ──────────────────────────────────
        models_data = [("Q3", "q3"), ("Q5", "q5"), ("A4", "a4"), ("A6", "a6")]
        models = []
        for name, slug in models_data:
            m = CarModel(brand_id=audi.id, name=name, slug=slug)
            session.add(m)
            models.append(m)
        await session.flush()

        q3 = models[0]

        # ── Generations (Audi Q3) ──────────────────────────────
        gen_8u = CarGeneration(
            model_id=q3.id, name="8U (I поколение)", slug="8u",
            chassis_code="8U", year_start=2011, year_end=2018,
        )
        gen_f3 = CarGeneration(
            model_id=q3.id, name="F3 (II поколение)", slug="f3",
            chassis_code="F3", year_start=2018, year_end=None,
        )
        session.add_all([gen_8u, gen_f3])
        await session.flush()

        # ── Engines ────────────────────────────────────────────
        engine_20tfsi = CarEngine(
            generation_id=gen_8u.id, code="CULB", name="2.0 TFSI (211 л.с.)",
            displacement_cc=1984, displacement_label="2.0 TFSI",
            fuel_type=FuelType.PETROL, power_hp=211, torque_nm=350,
        )
        engine_20tdi = CarEngine(
            generation_id=gen_8u.id, code="CFFB", name="2.0 TDI (140 л.с.)",
            displacement_cc=1968, displacement_label="2.0 TDI",
            fuel_type=FuelType.DIESEL, power_hp=140, torque_nm=320,
        )
        engine_14tfsi = CarEngine(
            generation_id=gen_8u.id, code="CZEA", name="1.4 TFSI (150 л.с.)",
            displacement_cc=1395, displacement_label="1.4 TFSI",
            fuel_type=FuelType.PETROL, power_hp=150, torque_nm=250,
        )
        session.add_all([engine_20tfsi, engine_20tdi, engine_14tfsi])
        await session.flush()

        # ── Bodies & Trims ─────────────────────────────────────
        body_suv = CarBody(generation_id=gen_8u.id, body_type=BodyType.SUV, doors_count=5, name="SUV")
        session.add(body_suv)
        await session.flush()

        trim_base = CarTrim(generation_id=gen_8u.id, name="Базовая", market="RU")
        trim_sline = CarTrim(generation_id=gen_8u.id, name="S-Line", market="RU")
        session.add_all([trim_base, trim_sline])
        await session.flush()

        # ── Configuration ──────────────────────────────────────
        config1 = CarConfiguration(
            generation_id=gen_8u.id, engine_id=engine_20tfsi.id,
            body_id=body_suv.id, trim_id=trim_sline.id,
            transmission_type=TransmissionType.ROBOT,
            drive_type=DriveType.AWD,
            year_start=2011, year_end=2018,
            display_name="Audi Q3 8U 2.0 TFSI quattro S-tronic S-Line",
        )
        session.add(config1)
        await session.flush()

        # ── User Cars ─────────────────────────────────────────
        session.add(UserCar(
            user_id=user1.id, configuration_id=config1.id,
            display_name="Audi Q3 2013", is_primary=True,
        ))
        session.add(UserCar(
            user_id=user2.id, configuration_id=config1.id,
            display_name="Audi Q3 2011", is_primary=True,
        ))
        await session.flush()

        # ── Component Categories ───────────────────────────────
        cat_fuel = ComponentCategory(name="Топливная система", slug="fuel-system", sort_order=1)
        cat_engine = ComponentCategory(name="Двигатель", slug="engine", sort_order=2)
        cat_brakes = ComponentCategory(name="Тормозная система", slug="brakes", sort_order=3)
        cat_ignition = ComponentCategory(name="Система зажигания", slug="ignition", sort_order=4)
        cat_suspension = ComponentCategory(name="Подвеска", slug="suspension", sort_order=5)
        session.add_all([cat_fuel, cat_engine, cat_brakes, cat_ignition, cat_suspension])
        await session.flush()

        # ── Components ─────────────────────────────────────────
        comp_fuel_pump = Component(
            category_id=cat_fuel.id, name="Модуль топливного насоса",
            slug="fuel-pump-module",
            description="Электрический бензонасос, расположенный в топливном баке.",
        )
        comp_fuel_filter = Component(
            category_id=cat_fuel.id, name="Топливный фильтр",
            slug="fuel-filter",
        )
        comp_oil = Component(
            category_id=cat_engine.id, name="Моторное масло и фильтр",
            slug="engine-oil",
        )
        comp_brake_pads = Component(
            category_id=cat_brakes.id, name="Передние тормозные колодки",
            slug="front-brake-pads",
        )
        comp_air_filter = Component(
            category_id=cat_engine.id, name="Воздушный фильтр",
            slug="air-filter",
        )
        comp_coil = Component(
            category_id=cat_ignition.id, name="Катушка зажигания",
            slug="ignition-coil",
        )
        session.add_all([comp_fuel_pump, comp_fuel_filter, comp_oil, comp_brake_pads, comp_air_filter, comp_coil])
        await session.flush()

        # ── Guide 1: Fuel pump ─────────────────────────────────
        guide1 = Guide(
            component_id=comp_fuel_pump.id, title="Замена модуля топливного насоса",
            slug="fuel-pump-module", difficulty=Difficulty.MEDIUM,
            estimated_time_min=90, status=ContentStatus.PUBLISHED,
            author_id=author.id, is_verified=True,
        )
        session.add(guide1)
        await session.flush()

        session.add(GuideConfiguration(guide_id=guide1.id, configuration_id=config1.id))

        # Precautions
        session.add(GuidePrecaution(
            guide_id=guide1.id, warning_type=WarningType.SAFETY,
            severity=WarningSeverity.DANGER,
            text="Работы с топливной системой — риск возгорания. Работайте в проветриваемом помещении.",
        ))
        session.add(GuidePrecaution(
            guide_id=guide1.id, warning_type=WarningType.SAFETY,
            severity=WarningSeverity.WARNING,
            text="Отключите аккумулятор перед началом работ.",
        ))

        # Step 1
        step1 = GuideStep(
            guide_id=guide1.id, step_number=1,
            title="Сбросьте давление в топливной системе",
            description="Извлеките предохранитель бензонасоса (F33) и заведите двигатель. Подождите, пока двигатель заглохнет сам.",
        )
        session.add(step1)
        await session.flush()

        session.add(StepWarning(step_id=step1.id, warning_type=WarningType.SAFETY, severity=WarningSeverity.DANGER, text="Опасность пожара! Работайте вдали от открытого огня."))
        session.add(StepWarning(step_id=step1.id, warning_type=WarningType.SAFETY, severity=WarningSeverity.WARNING, text="Отключите клемму «минус» аккумулятора."))
        session.add(StepTool(step_id=step1.id, tool_name="Ключ рожковый", tool_spec="10 мм"))
        session.add(StepCheck(step_id=step1.id, description="Убедитесь, что двигатель полностью заглох."))

        # Step 2
        step2 = GuideStep(
            guide_id=guide1.id, step_number=2,
            title="Снимите заднее сиденье",
            description="Потяните подушку заднего сиденья вверх за передний край. Фиксаторы отщёлкиваются при вытягивании вверх.",
        )
        session.add(step2)
        await session.flush()

        session.add(StepWarning(step_id=step2.id, warning_type=WarningType.CAUTION, severity=WarningSeverity.CAUTION, text="Фиксаторы хрупкие. Тяните ровно вверх."))
        session.add(StepTool(step_id=step2.id, tool_name="Плоская отвёртка", tool_spec="тонкая"))
        session.add(StepCheck(step_id=step2.id, description="Оба фиксатора отщелкнулись, подушка снята."))

        # Step 3
        step3 = GuideStep(
            guide_id=guide1.id, step_number=3,
            title="Очистите зону вокруг крышки доступа",
            description="Протрите пыль мягкой тряпкой, чтобы грязь не попала в бак.",
        )
        session.add(step3)
        await session.flush()

        session.add(StepTool(step_id=step3.id, tool_name="Мягкая ветошь"))
        session.add(StepTool(step_id=step3.id, tool_name="Пылесос", is_required=False))
        session.add(StepConsumable(step_id=step3.id, name="Ветошь безворсовая", quantity="2-3 шт"))
        session.add(StepWarning(step_id=step3.id, warning_type=WarningType.CAUTION, severity=WarningSeverity.CAUTION, text="Попадание грязи в бак выведет из строя новый насос."))
        session.add(StepCheck(step_id=step3.id, description="Область вокруг крышки чистая."))

        # Step 4
        step4 = GuideStep(
            guide_id=guide1.id, step_number=4,
            title="Снимите крышку и отсоедините разъёмы",
            description="Открутите 4 самореза крышки лючка (Torx T20). Отсоедините электрический разъём и топливные шланги.",
        )
        session.add(step4)
        await session.flush()

        session.add(StepTool(step_id=step4.id, tool_name="Отвёртка Torx", tool_spec="T20"))
        session.add(StepWarning(step_id=step4.id, warning_type=WarningType.CAUTION, severity=WarningSeverity.WARNING, text="При отсоединении шлангов вытечет бензин."))
        session.add(StepCheck(step_id=step4.id, description="Все разъёмы и шланги отсоединены."))

        # Step 5
        step5 = GuideStep(
            guide_id=guide1.id, step_number=5,
            title="Извлеките старый модуль и установите новый",
            description="Поверните прижимное кольцо против часовой стрелки. Извлеките модуль из бака. Установите новый модуль и замените уплотнительное кольцо.",
        )
        session.add(step5)
        await session.flush()

        session.add(StepTool(step_id=step5.id, tool_name="Ключ для прижимного кольца"))
        session.add(StepConsumable(step_id=step5.id, name="Модуль топливного насоса", part_number="7P0 919 087 E", quantity="1 шт"))
        session.add(StepConsumable(step_id=step5.id, name="Уплотнительное кольцо", quantity="1 шт"))
        session.add(StepCheck(step_id=step5.id, description="Новый модуль плотно сидит в баке."))

        # Step 6
        step6 = GuideStep(
            guide_id=guide1.id, step_number=6,
            title="Соберите обратно и проверьте",
            description="Подключите разъёмы, установите крышку, верните предохранитель. Включите зажигание 3 раза по 3 секунды для создания давления.",
        )
        session.add(step6)
        await session.flush()

        session.add(StepTool(step_id=step6.id, tool_name="Отвёртка Torx", tool_spec="T20"))
        session.add(StepCheck(step_id=step6.id, description="Двигатель завёлся без проблем."))
        session.add(StepCheck(step_id=step6.id, description="Нет утечек топлива.", is_post_step=True))

        # ── Guide 2: Oil change ────────────────────────────────
        guide2 = Guide(
            component_id=comp_oil.id, title="Замена масла и масляного фильтра",
            slug="oil-change", difficulty=Difficulty.EASY,
            estimated_time_min=30, status=ContentStatus.PUBLISHED,
            author_id=author.id, is_verified=True,
        )
        session.add(guide2)
        await session.flush()
        session.add(GuideConfiguration(guide_id=guide2.id, configuration_id=config1.id))

        # ── Guide 3: Brake pads ────────────────────────────────
        guide3 = Guide(
            component_id=comp_brake_pads.id, title="Замена передних тормозных колодок",
            slug="front-brake-pads", difficulty=Difficulty.MEDIUM,
            estimated_time_min=60, status=ContentStatus.PUBLISHED,
            author_id=author.id, is_verified=True,
        )
        session.add(guide3)
        await session.flush()
        session.add(GuideConfiguration(guide_id=guide3.id, configuration_id=config1.id))

        # ── Comments ───────────────────────────────────────────
        session.add(Comment(
            user_id=user1.id, guide_id=guide1.id, step_id=step3.id,
            text="На моём 2013 лючок был ближе к правой стороне, клипсы сиденья туже.",
            status=CommentStatus.APPROVED,
        ))
        session.add(Comment(
            user_id=user2.id, guide_id=guide1.id, step_id=step3.id,
            text="Спасибо за совет про пылесос — реально много грязи было.",
            status=CommentStatus.APPROVED,
        ))

        # ── Symptom Categories & Symptoms ──────────────────────
        sym_cat_engine = SymptomCategory(name="Двигатель", slug="engine")
        sym_cat_fuel = SymptomCategory(name="Топливная система", slug="fuel")
        sym_cat_electrical = SymptomCategory(name="Электрика", slug="electrical")
        session.add_all([sym_cat_engine, sym_cat_fuel, sym_cat_electrical])
        await session.flush()

        sym1 = Symptom(category_id=sym_cat_engine.id, name="Не заводится")
        sym2 = Symptom(category_id=sym_cat_engine.id, name="Плавают обороты")
        sym3 = Symptom(category_id=sym_cat_engine.id, name="Троит")
        sym4 = Symptom(category_id=sym_cat_fuel.id, name="Запах бензина в салоне")
        sym5 = Symptom(category_id=sym_cat_fuel.id, name="Потеря мощности")
        sym6 = Symptom(category_id=sym_cat_electrical.id, name="Горит Check Engine")
        session.add_all([sym1, sym2, sym3, sym4, sym5, sym6])
        await session.flush()

        # ── Diagnostic Rules ───────────────────────────────────
        rule1 = DiagnosticRule(
            symptom_id=sym1.id, configuration_id=config1.id,
            probable_cause="Неисправность бензонасоса",
            probability=0.75,
            related_component_id=comp_fuel_pump.id,
            related_guide_id=guide1.id,
            check_description="Проверить давление в рампе. Норма: 4–5 бар.",
        )
        session.add(rule1)
        await session.flush()

        session.add(DiagnosticCheck(
            rule_id=rule1.id, step_number=1,
            description="Проверить давление в рампе. Норма: 4–5 бар",
            expected_result="4.0 - 5.0 бар",
            tool_needed="Манометр топливной системы",
        ))
        session.add(DiagnosticCheck(
            rule_id=rule1.id, step_number=2,
            description="Послушать работу насоса при включении зажигания",
            expected_result="Должен быть слышен гул 2-3 секунды",
        ))

        # ── Tech Specs ─────────────────────────────────────────
        ts_cat = TechSpecCategory(name="Давления", slug="pressures")
        session.add(ts_cat)
        await session.flush()

        session.add(TechSpec(
            configuration_id=config1.id, component_id=comp_fuel_pump.id,
            category_id=ts_cat.id,
            spec_key="Давление топлива в рампе",
            spec_value="4.5", unit="бар",
            min_value=4.0, max_value=5.0,
            notes="Измерять при работающем двигателе на холостых оборотах",
        ))
        session.add(TechSpec(
            configuration_id=config1.id, component_id=comp_oil.id,
            category_id=ts_cat.id,
            spec_key="Давление масла на холостых",
            spec_value="2.0", unit="бар",
            min_value=1.5, max_value=3.0,
            notes="При прогретом двигателе (90°C)",
        ))

        await session.commit()
        print("Демо-данные успешно загружены!")
        print(f"  Марки: {len(brands_data)}")
        print(f"  Пользователи: 5 (admin, author, 3 users)")
        print(f"  Инструкции: 3")
        print(f"  Симптомы: 6")


async def main():
    await seed_database()
    await engine.dispose()


if __name__ == "__main__":
    asyncio.run(main())
