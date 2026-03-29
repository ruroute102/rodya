package ru.techgid.data.remote.api

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import ru.techgid.data.remote.dto.ComponentCategoryDto
import ru.techgid.data.remote.dto.ComponentDto
import ru.techgid.data.remote.dto.GuideDetailDto
import ru.techgid.data.remote.dto.GuideListItemDto
import ru.techgid.data.remote.dto.PaginatedResponseDto

interface GuideApi {

    @GET("guides/categories")
    suspend fun getCategories(): List<ComponentCategoryDto>

    @GET("guides/categories/{categoryId}/components")
    suspend fun getComponents(@Path("categoryId") categoryId: Int): List<ComponentDto>

    @GET("guides/")
    suspend fun getGuides(
        @Query("configuration_id") configurationId: Int? = null,
        @Query("component_id") componentId: Int? = null,
        @Query("difficulty") difficulty: String? = null,
        @Query("search") search: String? = null,
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
    ): PaginatedResponseDto<GuideListItemDto>

    @GET("guides/{guideId}")
    suspend fun getGuide(@Path("guideId") guideId: Int): GuideDetailDto
}
