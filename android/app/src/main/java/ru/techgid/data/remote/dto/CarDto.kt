package ru.techgid.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CarBrandDto(
    val id: Int,
    val name: String,
    val slug: String,
    @Json(name = "logo_url") val logoUrl: String? = null,
    val country: String? = null,
)

@JsonClass(generateAdapter = true)
data class CarModelDto(
    val id: Int,
    @Json(name = "brand_id") val brandId: Int,
    val name: String,
    val slug: String,
)

@JsonClass(generateAdapter = true)
data class CarGenerationDto(
    val id: Int,
    @Json(name = "model_id") val modelId: Int,
    val name: String,
    val slug: String,
    @Json(name = "chassis_code") val chassisCode: String? = null,
    @Json(name = "year_start") val yearStart: Int,
    @Json(name = "year_end") val yearEnd: Int? = null,
    @Json(name = "image_url") val imageUrl: String? = null,
)

@JsonClass(generateAdapter = true)
data class CarEngineDto(
    val id: Int,
    @Json(name = "generation_id") val generationId: Int,
    val code: String,
    val name: String,
    @Json(name = "displacement_label") val displacementLabel: String? = null,
    @Json(name = "fuel_type") val fuelType: String,
    @Json(name = "power_hp") val powerHp: Int? = null,
    @Json(name = "torque_nm") val torqueNm: Int? = null,
)

@JsonClass(generateAdapter = true)
data class CarBodyDto(
    val id: Int,
    @Json(name = "generation_id") val generationId: Int,
    @Json(name = "body_type") val bodyType: String,
    @Json(name = "doors_count") val doorsCount: Int,
    val name: String? = null,
)

@JsonClass(generateAdapter = true)
data class CarTrimDto(
    val id: Int,
    @Json(name = "generation_id") val generationId: Int,
    val name: String,
    val market: String? = null,
)

@JsonClass(generateAdapter = true)
data class CarConfigurationDto(
    val id: Int,
    @Json(name = "generation_id") val generationId: Int,
    @Json(name = "engine_id") val engineId: Int,
    @Json(name = "body_id") val bodyId: Int? = null,
    @Json(name = "trim_id") val trimId: Int? = null,
    @Json(name = "transmission_type") val transmissionType: String? = null,
    @Json(name = "drive_type") val driveType: String? = null,
    @Json(name = "year_start") val yearStart: Int? = null,
    @Json(name = "year_end") val yearEnd: Int? = null,
    @Json(name = "display_name") val displayName: String? = null,
)

@JsonClass(generateAdapter = true)
data class CarGenerationFullDto(
    val id: Int,
    @Json(name = "model_id") val modelId: Int,
    val name: String,
    val slug: String,
    @Json(name = "chassis_code") val chassisCode: String? = null,
    @Json(name = "year_start") val yearStart: Int,
    @Json(name = "year_end") val yearEnd: Int? = null,
    @Json(name = "image_url") val imageUrl: String? = null,
    val engines: List<CarEngineDto> = emptyList(),
    val bodies: List<CarBodyDto> = emptyList(),
    val trims: List<CarTrimDto> = emptyList(),
)
