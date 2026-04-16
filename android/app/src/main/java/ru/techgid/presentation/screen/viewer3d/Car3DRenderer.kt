package ru.techgid.presentation.screen.viewer3d

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
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
import kotlin.math.tan

/**
 * Состояние камеры: углы орбиты, зум. Всё изменяется жестами или внешним API.
 */
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
}

/** Настройки рендера: какие части подсвечены/скрыты. */
data class RenderOptions(
    val highlightedPartIds: Set<String> = emptySet(),
    val hiddenPartIds: Set<String> = emptySet(),
)

/**
 * Real-time software 3D-рендер автомобиля в Jetpack Compose.
 *
 * Что делает:
 *  1. Применяет повороты вокруг Y и X ко всем вершинам.
 *  2. Выполняет перспективную проекцию в экранные координаты.
 *  3. Отбрасывает back-face грани.
 *  4. Сортирует грани по средней Z (painter's algorithm).
 *  5. Рисует каждую грань как заполненный треугольник + тонкий контур.
 *  6. Применяет Lambert-затенение по углу к направлению света.
 *  7. Подсвечивает выбранные детали цветом акцента.
 *
 * @param zoomIndicator обновляется внутри, чтобы внешний UI мог показывать проценты.
 */
@Composable
fun Car3DRenderer(
    mesh: Mesh,
    cameraState: Car3DCameraState,
    modifier: Modifier = Modifier,
    options: RenderOptions = RenderOptions(),
    accentColor: Color = Color(0xFF4A9EF5),
    lightDirection: Vec3 = Vec3(-0.4f, 1f, 0.6f),
) {
    val lightDirN = remember(lightDirection) { lightDirection.normalized() }

    Box(
        modifier = modifier
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    // Сенсимость drag подобрана эмпирически
                    cameraState.yaw += dragAmount.x * 0.009f
                    cameraState.pitch = (cameraState.pitch - dragAmount.y * 0.009f)
                        .coerceIn((-PI / 2 + 0.1).toFloat(), (PI / 2 - 0.1).toFloat())
                }
            }
            .pointerInput(Unit) {
                detectTransformGestures { _, _, zoomChange, _ ->
                    cameraState.zoom = (cameraState.zoom * zoomChange).coerceIn(0.4f, 2.5f)
                }
            },
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasSize = size
            if (canvasSize.width <= 0f || canvasSize.height <= 0f) return@Canvas

            // 1) Преобразуем все вершины в экранные координаты и мировые Z
            val yaw = cameraState.yaw
            val pitch = cameraState.pitch
            val focus = cameraState.focus
            val cameraDistance = 8.5f / cameraState.zoom
            val fov = (PI / 3).toFloat() // 60°
            val projected = Array(mesh.vertices.size) { i ->
                val v = mesh.vertices[i] - focus
                val r = v.rotateY(yaw).rotateX(pitch)
                val z = r.z + cameraDistance
                val screen = perspectiveProject(r, cameraDistance, fov, canvasSize)
                VertexProjected(screen, z, r)
            }

            // 2) Отбор видимых граней + сортировка
            val visibleFaces = mesh.faces.mapNotNull { face ->
                if (face.partId in options.hiddenPartIds) return@mapNotNull null
                val va = projected[face.a]
                val vb = projected[face.b]
                val vc = projected[face.c]

                // Back-face culling: считаем 2D-нормаль через знак кросс-произведения
                val ax = vb.screen.x - va.screen.x
                val ay = vb.screen.y - va.screen.y
                val bx = vc.screen.x - va.screen.x
                val by = vc.screen.y - va.screen.y
                val sign2d = ax * by - ay * bx
                // Поскольку ось Y экрана вниз, грань «наружу» даёт отрицательный sign2d
                if (sign2d >= 0f) return@mapNotNull null

                // Клиппинг по Z: грани слишком близко пропускаем
                val avgZ = (va.z + vb.z + vc.z) / 3f
                if (avgZ < 0.5f) return@mapNotNull null

                // 3D-нормаль (в view space) для освещения
                val edge1 = vb.view - va.view
                val edge2 = vc.view - va.view
                val normal = edge1.cross(edge2).normalized()

                FaceDraw(
                    face = face,
                    a = va.screen,
                    b = vb.screen,
                    c = vc.screen,
                    avgZ = avgZ,
                    normal = normal,
                )
            }.sortedByDescending { it.avgZ } // дальние сперва

            // 3) Рисуем
            visibleFaces.forEach { f ->
                val isHighlighted = f.face.partId in options.highlightedPartIds
                val base = if (isHighlighted) {
                    blend(f.face.baseColor, accentColor, 0.55f)
                } else {
                    f.face.baseColor
                }

                // Lambert-затенение
                val lambert = (f.normal.dot(lightDirN)).coerceIn(0f, 1f)
                val shade = 0.35f + 0.65f * lambert
                val shaded = Color(
                    red = (base.red * shade).coerceIn(0f, 1f),
                    green = (base.green * shade).coerceIn(0f, 1f),
                    blue = (base.blue * shade).coerceIn(0f, 1f),
                    alpha = base.alpha,
                )

                val path = Path().apply {
                    moveTo(f.a.x, f.a.y)
                    lineTo(f.b.x, f.b.y)
                    lineTo(f.c.x, f.c.y)
                    close()
                }
                drawPath(path, color = shaded)
                // Тонкий контур — делает low-poly приятнее на глаз
                drawPath(
                    path = path,
                    color = if (isHighlighted) accentColor else Color(0x33000000),
                    style = Stroke(width = if (isHighlighted) 2f else 0.8f),
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Внутренние типы и утилиты
// ─────────────────────────────────────────────────────────────────────────────

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

/** Перспективная проекция view-space точки в экранные координаты. */
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

/** Линейная интерполяция между двумя цветами. */
private fun blend(a: Color, b: Color, t: Float): Color {
    val it = 1f - t
    return Color(
        red = a.red * it + b.red * t,
        green = a.green * it + b.green * t,
        blue = a.blue * it + b.blue * t,
        alpha = a.alpha * it + b.alpha * t,
    )
}

/** Плавно направить камеру на заданную точку в модельном пространстве. */
fun Car3DCameraState.focusOn(target: Vec3) {
    focus = target
}
