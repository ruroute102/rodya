package ru.techgid.data.mapper

import ru.techgid.data.remote.dto.CommentDto
import ru.techgid.data.remote.dto.GuideDetailDto
import ru.techgid.data.remote.dto.GuideListItemDto
import ru.techgid.data.remote.dto.GuidePrecautionDto
import ru.techgid.data.remote.dto.GuideStepDto
import ru.techgid.data.remote.dto.StepCheckDto
import ru.techgid.data.remote.dto.StepConsumableDto
import ru.techgid.data.remote.dto.StepToolDto
import ru.techgid.data.remote.dto.StepVariationDto
import ru.techgid.data.remote.dto.StepWarningDto
import ru.techgid.domain.model.Comment
import ru.techgid.domain.model.Difficulty
import ru.techgid.domain.model.GuideDetail
import ru.techgid.domain.model.GuidePrecaution
import ru.techgid.domain.model.GuideStep
import ru.techgid.domain.model.StepCheck
import ru.techgid.domain.model.StepConsumable
import ru.techgid.domain.model.StepTool
import ru.techgid.domain.model.StepVariation
import ru.techgid.domain.model.StepWarning
import ru.techgid.domain.model.WarningSeverity
import ru.techgid.domain.model.GuideListItem as DomainGuideListItem

fun String.toDifficulty(): Difficulty = when (this.lowercase()) {
    "easy" -> Difficulty.EASY
    "medium" -> Difficulty.MEDIUM
    "hard" -> Difficulty.HARD
    "expert" -> Difficulty.EXPERT
    else -> Difficulty.MEDIUM
}

fun String.toWarningSeverity(): WarningSeverity = when (this.lowercase()) {
    "info" -> WarningSeverity.INFO
    "caution" -> WarningSeverity.CAUTION
    "warning" -> WarningSeverity.WARNING
    "danger" -> WarningSeverity.DANGER
    else -> WarningSeverity.INFO
}

fun GuideListItemDto.toDomain() = DomainGuideListItem(
    id = id,
    title = title,
    slug = slug,
    difficulty = difficulty.toDifficulty(),
    estimatedTimeMin = estimatedTimeMin,
    isVerified = isVerified,
    isPremium = isPremium,
    viewsCount = viewsCount,
    rating = rating,
    ratingCount = ratingCount,
    componentName = componentName,
    thumbnailUrl = thumbnailUrl,
    authorName = authorName,
)

fun StepToolDto.toDomain() = StepTool(
    toolName = toolName, toolSpec = toolSpec, isRequired = isRequired, note = note,
)

fun StepConsumableDto.toDomain() = StepConsumable(
    name = name, partNumber = partNumber, quantity = quantity,
)

fun StepWarningDto.toDomain() = StepWarning(
    warningType = warningType, severity = severity.toWarningSeverity(), text = text,
)

fun StepVariationDto.toDomain() = StepVariation(
    configurationId = configurationId, text = text, imageUrl = imageUrl,
)

fun StepCheckDto.toDomain() = StepCheck(
    description = description, isPostRepair = isPostRepair,
)

fun GuideStepDto.toDomain() = GuideStep(
    id = id,
    stepNumber = stepNumber,
    title = title,
    description = description,
    imageUrl = imageUrl,
    videoUrl = videoUrl,
    model3dUrl = model3dUrl,
    highlightedParts = highlightedParts.orEmpty(),
    hiddenParts = hiddenParts.orEmpty(),
    tools = tools.map { it.toDomain() },
    consumables = consumables.map { it.toDomain() },
    warnings = warnings.map { it.toDomain() },
    variations = variations.map { it.toDomain() },
    checks = checks.map { it.toDomain() },
    commentsCount = commentsCount,
    waitTimeSeconds = waitTimeSeconds,
)

fun GuidePrecautionDto.toDomain() = GuidePrecaution(
    warningType = warningType, severity = severity.toWarningSeverity(), text = text,
)

fun GuideDetailDto.toDomain() = GuideDetail(
    id = id,
    title = title,
    slug = slug,
    description = description,
    difficulty = difficulty.toDifficulty(),
    estimatedTimeMin = estimatedTimeMin,
    isVerified = isVerified,
    isPremium = isPremium,
    viewsCount = viewsCount,
    rating = rating,
    ratingCount = ratingCount,
    authorName = authorName,
    authorId = authorId,
    componentName = component.name,
    steps = steps.map { it.toDomain() },
    precautions = precautions.map { it.toDomain() },
)

fun CommentDto.toDomain(): Comment = Comment(
    id = id,
    userId = userId,
    userName = userName,
    userAvatar = userAvatar,
    userCarDisplay = userCarDisplay,
    guideId = guideId,
    stepId = stepId,
    parentId = parentId,
    text = text,
    createdAt = createdAt,
    replies = replies.map { it.toDomain() },
)
