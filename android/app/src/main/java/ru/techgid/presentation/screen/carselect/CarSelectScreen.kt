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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DirectionsCar
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import ru.techgid.presentation.components.CarSelectorItem
import ru.techgid.presentation.components.PrimaryButton
import ru.techgid.presentation.screen.viewer3d.Car3DCameraState
import ru.techgid.presentation.screen.viewer3d.Car3DRenderer
import ru.techgid.presentation.screen.viewer3d.buildMeshForCar
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

    // Step progress
    val completedSteps = listOfNotNull(
        state.selectedBrand,
        state.selectedModel,
        state.selectedGeneration,
        state.selectedEngine,
    ).size

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
            Spacer(modifier = Modifier.height(28.dp))

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

            Spacer(modifier = Modifier.height(20.dp))

            // Step progress indicator
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                repeat(4) { index ->
                    val isCompleted = index < completedSteps
                    val isCurrent = index == completedSteps
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                when {
                                    isCompleted -> MaterialTheme.colorScheme.primary
                                    isCurrent -> MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                                    else -> TechGidTheme.extendedColors.cardBorder
                                }
                            ),
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (state.isLoading && state.brands.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
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

            // Car illustration area — live 3D preview
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(24.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.08f),
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                MaterialTheme.colorScheme.surface,
                            ),
                        ),
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (state.selectedBrand != null) {
                    val carDisplayName = remember(
                        state.selectedBrand,
                        state.selectedModel,
                        state.selectedGeneration,
                        state.selectedEngine,
                    ) {
                        buildString {
                            append(state.selectedBrand?.name ?: "")
                            state.selectedModel?.let { append(" ${it.name}") }
                            state.selectedGeneration?.let { append(" ${it.yearStart}") }
                            state.selectedEngine?.let { e -> e.displacementLabel?.let { append(" · $it") } }
                        }
                    }
                    val mesh = remember(carDisplayName) { buildMeshForCar(carDisplayName) }
                    val cameraState = remember(carDisplayName) { Car3DCameraState() }

                    LaunchedEffect(carDisplayName) {
                        while (true) {
                            delay(16L)
                            cameraState.yaw += 0.002f
                        }
                    }

                    Car3DRenderer(
                        mesh = mesh,
                        cameraState = cameraState,
                        modifier = Modifier.fillMaxSize(),
                        accentColor = MaterialTheme.colorScheme.primary,
                    )

                    if (state.isComplete) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(12.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = buildString {
                                        append(state.selectedBrand?.name ?: "")
                                        state.selectedModel?.let { append(" ${it.name}") }
                                        state.selectedGeneration?.let { append(" ${it.yearStart}") }
                                    },
                                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                                    color = MaterialTheme.colorScheme.onSurface,
                                    textAlign = TextAlign.Center,
                                )
                                state.selectedEngine?.let { engine ->
                                    Text(
                                        text = "${engine.displacementLabel ?: engine.name} · ${engine.powerHp ?: "?"} л.с.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TechGidTheme.extendedColors.textTertiary,
                                        textAlign = TextAlign.Center,
                                    )
                                }
                            }
                        }
                    }
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Filled.DirectionsCar,
                            contentDescription = null,
                            modifier = Modifier.size(90.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f),
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "3D-модель появится\nпосле выбора авто",
                            style = MaterialTheme.typography.bodySmall,
                            color = TechGidTheme.extendedColors.textTertiary,
                            textAlign = TextAlign.Center,
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            PrimaryButton(
                text = if (state.isComplete) "Показать инструкции" else "Выберите авто",
                onClick = { onShowGuides(state.selectedEngine?.id ?: 1) },
                enabled = state.isComplete,
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Quick links
            Text(
                text = "Быстрый доступ",
                style = MaterialTheme.typography.titleSmall,
                color = TechGidTheme.extendedColors.textTertiary,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp),
            )

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

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Bottom Sheet
    if (activeSelector != null) {
        val sheetState = rememberModalBottomSheetState()

        ModalBottomSheet(
            onDismissRequest = { activeSelector = null },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
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
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
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
                                    isSelected = state.selectedBrand?.id == brand.id,
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
                                    isSelected = state.selectedModel?.id == model.id,
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
                                    isSelected = state.selectedGeneration?.id == gen.id,
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
                                    isSelected = state.selectedEngine?.id == engine.id,
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
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 16.dp, horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
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
    isSelected: Boolean = false,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)
                else MaterialTheme.colorScheme.surface,
            )
            .clickable { onClick() }
            .padding(vertical = 14.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                ),
                color = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurface,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
