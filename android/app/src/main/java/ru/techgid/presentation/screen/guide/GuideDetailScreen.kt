package ru.techgid.presentation.screen.guide

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
import ru.techgid.presentation.components.ActionChipButton
import ru.techgid.presentation.components.DifficultyBadge
import ru.techgid.presentation.components.WarningBlock
import ru.techgid.presentation.theme.TechGidColors
import ru.techgid.presentation.theme.TechGidTheme

/**
 * Экран пошаговой инструкции — точная копия референса.
 */
@Composable
fun GuideDetailScreen(
    guideId: Int,
    onBack: () -> Unit,
) {
    val guide = remember { createDemoGuide() }
    var currentStepIndex by remember { mutableIntStateOf(0) }
    val currentStep = guide.steps.getOrNull(currentStepIndex)
    val totalSteps = guide.steps.size
    var commentText by remember { mutableStateOf("") }
    val demoComments = remember { createDemoComments() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // ── Верхняя панель: < назад | Шаг X из Y | сохранить ──
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Шаг ${currentStepIndex + 1}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            )
            Text(
                text = " из $totalSteps",
                style = MaterialTheme.typography.titleMedium,
                color = TechGidTheme.extendedColors.textTertiary,
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { /* сохранить офлайн */ }) {
                Icon(
                    Icons.Filled.CloudDownload,
                    "Сохранить",
                    tint = TechGidTheme.extendedColors.iconTint,
                )
            }
        }

        // ── Контент ───────────────────────────────────────────
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            // Заголовок шага
            item {
                if (currentStep != null) {
                    Text(
                        text = currentStep.title,
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold,
                            lineHeight = 32.sp,
                        ),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = currentStep.description,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            // Зона изображения (3D / фото)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "3D / Иллюстрация\nШаг ${currentStepIndex + 1}",
                        style = MaterialTheme.typography.titleLarge,
                        color = TechGidTheme.extendedColors.textTertiary,
                        textAlign = TextAlign.Center,
                    )
                }
            }

            // Кнопки навигации
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    ActionChipButton(
                        text = "Начать паять",
                        onClick = { },
                        icon = {
                            Icon(Icons.Filled.PlayArrow, null, Modifier.size(16.dp))
                        },
                    )
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        if (currentStepIndex > 0) {
                            IconButton(onClick = { currentStepIndex-- }, Modifier.size(36.dp)) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад", Modifier.size(18.dp))
                            }
                        }
                        Text(
                            text = "${currentStepIndex + 1}",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "Нокт мест",
                            style = MaterialTheme.typography.labelMedium,
                            color = TechGidTheme.extendedColors.textTertiary,
                        )
                        if (currentStepIndex < totalSteps - 1) {
                            IconButton(onClick = { currentStepIndex++ }, Modifier.size(36.dp)) {
                                Icon(Icons.AutoMirrored.Filled.ArrowForward, "Далее", Modifier.size(18.dp))
                            }
                        }
                    }
                }
            }

            // Предупреждения
            if (currentStep != null) {
                items(currentStep.warnings) { warning ->
                    WarningBlock(text = warning.text, severity = warning.severity)
                }
            }

            // Инструменты
            if (currentStep != null && currentStep.tools.isNotEmpty()) {
                item {
                    ExpandableSection(title = "Инструменты", icon = Icons.Filled.Build) {
                        currentStep.tools.forEach { tool ->
                            Text(
                                text = "• ${tool.toolName}${if (tool.toolSpec != null) " (${tool.toolSpec})" else ""}",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(start = 8.dp, bottom = 4.dp),
                            )
                        }
                    }
                }
            }

            // Проверки
            if (currentStep != null && currentStep.checks.isNotEmpty()) {
                item {
                    currentStep.checks.forEach { check ->
                        Row(
                            modifier = Modifier.padding(vertical = 4.dp),
                            verticalAlignment = Alignment.Top,
                        ) {
                            Icon(
                                Icons.Filled.CheckCircle, null,
                                Modifier.size(16.dp),
                                tint = TechGidColors.DifficultyEasy,
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = check.description,
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }
            }

            // Разделитель
            item { HorizontalDivider(color = TechGidTheme.extendedColors.divider) }

            // Комментарии
            item {
                Text(
                    text = "Комментарии",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                )
            }

            items(demoComments, key = { it.id }) { comment ->
                CommentItem(comment = comment)
            }

            // Ввод комментария
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Добавить комментарий...") },
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = TechGidTheme.extendedColors.cardBackground,
                            unfocusedContainerColor = TechGidTheme.extendedColors.cardBackground,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = TechGidTheme.extendedColors.cardBorder,
                        ),
                    )
                    Spacer(Modifier.width(8.dp))
                    IconButton(
                        onClick = { },
                        enabled = commentText.isNotBlank(),
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.Send,
                            "Отправить",
                            tint = if (commentText.isNotBlank()) MaterialTheme.colorScheme.primary
                            else TechGidTheme.extendedColors.textTertiary,
                        )
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

// ── Раскрывающаяся секция ─────────────────────────────────────

@Composable
private fun ExpandableSection(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    content: @Composable () -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = TechGidTheme.extendedColors.cardBackground),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, null, Modifier.size(18.dp), tint = TechGidTheme.extendedColors.iconTint)
                Spacer(Modifier.width(8.dp))
                Text(title, style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold))
                Spacer(Modifier.weight(1f))
                Icon(
                    if (expanded) Icons.AutoMirrored.Filled.ArrowBack else Icons.AutoMirrored.Filled.ArrowForward,
                    null, Modifier.size(14.dp),
                    tint = TechGidTheme.extendedColors.textTertiary,
                )
            }
            if (expanded) {
                Spacer(Modifier.height(8.dp))
                content()
            }
        }
    }
}

// ── Комментарий ───────────────────────────────────────────────

@Composable
private fun CommentItem(comment: Comment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = comment.userName.take(1).uppercase(),
                style = MaterialTheme.typography.titleSmall,
                color = TechGidTheme.extendedColors.iconTint,
            )
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.userName,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = comment.createdAt,
                    style = MaterialTheme.typography.labelSmall,
                    color = TechGidTheme.extendedColors.textTertiary,
                )
            }
            if (comment.userCarDisplay != null) {
                Text(
                    text = comment.userCarDisplay,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(text = comment.text, style = MaterialTheme.typography.bodyMedium)
            comment.replies.forEach { reply ->
                Spacer(Modifier.height(8.dp))
                CommentItem(comment = reply)
            }
        }
    }
}

// ── Демо-данные ───────────────────────────────────────────────

private fun createDemoGuide(): GuideDetail {
    return GuideDetail(
        id = 1,
        title = "Замена модуля топливного насоса",
        slug = "fuel-pump-module",
        description = "Замена модуля бензонасоса на Audi Q3 8U с двигателем 2.0 TFSI.",
        difficulty = Difficulty.MEDIUM,
        estimatedTimeMin = 90,
        isVerified = true,
        rating = 4.2f,
        ratingCount = 18,
        authorName = "Алексей",
        componentName = "Модуль топливного насоса",
        steps = listOf(
            GuideStep(
                id = 1, stepNumber = 1,
                title = "Сбросьте давление в топливной системе",
                description = "Перед началом работы необходимо сбросить остаточное давление в топливной рампе. Извлеките предохранитель бензонасоса (F33) и заведите двигатель. Подождите, пока двигатель заглохнет сам.",
                warnings = listOf(
                    StepWarning("safety", WarningSeverity.DANGER, "Опасность пожара! Работайте вдали от открытого огня."),
                    StepWarning("safety", WarningSeverity.WARNING, "Отключите клемму «минус» аккумулятора."),
                ),
                tools = listOf(StepTool("Ключ", "10 мм")),
                checks = listOf(StepCheck("Убедитесь, что двигатель полностью заглох.", false)),
                commentsCount = 3,
            ),
            GuideStep(
                id = 2, stepNumber = 2,
                title = "Снимите заднее сиденье",
                description = "Потяните подушку заднего сиденья вверх за передний край. Фиксаторы отщёлкиваются при вытягивании вверх.",
                warnings = listOf(StepWarning("caution", WarningSeverity.CAUTION, "Фиксаторы хрупкие. Тяните ровно вверх.")),
                tools = listOf(StepTool("Плоская отвёртка", "тонкая")),
                checks = listOf(StepCheck("Оба фиксатора отщелкнулись, подушка снята.", false)),
                commentsCount = 5,
            ),
            GuideStep(
                id = 3, stepNumber = 3,
                title = "Очистите зону вокруг крышки доступа",
                description = "Протрите пыль мягкой тряпкой, чтобы грязь не попадала в бак.",
                tools = listOf(StepTool("Мягкая ветошь", null), StepTool("Пылесос", null, isRequired = false)),
                warnings = listOf(StepWarning("caution", WarningSeverity.CAUTION, "Попадание грязи в бак выведет из строя новый насос.")),
                consumables = listOf(StepConsumable("Ветошь безворсовая", null, "2-3 шт")),
                checks = listOf(StepCheck("Область вокруг крышки чистая.", false)),
                commentsCount = 2,
            ),
        ),
        precautions = listOf(
            GuidePrecaution("safety", WarningSeverity.DANGER, "Работы с топливной системой — риск возгорания."),
            GuidePrecaution("safety", WarningSeverity.WARNING, "Отключите аккумулятор перед началом работ."),
        ),
    )
}

private fun createDemoComments(): List<Comment> {
    return listOf(
        Comment(
            id = 1, userId = 10, userName = "Алексей",
            userCarDisplay = "Владелец Audi Q3 2013",
            guideId = 1, stepId = 3,
            text = "На моём 2013 лючок был ближе к правой стороне, клипсы сиденья туже.",
            createdAt = "24 апр",
            replies = listOf(
                Comment(
                    id = 3, userId = 11, userName = "Дмитрий",
                    userCarDisplay = "Владелец Audi Q3 2015",
                    guideId = 1, stepId = 3, parentId = 1,
                    text = "У меня тоже 2013, подтверждаю — чуть правее.",
                    createdAt = "25 апр",
                ),
            ),
        ),
        Comment(
            id = 2, userId = 12, userName = "Эрик",
            userCarDisplay = "Владелец Audi Q3 2011",
            guideId = 1, stepId = 3,
            text = "Протирать было много грязи, грязи вреди сразу стало лучше после пылесоса.",
            createdAt = "14 апр",
        ),
    )
}
