package ru.techgid.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ── Категории и узлы ───────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class ComponentCategoryDto(
    val id: Int,
    @Json(name = "parent_id") val parentId: Int? = null,
    val name: String,
    val slug: String,
    val icon: String? = null,
    val description: String? = null,
)

@JsonClass(generateAdapter = true)
data class ComponentDto(
    val id: Int,
    @Json(name = "category_id") val categoryId: Int,
    val name: String,
    val slug: String,
    val description: String? = null,
    val icon: String? = null,
)

// ── Инструкции ─────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class GuideListItemDto(
    val id: Int,
    val title: String,
    val slug: String,
    val difficulty: String,
    @Json(name = "estimated_time_min") val estimatedTimeMin: Int? = null,
    @Json(name = "is_verified") val isVerified: Boolean = false,
    @Json(name = "is_premium") val isPremium: Boolean = false,
    @Json(name = "views_count") val viewsCount: Int = 0,
    val rating: Float = 0f,
    @Json(name = "rating_count") val ratingCount: Int = 0,
    @Json(name = "component_name") val componentName: String = "",
    @Json(name = "thumbnail_url") val thumbnailUrl: String? = null,
    @Json(name = "author_name") val authorName: String = "",
    @Json(name = "created_at") val createdAt: String = "",
)

@JsonClass(generateAdapter = true)
data class PaginatedResponseDto<T>(
    val items: List<T>,
    val total: Int = 0,
    val page: Int = 1,
    @Json(name = "page_size") val pageSize: Int = 20,
    val pages: Int = 0,
)

@JsonClass(generateAdapter = true)
data class StepToolDto(
    val id: Int,
    @Json(name = "tool_name") val toolName: String,
    @Json(name = "tool_spec") val toolSpec: String? = null,
    @Json(name = "is_required") val isRequired: Boolean = true,
    val note: String? = null,
)

@JsonClass(generateAdapter = true)
data class StepConsumableDto(
    val id: Int,
    val name: String,
    @Json(name = "part_number") val partNumber: String? = null,
    val quantity: String? = null,
    val note: String? = null,
)

@JsonClass(generateAdapter = true)
data class StepWarningDto(
    val id: Int,
    @Json(name = "warning_type") val warningType: String,
    val severity: String,
    val text: String,
)

@JsonClass(generateAdapter = true)
data class StepVariationDto(
    val id: Int,
    @Json(name = "configuration_id") val configurationId: Int,
    val text: String,
    @Json(name = "image_url") val imageUrl: String? = null,
)

@JsonClass(generateAdapter = true)
data class StepCheckDto(
    val id: Int,
    val description: String,
    @Json(name = "is_post_repair") val isPostRepair: Boolean = false,
)

@JsonClass(generateAdapter = true)
data class GuideStepDto(
    val id: Int,
    @Json(name = "step_number") val stepNumber: Int,
    val title: String,
    val description: String,
    @Json(name = "image_url") val imageUrl: String? = null,
    @Json(name = "video_url") val videoUrl: String? = null,
    @Json(name = "model_3d_url") val model3dUrl: String? = null,
    @Json(name = "camera_position") val cameraPosition: Map<String, Any>? = null,
    @Json(name = "highlighted_parts") val highlightedParts: List<String>? = null,
    @Json(name = "hidden_parts") val hiddenParts: List<String>? = null,
    val tools: List<StepToolDto> = emptyList(),
    val consumables: List<StepConsumableDto> = emptyList(),
    val warnings: List<StepWarningDto> = emptyList(),
    val variations: List<StepVariationDto> = emptyList(),
    val checks: List<StepCheckDto> = emptyList(),
    @Json(name = "comments_count") val commentsCount: Int = 0,
)

@JsonClass(generateAdapter = true)
data class GuidePrecautionDto(
    val id: Int,
    @Json(name = "warning_type") val warningType: String,
    val severity: String,
    val text: String,
)

@JsonClass(generateAdapter = true)
data class GuideMediaDto(
    val id: Int,
    @Json(name = "media_type") val mediaType: String,
    val url: String,
    @Json(name = "thumbnail_url") val thumbnailUrl: String? = null,
    val title: String? = null,
)

@JsonClass(generateAdapter = true)
data class GuideDetailDto(
    val id: Int,
    val title: String,
    val slug: String,
    val description: String? = null,
    val difficulty: String,
    @Json(name = "estimated_time_min") val estimatedTimeMin: Int? = null,
    val status: String,
    @Json(name = "is_verified") val isVerified: Boolean = false,
    @Json(name = "is_premium") val isPremium: Boolean = false,
    @Json(name = "views_count") val viewsCount: Int = 0,
    val rating: Float = 0f,
    @Json(name = "rating_count") val ratingCount: Int = 0,
    @Json(name = "author_name") val authorName: String = "",
    @Json(name = "author_id") val authorId: Int = 0,
    val component: ComponentDto,
    val steps: List<GuideStepDto> = emptyList(),
    val precautions: List<GuidePrecautionDto> = emptyList(),
    val media: List<GuideMediaDto> = emptyList(),
    @Json(name = "created_at") val createdAt: String = "",
    @Json(name = "updated_at") val updatedAt: String = "",
)

// ── Комментарии ────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class CommentDto(
    val id: Int,
    @Json(name = "user_id") val userId: Int,
    @Json(name = "user_name") val userName: String = "",
    @Json(name = "user_avatar") val userAvatar: String? = null,
    @Json(name = "user_car_display") val userCarDisplay: String? = null,
    @Json(name = "guide_id") val guideId: Int,
    @Json(name = "step_id") val stepId: Int? = null,
    @Json(name = "parent_id") val parentId: Int? = null,
    val text: String,
    val status: String,
    @Json(name = "created_at") val createdAt: String,
    val replies: List<CommentDto> = emptyList(),
)

@JsonClass(generateAdapter = true)
data class CommentCreateDto(
    @Json(name = "guide_id") val guideId: Int,
    @Json(name = "step_id") val stepId: Int? = null,
    @Json(name = "parent_id") val parentId: Int? = null,
    val text: String,
)

// ── Техданные ──────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class TechSpecDto(
    val id: Int,
    @Json(name = "spec_key") val specKey: String,
    @Json(name = "spec_value") val specValue: String,
    val unit: String? = null,
    @Json(name = "min_value") val minValue: Float? = null,
    @Json(name = "max_value") val maxValue: Float? = null,
    val notes: String? = null,
    @Json(name = "component_name") val componentName: String? = null,
    @Json(name = "category_name") val categoryName: String? = null,
)

// ── Диагностика ────────────────────────────────────────────────

@JsonClass(generateAdapter = true)
data class SymptomCategoryDto(
    val id: Int,
    val name: String,
    val slug: String,
    val icon: String? = null,
)

@JsonClass(generateAdapter = true)
data class SymptomDto(
    val id: Int,
    @Json(name = "category_id") val categoryId: Int,
    val name: String,
    val description: String? = null,
)

@JsonClass(generateAdapter = true)
data class DiagnosticRequestDto(
    @Json(name = "configuration_id") val configurationId: Int,
    @Json(name = "symptom_ids") val symptomIds: List<Int>,
)

@JsonClass(generateAdapter = true)
data class DiagnosticCheckDto(
    val id: Int,
    @Json(name = "step_number") val stepNumber: Int,
    val description: String,
    @Json(name = "expected_result") val expectedResult: String? = null,
    @Json(name = "tool_needed") val toolNeeded: String? = null,
)

@JsonClass(generateAdapter = true)
data class DiagnosticResultDto(
    val id: Int,
    @Json(name = "probable_cause") val probableCause: String,
    val probability: Float,
    @Json(name = "component_name") val componentName: String? = null,
    @Json(name = "guide_id") val guideId: Int? = null,
    @Json(name = "guide_title") val guideTitle: String? = null,
    @Json(name = "check_description") val checkDescription: String? = null,
    val checks: List<DiagnosticCheckDto> = emptyList(),
)
