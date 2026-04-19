# 3D-модели автомобилей (.glb)

Приложение загружает `.glb`-модели из этой папки через Google Filament (SceneView).
Если модель для конкретного авто не найдена, отображается процедурная 3D-модель (fallback).

## Как добавить модель

1. Получите файл `.glb` (см. источники ниже).
2. Назовите файл в соответствии с таблицей имён (см. ниже).
3. Положите файл в эту папку: `android/app/src/main/assets/models/`.
4. Пересоберите приложение — модель подхватится автоматически.

## Таблица имён файлов

| Марка / модель        | Имя файла          |
|-----------------------|--------------------|
| Audi Q3               | `audi_q3.glb`      |
| BMW (любая)           | `bmw.glb`           |
| Mercedes (любая)      | `mercedes.glb`      |
| Toyota (любая)        | `toyota.glb`        |
| Volkswagen / VW / Golf| `volkswagen.glb`    |
| Lada / ВАЗ            | `lada.glb`          |
| Hyundai / Kia         | `hyundai.glb`       |
| Skoda                 | `skoda.glb`         |
| Renault               | `renault.glb`       |
| Mazda                 | `mazda.glb`         |
| Honda                 | `honda.glb`         |
| Nissan                | `nissan.glb`        |
| Mitsubishi            | `mitsubishi.glb`    |
| Ford                  | `ford.glb`          |
| Chevrolet             | `chevrolet.glb`     |
| Lexus                 | `lexus.glb`         |
| Subaru                | `subaru.glb`        |

Логика сопоставления определена в `ModelRegistry.kt`.
Чтобы добавить новую марку, добавьте запись в `ModelRegistry.nameToAsset` и файл в эту папку.

## Требования к модели

- **Формат:** GLB (binary glTF 2.0). Не GLTF с отдельными файлами.
- **Размер файла:** до 20 МБ (APK-бюджет). Идеально — 5-10 МБ.
- **Полигоны:** 30 000–80 000 треугольников. Больше — тормоза на слабых устройствах.
- **Материалы:** PBR (metallic-roughness workflow). Filament поддерживает
  baseColor, metallic, roughness, normal, occlusion, emissive текстуры.
- **Ориентация:** Y — вверх, капот — в сторону -Z. Масштаб ~4-5м по длине
  (рендерер применяет `scaleToUnits = 1.8f`).

## Источники моделей

### 1. Meshy.ai (AI text-to-3D) — рекомендуемый
1. Зайдите на [meshy.ai](https://www.meshy.ai/).
2. Выберите **Text to 3D** → введите промпт, например:
   `"sedan car, Audi Q3 2023, full body, clean studio, PBR materials, no background"`
3. Дождитесь генерации (~2 мин), отрефайните если нужно.
4. Скачайте в формате **GLB**.
5. Проверьте размер; при необходимости оптимизируйте (см. ниже).

### 2. Tripo3D (AI image/text-to-3D)
1. Зайдите на [tripo3d.ai](https://www.tripo3d.ai/).
2. Загрузите фото автомобиля или введите текстовый промпт.
3. Экспорт → GLB.

### 3. Luma Genie / Luma AI
1. Откройте [lumalabs.ai/genie](https://lumalabs.ai/genie).
2. Введите промпт: `"car, [brand] [model], studio lighting, PBR"`.
3. Скачайте GLB.

### 4. Sketchfab (готовые модели)
1. Зайдите на [sketchfab.com](https://sketchfab.com/search?q=car&type=models&downloadable=true).
2. Фильтр: Downloadable → Price: Free → формат glTF.
3. Скачайте, конвертируйте при необходимости.

### 5. CGTrader / TurboSquid (платные, высокое качество)
1. Ищите `"[brand] car low poly glTF"`.
2. Покупайте модель в формате glTF/GLB.

## Оптимизация модели

Если файл слишком большой или тормозит:

```bash
# Установите gltf-transform (Node.js)
npm install -g @gltf-transform/cli

# Уменьшите полигоны
gltf-transform simplify input.glb output.glb --ratio 0.5

# Сожмите текстуры (WebP, KTX2)
gltf-transform resize input.glb output.glb --width 1024 --height 1024

# Draco-компрессия геометрии
gltf-transform draco input.glb output.glb

# Всё вместе
gltf-transform optimize input.glb output.glb
```

Или используйте онлайн: [gltf.report](https://gltf.report/) — загрузите,
оптимизируйте, скачайте.

## HDR-окружение

Для реалистичного PBR-освещения нужен файл `environments/studio.hdr`
в папке `assets/`. Скачайте любой студийный HDRI с
[Poly Haven](https://polyhaven.com/hdris) (512×256 достаточно),
назовите `studio.hdr`, положите в `android/app/src/main/assets/environments/`.
