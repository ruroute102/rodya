package ru.techgid.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.techgid.data.local.dao.CarDao
import ru.techgid.data.local.dao.GuideDao
import ru.techgid.data.local.entity.CachedBrand
import ru.techgid.data.local.entity.CachedEngine
import ru.techgid.data.local.entity.CachedGeneration
import ru.techgid.data.local.entity.CachedModel
import ru.techgid.data.local.entity.OfflineGuide
import ru.techgid.data.local.entity.UserCarLocal

@Database(
    entities = [
        CachedBrand::class,
        CachedModel::class,
        CachedGeneration::class,
        CachedEngine::class,
        OfflineGuide::class,
        UserCarLocal::class,
    ],
    version = 1,
    exportSchema = true,
)
abstract class TechGidDatabase : RoomDatabase() {
    abstract fun carDao(): CarDao
    abstract fun guideDao(): GuideDao
}
