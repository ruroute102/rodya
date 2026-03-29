package ru.techgid.data.remote.api

import retrofit2.http.GET
import retrofit2.http.Path
import ru.techgid.data.remote.dto.CarBrandDto
import ru.techgid.data.remote.dto.CarConfigurationDto
import ru.techgid.data.remote.dto.CarEngineDto
import ru.techgid.data.remote.dto.CarGenerationDto
import ru.techgid.data.remote.dto.CarGenerationFullDto
import ru.techgid.data.remote.dto.CarModelDto

interface CarApi {

    @GET("cars/brands")
    suspend fun getBrands(): List<CarBrandDto>

    @GET("cars/brands/{brandId}/models")
    suspend fun getModels(@Path("brandId") brandId: Int): List<CarModelDto>

    @GET("cars/models/{modelId}/generations")
    suspend fun getGenerations(@Path("modelId") modelId: Int): List<CarGenerationDto>

    @GET("cars/generations/{generationId}")
    suspend fun getGenerationFull(@Path("generationId") generationId: Int): CarGenerationFullDto

    @GET("cars/generations/{generationId}/engines")
    suspend fun getEngines(@Path("generationId") generationId: Int): List<CarEngineDto>

    @GET("cars/generations/{generationId}/configurations")
    suspend fun getConfigurations(@Path("generationId") generationId: Int): List<CarConfigurationDto>
}
