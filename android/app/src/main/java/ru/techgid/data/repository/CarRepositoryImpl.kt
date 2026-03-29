package ru.techgid.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.techgid.data.local.dao.CarDao
import ru.techgid.data.mapper.toCache
import ru.techgid.data.mapper.toDomain
import ru.techgid.data.remote.api.CarApi
import ru.techgid.domain.model.CarBrand
import ru.techgid.domain.model.CarConfiguration
import ru.techgid.domain.model.CarEngine
import ru.techgid.domain.model.CarGeneration
import ru.techgid.domain.model.CarModel
import ru.techgid.domain.repository.CarRepository
import javax.inject.Inject

class CarRepositoryImpl @Inject constructor(
    private val carApi: CarApi,
    private val carDao: CarDao,
) : CarRepository {

    override fun getBrands(): Flow<List<CarBrand>> =
        carDao.getAllBrands().map { list -> list.map { it.toDomain() } }

    override fun getModels(brandId: Int): Flow<List<CarModel>> =
        carDao.getModelsByBrand(brandId).map { list -> list.map { it.toDomain() } }

    override fun getGenerations(modelId: Int): Flow<List<CarGeneration>> =
        carDao.getGenerationsByModel(modelId).map { list -> list.map { it.toDomain() } }

    override fun getEngines(generationId: Int): Flow<List<CarEngine>> =
        carDao.getEnginesByGeneration(generationId).map { list -> list.map { it.toDomain() } }

    override suspend fun getConfigurations(generationId: Int): List<CarConfiguration> {
        return carApi.getConfigurations(generationId).map {
            CarConfiguration(
                id = it.id,
                generationId = it.generationId,
                engineId = it.engineId,
                displayName = it.displayName,
            )
        }
    }

    override suspend fun refreshBrands() {
        val brands = carApi.getBrands()
        carDao.insertBrands(brands.map { it.toCache() })
    }

    override suspend fun refreshModels(brandId: Int) {
        val models = carApi.getModels(brandId)
        carDao.insertModels(models.map { it.toCache() })
    }

    override suspend fun refreshGenerations(modelId: Int) {
        val generations = carApi.getGenerations(modelId)
        carDao.insertGenerations(generations.map { it.toCache() })
    }

    override suspend fun refreshEngines(generationId: Int) {
        val engines = carApi.getEngines(generationId)
        carDao.insertEngines(engines.map { it.toCache() })
    }
}
