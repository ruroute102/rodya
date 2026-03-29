package ru.techgid.data.remote.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query
import ru.techgid.data.remote.dto.DiagnosticRequestDto
import ru.techgid.data.remote.dto.DiagnosticResultDto
import ru.techgid.data.remote.dto.SymptomCategoryDto
import ru.techgid.data.remote.dto.SymptomDto
import ru.techgid.data.remote.dto.TechSpecDto

interface DiagnosticApi {

    @GET("diagnostics/symptom-categories")
    suspend fun getSymptomCategories(): List<SymptomCategoryDto>

    @GET("diagnostics/symptoms")
    suspend fun getSymptoms(@Query("category_id") categoryId: Int? = null): List<SymptomDto>

    @POST("diagnostics/diagnose")
    suspend fun diagnose(@Body body: DiagnosticRequestDto): List<DiagnosticResultDto>

    @GET("techspecs/search")
    suspend fun searchTechSpecs(
        @Query("configuration_id") configurationId: Int,
        @Query("q") query: String,
    ): List<TechSpecDto>

    @GET("techspecs/configuration/{configurationId}")
    suspend fun getTechSpecs(
        @Path("configurationId") configurationId: Int,
    ): List<TechSpecDto>
}
