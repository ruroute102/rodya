package ru.techgid.data.repository

import com.squareup.moshi.Moshi
import ru.techgid.data.local.dao.GuideDao
import ru.techgid.data.local.entity.OfflineGuide
import ru.techgid.data.mapper.toDomain
import ru.techgid.data.remote.api.CommentApi
import ru.techgid.data.remote.api.GuideApi
import ru.techgid.data.remote.dto.CommentCreateDto
import ru.techgid.data.remote.dto.GuideDetailDto
import ru.techgid.domain.model.Comment
import ru.techgid.domain.model.GuideDetail
import ru.techgid.domain.model.GuideListItem
import ru.techgid.domain.repository.GuideRepository
import ru.techgid.domain.repository.PaginatedResult
import javax.inject.Inject

class GuideRepositoryImpl @Inject constructor(
    private val guideApi: GuideApi,
    private val commentApi: CommentApi,
    private val guideDao: GuideDao,
    private val moshi: Moshi,
) : GuideRepository {

    override suspend fun getGuides(
        configurationId: Int?,
        componentId: Int?,
        difficulty: String?,
        search: String?,
        page: Int,
        pageSize: Int,
    ): PaginatedResult<GuideListItem> {
        val response = guideApi.getGuides(
            configurationId = configurationId,
            componentId = componentId,
            difficulty = difficulty,
            search = search,
            page = page,
            pageSize = pageSize,
        )
        return PaginatedResult(
            items = response.items.map { it.toDomain() },
            total = response.total,
            page = response.page,
            pageSize = response.pageSize,
            totalPages = response.pages,
        )
    }

    override suspend fun getGuide(guideId: Int): GuideDetail {
        // Сначала пробуем офлайн
        val offline = guideDao.getOfflineGuide(guideId)
        if (offline != null) {
            val adapter = moshi.adapter(GuideDetailDto::class.java)
            val dto = adapter.fromJson(offline.jsonData)
            if (dto != null) return dto.toDomain()
        }

        return guideApi.getGuide(guideId).toDomain()
    }

    override suspend fun getComments(guideId: Int, stepId: Int?): List<Comment> {
        return commentApi.getGuideComments(guideId, stepId).map { it.toDomain() }
    }

    override suspend fun addComment(
        guideId: Int,
        stepId: Int?,
        parentId: Int?,
        text: String,
    ): Comment {
        val dto = commentApi.createComment(
            CommentCreateDto(
                guideId = guideId,
                stepId = stepId,
                parentId = parentId,
                text = text,
            )
        )
        return dto.toDomain()
    }

    override suspend fun saveGuideOffline(guideId: Int, configurationId: Int) {
        val dto = guideApi.getGuide(guideId)
        val adapter = moshi.adapter(GuideDetailDto::class.java)
        val json = adapter.toJson(dto)

        guideDao.saveGuide(
            OfflineGuide(
                id = dto.id,
                configurationId = configurationId,
                title = dto.title,
                slug = dto.slug,
                difficulty = dto.difficulty,
                estimatedTimeMin = dto.estimatedTimeMin,
                componentName = dto.component.name,
                jsonData = json,
            )
        )
    }

    override suspend fun isGuideSavedOffline(guideId: Int): Boolean {
        return guideDao.isGuideSaved(guideId)
    }

    override suspend fun removeGuideOffline(guideId: Int) {
        guideDao.deleteGuide(guideId)
    }
}
