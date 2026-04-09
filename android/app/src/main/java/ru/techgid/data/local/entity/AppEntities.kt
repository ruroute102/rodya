package ru.techgid.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Запись в журнале обслуживания автомобиля.
 */
@Entity(tableName = "service_records")
data class ServiceRecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    @ColumnInfo(name = "date_iso") val dateIso: String,
    @ColumnInfo(name = "mileage_km") val mileageKm: Int,
    val notes: String,
    @ColumnInfo(name = "cost_rub") val costRub: Int,
    @ColumnInfo(name = "category") val category: String = "general",
)

/**
 * Избранная инструкция.
 */
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val guideId: Int,
    val title: String,
    @ColumnInfo(name = "component_name") val componentName: String,
    val difficulty: String,
    @ColumnInfo(name = "estimated_time_min") val estimatedTimeMin: Int?,
    @ColumnInfo(name = "added_at") val addedAt: Long = System.currentTimeMillis(),
)

/**
 * Прогресс по шагу инструкции (отметка «выполнено»).
 */
@Entity(
    tableName = "step_progress",
    primaryKeys = ["guide_id", "step_id"],
    indices = [Index("guide_id")],
)
data class StepProgressEntity(
    @ColumnInfo(name = "guide_id") val guideId: Int,
    @ColumnInfo(name = "step_id") val stepId: Int,
    @ColumnInfo(name = "is_done") val isDone: Boolean,
    @ColumnInfo(name = "updated_at") val updatedAt: Long = System.currentTimeMillis(),
)

/**
 * Недавний поисковый запрос.
 */
@Entity(tableName = "search_history")
data class SearchHistoryEntity(
    @PrimaryKey val query: String,
    @ColumnInfo(name = "used_at") val usedAt: Long = System.currentTimeMillis(),
)

/**
 * Напоминание о техобслуживании.
 */
@Entity(tableName = "reminders")
data class ReminderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    @ColumnInfo(name = "due_mileage") val dueMileage: Int?,
    @ColumnInfo(name = "due_date_iso") val dueDateIso: String?,
    @ColumnInfo(name = "is_done") val isDone: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis(),
)
