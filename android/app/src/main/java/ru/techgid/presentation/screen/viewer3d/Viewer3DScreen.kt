package ru.techgid.presentation.screen.viewer3d

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material.icons.filled.ZoomOut
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.techgid.presentation.components.DifficultyBadge
import ru.techgid.presentation.theme.TechGidTheme
import ru.techgid.domain.model.Difficulty

@Composable
fun Viewer3DScreen(
    viewModel: Viewer3DViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
) {
    val carName by viewModel.carName.collectAsState()

    val mesh by viewModel.mesh.collectAsState()
    val cameraState = remember { Car3DCameraState() }
    var hiddenPartIds by remember { mutableStateOf<Set<String>>(emptySet()) }

    val parts = remember {
        listOf(
            PartInfo("Двигатель", "2.0 TFSI", "Моторный отсек", "2.0 TFSI, 4 цилиндра, 211 л.с., непосредственный впрыск, турбонаддув. Цепь ГРМ, интеркулер.", Difficulty.MEDIUM),
            PartInfo("Топливная система", "Топливный насос", "Заднее сиденье · доступ снизу", "Электробензонасос в баке, давление 4.5 бар, инжекторы непосредственного впрыска. Бак 60 л.", Difficulty.MEDIUM),
            PartInfo("Тормоза", "Тормозные колодки", "Колёсные арки", "Дисковые передние/задние, ABS, ESP, диаметр переднего диска 320 мм. Суппорт однопоршневый.", Difficulty.EASY),
            PartInfo("Подвеска", "McPherson / Многорычажная", "Колёсные арки · днище", "Передняя — McPherson, задняя — многорычажная. Стабилизаторы поперечной устойчивости.", Difficulty.HARD),
            PartInfo("Электрика", "CAN-шина", "Блок предохранителей", "Аккумулятор 70 А·ч, генератор 140 А, CAN-шина. Блок предохранителей под капотом и в салоне.", Difficulty.MEDIUM),
            PartInfo("Кузов", "Несущий кузов", "Наружные панели", "Несущий кузов, оцинковка, лакокрасочное покрытие в 4 слоя. Зоны программируемой деформации.", Difficulty.EXPERT),
            PartInfo("Салон", "Климат-контроль", "Торпедо · центральная консоль", "Климат-контроль, мультимедиа MMI, электрорегулировка сидений, подогрев передних сидений.", Difficulty.EASY),
        )
    }

    val partHighlightMap = remember {
        mapOf(
            0 to setOf(PartId.HOOD),
            1 to setOf(PartId.TRUNK),
            2 to setOf(PartId.WHEEL_FL, PartId.WHEEL_FR, PartId.WHEEL_RL, PartId.WHEEL_RR),
            3 to setOf(PartId.WHEEL_FL, PartId.WHEEL_FR, PartId.WHEEL_RL, PartId.WHEEL_RR),
            4 to setOf(PartId.HOOD),
            5 to setOf(PartId.BODY),
            6 to setOf(PartId.CABIN, PartId.GLASS),
        )
    }

    var selectedPartIndex by remember { mutableStateOf(1) }
    val selectedPart = parts[selectedPartIndex]
    val highlightedIds = partHighlightMap[selectedPartIndex] ?: emptySet()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            Spacer(Modifier.weight(1f))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "3D-модель",
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = carName,
                    style = MaterialTheme.typography.labelSmall,
                    color = TechGidTheme.extendedColors.textTertiary,
                )
            }
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.size(48.dp))
        }

        // 3D Viewport
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.verticalGradient(
                        listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.06f),
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.surface,
                        ),
                    ),
                ),
        ) {
            Car3DRenderer(
                mesh = mesh,
                cameraState = cameraState,
                modifier = Modifier.fillMaxSize(),
                options = RenderOptions(
                    highlightedPartIds = highlightedIds,
                    hiddenPartIds = hiddenPartIds,
                ),
                accentColor = MaterialTheme.colorScheme.primary,
            )

            // Zoom controls
            Column(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                ControlButton(Icons.Filled.ZoomIn) {
                    cameraState.zoom = (cameraState.zoom + 0.15f).coerceAtMost(2.5f)
                }
                ControlButton(Icons.Filled.ZoomOut) {
                    cameraState.zoom = (cameraState.zoom - 0.15f).coerceAtLeast(0.4f)
                }
                ControlButton(Icons.Filled.Refresh) {
                    cameraState.reset()
                }
            }

            // Zoom level indicator
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(12.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
            ) {
                Text(
                    text = "${(cameraState.zoom * 100).toInt()}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = TechGidTheme.extendedColors.textTertiary,
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Bottom info panel (scrollable)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            // Part selector chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                parts.forEachIndexed { index, part ->
                    PartChip(
                        text = part.name,
                        selected = index == selectedPartIndex,
                        onClick = { selectedPartIndex = index },
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Part info card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = TechGidTheme.extendedColors.cardBackground,
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                border = BorderStroke(1.dp, TechGidTheme.extendedColors.cardBorder),
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = selectedPart.title,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.weight(1f),
                        )
                        Spacer(Modifier.width(8.dp))
                        DifficultyBadge(difficulty = selectedPart.difficulty)
                    }

                    Spacer(Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Filled.LocationOn,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = TechGidTheme.extendedColors.textTertiary,
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            text = selectedPart.location,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TechGidTheme.extendedColors.textTertiary,
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    Text(
                        text = selectedPart.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp,
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Action buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedButton(
                    onClick = {
                        val ids = partHighlightMap[selectedPartIndex] ?: emptySet()
                        hiddenPartIds = if (ids.any { it in hiddenPartIds }) {
                            hiddenPartIds - ids
                        } else {
                            hiddenPartIds + ids
                        }
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Icon(
                        Icons.Filled.VisibilityOff,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Скрыть слой", style = MaterialTheme.typography.labelMedium)
                }
                OutlinedButton(
                    onClick = {
                        cameraState.reset()
                        hiddenPartIds = emptySet()
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Сброс камеры", style = MaterialTheme.typography.labelMedium)
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

private data class PartInfo(
    val name: String,
    val title: String,
    val location: String,
    val description: String,
    val difficulty: Difficulty,
)

@Composable
private fun ControlButton(icon: androidx.compose.ui.graphics.vector.ImageVector, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.92f))
            .clickable { onClick() },
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
    }
}

@Composable
private fun PartChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                if (selected) MaterialTheme.colorScheme.primary
                else TechGidTheme.extendedColors.cardBackground,
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp),
    ) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selected) MaterialTheme.colorScheme.onPrimary
            else MaterialTheme.colorScheme.onSurface,
        )
    }
}
