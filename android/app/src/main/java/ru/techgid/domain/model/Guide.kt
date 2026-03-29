package ru.techgid.domain.model

/**
 * Domain-модели инструкций.
 */
enum class Difficulty(val label: String) {
    EASY("Лёгкая"),
    MEDIUM("Средняя"),
    HARD("Сложная"),
    EXPERT("Экспертная"),
}

enum class WarningSeverity(val label: String) {
    INFO("Информация"),
    CAUTION("Внимание"),
    WARNING("Предупреждение"),
    DANGER("Опасность"),
}

data class GuideListItem(
    val id: Int,
    val title: String,
    val slug: String,
    val difficulty: Difficulty,
    val estimatedTimeMin: Int? = null,
    val isVerified: Boolean = false,
    val isPremium: Boolean = false,
    val viewsCount: Int = 0,
    val rating: Float = 0f,
    val ratingCount: Int = 0,
    val componentName: String = "",
    val thumbnailUrl: String? = null,
    val authorName: String = "",
)

data class GuideDetail(
    val id: Int,
    val title: String,
    val slug: String,
    val description: String? = null,
    val difficulty: Difficulty,
    val estimatedTimeMin: Int? = null,
    val isVerified: Boolean = false,
    val isPremium: Boolean = false,
    val viewsCount: Int = 0,
    val rating: Float = 0f,
    val ratingCount: Int = 0,
    val authorName: String = "",
    val authorId: Int = 0,
    val componentName: String = "",
    val steps: List<GuideStep> = emptyList(),
    val precautions: List<GuidePrecaution> = emptyList(),
)

data class GuideStep(
    val id: Int,
    val stepNumber: Int,
    val title: String,
    val description: String,
    val imageUrl: String? = null,
    val videoUrl: String? = null,
    val model3dUrl: String? = null,
    val highlightedParts: List<String> = emptyList(),
    val hiddenParts: List<String> = emptyList(),
    val tools: List<StepTool> = emptyList(),
    val consumables: List<StepConsumable> = emptyList(),
    val warnings: List<StepWarning> = emptyList(),
    val variations: List<StepVariation> = emptyList(),
    val checks: List<StepCheck> = emptyList(),
    val commentsCount: Int = 0,
)

data class StepTool(
    val toolName: String,
    val toolSpec: String? = null,
    val isRequired: Boolean = true,
    val note: String? = null,
)

data class StepConsumable(
    val name: String,
    val partNumber: String? = null,
    val quantity: String? = null,
)

data class StepWarning(
    val warningType: String,
    val severity: WarningSeverity,
    val text: String,
)

data class StepVariation(
    val configurationId: Int,
    val text: String,
    val imageUrl: String? = null,
)

data class StepCheck(
    val description: String,
    val isPostRepair: Boolean = false,
)

data class GuidePrecaution(
    val warningType: String,
    val severity: WarningSeverity,
    val text: String,
)
