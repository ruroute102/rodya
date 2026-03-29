package ru.techgid.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.techgid.data.repository.AuthRepositoryImpl
import ru.techgid.data.repository.CarRepositoryImpl
import ru.techgid.data.repository.GuideRepositoryImpl
import ru.techgid.domain.repository.AuthRepository
import ru.techgid.domain.repository.CarRepository
import ru.techgid.domain.repository.GuideRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCarRepository(impl: CarRepositoryImpl): CarRepository

    @Binds
    @Singleton
    abstract fun bindGuideRepository(impl: GuideRepositoryImpl): GuideRepository

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository
}
