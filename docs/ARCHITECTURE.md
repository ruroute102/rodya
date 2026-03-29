# ТехГид — Архитектура продукта

## 1. Общее описание

ТехГид — Android-приложение для ремонта и обслуживания автомобилей с визуальным 3D-представлением, пошаговыми инструкциями, базой техданных, диагностикой по симптомам и офлайн-режимом.

---

## 2. Стек технологий

### 2.1 Android-приложение
| Компонент | Технология | Почему |
|-----------|-----------|--------|
| Язык | Kotlin 2.0 | Стандарт Android-разработки, null-safety, корутины |
| UI | Jetpack Compose + Material 3 | Декларативный UI, гибкие темы, анимации |
| Архитектура | Clean Architecture + MVI | Разделение слоёв, тестируемость, предсказуемость |
| DI | Hilt (Dagger) | Стандарт DI для Android, compile-time проверки |
| Навигация | Navigation Compose | Type-safe навигация между экранами |
| Сеть | Retrofit + OkHttp + Moshi | Надёжный HTTP-клиент, JSON-сериализация |
| Локальная БД | Room | ORM для SQLite, миграции, реактивные запросы |
| 3D-рендеринг | Filament (через SceneView) | Google-движок, поддержка glTF/GLB, PBR |
| Изображения | Coil | Kotlin-first, корутины, кэширование |
| Безопасность | EncryptedSharedPreferences, SQLCipher | Шифрование токенов и офлайн-данных |
| Настройки | DataStore Proto | Type-safe хранение настроек |
| Пагинация | Paging 3 | Загрузка данных порциями |

### 2.2 Backend
| Компонент | Технология | Почему |
|-----------|-----------|--------|
| Язык | Python 3.12 | Уже установлен, быстрая разработка |
| Фреймворк | FastAPI | Async, автодокументация OpenAPI, валидация Pydantic |
| ORM | SQLAlchemy 2.0 (async) | Мощный ORM, поддержка PostgreSQL |
| Миграции | Alembic | Версионирование схемы БД |
| БД | PostgreSQL 16 | Уже установлен pgAdmin4, JSONB, полнотекстовый поиск |
| Кэш | Redis | Кэш запросов, сессии, rate limiting |
| Хранилище файлов | MinIO (S3-совместимый) | Self-hosted, API как у AWS S3 |
| Аутентификация | JWT (access + refresh) | Stateless, масштабируемость |
| SMS | SMS-провайдер (SMS.ru / Twilio) | OTP для верификации телефона |
| Email | SMTP (Mailgun / Yandex) | Подтверждение почты |
| Задачи | Celery + Redis | Фоновые задачи (модерация, уведомления) |
| Поиск | PostgreSQL FTS + pg_trgm | Полнотекстовый поиск без Elasticsearch |

### 2.3 Админ-панель
| Компонент | Технология | Почему |
|-----------|-----------|--------|
| Фреймворк | React 18 + TypeScript | Экосистема, типизация, компонентность |
| UI-библиотека | Ant Design 5 | Готовые таблицы, формы, фильтры, дашборды |
| Сборка | Vite | Быстрая сборка, HMR |
| Запросы | TanStack Query | Кэширование, ревалидация, пагинация |
| Состояние | Zustand | Простой глобальный стейт |
| Роутинг | React Router 6 | SPA-навигация |
| 3D-превью | Three.js | Превью 3D-моделей в браузере |
| Загрузка файлов | tus-js-client | Resumable upload больших файлов |

---

## 3. Архитектура системы

```
┌─────────────────┐     ┌─────────────────┐     ┌─────────────────┐
│  Android App    │     │  Admin Panel     │     │  (Будущее)      │
│  Kotlin/Compose │     │  React/TS        │     │  iOS App        │
└────────┬────────┘     └────────┬────────┘     └────────┬────────┘
         │                       │                       │
         │              HTTPS + JWT                      │
         └───────────────┬───────┴───────────────────────┘
                         │
                    ┌────▼────┐
                    │  Nginx  │  Reverse proxy, SSL, rate limit
                    └────┬────┘
                         │
              ┌──────────▼──────────┐
              │   FastAPI Backend   │
              │                     │
              │  ┌───────────────┐  │
              │  │ Auth Service  │  │
              │  │ Content API   │  │
              │  │ Search API    │  │
              │  │ Diagnostic    │  │
              │  │ Comments API  │  │
              │  │ Admin API     │  │
              │  │ Subscription  │  │
              │  └───────────────┘  │
              └──┬──────┬───────┬───┘
                 │      │       │
         ┌───────▼┐ ┌───▼───┐ ┌▼────────┐
         │PostgreSQL│ │ Redis │ │  MinIO  │
         │         │ │       │ │  (S3)   │
         └─────────┘ └───────┘ └─────────┘
```

### 3.1 Слои Android-приложения (Clean Architecture)

```
app/
├── data/               # Слой данных
│   ├── remote/         # API-клиент, DTO
│   ├── local/          # Room DAO, Entity
│   ├── repository/     # Реализация репозиториев
│   └── mapper/         # DTO <-> Domain маппинг
├── domain/             # Бизнес-логика
│   ├── model/          # Domain-модели
│   ├── repository/     # Интерфейсы репозиториев
│   └── usecase/        # Use cases
├── presentation/       # UI-слой
│   ├── theme/          # Темы, цвета, типография
│   ├── components/     # Общие Compose-компоненты
│   ├── screen/         # Экраны
│   │   ├── carselect/  # Выбор автомобиля
│   │   ├── catalog/    # Каталог инструкций
│   │   ├── viewer3d/   # 3D-просмотр
│   │   ├── guide/      # Пошаговая инструкция
│   │   ├── search/     # Поиск
│   │   ├── diagnostic/ # Диагностика по симптомам
│   │   ├── profile/    # Профиль и ЛК
│   │   └── auth/       # Авторизация
│   └── navigation/     # Граф навигации
├── di/                 # Hilt-модули
├── security/           # Шифрование, certificate pinning
└── util/               # Утилиты
```

---

## 4. Структура базы данных

### 4.1 Автомобили (иерархическая структура)

```
car_brands (id, name, slug, logo_url, sort_order)
    │
    ├── car_models (id, brand_id, name, slug, sort_order)
    │       │
    │       ├── car_generations (id, model_id, name, year_start, year_end, slug)
    │       │       │
    │       │       ├── car_bodies (id, generation_id, body_type, doors_count)
    │       │       │
    │       │       ├── car_engines (id, generation_id, code, displacement,
    │       │       │                fuel_type, power_hp, torque_nm)
    │       │       │
    │       │       └── car_trims (id, generation_id, name, market)
    │       │
    │       └── car_configurations (id, generation_id, engine_id, body_id,
    │                                trim_id, years, transmission_type)
    │
    └── [car_configurations — конкретная сборка: поколение + двигатель + кузов + комплектация]
```

### 4.2 Контент

```
component_categories (id, parent_id, name, slug, icon, sort_order)
    │
    └── components (id, category_id, name, slug, description)
            │
            ├── component_positions (id, component_id, configuration_id,
            │                        position_3d_x/y/z, zone, access_notes)
            │
            └── guides (id, component_id, title, slug, difficulty,
                        estimated_time_min, status, author_id,
                        is_verified, created_at, updated_at)
                    │
                    ├── guide_configurations (guide_id, configuration_id)
                    │   [какие конфигурации авто поддерживает инструкция]
                    │
                    ├── guide_steps (id, guide_id, step_number, title,
                    │                description, image_url, model_3d_url,
                    │                camera_position, highlighted_parts,
                    │                hidden_parts)
                    │       │
                    │       ├── step_tools (step_id, tool_name, tool_spec, required)
                    │       │
                    │       ├── step_consumables (step_id, name, part_number, quantity)
                    │       │
                    │       ├── step_warnings (step_id, type, text, severity)
                    │       │   [type: safety/caution/info/variation]
                    │       │
                    │       ├── step_variations (step_id, configuration_id, text, image_url)
                    │       │   [отличия для конкретных конфигураций]
                    │       │
                    │       └── step_checks (step_id, description, is_post_step)
                    │           [проверки после шага и после всего ремонта]
                    │
                    ├── guide_precautions (guide_id, type, text, severity)
                    │
                    └── guide_media (guide_id, type, url, sort_order, watermarked)
```

### 4.3 Техданные

```
tech_specs (id, configuration_id, component_id, spec_key, spec_value,
            unit, min_value, max_value, notes)
    [Пример: configuration=Audi_Q3_2011_2.0TFSI, component=fuel_rail,
     spec_key="pressure", spec_value="4.5", unit="bar",
     min_value=4.0, max_value=5.0]

tech_spec_categories (id, name, slug)
    [Пример: "Давления", "Моменты затяжки", "Объёмы", "Зазоры"]

Полнотекстовый поиск по spec_key, spec_value, notes, component.name
```

### 4.4 Диагностика

```
symptoms (id, category_id, name, description)
    [Пример: "Двигатель не заводится", "Плавают обороты"]

symptom_categories (id, name, slug)
    [Пример: "Двигатель", "Трансмиссия", "Электрика"]

diagnostic_rules (id, symptom_id, configuration_id, probable_cause,
                  probability, related_component_id, related_guide_id,
                  check_description)
    [Пример: symptom="не заводится" → cause="бензонасос",
     probability=0.3, component=fuel_pump, guide=replace_fuel_pump]

diagnostic_checks (id, rule_id, step_number, description,
                   expected_result, tool_needed)
    [Пример: "Проверить давление в рампе. Норма: 4-5 бар.
     Инструмент: манометр"]
```

### 4.5 Пользователи и комментарии

```
users (id, phone, email, email_verified, password_hash,
       display_name, avatar_url, role, is_verified_author,
       created_at, last_login)
    │
    ├── user_cars (id, user_id, configuration_id, display_name,
    │              is_primary, vin)
    │
    ├── comments (id, user_id, parent_id, guide_id, step_id,
    │             text, status, created_at, updated_at)
    │   [parent_id → древовидные комментарии]
    │   [step_id — null если комментарий к инструкции, не к шагу]
    │   [status: pending/approved/rejected/hidden]
    │
    └── user_offline_packs (id, user_id, configuration_id,
                            downloaded_at, version, size_bytes)

roles: user, author, moderator, editor, admin
```

### 4.6 Монетизация

```
subscription_plans (id, name, price, currency, duration_days,
                    features_json, is_active)

user_subscriptions (id, user_id, plan_id, started_at, expires_at,
                    status, payment_provider, payment_id)

premium_content (content_type, content_id, required_plan_id)
    [Разметка: какой контент требует какую подписку]
```

### 4.7 3D-модели

```
models_3d (id, configuration_id, file_url, format, file_size,
           version, parts_metadata_json)
    │
    └── model_3d_parts (id, model_3d_id, part_name, component_id,
                        mesh_name, default_visible, selectable,
                        highlight_color)
```

---

## 5. Модель безопасности

### 5.1 Аутентификация
- **Регистрация**: телефон → OTP (6 цифр, 5 мин TTL, 3 попытки) → создание аккаунта
- **Email**: подтверждение по ссылке (токен 24ч)
- **Пароль**: bcrypt, минимум 8 символов
- **JWT**: access token (15 мин) + refresh token (30 дней, ротация)
- **Хранение токенов**: EncryptedSharedPreferences (Android Keystore)

### 5.2 API-безопасность
- HTTPS only (certificate pinning через OkHttp CertificatePinner)
- Rate limiting: Redis-based (100 req/min для обычных, 5 req/min для auth)
- CORS: только для админ-панели
- Input validation: Pydantic на backend, валидация на клиенте
- SQL injection: SQLAlchemy параметризованные запросы
- XSS: экранирование на фронте (React + Compose автоматически)

### 5.3 Защита контента
- **Офлайн-данные**: SQLCipher (AES-256) для локальной БД
- **Файлы**: AES-256-GCM шифрование, ключ = device_id + user_id + server_secret
- **3D-модели**: проприетарный бинарный формат (GLB → зашифрованный .tgm)
- **Изображения**: серверный watermark (Pillow) — логотип + user_id в метаданных
- **Невидимый watermark**: стеганография в LSB пикселей
- **API**: подписанные URL с TTL для медиа-файлов (pre-signed S3 URLs)
- **Root/Jailbreak detection**: SafetyNet / Play Integrity API
- **Anti-tampering**: ProGuard/R8 обфускация + integrity checks

> **Честно**: 100% защита невозможна. Мотивированный reverse-engineer всегда может
> извлечь контент. Цель — сделать это настолько дорого и неудобно, чтобы было
> проще купить подписку. Многослойная защита (шифрование + watermark +
> obfuscation + pinning + integrity checks) делает стоимость взлома
> непропорционально высокой.

### 5.4 RBAC (Role-Based Access Control)

| Действие | user | author | moderator | editor | admin |
|----------|------|--------|-----------|--------|-------|
| Читать инструкции | ✓ | ✓ | ✓ | ✓ | ✓ |
| Комментировать | ✓ | ✓ | ✓ | ✓ | ✓ |
| Предложить инструкцию | ✓ | ✓ | ✓ | ✓ | ✓ |
| Публиковать инструкции | ✗ | ✓* | ✗ | ✓ | ✓ |
| Модерировать комментарии | ✗ | ✗ | ✓ | ✓ | ✓ |
| Модерировать инструкции | ✗ | ✗ | ✓ | ✓ | ✓ |
| Редактировать весь контент | ✗ | ✗ | ✗ | ✓ | ✓ |
| Управлять пользователями | ✗ | ✗ | ✗ | ✗ | ✓ |
| Управлять подписками | ✗ | ✗ | ✗ | ✗ | ✓ |

*автор — после верификации, его инструкции всё равно проходят модерацию

---

## 6. Дополнительные полезные функции

1. **QR-код на детали** — сканируешь QR с упаковки запчасти → видишь инструкцию по установке
2. **Таймер на шаге** — для операций, где нужно ждать (клей, герметик, сброс давления)
3. **Голосовое управление** — «следующий шаг» / «повтори» для работы грязными руками
4. **Калькулятор расходников** — автоматический расчёт количества масла, антифриза и т.д.
5. **История обслуживания** — личный журнал: что, когда, на каком пробеге делал
6. **Уведомления о ТО** — напоминания по пробегу или времени
7. **Сравнение вариантов** — сравнить два способа ремонта (OEM vs альтернативный)
8. **Экспорт списка инструментов** — список покупок перед ремонтом
9. **Привязка к магазинам запчастей** — партнёрские ссылки на детали (каталог применимости)
10. **OBD-II интеграция** (будущее) — чтение кодов ошибок → автоматический подбор диагностики

---

## 7. Монетизация

### 7.1 Тарифные планы

| План | Цена | Что включено |
|------|------|-------------|
| **Бесплатный** | 0 ₽ | 3 инструкции/мес, базовый поиск, ограниченные техданные, реклама |
| **Стандарт** | 299 ₽/мес | Все инструкции, техданные, диагностика, офлайн (1 авто), без рекламы |
| **Про** | 599 ₽/мес | Всё + офлайн (3 авто), 3D-модели, приоритетная поддержка, ранний доступ |
| **Годовой** | 2990 ₽/год | = Стандарт, экономия ~17% |

### 7.2 Дополнительные источники дохода

1. **Партнёрские ссылки на запчасти** — CPA от магазинов (Exist, Autodoc, Emex)
   - Пассивный доход: каждая инструкция содержит ссылки на нужные детали
2. **B2B-лицензия для автосервисов** — доступ для команды, интеграция с CRM
   - 2990 ₽/мес за 5 рабочих мест
3. **Платный контент от производителей** — OEM-инструкции за доп. плату
4. **API для партнёров** — продажа доступа к базе техданных
5. **Продажа офлайн-пакетов** — полный пакет для конкретного авто (единоразово)
   - 499 ₽ за полный пакет одной модели навсегда
6. **Премиум-авторы** — верифицированные мастера продают свои инструкции
   - Платформа берёт 20-30% комиссии
7. **Белая реклама** — ненавязчивые баннеры инструментов, масел, запчастей
   - Только для бесплатного плана, только релевантная реклама

---

## 8. Пошаговый план разработки

### Фаза 1: Фундамент (текущая)
1. ✅ Архитектура и документация
2. Backend: модели БД, миграции, базовые API
3. Android: проект, тема, навигация, экран выбора авто
4. Базовая аутентификация

### Фаза 2: Ядро контента
5. API инструкций, шагов, узлов
6. Android: экран каталога, карточки, поиск
7. Android: экран пошаговой инструкции
8. Комментарии (древовидные)

### Фаза 3: 3D и визуализация
9. Загрузка и рендеринг 3D-моделей (Filament)
10. Интерактивный выбор узлов на 3D-модели
11. Интеграция 3D с инструкциями

### Фаза 4: Расширение
12. Диагностика по симптомам
13. Техданные и справочная база
14. Офлайн-режим (шифрованный)

### Фаза 5: Админ-панель
15. React-приложение
16. CRUD автомобилей, инструкций, узлов
17. Модерация контента и комментариев
18. Загрузка медиа и 3D

### Фаза 6: Монетизация и запуск
19. Подписки (Google Play Billing)
20. Партнёрские интеграции
21. Безопасность (pinning, шифрование, watermarks)
22. Тестирование, оптимизация, публикация
