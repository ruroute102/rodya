package ru.techgid.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Кэшированная марка автомобиля.
 */
@Entity(tableName = "cached_brands")
data class CachedBrand(
    @PrimaryKey val id: Int,
    val name: String,
    val slug: String,
    @ColumnInfo(name = "logo_url") val logoUrl: String? = null,
    val country: String? = null,
    @ColumnInfo(name = "sort_order") val sortOrder: Int = 0,
)

/**
 * Кэшированная модель автомобиля.
 */
@Entity(
    tableName = "cached_models",
    indices = [Index("brand_id")],
)
data class CachedModel(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "brand_id") val brandId: Int,
    val name: String,
    val slug: String,
)

/**
 * Кэшированное поколение.
 */
@Entity(
    tableName = "cached_generations",
    indices = [Index("model_id")],
)
data class CachedGeneration(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "model_id") val modelId: Int,
    val name: String,
    val slug: String,
    @ColumnInfo(name = "chassis_code") val chassisCode: String? = null,
    @ColumnInfo(name = "year_start") val yearStart: Int,
    @ColumnInfo(name = "year_end") val yearEnd: Int? = null,
    @ColumnInfo(name = "image_url") val imageUrl: String? = null,
)

/**
 * Кэшированный двигатель.
 */
@Entity(
    tableName = "cached_engines",
    indices = [Index("generation_id")],
)
data class CachedEngine(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "generation_id") val generationId: Int,
    val code: String,
    val name: String,
    @ColumnInfo(name = "displacement_label") val displacementLabel: String? = null,
    @ColumnInfo(name = "fuel_type") val fuelType: String,
    @ColumnInfo(name = "power_hp") val powerHp: Int? = null,
)

/**
 * Сохранённая инструкция для офлайн-доступа.
 */
@Entity(
    tableName = "offline_guides",
    indices = [Index("configuration_id")],
)
data class OfflineGuide(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "configuration_id") val configurationId: Int,
    val title: String,
    val slug: String,
    val difficulty: String,
    @ColumnInfo(name = "estimated_time_min") val estimatedTimeMin: Int? = null,
    @ColumnInfo(name = "component_name") val componentName: String = "",
    @ColumnInfo(name = "json_data") val jsonData: String, // полный JSON инструкции
    @ColumnInfo(name = "downloaded_at") val downloadedAt: Long = System.currentTimeMillis(),
    @ColumnInfo(name = "version") val version: Int = 1,
)

/**
 * Автомобиль пользователя (локальная копия).
 */
@Entity(tableName = "user_cars_local")
data class UserCarLocal(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "configuration_id") val configurationId: Int,
    @ColumnInfo(name = "display_name") val displayName: String? = null,
    @ColumnInfo(name = "is_primary") val isPrimary: Boolean = false,
    @ColumnInfo(name = "brand_name") val brandName: String = "",
    @ColumnInfo(name = "model_name") val modelName: String = "",
    @ColumnInfo(name = "year") val year: Int? = null,
    @ColumnInfo(name = "engine_name") val engineName: String = "",
)
