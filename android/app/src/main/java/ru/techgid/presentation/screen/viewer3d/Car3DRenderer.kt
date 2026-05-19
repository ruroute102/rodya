package ru.techgid.presentation.screen.viewer3d

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.isActive
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

class Car3DCameraState(
    initialYaw: Float = (PI / 6).toFloat(),
    initialPitch: Float = (-PI / 9).toFloat(),
    initialZoom: Float = 1f,
) {
    var yaw by mutableFloatStateOf(initialYaw)
    var pitch by mutableFloatStateOf(initialPitch)
    var zoom by mutableFloatStateOf(initialZoom)
    var focus by mutableStateOf(Vec3(0f, 0f, 0f))

    fun reset() {
        yaw = (PI / 6).toFloat()
        pitch = (-PI / 9).toFloat()
        zoom = 1f
        focus = Vec3(0f, 0f, 0f)
    }

    fun applyPreset(preset: CameraPreset) {
        yaw = preset.yaw
        pitch = preset.pitch
        zoom = preset.zoom
        focus = Vec3(preset.focusX, preset.focusY, preset.focusZ)
    }
}

data class RenderOptions(
    val highlightedPartIds: Set<String> = emptySet(),
    val hiddenPartIds: Set<String> = emptySet(),
    val partOffsets: Map<String, Vec3> = emptyMap(),
    val calloutTitle: String? = null,
    val calloutSubtitle: String? = null,
)

@Composable
fun Car3DRenderer(
    mesh: Mesh,
    cameraState: Car3DCameraState,
    modifier: Modifier = Modifier,
    options: RenderOptions = RenderOptions(),
    accentColor: Color = Color(0xFFFF6D00),
    lightDirection: Vec3 = Vec3(-0.4f, 1f, 0.6f),
    interactive: Boolean = true,
    ghostMode: Boolean = false,
) {
    val lightDirN = remember(lightDirection) { lightDirection.normalized() }
    val viewDirN = remember { Vec3(0f, 0f, -1f) }
    val halfDirN = remember(lightDirN) {
        Vec3(lightDirN.x + viewDirN.x, lightDirN.y + viewDirN.y, lightDirN.z + viewDirN.z).normalized()
    }

    LaunchedEffect(Unit) {
        while (isActive) {
            withFrameMillis {
                if (!interactive) {
                    cameraState.yaw += 0.003f
                }
            }
        }
    }

    val shimmerTransition = rememberInfiniteTransition(label = "ghost_shimmer")
    val shimmerPhase by shimmerTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(5000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "shimmer_phase",
    )
    val scanPhase by shimmerTransition.animateFloat(
        initialValue = -0.2f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(3500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "scan_phase",
    )

    Box(
        modifier = modifier.let { m ->
            if (interactive) {
                m.pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoomChange, _ ->
                        cameraState.yaw += pan.x * ORBIT_YAW_SENSITIVITY
                        cameraState.pitch = (cameraState.pitch - pan.y * ORBIT_PITCH_SENSITIVITY)
                            .coerceIn(MIN_ORBIT_PITCH, MAX_ORBIT_PITCH)
                        cameraState.zoom = (cameraState.zoom * zoomChange).coerceIn(0.55f, 2.7f)
                    }
                }
            } else m
        },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = size
            if (canvasSize.width <= 0f || canvasSize.height <= 0f) return@Canvas

            if (ghostMode) {
                drawRect(color = Color(0xFF0A1020))

                val shadowCenterX = canvasSize.width / 2f
                val shadowCenterY = canvasSize.height * 0.74f
                val shadowW = canvasSize.width * 0.60f
                val shadowH = canvasSize.height * 0.055f
                for (i in 4 downTo 0) {
                    val t = i / 4f
                    val w = shadowW * (0.55f + 0.45f * t)
                    val h = shadowH * (0.55f + 0.45f * t)
                    val a = (0.10f * (1f - t) + 0.02f).coerceAtLeast(0f)
                    drawOval(
                        color = Color(0f, 0f, 0f, a),
                        topLeft = Offset(shadowCenterX - w / 2f, shadowCenterY - h / 2f),
                        size = Size(w, h),
                    )
                }
            }

            val yaw = cameraState.yaw
            val pitch = cameraState.pitch
            val focus = cameraState.focus
            val cameraDistance = 8.5f / cameraState.zoom
            val fov = (PI / 3).toFloat()
            fun projectVertex(vertexIndex: Int, partId: String): VertexProjected {
                val partOffset = options.partOffsets[partId] ?: Vec3(0f, 0f, 0f)
                val v = mesh.vertices[vertexIndex] + partOffset - focus
                val r = v.rotateY(yaw).rotateX(pitch)
                val z = r.z + cameraDistance
                val screen = perspectiveProject(r, cameraDistance, fov, canvasSize)
                return VertexProjected(screen, z, r)
            }

            val visibleFaces = mesh.faces.mapNotNull { face ->
                if (face.partId in options.hiddenPartIds) return@mapNotNull null
                val va = projectVertex(face.a, face.partId)
                val vb = projectVertex(face.b, face.partId)
                val vc = projectVertex(face.c, face.partId)

                val ax = vb.screen.x - va.screen.x
                val ay = vb.screen.y - va.screen.y
                val bx = vc.screen.x - va.screen.x
                val by = vc.screen.y - va.screen.y
                val sign2d = ax * by - ay * bx

                val isGhostFace = ghostMode && face.partId in GHOST_EXTERIOR_IDS
                if (!isGhostFace && sign2d >= 0f) return@mapNotNull null

                val avgZ = (va.z + vb.z + vc.z) / 3f
                if (avgZ < 0.5f) return@mapNotNull null

                val edge1 = vb.view - va.view
                val edge2 = vc.view - va.view
                val normal = edge1.cross(edge2).normalized()

                FaceDraw(face, va.screen, vb.screen, vc.screen, avgZ, normal)
            }.sortedByDescending { it.avgZ }

            val skyR = 0.62f; val skyG = 0.72f; val skyB = 0.88f
            val grdR = 0.28f; val grdG = 0.24f; val grdB = 0.20f
            val ghostSkyR = 0.35f; val ghostSkyG = 0.50f; val ghostSkyB = 0.72f
            val ghostGrdR = 0.08f; val ghostGrdG = 0.10f; val ghostGrdB = 0.16f

            visibleFaces.forEach { f ->
                val isHighlighted = f.face.partId in options.highlightedPartIds
                val isGhostFace = ghostMode && f.face.partId in GHOST_EXTERIOR_IDS
                val isSemiGhost = ghostMode && f.face.partId in SEMI_GHOST_IDS

                val nDotL = f.normal.dot(lightDirN)
                val halfLam = (nDotL * 0.5f + 0.5f)
                val diffuse = halfLam * halfLam

                val nDotH = f.normal.dot(halfDirN).coerceAtLeast(0f)
                val h2 = nDotH * nDotH
                val h4 = h2 * h2
                val h8 = h4 * h4
                val h16 = h8 * h8
                val spec32 = h16 * h16

                val hemi = f.normal.y * 0.5f + 0.5f
                val fresnel = (1f - abs(f.normal.z)).coerceIn(0f, 1f).let { it * it }

                val path = Path().apply {
                    moveTo(f.a.x, f.a.y)
                    lineTo(f.b.x, f.b.y)
                    lineTo(f.c.x, f.c.y)
                    close()
                }

                when {
                    isGhostFace -> {
                        val shimmerMod = 1f + sin(shimmerPhase) * 0.12f
                        val faceCenterY = (f.a.y + f.b.y + f.c.y) / 3f
                        val normalizedY = faceCenterY / canvasSize.height
                        val scanDist = abs(normalizedY - scanPhase)
                        val scanBoost = (1f - (scanDist * 6f).coerceAtMost(1f)).coerceAtLeast(0f)

                        val fillAlpha = if (isHighlighted) 0.24f
                            else (0.14f + fresnel * 0.12f + scanBoost * 0.10f) * shimmerMod
                        val ambR = ghostGrdR * (1f - hemi) + ghostSkyR * hemi
                        val ambG = ghostGrdG * (1f - hemi) + ghostSkyG * hemi
                        val ambB = ghostGrdB * (1f - hemi) + ghostSkyB * hemi
                        val base = f.face.baseColor
                        val rim = fresnel * fresnel * 0.75f
                        val litR = base.red * (0.32f + 0.50f * diffuse) + ambR * 0.28f + spec32 * 0.45f + rim * 0.55f + scanBoost * 0.18f
                        val litG = base.green * (0.32f + 0.50f * diffuse) + ambG * 0.30f + spec32 * 0.55f + rim * 0.78f + scanBoost * 0.28f
                        val litB = base.blue * (0.32f + 0.50f * diffuse) + ambB * 0.34f + spec32 * 0.70f + rim * 1.05f + scanBoost * 0.40f
                        drawPath(
                            path,
                            color = Color(
                                red = litR.coerceIn(0f, 1f),
                                green = litG.coerceIn(0f, 1f),
                                blue = litB.coerceIn(0f, 1f),
                                alpha = fillAlpha.coerceIn(0f, 1f),
                            ),
                        )
                        val wireAlpha = (0.02f + fresnel * 0.12f + scanBoost * 0.15f) * shimmerMod
                        val wireWidth = 0.2f + fresnel * 0.7f + scanBoost * 0.5f
                        val wireColor = if (isHighlighted)
                            accentColor.copy(alpha = (wireAlpha * 2.4f).coerceAtMost(1f))
                        else
                            Color(0.60f, 0.78f, 1.0f, wireAlpha.coerceIn(0f, 1f))
                        drawPath(path, color = wireColor, style = Stroke(width = wireWidth))
                    }

                    isSemiGhost -> {
                        val semiAlpha = 0.28f + fresnel * 0.12f
                        val ambR = ghostGrdR * (1f - hemi) + ghostSkyR * hemi
                        val ambG = ghostGrdG * (1f - hemi) + ghostSkyG * hemi
                        val ambB = ghostGrdB * (1f - hemi) + ghostSkyB * hemi
                        val base = f.face.baseColor
                        drawPath(
                            path,
                            color = Color(
                                red = (base.red * (0.30f + 0.65f * diffuse) + ambR * 0.15f + spec32 * 0.25f).coerceIn(0f, 1f),
                                green = (base.green * (0.30f + 0.65f * diffuse) + ambG * 0.15f + spec32 * 0.25f).coerceIn(0f, 1f),
                                blue = (base.blue * (0.30f + 0.65f * diffuse) + ambB * 0.18f + spec32 * 0.28f).coerceIn(0f, 1f),
                                alpha = semiAlpha,
                            ),
                        )
                        val wireAlpha = 0.03f + fresnel * 0.08f
                        drawPath(path, color = Color(0.4f, 0.55f, 0.75f, wireAlpha),
                            style = Stroke(width = 0.3f))
                    }

                    else -> {
                        val base = if (isHighlighted)
                            blend(f.face.baseColor, accentColor, 0.30f)
                        else
                            f.face.baseColor

                        val ambR = if (ghostMode) ghostGrdR * (1f - hemi) + ghostSkyR * hemi
                            else grdR * (1f - hemi) + skyR * hemi
                        val ambG = if (ghostMode) ghostGrdG * (1f - hemi) + ghostSkyG * hemi
                            else grdG * (1f - hemi) + skyG * hemi
                        val ambB = if (ghostMode) ghostGrdB * (1f - hemi) + ghostSkyB * hemi
                            else grdB * (1f - hemi) + skyB * hemi

                        val rimBoost = if (ghostMode) fresnel * 0.20f else 0f
                        val specStrength = if (isHighlighted) 0.52f else 0.55f
                        val litR = base.red * (0.32f + 0.60f * diffuse + rimBoost) + ambR * 0.18f + spec32 * specStrength
                        val litG = base.green * (0.32f + 0.60f * diffuse + rimBoost) + ambG * 0.18f + spec32 * specStrength
                        val litB = base.blue * (0.32f + 0.60f * diffuse + rimBoost) + ambB * 0.20f + spec32 * specStrength

                        drawPath(
                            path,
                            color = Color(
                                red = litR.coerceIn(0f, 1f),
                                green = litG.coerceIn(0f, 1f),
                                blue = litB.coerceIn(0f, 1f),
                                alpha = base.alpha,
                            ),
                        )

                        if (isHighlighted) {
                            drawPath(path, color = accentColor.copy(alpha = 0.018f),
                                style = Stroke(width = 32f))
                            drawPath(path, color = accentColor.copy(alpha = 0.035f),
                                style = Stroke(width = 20f))
                            drawPath(path, color = accentColor.copy(alpha = 0.07f),
                                style = Stroke(width = 12f))
                            drawPath(path, color = accentColor.copy(alpha = 0.14f),
                                style = Stroke(width = 5f))
                            drawPath(path, color = accentColor.copy(alpha = 0.42f),
                                style = Stroke(width = 1.2f))
                        } else if (ghostMode) {
                            drawPath(path, color = Color(0x15A0B8D0),
                                style = Stroke(width = 0.4f))
                        } else {
                            drawPath(path, color = Color(0x33000000),
                                style = Stroke(width = 0.8f))
                        }
                    }
                }
            }

            if (ghostMode) {
                val scanY = scanPhase * canvasSize.height
                for (i in 0..4) {
                    val spread = i * 3f
                    val lineAlpha = (0.25f - i * 0.05f).coerceAtLeast(0f)
                    drawLine(
                        color = Color(0.45f, 0.70f, 1.0f, lineAlpha),
                        start = Offset(0f, scanY - spread),
                        end = Offset(canvasSize.width, scanY - spread),
                        strokeWidth = if (i == 0) 1.5f else 0.8f,
                    )
                    if (i > 0) {
                        drawLine(
                            color = Color(0.45f, 0.70f, 1.0f, lineAlpha),
                            start = Offset(0f, scanY + spread),
                            end = Offset(canvasSize.width, scanY + spread),
                            strokeWidth = 0.8f,
                        )
                    }
                }
            }

            drawRepairCallout(
                visibleFaces = visibleFaces,
                highlightedPartIds = options.highlightedPartIds,
                title = options.calloutTitle,
                subtitle = options.calloutSubtitle,
                accentColor = accentColor,
                ghostMode = ghostMode,
                canvasSize = canvasSize,
            )
        }
    }
}

private val GHOST_EXTERIOR_IDS = setOf(
    PartId.BODY, PartId.CABIN, PartId.HOOD, PartId.TRUNK,
    PartId.GLASS, PartId.HEADLIGHT, PartId.TAILLIGHT,
)

private val SEMI_GHOST_IDS = setOf(
    PartId.WHEEL_FL, PartId.WHEEL_FR, PartId.WHEEL_RL, PartId.WHEEL_RR,
    PartId.REAR_SEAT,
)

private const val ORBIT_YAW_SENSITIVITY = 0.0065f
private const val ORBIT_PITCH_SENSITIVITY = 0.0055f
private const val MIN_ORBIT_PITCH = -1.12f
private const val MAX_ORBIT_PITCH = 0.42f

private data class VertexProjected(
    val screen: Offset,
    val z: Float,
    val view: Vec3,
)

private data class FaceDraw(
    val face: Face,
    val a: Offset,
    val b: Offset,
    val c: Offset,
    val avgZ: Float,
    val normal: Vec3,
)

private fun DrawScope.drawRepairCallout(
    visibleFaces: List<FaceDraw>,
    highlightedPartIds: Set<String>,
    title: String?,
    subtitle: String?,
    accentColor: Color,
    ghostMode: Boolean,
    canvasSize: Size,
) {
    if (highlightedPartIds.isEmpty() || title.isNullOrBlank()) return

    val highlightedFaces = visibleFaces.filter { it.face.partId in highlightedPartIds }
    if (highlightedFaces.isEmpty()) return

    var totalX = 0f
    var totalY = 0f
    var count = 0
    highlightedFaces.forEach { face ->
        totalX += face.a.x + face.b.x + face.c.x
        totalY += face.a.y + face.b.y + face.c.y
        count += 3
    }
    if (count == 0) return

    val anchor = Offset(totalX / count, totalY / count)
    val glowRadius = max(canvasSize.minDimension * 0.018f, 18f)

    drawCircle(
        color = accentColor.copy(alpha = 0.07f),
        radius = glowRadius * 1.7f,
        center = anchor,
    )
    drawCircle(
        color = accentColor.copy(alpha = 0.16f),
        radius = glowRadius * 0.82f,
        center = anchor,
    )
    drawCircle(
        color = accentColor.copy(alpha = 0.54f),
        radius = max(glowRadius * 0.16f, 4f),
        center = anchor,
    )

    val arrowLength = 34.dp.toPx()
    val arrowTail = Offset(
        x = (anchor.x + if (anchor.x < canvasSize.width * 0.5f) arrowLength else -arrowLength)
            .coerceIn(12.dp.toPx(), canvasSize.width - 12.dp.toPx()),
        y = (anchor.y - arrowLength * 0.42f).coerceIn(12.dp.toPx(), canvasSize.height - 12.dp.toPx()),
    )
    val dx = anchor.x - arrowTail.x
    val dy = anchor.y - arrowTail.y
    val length = sqrt(dx * dx + dy * dy).coerceAtLeast(1f)
    val ux = dx / length
    val uy = dy / length
    val px = -uy
    val py = ux
    val headLength = 9.dp.toPx()
    val headWidth = 5.5.dp.toPx()
    val headBase = Offset(anchor.x - ux * headLength, anchor.y - uy * headLength)
    val shaftEnd = Offset(anchor.x - ux * (headLength * 0.82f), anchor.y - uy * (headLength * 0.82f))

    drawLine(
        color = accentColor.copy(alpha = 0.28f),
        start = arrowTail,
        end = shaftEnd,
        strokeWidth = 1.25.dp.toPx(),
        cap = StrokeCap.Round,
    )
    drawPath(
        path = Path().apply {
            moveTo(anchor.x, anchor.y)
            lineTo(headBase.x + px * headWidth, headBase.y + py * headWidth)
            lineTo(headBase.x - px * headWidth, headBase.y - py * headWidth)
            close()
        },
        color = accentColor.copy(alpha = 0.38f),
    )
    drawCircle(
        color = accentColor.copy(alpha = 0.16f),
        radius = 2.dp.toPx(),
        center = arrowTail,
    )
}

private fun perspectiveProject(
    view: Vec3,
    cameraDistance: Float,
    fov: Float,
    canvasSize: Size,
): Offset {
    val z = view.z + cameraDistance
    val zSafe = if (z < 0.1f) 0.1f else z
    val f = 1f / tan(fov / 2f)
    val aspect = canvasSize.width / canvasSize.height
    val ndcX = (view.x * f / aspect) / zSafe
    val ndcY = (view.y * f) / zSafe
    val sx = (ndcX + 1f) * 0.5f * canvasSize.width
    val sy = (1f - (ndcY + 1f) * 0.5f) * canvasSize.height
    return Offset(sx, sy)
}

private fun blend(a: Color, b: Color, t: Float): Color {
    val it = 1f - t
    return Color(
        red = a.red * it + b.red * t,
        green = a.green * it + b.green * t,
        blue = a.blue * it + b.blue * t,
        alpha = a.alpha * it + b.alpha * t,
    )
}

fun Car3DCameraState.focusOn(target: Vec3) {
    focus = target
}
