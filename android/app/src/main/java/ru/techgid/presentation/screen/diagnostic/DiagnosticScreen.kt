package ru.techgid.presentation.screen.diagnostic

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.techgid.presentation.components.PrimaryButton
import ru.techgid.presentation.theme.TechGidColors
import ru.techgid.presentation.theme.TechGidTheme

/**
 * Экран диагностики: выбор симптомов → результаты.
 * Пользователь не знает, что сломалось — выбирает симптомы,
 * приложение подсказывает вероятные неисправности.
 */
@Composable
fun DiagnosticScreen(
    configurationId: Int,
    onGuideClick: (guideId: Int) -> Unit = {},
    onBack: () -> Unit = {},
) {
    var showResults by remember { mutableStateOf(false) }
    val selectedSymptoms = remember { mutableStateListOf<Int>() }

    // Демо-данные
    val symptomCategories = remember {
        listOf(
            DemoSymptomCategory(1, "Двигатель", listOf(
                DemoSymptom(1, "Не заводится"),
                DemoSymptom(2, "Плавают обороты"),
                DemoSymptom(3, "Троит"),
                DemoSymptom(4, "Посторонний стук"),
                DemoSymptom(5, "Повышенный расход топлива"),
            )),
            DemoSymptomCategory(2, "Топливная система", listOf(
                DemoSymptom(6, "Запах бензина в салоне"),
                DemoSymptom(7, "Потеря мощности"),
                DemoSymptom(8, "Долго заводится"),
            )),
            DemoSymptomCategory(3, "Электрика", listOf(
                DemoSymptom(9, "Горит Check Engine"),
                DemoSymptom(10, "Не работают приборы"),
                DemoSymptom(11, "Проблемы с зарядкой"),
            )),
        )
    }

    val demoResults = remember {
        listOf(
            DemoDiagnosticResult(
                cause = "Неисправность бензонасоса",
                probability = 0.75f,
                componentName = "Модуль топливного насоса",
                guideId = 1,
                guideTitle = "Замена модуля топливного насоса",
                checks = listOf(
                    "Проверить давление в рампе. Норма: 4–5 бар",
                    "Послушать работу насоса при включении зажигания",
                    "Проверить напряжение на разъёме насоса (12В)",
                ),
            ),
            DemoDiagnosticResult(
                cause = "Засорён топливный фильтр",
                probability = 0.45f,
                componentName = "Топливный фильтр",
                guideId = 2,
                guideTitle = "Замена топливного фильтра",
                checks = listOf(
                    "Проверить перепад давления до и после фильтра",
                ),
            ),
            DemoDiagnosticResult(
                cause = "Неисправность датчика положения коленвала",
                probability = 0.2f,
                componentName = "ДПКВ",
                guideId = null,
                guideTitle = null,
                checks = listOf(
                    "Проверить сопротивление датчика (700–900 Ом)",
                    "Проверить осциллограмму сигнала",
                ),
            ),
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(MaterialTheme.colorScheme.background),
    ) {
        // Шапка
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = {
                if (showResults) showResults = false else onBack()
            }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, "Назад")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Filled.LocalHospital,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (showResults) "Результаты диагностики" else "Диагностика по симптомам",
                style = MaterialTheme.typography.titleLarge,
            )
        }

        if (!showResults) {
            // ── Выбор симптомов ─────────────────────────────────
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                item {
                    Text(
                        text = "Отметьте симптомы, которые наблюдаете.\nЧем больше симптомов — тем точнее диагностика.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                symptomCategories.forEach { category ->
                    item {
                        Text(
                            text = category.name,
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onBackground,
                        )
                    }
                    items(category.symptoms, key = { it.id }) { symptom ->
                        SymptomCheckItem(
                            name = symptom.name,
                            isSelected = symptom.id in selectedSymptoms,
                            onClick = {
                                if (symptom.id in selectedSymptoms) {
                                    selectedSymptoms.remove(symptom.id)
                                } else {
                                    selectedSymptoms.add(symptom.id)
                                }
                            },
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }

            // Кнопка диагностики
            PrimaryButton(
                text = "Диагностировать (${selectedSymptoms.size})",
                onClick = { showResults = true },
                enabled = selectedSymptoms.isNotEmpty(),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            )
        } else {
            // ── Результаты ─────────────────────────────────────
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    Text(
                        text = "Вероятные неисправности, отсортированные по вероятности:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                items(demoResults) { result ->
                    DiagnosticResultCard(
                        result = result,
                        onGuideClick = { guideId -> onGuideClick(guideId) },
                    )
                }

                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun SymptomCheckItem(
    name: String,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = if (isSelected) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = if (isSelected) MaterialTheme.colorScheme.primary else TechGidTheme.extendedColors.textTertiary,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun DiagnosticResultCard(
    result: DemoDiagnosticResult,
    onGuideClick: (Int) -> Unit,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = TechGidTheme.extendedColors.cardBackground,
        ),
        border = BorderStroke(1.dp, TechGidTheme.extendedColors.cardBorder),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Причина + вероятность
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = result.cause,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    if (result.componentName != null) {
                        Text(
                            text = result.componentName,
                            style = MaterialTheme.typography.bodySmall,
                            color = TechGidTheme.extendedColors.textTertiary,
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "${(result.probability * 100).toInt()}%",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = when {
                        result.probability >= 0.7f -> TechGidColors.WarningDanger
                        result.probability >= 0.4f -> TechGidColors.WarningCaution
                        else -> TechGidTheme.extendedColors.textTertiary
                    },
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Индикатор вероятности
            LinearProgressIndicator(
                progress = { result.probability },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = when {
                    result.probability >= 0.7f -> TechGidColors.WarningDanger
                    result.probability >= 0.4f -> TechGidColors.WarningCaution
                    else -> MaterialTheme.colorScheme.primary
                },
                trackColor = TechGidTheme.extendedColors.divider,
            )

            // Проверки
            if (result.checks.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Как проверить:",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(modifier = Modifier.height(4.dp))
                result.checks.forEachIndexed { index, check ->
                    Text(
                        text = "${index + 1}. $check",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 4.dp, bottom = 2.dp),
                    )
                }
            }

            // Ссылка на инструкцию
            if (result.guideId != null && result.guideTitle != null) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = TechGidTheme.extendedColors.divider)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onGuideClick(result.guideId) }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = result.guideTitle,
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }
    }
}

// ── Демо-модели ────────────────────────────────────────────────

private data class DemoSymptomCategory(
    val id: Int,
    val name: String,
    val symptoms: List<DemoSymptom>,
)

private data class DemoSymptom(val id: Int, val name: String)

private data class DemoDiagnosticResult(
    val cause: String,
    val probability: Float,
    val componentName: String? = null,
    val guideId: Int? = null,
    val guideTitle: String? = null,
    val checks: List<String> = emptyList(),
)
