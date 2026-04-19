package ru.techgid.presentation.screen.viewer3d

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import kotlin.math.PI
import kotlin.math.abs
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

    Box(
        modifier = modifier.let { m ->
            if (interactive) {
                m.pointerInput(Unit) {
                    detectTransformGestures { _, pan, zoomChange, _ ->
                        cameraState.yaw += pan.x * 0.009f
                        cameraState.pitch = (cameraState.pitch - pan.y * 0.009f)
                            .coerceIn((-PI / 2 + 0.1).toFloat(), (PI / 2 - 0.1).toFloat())
                        cameraState.zoom = (cameraState.zoom * zoomChange).coerceIn(0.4f, 2.5f)
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
            val projected = Array(mesh.vertices.size) { i ->
                val v = mesh.vertices[i] - focus
                val r = v.rotateY(yaw).rotateX(pitch)
                val z = r.z + cameraDistance
                val screen = perspectiveProject(r, cameraDistance, fov, canvasSize)
                VertexProjected(screen, z, r)
            }

            val visibleFaces = mesh.faces.mapNotNull { face ->
                if (face.partId in options.hiddenPartIds) return@mapNotNull null
                val va = projected[face.a]
                val vb = projected[face.b]
                val vc = projected[face.c]

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

            visibleFaces.forEach { f ->
                val isHighlighted = f.face.partId in options.highlightedPartIds
                val isGhostFace = ghostMode && f.face.partId in GHOST_EXTERIOR_IDS
                val isSemiGhost = ghostMode && f.face.partId in SEMI_GHOST_IDS

                val lambert = (f.normal.dot(lightDirN)).coerceIn(0f, 1f)
                val fresnel = (1f - abs(f.normal.z)).coerceIn(0f, 1f).let { it * it }

                val path = Path().apply {
                    moveTo(f.a.x, f.a.y)
                    lineTo(f.b.x, f.b.y)
                    lineTo(f.c.x, f.c.y)
                    close()
                }

                when {
                    isGhostFace -> {
                        val fillAlpha = if (isHighlighted) 0.28f
                            else 0.12f + fresnel * 0.08f
                        val shade = 0.45f + 0.55f * lambert
                        val base = f.face.baseColor
                        val fill = Color(
                            red = (base.red * shade * 0.72f + 0.08f).coerceIn(0f, 1f),
                            green = (base.green * shade * 0.72f + 0.12f).coerceIn(0f, 1f),
                            blue = (base.blue * shade * 0.72f + 0.20f).coerceIn(0f, 1f),
                            alpha = fillAlpha,
                        )
                        drawPath(path, color = fill)
                        val wireAlpha = 0.02f + fresnel * 0.08f
                        val wireWidth = 0.2f + fresnel * 0.5f
                        val wireColor = if (isHighlighted)
                            accentColor.copy(alpha = (wireAlpha * 2f).coerceAtMost(1f))
                        else
                            Color(0.45f, 0.62f, 0.85f, wireAlpha)
                        drawPath(path, color = wireColor, style = Stroke(width = wireWidth))
                    }

                    isSemiGhost -> {
                        val semiAlpha = 0.25f + fresnel * 0.10f
                        val shade = 0.35f + 0.65f * lambert
                        val base = f.face.baseColor
                        val fill = Color(
                            red = (base.red * shade).coerceIn(0f, 1f),
                            green = (base.green * shade).coerceIn(0f, 1f),
                            blue = (base.blue * shade).coerceIn(0f, 1f),
                            alpha = semiAlpha,
                        )
                        drawPath(path, color = fill)
                        val wireAlpha = 0.03f + fresnel * 0.08f
                        drawPath(path, color = Color(0.4f, 0.55f, 0.75f, wireAlpha),
                            style = Stroke(width = 0.3f))
                    }

                    else -> {
                        val base = if (isHighlighted)
                            blend(f.face.baseColor, accentColor, 0.55f)
                        else
                            f.face.baseColor

                        val rimBoost = if (ghostMode) fresnel * 0.18f else 0f
                        val shade = (0.35f + 0.65f * lambert + rimBoost).coerceAtMost(1f)
                        val shaded = Color(
                            red = (base.red * shade).coerceIn(0f, 1f),
                            green = (base.green * shade).coerceIn(0f, 1f),
                            blue = (base.blue * shade).coerceIn(0f, 1f),
                            alpha = base.alpha,
                        )
                        drawPath(path, color = shaded)

                        if (isHighlighted) {
                            drawPath(path, color = Color(1f, 0.42f, 0f, 0.04f),
                                style = Stroke(width = 34f))
                            drawPath(path, color = Color(1f, 0.45f, 0f, 0.07f),
                                style = Stroke(width = 24f))
                            drawPath(path, color = Color(1f, 0.48f, 0f, 0.12f),
                                style = Stroke(width = 16f))
                            drawPath(path, color = Color(1f, 0.52f, 0f, 0.20f),
                                style = Stroke(width = 9f))
                            drawPath(path, color = accentColor.copy(alpha = 0.42f),
                                style = Stroke(width = 4f))
                            drawPath(path, color = accentColor,
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
        }
    }
}

private val GHOST_EXTERIOR_IDS = setOf(
    PartId.BODY, PartId.CABIN, PartId.HOOD, PartId.TRUNK,
    PartId.GLASS, PartId.HEADLIGHT, PartId.TAILLIGHT,
)

private val SEMI_GHOST_IDS = setOf(
    PartId.WHEEL_FL, PartId.WHEEL_FR, PartId.WHEEL_RL, PartId.WHEEL_RR,
)

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
