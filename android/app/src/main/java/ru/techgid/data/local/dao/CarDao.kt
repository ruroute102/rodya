package ru.techgid.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.techgid.data.local.entity.CachedBrand
import ru.techgid.data.local.entity.CachedEngine
import ru.techgid.data.local.entity.CachedGeneration
import ru.techgid.data.local.entity.CachedModel
import ru.techgid.data.local.entity.UserCarLocal

@Dao
interface CarDao {

    // ── Марки ──────────────────────────────────────────────────

    @Query("SELECT * FROM cached_brands ORDER BY sort_order, name")
    fun getAllBrands(): Flow<List<CachedBrand>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBrands(brands: List<CachedBrand>)

    @Query("DELETE FROM cached_brands")
    suspend fun clearBrands()

    // ── Модели ─────────────────────────────────────────────────

    @Query("SELECT * FROM cached_models WHERE brand_id = :brandId ORDER BY name")
    fun getModelsByBrand(brandId: Int): Flow<List<CachedModel>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertModels(models: List<CachedModel>)

    // ── Поколения ──────────────────────────────────────────────

    @Query("SELECT * FROM cached_generations WHERE model_id = :modelId ORDER BY year_start DESC")
    fun getGenerationsByModel(modelId: Int): Flow<List<CachedGeneration>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGenerations(generations: List<CachedGeneration>)

    // ── Двигатели ──────────────────────────────────────────────

    @Query("SELECT * FROM cached_engines WHERE generation_id = :generationId")
    fun getEnginesByGeneration(generationId: Int): Flow<List<CachedEngine>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEngines(engines: List<CachedEngine>)

    // ── Авто пользователя ──────────────────────────────────────

    @Query("SELECT * FROM user_cars_local ORDER BY is_primary DESC")
    fun getUserCars(): Flow<List<UserCarLocal>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserCar(car: UserCarLocal)

    @Query("DELETE FROM user_cars_local WHERE id = :carId")
    suspend fun deleteUserCar(carId: Int)
}
