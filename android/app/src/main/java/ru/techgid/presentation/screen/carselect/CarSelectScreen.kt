package ru.techgid.presentation.screen.carselect

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.HealthAndSafety
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.techgid.domain.model.CarBrand
import ru.techgid.domain.model.CarEngine
import ru.techgid.domain.model.CarGeneration
import ru.techgid.domain.model.CarModel
import ru.techgid.presentation.components.CarSelectorItem
import ru.techgid.presentation.components.PrimaryButton
import ru.techgid.presentation.theme.TechGidTheme

/**
 * Главный экран — выбор автомобиля.
 * Точная копия референса: заголовок, 4 селектора, изображение авто,
 * кнопка "Продолжить", пункты меню "Каталог работ" и "Формы диагностики".
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarSelectScreen(
    onShowGuides: (configurationId: Int) -> Unit,
    onCatalog: () -> Unit = {},
    onDiagnostics: () -> Unit = {},
) {
    // Состояние выбора
    var selectedBrand by remember { mutableStateOf<CarBrand?>(null) }
    var selectedModel by remember { mutableStateOf<CarModel?>(null) }
    var selectedGeneration by remember { mutableStateOf<CarGeneration?>(null) }
    var selectedEngine by remember { mutableStateOf<CarEngine?>(null) }

    // Какой селектор открыт
    var activeSelector by remember { mutableStateOf<SelectorType?>(null) }

    // Демо-данные
    val demoBrands = remember {
        listOf(
            CarBrand(1, "Audi", "audi", country = "Германия"),
            CarBrand(2, "BMW", "bmw", country = "Германия"),
            CarBrand(3, "Mercedes-Benz", "mercedes", country = "Германия"),
            CarBrand(4, "Toyota", "toyota", country = "Япония"),
            CarBrand(5, "Volkswagen", "volkswagen", country = "Германия"),
            CarBrand(6, "Kia", "kia", country = "Южная Корея"),
            CarBrand(7, "Hyundai", "hyundai", country = "Южная Корея"),
            CarBrand(8, "Lada", "lada", country = "Россия"),
        )
    }
    val demoModels = remember {
        listOf(
            CarModel(1, 1, "Q3", "q3"),
            CarModel(2, 1, "Q5", "q5"),
            CarModel(3, 1, "A4", "a4"),
            CarModel(4, 1, "A6", "a6"),
        )
    }
    val demoGenerations = remember {
        listOf(
            CarGeneration(1, 1, "8U (I поколение)", "8u", chassisCode = "8U", yearStart = 2011, yearEnd = 2018),
            CarGeneration(2, 1, "F3 (II поколение)", "f3", chassisCode = "F3", yearStart = 2018, yearEnd = null),
        )
    }
    val demoEngines = remember {
        listOf(
            CarEngine(1, 1, "CULB", "2.0 TFSI", "2.0 TFSI", "petrol", 211),
            CarEngine(2, 1, "CFFB", "2.0 TDI", "2.0 TDI", "diesel", 140),
        )
    }

    val isComplete = selectedBrand != null && selectedModel != null
            && selectedGeneration != null && selectedEngine != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .background(MaterialTheme.colorScheme.background),
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp),
        ) {
            Spacer(modifier = Modifier.height(32.dp))

            // ── Заголовок ─────────────────────────────────────────
            Text(
                text = "Выберите своё авто,\nчтобы найти инструкции",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    lineHeight = 36.sp,
                ),
                color = MaterialTheme.colorScheme.onBackground,
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Найдите инструкции и диагностику\nпо вашей комплектации.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // ── 4 селектора ───────────────────────────────────────
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CarSelectorItem(
                    label = "Марка",
                    value = selectedBrand?.name,
                    onClick = { activeSelector = SelectorType.BRAND },
                )
                CarSelectorItem(
                    label = "Модель",
                    value = selectedModel?.name,
                    onClick = {
                        if (selectedBrand != null) activeSelector = SelectorType.MODEL
                    },
                )
                CarSelectorItem(
                    label = "Год",
                    value = selectedGeneration?.let { "${it.yearStart}" },
                    onClick = {
                        if (selectedModel != null) activeSelector = SelectorType.GENERATION
                    },
                )
                CarSelectorItem(
                    label = "Двигатель",
                    value = selectedEngine?.displacementLabel ?: selectedEngine?.name,
                    onClick = {
                        if (selectedGeneration != null) activeSelector = SelectorType.ENGINE
                    },
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Зона изображения автомобиля ───────────────────────
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .alpha(if (selectedBrand != null) 1f else 0.5f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (isComplete) {
                        buildString {
                            append(selectedBrand?.name ?: "")
                            selectedModel?.let { append(" ${it.name}") }
                            selectedGeneration?.let { append("\n${it.yearStart}") }
                            selectedEngine?.let { append(" · ${it.displacementLabel}") }
                        }
                    } else {
                        "Здесь будет\nизображение автомобиля"
                    },
                    style = MaterialTheme.typography.titleLarge,
                    color = TechGidTheme.extendedColors.textTertiary,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ── Кнопка "Продолжить" ───────────────────────────────
            PrimaryButton(
                text = if (isComplete) "Показать инструкции" else "Продолжить",
                onClick = {
                    // В реальном приложении — configurationId из API
                    onShowGuides(1)
                },
                enabled = isComplete,
            )

            Spacer(modifier = Modifier.height(20.dp))

            // ── Меню: Каталог работ ───────────────────────────────
            MenuListItem(
                icon = Icons.AutoMirrored.Filled.List,
                text = "Каталог работ",
                onClick = onCatalog,
            )

            HorizontalDivider(
                color = TechGidTheme.extendedColors.divider,
                modifier = Modifier.padding(vertical = 2.dp),
            )

            // ── Меню: Формы диагностики ───────────────────────────
            MenuListItem(
                icon = Icons.Filled.HealthAndSafety,
                text = "Формы диагностики",
                onClick = onDiagnostics,
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // ── Bottom Sheet для выбора значения ───────────────────────
    if (activeSelector != null) {
        val sheetState = rememberModalBottomSheetState()

        ModalBottomSheet(
            onDismissRequest = { activeSelector = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
            ) {
                Text(
                    text = when (activeSelector) {
                        SelectorType.BRAND -> "Выберите марку"
                        SelectorType.MODEL -> "Выберите модель"
                        SelectorType.GENERATION -> "Выберите год / поколение"
                        SelectorType.ENGINE -> "Выберите двигатель"
                        null -> ""
                    },
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                when (activeSelector) {
                    SelectorType.BRAND -> {
                        demoBrands.forEach { brand ->
                            SelectorListItem(
                                text = brand.name,
                                subtitle = brand.country,
                                onClick = {
                                    selectedBrand = brand
                                    selectedModel = null
                                    selectedGeneration = null
                                    selectedEngine = null
                                    activeSelector = null
                                },
                            )
                        }
                    }
                    SelectorType.MODEL -> {
                        demoModels.forEach { model ->
                            SelectorListItem(
                                text = model.name,
                                onClick = {
                                    selectedModel = model
                                    selectedGeneration = null
                                    selectedEngine = null
                                    activeSelector = null
                                },
                            )
                        }
                    }
                    SelectorType.GENERATION -> {
                        demoGenerations.forEach { gen ->
                            SelectorListItem(
                                text = gen.name,
                                subtitle = "${gen.yearStart}–${gen.yearEnd ?: "н.в."}",
                                onClick = {
                                    selectedGeneration = gen
                                    selectedEngine = null
                                    activeSelector = null
                                },
                            )
                        }
                    }
                    SelectorType.ENGINE -> {
                        demoEngines.forEach { engine ->
                            SelectorListItem(
                                text = engine.displacementLabel ?: engine.name,
                                subtitle = "${engine.powerHp ?: "?"} л.с. · ${engine.fuelType}",
                                onClick = {
                                    selectedEngine = engine
                                    activeSelector = null
                                },
                            )
                        }
                    }
                    null -> {}
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// ── Пункт меню (Каталог работ / Формы диагностики) ────────────

@Composable
private fun MenuListItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = TechGidTheme.extendedColors.iconTint,
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
            contentDescription = null,
            modifier = Modifier.size(14.dp),
            tint = TechGidTheme.extendedColors.textTertiary,
        )
    }
}

private enum class SelectorType {
    BRAND, MODEL, GENERATION, ENGINE
}

@Composable
private fun SelectorListItem(
    text: String,
    subtitle: String? = null,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
