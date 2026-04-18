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
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import ru.techgid.presentation.components.DifficultyBadge
import ru.techgid.presentation.theme.TechGidTheme

@Composable
fun Viewer3DScreen(
    viewModel: Viewer3DViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
) {
    val mesh by viewModel.mesh.collectAsState()
    val sceneConfig by viewModel.sceneConfig.collectAsState()
    val cameraState = remember { Car3DCameraState() }
    var hiddenPartIds by remember { mutableStateOf<Set<String>>(emptySet()) }
    var selectedNodeIndex by remember { mutableIntStateOf(0) }
    var instructionMode by remember { mutableStateOf(false) }
    var instructionStep by remember { mutableIntStateOf(0) }

    val nodes = sceneConfig.nodes
    val selectedNode = nodes.getOrNull(selectedNodeIndex) ?: nodes.first()
    val steps = sceneConfig.instructions[selectedNode.id]

    val activeHighlightIds: Set<String>
    val activeHideIds: Set<String>

    if (instructionMode && steps != null && instructionStep in steps.indices) {
        val step = steps[instructionStep]
        activeHighlightIds = step.highlightPartIds
        activeHideIds = step.hidePartIds
    } else {
        activeHighlightIds = selectedNode.highlightPartIds
        activeHideIds = hiddenPartIds
    }

    val viewportBg = if (sceneConfig.ghostMode) {
        Color(0xFF080C14)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
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
                    text = sceneConfig.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = TechGidTheme.extendedColors.textTertiary,
                )
            }
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.size(48.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(viewportBg),
        ) {
            Car3DRenderer(
                mesh = mesh,
                cameraState = cameraState,
                modifier = Modifier.fillMaxSize(),
                options = RenderOptions(
                    highlightedPartIds = activeHighlightIds,
                    hiddenPartIds = activeHideIds,
                ),
                accentColor = Color(0xFFFF6D00),
                interactive = true,
                ghostMode = sceneConfig.ghostMode,
            )

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
                    hiddenPartIds = emptySet()
                    instructionMode = false
                }
            }

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

            if (instructionMode && steps != null) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xDD1A1A2E))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = "${instructionStep + 1}/${steps.size}: ${steps[instructionStep].label}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
                        color = Color(0xFFFF6D00),
                    )
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            if (instructionMode && steps != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(
                        onClick = {
                            if (instructionStep > 0) {
                                instructionStep--
                                cameraState.applyPreset(steps[instructionStep].cameraPreset)
                            }
                        },
                        enabled = instructionStep > 0,
                    ) {
                        Icon(Icons.Filled.ChevronLeft, contentDescription = "Назад",
                            tint = if (instructionStep > 0) MaterialTheme.colorScheme.primary
                            else TechGidTheme.extendedColors.textTertiary)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Шаг ${instructionStep + 1} из ${steps.size}",
                            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.SemiBold),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = steps[instructionStep].label,
                            style = MaterialTheme.typography.bodySmall,
                            color = TechGidTheme.extendedColors.textTertiary,
                        )
                    }
                    IconButton(
                        onClick = {
                            if (instructionStep < steps.size - 1) {
                                instructionStep++
                                cameraState.applyPreset(steps[instructionStep].cameraPreset)
                            }
                        },
                        enabled = instructionStep < steps.size - 1,
                    ) {
                        Icon(Icons.Filled.ChevronRight, contentDescription = "Далее",
                            tint = if (instructionStep < steps.size - 1) MaterialTheme.colorScheme.primary
                            else TechGidTheme.extendedColors.textTertiary)
                    }
                }

                Spacer(Modifier.height(8.dp))

                OutlinedButton(
                    onClick = {
                        instructionMode = false
                        cameraState.applyPreset(selectedNode.cameraPreset)
                        hiddenPartIds = emptySet()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(14.dp),
                ) {
                    Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Выйти из инструкции", style = MaterialTheme.typography.labelMedium)
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    nodes.forEachIndexed { index, node ->
                        PartChip(
                            text = node.label,
                            selected = index == selectedNodeIndex,
                            onClick = {
                                selectedNodeIndex = index
                                instructionMode = false
                                cameraState.applyPreset(node.cameraPreset)
                                hiddenPartIds = emptySet()
                            },
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

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
                                text = selectedNode.title,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f),
                            )
                            Spacer(Modifier.width(8.dp))
                            DifficultyBadge(difficulty = selectedNode.difficulty)
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
                                text = selectedNode.location,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TechGidTheme.extendedColors.textTertiary,
                            )
                        }

                        Spacer(Modifier.height(8.dp))

                        Text(
                            text = selectedNode.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            lineHeight = 18.sp,
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    OutlinedButton(
                        onClick = {
                            val ids = selectedNode.highlightPartIds + selectedNode.hidePartIds
                            hiddenPartIds = if (ids.any { it in hiddenPartIds }) {
                                hiddenPartIds - ids
                            } else {
                                hiddenPartIds + ids
                            }
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                    ) {
                        Icon(Icons.Filled.VisibilityOff, contentDescription = null,
                            modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Скрыть слой", style = MaterialTheme.typography.labelMedium)
                    }
                    if (steps != null) {
                        OutlinedButton(
                            onClick = {
                                instructionMode = true
                                instructionStep = 0
                                cameraState.applyPreset(steps[0].cameraPreset)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Icon(Icons.Filled.MenuBook, contentDescription = null,
                                modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Инструкция", style = MaterialTheme.typography.labelMedium)
                        }
                    } else {
                        OutlinedButton(
                            onClick = {
                                cameraState.reset()
                                hiddenPartIds = emptySet()
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = null,
                                modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Сброс камеры", style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

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
