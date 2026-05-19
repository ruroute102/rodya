package ru.techgid.presentation.screen.viewer3d

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import io.github.sceneview.Scene
import io.github.sceneview.math.Position
import io.github.sceneview.node.ModelNode
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberEnvironmentLoader
import io.github.sceneview.rememberMaterialLoader
import io.github.sceneview.rememberModelLoader
import io.github.sceneview.rememberNodes
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

@Composable
fun Car3DGlbRenderer(
    modelAssetPath: String,
    cameraState: Car3DCameraState,
    assetSpec: ResolvedCarModelAsset?,
    options: RenderOptions,
    modifier: Modifier = Modifier,
    bgColor: Color = Color(0xFF0A1020),
) {
    val engine = rememberEngine()
    val modelLoader = rememberModelLoader(engine)
    val materialLoader = rememberMaterialLoader(engine)
    val environmentLoader = rememberEnvironmentLoader(engine)

    val cameraNode = rememberCameraNode(engine) {
        position = Position(x = 0f, y = 0.6f, z = 4.2f)
        lookAt(Position(0f, 0f, 0f))
    }

    LaunchedEffect(
        cameraState.yaw,
        cameraState.pitch,
        cameraState.zoom,
        cameraState.focus,
    ) {
        val distance = 4.2f / cameraState.zoom
        val horizontal = cos(cameraState.pitch) * distance
        val focus = cameraState.focus
        cameraNode.position = Position(
            x = focus.x + sin(cameraState.yaw) * horizontal,
            y = focus.y + sin(-cameraState.pitch) * distance + 0.45f,
            z = focus.z + cos(cameraState.yaw) * horizontal,
        )
        cameraNode.lookAt(Position(focus.x, focus.y + 0.35f, focus.z))
    }

    var modelLoaded by remember { mutableStateOf(false) }
    var modelError by remember { mutableStateOf(false) }
    var modelNode by remember { mutableStateOf<ModelNode?>(null) }

    val childNodes = rememberNodes {
        val result = runCatching {
            val instance = modelLoader.createModelInstance(assetFileLocation = modelAssetPath)
            val node = ModelNode(
                modelInstance = instance,
                scaleToUnits = 4.4f,
                centerOrigin = Position(y = 0.0f),
            ).apply {
                isEditable = true
            }
            modelNode = node
            add(node)
        }
        modelLoaded = result.isSuccess
        modelError = result.isFailure
    }

    LaunchedEffect(modelNode, options.hiddenPartIds, assetSpec?.spec?.carId) {
        val node = modelNode ?: return@LaunchedEffect
        val hiddenGroups = assetSpec?.spec?.partGroups
            ?.filter { it.partId in options.hiddenPartIds }
            .orEmpty()

        node.renderableNodes.forEach { renderableNode ->
            val nodeName = renderableNode.name.orEmpty()
            renderableNode.isVisible = hiddenGroups.none { it.matchesMeshName(nodeName) }
        }
    }

    Box(
        modifier = modifier
            .background(bgColor)
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoomChange, _ ->
                    cameraState.yaw += pan.x * GLB_ORBIT_YAW_SENSITIVITY
                    cameraState.pitch = (cameraState.pitch - pan.y * GLB_ORBIT_PITCH_SENSITIVITY)
                        .coerceIn(GLB_MIN_ORBIT_PITCH, GLB_MAX_ORBIT_PITCH)
                    cameraState.zoom = (cameraState.zoom * zoomChange).coerceIn(0.55f, 2.7f)
                }
            },
        contentAlignment = Alignment.Center,
    ) {
        Scene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            modelLoader = modelLoader,
            materialLoader = materialLoader,
            cameraNode = cameraNode,
            childNodes = childNodes,
            isOpaque = false,
        )

        GlbRepairArrowOverlay(
            cameraState = cameraState,
            options = options,
            accentColor = Color(0xFF58BFEA),
            modifier = Modifier.fillMaxSize(),
        )

        AnimatedVisibility(
            visible = !modelLoaded && !modelError,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(
                    modifier = Modifier.size(40.dp),
                    color = Color(0xFF4A90D9),
                    strokeWidth = 3.dp,
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    text = "Загрузка модели...",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF8899AA),
                )
            }
        }

        if (modelError) {
            Text(
                text = "Не удалось загрузить модель",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFFFF6B6B),
            )
        }
    }
}

@Composable
private fun GlbRepairArrowOverlay(
    cameraState: Car3DCameraState,
    options: RenderOptions,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    val anchor = resolveGlbAnchor(options.highlightedPartIds, options.partOffsets) ?: return
    if (options.highlightedPartIds.isEmpty() || options.calloutTitle.isNullOrBlank()) return

    Canvas(modifier = modifier) {
        val projected = projectGlbPoint(anchor, cameraState, size.width, size.height) ?: return@Canvas
        val glowRadius = max(size.minDimension * 0.016f, 14f)

        drawCircle(
            color = accentColor.copy(alpha = 0.07f),
            radius = glowRadius * 1.6f,
            center = projected,
        )
        drawCircle(
            color = accentColor.copy(alpha = 0.17f),
            radius = glowRadius * 0.78f,
            center = projected,
        )
        drawCircle(
            color = accentColor.copy(alpha = 0.48f),
            radius = max(glowRadius * 0.16f, 3.5f),
            center = projected,
        )

        val arrowLength = 32.dp.toPx()
        val tail = Offset(
            x = (projected.x + if (projected.x < size.width * 0.5f) arrowLength else -arrowLength)
                .coerceIn(12.dp.toPx(), size.width - 12.dp.toPx()),
            y = (projected.y - arrowLength * 0.42f).coerceIn(12.dp.toPx(), size.height - 12.dp.toPx()),
        )
        val dx = projected.x - tail.x
        val dy = projected.y - tail.y
        val len = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
        val ux = dx / len
        val uy = dy / len
        val px = -uy
        val py = ux
        val headLength = 8.dp.toPx()
        val headWidth = 5.dp.toPx()
        val headBase = Offset(projected.x - ux * headLength, projected.y - uy * headLength)
        val shaftEnd = Offset(
            projected.x - ux * (headLength * 0.82f),
            projected.y - uy * (headLength * 0.82f),
        )

        drawLine(
            color = accentColor.copy(alpha = 0.28f),
            start = tail,
            end = shaftEnd,
            strokeWidth = 1.2.dp.toPx(),
            cap = StrokeCap.Round,
        )
        drawPath(
            path = Path().apply {
                moveTo(projected.x, projected.y)
                lineTo(headBase.x + px * headWidth, headBase.y + py * headWidth)
                lineTo(headBase.x - px * headWidth, headBase.y - py * headWidth)
                close()
            },
            color = accentColor.copy(alpha = 0.38f),
        )
    }
}

private fun resolveGlbAnchor(
    highlightedPartIds: Set<String>,
    partOffsets: Map<String, Vec3>,
): Vec3? {
    val points = highlightedPartIds.mapNotNull { partId ->
        GLB_PART_ANCHORS[partId]?.let { it + (partOffsets[partId] ?: Vec3(0f, 0f, 0f)) }
    }
    if (points.isEmpty()) return null
    return Vec3(
        x = points.sumOf { it.x.toDouble() }.toFloat() / points.size,
        y = points.sumOf { it.y.toDouble() }.toFloat() / points.size,
        z = points.sumOf { it.z.toDouble() }.toFloat() / points.size,
    )
}

private fun projectGlbPoint(point: Vec3, cameraState: Car3DCameraState, width: Float, height: Float): Offset? {
    if (width <= 0f || height <= 0f) return null
    val distance = 4.2f / cameraState.zoom
    val horizontal = cos(cameraState.pitch) * distance
    val focus = cameraState.focus
    val camera = Vec3(
        x = focus.x + sin(cameraState.yaw) * horizontal,
        y = focus.y + sin(-cameraState.pitch) * distance + 0.45f,
        z = focus.z + cos(cameraState.yaw) * horizontal,
    )
    val target = Vec3(focus.x, focus.y + 0.35f, focus.z)
    val forward = (target - camera).normalizedOrNull() ?: return null
    val right = forward.cross(Vec3(0f, 1f, 0f)).normalizedOrNull() ?: Vec3(1f, 0f, 0f)
    val up = right.cross(forward).normalizedOrNull() ?: Vec3(0f, 1f, 0f)
    val rel = point - camera
    val z = rel.dot(forward)
    if (z <= 0.05f) return null
    val x = rel.dot(right)
    val y = rel.dot(up)
    val f = 1f / tan((Math.PI.toFloat() / 3f) / 2f)
    val aspect = width / height
    val ndcX = (x * f / aspect) / z
    val ndcY = (y * f) / z
    return Offset(
        x = (ndcX + 1f) * 0.5f * width,
        y = (1f - (ndcY + 1f) * 0.5f) * height,
    )
}

private fun Vec3.dot(other: Vec3): Float = x * other.x + y * other.y + z * other.z

private fun Vec3.cross(other: Vec3): Vec3 = Vec3(
    y * other.z - z * other.y,
    z * other.x - x * other.z,
    x * other.y - y * other.x,
)

private fun Vec3.normalizedOrNull(): Vec3? {
    val len = sqrt(x * x + y * y + z * z)
    return if (len < 1e-6f) null else Vec3(x / len, y / len, z / len)
}

private fun PartMeshGroup.matchesMeshName(meshName: String): Boolean {
    if (meshName.isBlank()) return false
    val normalized = meshName.lowercase()
    return meshNameHints.any { hint -> normalized.contains(hint.lowercase()) }
}

private const val GLB_ORBIT_YAW_SENSITIVITY = 0.0065f
private const val GLB_ORBIT_PITCH_SENSITIVITY = 0.0055f
private const val GLB_MIN_ORBIT_PITCH = -1.12f
private const val GLB_MAX_ORBIT_PITCH = 0.42f

private val GLB_PART_ANCHORS = mapOf(
    PartId.REAR_SEAT to Vec3(0.0f, 0.95f, -1.05f),
    PartId.ACCESS_MARKER to Vec3(0.34f, 1.18f, -0.92f),
    PartId.FUEL_TANK to Vec3(0.0f, 0.40f, -1.02f),
    PartId.FUEL_PUMP_COVER to Vec3(0.34f, 0.57f, -0.92f),
    PartId.FUEL_LOCKING_RING to Vec3(0.34f, 0.61f, -0.92f),
    PartId.FUEL_PUMP to Vec3(0.34f, 0.66f, -0.92f),
    PartId.FUEL_CONNECTOR to Vec3(0.34f, 0.76f, -0.74f),
    PartId.FUEL_LINE to Vec3(0.42f, 0.50f, -0.20f),
    PartId.FUEL_PUMP_CONTROLLER to Vec3(0.64f, 0.66f, -0.76f),
    PartId.FUEL_SENDER_LEFT to Vec3(-0.34f, 0.56f, -0.92f),
    PartId.SUCTION_JET_PUMP to Vec3(-0.12f, 0.52f, -0.78f),
    PartId.ENGINE_BLOCK to Vec3(0.05f, 0.62f, 1.15f),
    PartId.OIL_FILTER to Vec3(0.28f, 0.74f, 1.34f),
    PartId.AIR_FILTER to Vec3(0.58f, 0.72f, 1.28f),
    PartId.TURBO to Vec3(0.08f, 0.40f, 0.82f),
    PartId.TRANSMISSION to Vec3(-0.48f, 0.32f, 1.03f),
    PartId.DRIVE_SHAFT to Vec3(0.0f, 0.30f, 0.35f),
)
