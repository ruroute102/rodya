package ru.techgid.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.techgid.data.local.dao.CarDao
import ru.techgid.data.local.dao.GuideDao
import ru.techgid.data.local.db.TechGidDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): TechGidDatabase {
        // TODO: В продакшене использовать SQLCipher через SupportFactory
        // val passphrase = SQLiteDatabase.getBytes("encryption_key".toCharArray())
        // val factory = SupportFactory(passphrase)
        return Room.databaseBuilder(
            context,
            TechGidDatabase::class.java,
            "techgid.db",
        )
            // .openHelperFactory(factory)  // SQLCipher
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides
    fun provideCarDao(db: TechGidDatabase): CarDao = db.carDao()

    @Provides
    fun provideGuideDao(db: TechGidDatabase): GuideDao = db.guideDao()
}
