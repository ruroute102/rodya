package ru.techgid.domain.repository

import ru.techgid.domain.model.Comment
import ru.techgid.domain.model.GuideDetail
import ru.techgid.domain.model.GuideListItem

interface GuideRepository {
    suspend fun getGuides(
        configurationId: Int? = null,
        componentId: Int? = null,
        difficulty: String? = null,
        search: String? = null,
        page: Int = 1,
        pageSize: Int = 20,
    ): PaginatedResult<GuideListItem>

    suspend fun getGuide(guideId: Int): GuideDetail
    suspend fun getComments(guideId: Int, stepId: Int? = null): List<Comment>
    suspend fun addComment(guideId: Int, stepId: Int?, parentId: Int?, text: String): Comment
    suspend fun saveGuideOffline(guideId: Int, configurationId: Int)
    suspend fun isGuideSavedOffline(guideId: Int): Boolean
    suspend fun removeGuideOffline(guideId: Int)
}

data class PaginatedResult<T>(
    val items: List<T>,
    val total: Int,
    val page: Int,
    val pageSize: Int,
    val totalPages: Int,
)
