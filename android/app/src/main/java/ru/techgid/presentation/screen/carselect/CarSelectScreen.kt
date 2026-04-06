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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import androidx.hilt.navigation.compose.hiltViewModel
import ru.techgid.presentation.components.CarSelectorItem
import ru.techgid.presentation.components.PrimaryButton
import ru.techgid.presentation.theme.TechGidTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CarSelectScreen(
    viewModel: CarSelectViewModel = hiltViewModel(),
    onShowGuides: (configurationId: Int) -> Unit,
    onCatalog: () -> Unit = {},
    onDiagnostics: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    var activeSelector by remember { mutableStateOf<SelectorType?>(null) }

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

            if (state.isLoading && state.brands.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    CarSelectorItem(
                        label = "Марка",
                        value = state.selectedBrand?.name,
                        onClick = { activeSelector = SelectorType.BRAND },
                    )
                    CarSelectorItem(
                        label = "Модель",
                        value = state.selectedModel?.name,
                        onClick = {
                            if (state.selectedBrand != null) activeSelector = SelectorType.MODEL
                        },
                    )
                    CarSelectorItem(
                        label = "Год / Поколение",
                        value = state.selectedGeneration?.let { "${it.name} (${it.yearStart}–${it.yearEnd ?: "н.в."})" },
                        onClick = {
                            if (state.selectedModel != null) activeSelector = SelectorType.GENERATION
                        },
                    )
                    CarSelectorItem(
                        label = "Двигатель",
                        value = state.selectedEngine?.let { "${it.displacementLabel ?: it.name} · ${it.powerHp ?: "?"} л.с." },
                        onClick = {
                            if (state.selectedGeneration != null) activeSelector = SelectorType.ENGINE
                        },
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .alpha(if (state.selectedBrand != null) 1f else 0.5f),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = if (state.isComplete) {
                        buildString {
                            append(state.selectedBrand?.name ?: "")
                            state.selectedModel?.let { append(" ${it.name}") }
                            state.selectedGeneration?.let { append("\n${it.yearStart}") }
                            state.selectedEngine?.let { append(" · ${it.displacementLabel}") }
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

            PrimaryButton(
                text = if (state.isComplete) "Показать инструкции" else "Продолжить",
                onClick = { onShowGuides(1) },
                enabled = state.isComplete,
            )

            Spacer(modifier = Modifier.height(20.dp))

            MenuListItem(
                icon = Icons.AutoMirrored.Filled.List,
                text = "Каталог работ",
                onClick = onCatalog,
            )

            HorizontalDivider(
                color = TechGidTheme.extendedColors.divider,
                modifier = Modifier.padding(vertical = 2.dp),
            )

            MenuListItem(
                icon = Icons.Filled.HealthAndSafety,
                text = "Формы диагностики",
                onClick = onDiagnostics,
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Bottom Sheet
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
                        SelectorType.GENERATION -> "Выберите поколение"
                        SelectorType.ENGINE -> "Выберите двигатель"
                        null -> ""
                    },
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                    modifier = Modifier.padding(bottom = 16.dp),
                )

                if (state.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    when (activeSelector) {
                        SelectorType.BRAND -> {
                            state.brands.forEach { brand ->
                                SelectorListItem(
                                    text = brand.name,
                                    subtitle = brand.country,
                                    onClick = {
                                        viewModel.selectBrand(brand)
                                        activeSelector = null
                                    },
                                )
                            }
                        }
                        SelectorType.MODEL -> {
                            state.models.forEach { model ->
                                SelectorListItem(
                                    text = model.name,
                                    onClick = {
                                        viewModel.selectModel(model)
                                        activeSelector = null
                                    },
                                )
                            }
                        }
                        SelectorType.GENERATION -> {
                            state.generations.forEach { gen ->
                                SelectorListItem(
                                    text = gen.name,
                                    subtitle = "${gen.yearStart}–${gen.yearEnd ?: "н.в."}",
                                    onClick = {
                                        viewModel.selectGeneration(gen)
                                        activeSelector = null
                                    },
                                )
                            }
                        }
                        SelectorType.ENGINE -> {
                            state.engines.forEach { engine ->
                                SelectorListItem(
                                    text = engine.displacementLabel ?: engine.name,
                                    subtitle = "${engine.powerHp ?: "?"} л.с. · ${engine.fuelType}",
                                    onClick = {
                                        viewModel.selectEngine(engine)
                                        activeSelector = null
                                    },
                                )
                            }
                        }
                        null -> {}
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

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
