package ru.techgid.domain.model

/**
 * Domain-модели автомобилей.
 */
data class CarBrand(
    val id: Int,
    val name: String,
    val slug: String,
    val logoUrl: String? = null,
    val country: String? = null,
)

data class CarModel(
    val id: Int,
    val brandId: Int,
    val name: String,
    val slug: String,
)

data class CarGeneration(
    val id: Int,
    val modelId: Int,
    val name: String,
    val slug: String,
    val chassisCode: String? = null,
    val yearStart: Int,
    val yearEnd: Int? = null,
    val imageUrl: String? = null,
)

data class CarEngine(
    val id: Int,
    val generationId: Int,
    val code: String,
    val name: String,
    val displacementLabel: String? = null,
    val fuelType: String,
    val powerHp: Int? = null,
)

data class CarConfiguration(
    val id: Int,
    val generationId: Int,
    val engineId: Int,
    val displayName: String? = null,
)

/**
 * Полный выбор пользователя — от марки до конфигурации.
 */
data class CarSelection(
    val brand: CarBrand? = null,
    val model: CarModel? = null,
    val generation: CarGeneration? = null,
    val engine: CarEngine? = null,
    val configuration: CarConfiguration? = null,
) {
    val isComplete: Boolean
        get() = brand != null && model != null && generation != null && engine != null

    val displayTitle: String
        get() = buildString {
            brand?.let { append(it.name) }
            model?.let { append(" ${it.name}") }
            generation?.let { append(" (${it.yearStart})") }
            engine?.let { append(" ${it.displacementLabel ?: it.name}") }
        }.trim()
}
