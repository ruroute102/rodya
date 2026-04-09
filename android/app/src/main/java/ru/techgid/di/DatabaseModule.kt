package ru.techgid.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.techgid.data.local.dao.CarDao
import ru.techgid.data.local.dao.FavoriteDao
import ru.techgid.data.local.dao.GuideDao
import ru.techgid.data.local.dao.ReminderDao
import ru.techgid.data.local.dao.SearchHistoryDao
import ru.techgid.data.local.dao.ServiceRecordDao
import ru.techgid.data.local.dao.StepProgressDao
import ru.techgid.data.local.db.TechGidDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TechGidDatabase {
        return Room.databaseBuilder(
            context,
            TechGidDatabase::class.java,
            "techgid.db",
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideCarDao(db: TechGidDatabase): CarDao = db.carDao()

    @Provides
    fun provideGuideDao(db: TechGidDatabase): GuideDao = db.guideDao()

    @Provides
    fun provideServiceRecordDao(db: TechGidDatabase): ServiceRecordDao = db.serviceRecordDao()

    @Provides
    fun provideFavoriteDao(db: TechGidDatabase): FavoriteDao = db.favoriteDao()

    @Provides
    fun provideStepProgressDao(db: TechGidDatabase): StepProgressDao = db.stepProgressDao()

    @Provides
    fun provideSearchHistoryDao(db: TechGidDatabase): SearchHistoryDao = db.searchHistoryDao()

    @Provides
    fun provideReminderDao(db: TechGidDatabase): ReminderDao = db.reminderDao()
}
