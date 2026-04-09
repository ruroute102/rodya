package ru.techgid.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.techgid.data.local.dao.CarDao
import ru.techgid.data.local.dao.FavoriteDao
import ru.techgid.data.local.dao.GuideDao
import ru.techgid.data.local.dao.ReminderDao
import ru.techgid.data.local.dao.SearchHistoryDao
import ru.techgid.data.local.dao.ServiceRecordDao
import ru.techgid.data.local.dao.StepProgressDao
import ru.techgid.data.local.entity.CachedBrand
import ru.techgid.data.local.entity.CachedEngine
import ru.techgid.data.local.entity.CachedGeneration
import ru.techgid.data.local.entity.CachedModel
import ru.techgid.data.local.entity.FavoriteEntity
import ru.techgid.data.local.entity.OfflineGuide
import ru.techgid.data.local.entity.ReminderEntity
import ru.techgid.data.local.entity.SearchHistoryEntity
import ru.techgid.data.local.entity.ServiceRecordEntity
import ru.techgid.data.local.entity.StepProgressEntity
import ru.techgid.data.local.entity.UserCarLocal

@Database(
    entities = [
        CachedBrand::class,
        CachedModel::class,
        CachedGeneration::class,
        CachedEngine::class,
        OfflineGuide::class,
        UserCarLocal::class,
        ServiceRecordEntity::class,
        FavoriteEntity::class,
        StepProgressEntity::class,
        SearchHistoryEntity::class,
        ReminderEntity::class,
    ],
    version = 2,
    exportSchema = false,
)
abstract class TechGidDatabase : RoomDatabase() {
    abstract fun carDao(): CarDao
    abstract fun guideDao(): GuideDao
    abstract fun serviceRecordDao(): ServiceRecordDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun stepProgressDao(): StepProgressDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun reminderDao(): ReminderDao
}
