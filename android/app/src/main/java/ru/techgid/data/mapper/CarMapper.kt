package ru.techgid.data.mapper

import ru.techgid.data.local.entity.CachedBrand
import ru.techgid.data.local.entity.CachedEngine
import ru.techgid.data.local.entity.CachedGeneration
import ru.techgid.data.local.entity.CachedModel
import ru.techgid.data.remote.dto.CarBrandDto
import ru.techgid.data.remote.dto.CarEngineDto
import ru.techgid.data.remote.dto.CarGenerationDto
import ru.techgid.data.remote.dto.CarModelDto
import ru.techgid.domain.model.CarBrand
import ru.techgid.domain.model.CarEngine
import ru.techgid.domain.model.CarGeneration
import ru.techgid.domain.model.CarModel

// ── DTO → Domain ───────────────────────────────────────────────

fun CarBrandDto.toDomain() = CarBrand(
    id = id, name = name, slug = slug, logoUrl = logoUrl, country = country,
)

fun CarModelDto.toDomain() = CarModel(
    id = id, brandId = brandId, name = name, slug = slug,
)

fun CarGenerationDto.toDomain() = CarGeneration(
    id = id, modelId = modelId, name = name, slug = slug,
    chassisCode = chassisCode, yearStart = yearStart, yearEnd = yearEnd, imageUrl = imageUrl,
)

fun CarEngineDto.toDomain() = CarEngine(
    id = id, generationId = generationId, code = code, name = name,
    displacementLabel = displacementLabel, fuelType = fuelType, powerHp = powerHp,
)

// ── DTO → Cache ────────────────────────────────────────────────

fun CarBrandDto.toCache() = CachedBrand(
    id = id, name = name, slug = slug, logoUrl = logoUrl, country = country,
)

fun CarModelDto.toCache() = CachedModel(
    id = id, brandId = brandId, name = name, slug = slug,
)

fun CarGenerationDto.toCache() = CachedGeneration(
    id = id, modelId = modelId, name = name, slug = slug,
    chassisCode = chassisCode, yearStart = yearStart, yearEnd = yearEnd, imageUrl = imageUrl,
)

fun CarEngineDto.toCache() = CachedEngine(
    id = id, generationId = generationId, code = code, name = name,
    displacementLabel = displacementLabel, fuelType = fuelType, powerHp = powerHp,
)

// ── Cache → Domain ─────────────────────────────────────────────

fun CachedBrand.toDomain() = CarBrand(
    id = id, name = name, slug = slug, logoUrl = logoUrl, country = country,
)

fun CachedModel.toDomain() = CarModel(
    id = id, brandId = brandId, name = name, slug = slug,
)

fun CachedGeneration.toDomain() = CarGeneration(
    id = id, modelId = modelId, name = name, slug = slug,
    chassisCode = chassisCode, yearStart = yearStart, yearEnd = yearEnd, imageUrl = imageUrl,
)

fun CachedEngine.toDomain() = CarEngine(
    id = id, generationId = generationId, code = code, name = name,
    displacementLabel = displacementLabel, fuelType = fuelType, powerHp = powerHp,
)
