package ru.techgid.presentation.screen.guide

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
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
 * Экран пошаговой инструкции.
 *
 * Референс: шаг навигация сверху, заголовок, описание,
 * визуальная зона, инструменты, предупреждения,
 * комментарии — всё на одном экране, прокруткой.
 */
@Composable
fun GuideDetailScreen(
    guideId: Int,
    onBack: () -> Unit,
) {
    // Демо-данные
    val guide = remember { createDemoGuide() }
    var currentStepIndex by remember { mutableIntStateOf(0) }
    val currentStep = guide.steps.getOrNull(currentStepIndex)
    val totalSteps = guide.steps.size

    // Комментарии
    var commentText by remember { mutableStateOf("") }
    val demoComments = remember { createDemoComments() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // ── Верхняя навигация ──────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Шаг ${currentStepIndex + 1}",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onBackground,
            )
            Text(
                text = " из $totalSteps",
                style = MaterialTheme.typography.titleMedium,
                color = TechGidTheme.extendedColors.textTertiary,
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { /* TODO: сохранить офлайн */ }) {
                Icon(
                    imageVector = Icons.Filled.CloudDownload,
                    contentDescription = "Сохранить офлайн",
                    tint = TechGidTheme.extendedColors.iconTint,
                )
            }
        }

        // ── Прокручиваемый контент ─────────────────────────────
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // Заголовок шага
            item {
                if (currentStep != null) {
                    Text(
                        text = currentStep.title,
                        style = MaterialTheme.typography.headlineMedium,
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

            // Визуальная зона (3D / иллюстрация)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(MaterialTheme.shapes.large)
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

                // Кнопки на визуальной зоне
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    ActionChipButton(
                        text = "Начать пауть",
                        onClick = { /* TODO */ },
                        modifier = Modifier.weight(1f),
                        icon = {
                            Icon(
                                imageVector = Icons.Filled.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp),
                            )
                        },
                    )

                    // Навигация по шагам
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        if (currentStepIndex > 0) {
                            IconButton(
                                onClick = { currentStepIndex-- },
                                modifier = Modifier.size(40.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Предыдущий шаг",
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                        Text(
                            text = "${currentStepIndex + 1}",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Text(
                            text = "Пред. шаг",
                            style = MaterialTheme.typography.labelMedium,
                            color = TechGidTheme.extendedColors.textTertiary,
                        )
                        if (currentStepIndex < totalSteps - 1) {
                            IconButton(
                                onClick = { currentStepIndex++ },
                                modifier = Modifier.size(40.dp),
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                    contentDescription = "Следующий шаг",
                                    modifier = Modifier.size(18.dp),
                                )
                            }
                        }
                    }
                }
            }

            // Предупреждения шага
            if (currentStep != null) {
                items(currentStep.warnings, key = { "w_${it.text.hashCode()}" }) { warning ->
                    WarningBlock(
                        text = warning.text,
                        severity = warning.severity,
                    )
                }
            }

            // Инструменты и расходники
            if (currentStep != null && (currentStep.tools.isNotEmpty() || currentStep.consumables.isNotEmpty())) {
                item {
                    ToolsAndConsumablesSection(
                        tools = currentStep.tools,
                        consumables = currentStep.consumables,
                    )
                }
            }

            // Вариации (отличия для разных конфигураций)
            if (currentStep != null && currentStep.variations.isNotEmpty()) {
                item {
                    Column {
                        currentStep.variations.forEach { variation ->
                            WarningBlock(
                                text = variation.text,
                                severity = WarningSeverity.INFO,
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }
            }

            // Проверки после шага
            if (currentStep != null && currentStep.checks.isNotEmpty()) {
                item {
                    ChecksSection(checks = currentStep.checks)
                }
            }

            // Разделитель
            item {
                HorizontalDivider(color = TechGidTheme.extendedColors.divider)
            }

            // Комментарии к шагу
            item {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Comment,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp),
                        tint = TechGidTheme.extendedColors.iconTint,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Комментарии",
                        style = MaterialTheme.typography.titleMedium,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${demoComments.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }

            // Список комментариев
            items(demoComments, key = { it.id }) { comment ->
                CommentItem(comment = comment)
            }

            // Поле ввода комментария
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        modifier = Modifier.weight(1f),
                        placeholder = {
                            Text(
                                text = "Добавить комментарий...",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(20.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = TechGidTheme.extendedColors.cardBackground,
                            unfocusedContainerColor = TechGidTheme.extendedColors.cardBackground,
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = TechGidTheme.extendedColors.cardBorder,
                        ),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = { /* TODO: отправить комментарий */ },
                        enabled = commentText.isNotBlank(),
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Отправить",
                            tint = if (commentText.isNotBlank()) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                TechGidTheme.extendedColors.textTertiary
                            },
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

// ── Секция инструментов и расходников ──────────────────────────

@Composable
private fun ToolsAndConsumablesSection(
    tools: List<StepTool>,
    consumables: List<StepConsumable>,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = TechGidTheme.extendedColors.cardBackground,
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            TechGidTheme.extendedColors.cardBorder,
        ),
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Инструменты
            if (tools.isNotEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Filled.Build,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = TechGidTheme.extendedColors.iconTint,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Инструменты",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                tools.forEach { tool ->
                    Row(
                        modifier = Modifier.padding(start = 24.dp, bottom = 2.dp),
                    ) {
                        Text(
                            text = "• ${tool.toolName}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        if (tool.toolSpec != null) {
                            Text(
                                text = " (${tool.toolSpec})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TechGidTheme.extendedColors.textTertiary,
                            )
                        }
                    }
                }
            }

            if (tools.isNotEmpty() && consumables.isNotEmpty()) {
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Расходники
            if (consumables.isNotEmpty()) {
                Text(
                    text = "Расходники",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                consumables.forEach { item ->
                    Row(
                        modifier = Modifier.padding(start = 24.dp, bottom = 2.dp),
                    ) {
                        Text(
                            text = "• ${item.name}",
                            style = MaterialTheme.typography.bodyMedium,
                        )
                        if (item.quantity != null) {
                            Text(
                                text = " — ${item.quantity}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TechGidTheme.extendedColors.textTertiary,
                            )
                        }
                    }
                }
            }
        }
    }
}

// ── Секция проверок ────────────────────────────────────────────

@Composable
private fun ChecksSection(checks: List<StepCheck>) {
    Column {
        Text(
            text = "Проверки",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Spacer(modifier = Modifier.height(6.dp))
        checks.forEach { check ->
            Row(
                modifier = Modifier.padding(bottom = 4.dp),
                verticalAlignment = Alignment.Top,
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = TechGidColors.DifficultyEasy,
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = check.description,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    if (check.isPostRepair) {
                        Text(
                            text = "Проверка после завершения ремонта",
                            style = MaterialTheme.typography.labelSmall,
                            color = TechGidColors.WarningCaution,
                        )
                    }
                }
            }
        }
    }
}

// ── Комментарий ────────────────────────────────────────────────

@Composable
private fun CommentItem(comment: Comment) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
    ) {
        // Аватар
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

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = comment.userName,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.width(8.dp))
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
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = comment.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            // Вложенные ответы
            comment.replies.forEach { reply ->
                Spacer(modifier = Modifier.height(8.dp))
                CommentItem(comment = reply)
            }
        }
    }
}

// ── Демо-данные ────────────────────────────────────────────────

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
                id = 1,
                stepNumber = 1,
                title = "Сбросьте давление в топливной системе",
                description = "Перед началом работы необходимо сбросить остаточное давление в топливной рампе. Извлеките предохранитель бензонасоса (F33, блок предохранителей в салоне) и заведите двигатель. Подождите, пока двигатель заглохнет сам.",
                warnings = listOf(
                    StepWarning("safety", WarningSeverity.DANGER, "Опасность пожара! Работайте вдали от открытого огня. Не курите рядом с автомобилем."),
                    StepWarning("safety", WarningSeverity.WARNING, "Перед началом работы отключите клемму «минус» аккумулятора."),
                ),
                tools = listOf(
                    StepTool("Ключ", "10 мм"),
                ),
                checks = listOf(
                    StepCheck("Убедитесь, что двигатель полностью заглох и не заводится повторно.", false),
                ),
                commentsCount = 3,
            ),
            GuideStep(
                id = 2,
                stepNumber = 2,
                title = "Снимите заднее сиденье",
                description = "Потяните подушку заднего сиденья вверх за передний край. Подушка крепится двумя фиксаторами — они отщёлкиваются при вытягивании вверх.",
                warnings = listOf(
                    StepWarning("caution", WarningSeverity.CAUTION, "Фиксаторы хрупкие. Тяните ровно вверх, не в сторону. Особенно осторожно в мороз."),
                ),
                tools = listOf(
                    StepTool("Плоская отвёртка", "тонкая, для поддевания"),
                ),
                variations = listOf(
                    StepVariation(1, "На версиях до 2013 года фиксаторы могут быть ближе к правой стороне."),
                ),
                checks = listOf(
                    StepCheck("Убедитесь, что оба фиксатора отщелкнулись и подушка снята без повреждений.", false),
                ),
                commentsCount = 5,
            ),
            GuideStep(
                id = 3,
                stepNumber = 3,
                title = "Очистите зону вокруг крышки доступа",
                description = "Протрите пыль мягкой тряпкой, чтобы грязь не попадала в бак при вскрытии крышки.",
                tools = listOf(
                    StepTool("Мягкая ветошь", null),
                    StepTool("Пылесос", null, isRequired = false, note = "Рекомендуется для тщательной очистки"),
                ),
                warnings = listOf(
                    StepWarning("caution", WarningSeverity.CAUTION, "Попадание грязи в бак может вывести из строя новый насос. Очищайте тщательно."),
                ),
                consumables = listOf(
                    StepConsumable("Ветошь безворсовая", null, "2-3 шт"),
                ),
                checks = listOf(
                    StepCheck("Область вокруг крышки чистая, нет пыли и песка.", false),
                ),
                commentsCount = 2,
            ),
        ),
        precautions = listOf(
            GuidePrecaution("safety", WarningSeverity.DANGER, "Работы с топливной системой — риск возгорания. Работайте в проветриваемом помещении."),
            GuidePrecaution("safety", WarningSeverity.WARNING, "Отключите аккумулятор перед началом работ."),
            GuidePrecaution("info", WarningSeverity.INFO, "Расположение люка доступа может незначительно отличаться на автомобилях для разных рынков."),
        ),
    )
}

private fun createDemoComments(): List<Comment> {
    return listOf(
        Comment(
            id = 1,
            userId = 10,
            userName = "Алексей",
            userCarDisplay = "Владелец Audi Q3 2013",
            guideId = 1,
            stepId = 3,
            text = "На моём 2013 лючок был ближе к правой стороне, клипсы сиденья туже.",
            createdAt = "24 апр",
            replies = listOf(
                Comment(
                    id = 3,
                    userId = 11,
                    userName = "Дмитрий",
                    userCarDisplay = "Владелец Audi Q3 2015",
                    guideId = 1,
                    stepId = 3,
                    parentId = 1,
                    text = "У меня тоже 2013, подтверждаю — чуть правее.",
                    createdAt = "25 апр",
                ),
            ),
        ),
        Comment(
            id = 2,
            userId = 12,
            userName = "Олег",
            userCarDisplay = "Владелец Audi Q3 2011",
            guideId = 1,
            stepId = 3,
            text = "Протирать было мало, грязи вреди — сразу стало лучше после пылесоса.",
            createdAt = "14 апр",
        ),
    )
}
