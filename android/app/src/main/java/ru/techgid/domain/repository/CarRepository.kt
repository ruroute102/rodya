package ru.techgid.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.techgid.domain.model.CarBrand
import ru.techgid.domain.model.CarConfiguration
import ru.techgid.domain.model.CarEngine
import ru.techgid.domain.model.CarGeneration
import ru.techgid.domain.model.CarModel

interface CarRepository {
    fun getBrands(): Flow<List<CarBrand>>
    fun getModels(brandId: Int): Flow<List<CarModel>>
    fun getGenerations(modelId: Int): Flow<List<CarGeneration>>
    fun getEngines(generationId: Int): Flow<List<CarEngine>>
    suspend fun getConfigurations(generationId: Int): List<CarConfiguration>
    suspend fun refreshBrands()
    suspend fun refreshModels(brandId: Int)
    suspend fun refreshGenerations(modelId: Int)
    suspend fun refreshEngines(generationId: Int)
}
