package ru.techgid.presentation.screen.viewer3d

import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin

// ─────────────────────────────────────────────────────────────────────────────
// Базовые типы 3D-сцены: вектор, грань, меш.
// Используются собственным software-рендерером на Canvas.
// ─────────────────────────────────────────────────────────────────────────────

/** 3D-вектор в модельном пространстве. */
data class Vec3(val x: Float, val y: Float, val z: Float) {
    operator fun plus(o: Vec3) = Vec3(x + o.x, y + o.y, z + o.z)
    operator fun minus(o: Vec3) = Vec3(x - o.x, y - o.y, z - o.z)
    operator fun times(k: Float) = Vec3(x * k, y * k, z * k)

    fun cross(o: Vec3) = Vec3(
        y * o.z - z * o.y,
        z * o.x - x * o.z,
        x * o.y - y * o.x,
    )

    fun dot(o: Vec3) = x * o.x + y * o.y + z * o.z

    fun normalized(): Vec3 {
        val len = kotlin.math.sqrt(x * x + y * y + z * z)
        return if (len < 1e-6f) this else Vec3(x / len, y / len, z / len)
    }

    /** Поворот вокруг оси Y. */
    fun rotateY(angle: Float): Vec3 {
        val c = cos(angle); val s = sin(angle)
        return Vec3(x * c + z * s, y, -x * s + z * c)
    }

    /** Поворот вокруг оси X. */
    fun rotateX(angle: Float): Vec3 {
        val c = cos(angle); val s = sin(angle)
        return Vec3(x, y * c - z * s, y * s + z * c)
    }
}

/** Идентификаторы частей автомобиля (должны совпадать с ключами в UI-списке). */
object PartId {
    const val BODY = "body"
    const val CABIN = "cabin"
    const val HOOD = "hood"
    const val TRUNK = "trunk"
    const val WHEEL_FL = "wheel_fl"
    const val WHEEL_FR = "wheel_fr"
    const val WHEEL_RL = "wheel_rl"
    const val WHEEL_RR = "wheel_rr"
    const val GLASS = "glass"
    const val HEADLIGHT = "headlight"
    const val TAILLIGHT = "taillight"
}

/** Треугольная грань с привязкой к детали и базовым цветом. */
data class Face(
    val a: Int,
    val b: Int,
    val c: Int,
    val partId: String,
    val baseColor: Color,
)

/** Меш: набор вершин + набор граней, ссылающихся на вершины по индексу. */
data class Mesh(
    val vertices: List<Vec3>,
    val faces: List<Face>,
)

// ─────────────────────────────────────────────────────────────────────────────
// Процедурный меш лёгкого low-poly автомобиля.
// Всё — параметрические кубоиды и шестигранные колёса.
// Ось X — вправо, Y — вверх, Z — от зрителя в глубину, +Z — вперёд капота.
// ─────────────────────────────────────────────────────────────────────────────

/** Собирает стандартный low-poly автомобиль. */
fun buildCarMesh(): Mesh {
    val verts = mutableListOf<Vec3>()
    val faces = mutableListOf<Face>()

    val bodyColor = Color(0xFF4A9EF5)
    val bodyDark = Color(0xFF2B6CB0)
    val cabinColor = Color(0xFF64B5F6)
    val glassColor = Color(0xFF0F172A)
    val wheelColor = Color(0xFF1A1A1A)
    val hubColor = Color(0xFF94A3B8)
    val headlightColor = Color(0xFFFFECB3)
    val taillightColor = Color(0xFFEF4444)

    // ── Основной кузов: плоский кубоид ────────────────────────────────
    addBox(
        verts, faces,
        center = Vec3(0f, 0.0f, 0f),
        size = Vec3(2.0f, 0.5f, 4.2f),
        partId = PartId.BODY,
        color = bodyColor,
        bottomColor = bodyDark,
    )

    // ── Передний капот (ниже, перед салоном) ──────────────────────────
    addBox(
        verts, faces,
        center = Vec3(0f, 0.35f, 1.15f),
        size = Vec3(1.88f, 0.2f, 1.5f),
        partId = PartId.HOOD,
        color = bodyColor,
        bottomColor = bodyDark,
    )

    // ── Салон (выше тела, короче) ─────────────────────────────────────
    addBox(
        verts, faces,
        center = Vec3(0f, 0.75f, -0.25f),
        size = Vec3(1.7f, 0.7f, 2.1f),
        partId = PartId.CABIN,
        color = cabinColor,
        bottomColor = bodyDark,
    )

    // ── Стёкла: окна салона (чуть меньше кубика салона) ───────────────
    addBox(
        verts, faces,
        center = Vec3(0f, 0.9f, -0.25f),
        size = Vec3(1.72f, 0.45f, 1.95f),
        partId = PartId.GLASS,
        color = glassColor,
        bottomColor = glassColor,
    )

    // ── Багажник ─────────────────────────────────────────────────────
    addBox(
        verts, faces,
        center = Vec3(0f, 0.4f, -1.55f),
        size = Vec3(1.88f, 0.3f, 0.8f),
        partId = PartId.TRUNK,
        color = bodyColor,
        bottomColor = bodyDark,
    )

    // ── Фары передние ────────────────────────────────────────────────
    addBox(
        verts, faces,
        center = Vec3(-0.75f, 0.35f, 2.05f),
        size = Vec3(0.35f, 0.18f, 0.05f),
        partId = PartId.HEADLIGHT,
        color = headlightColor,
        bottomColor = headlightColor,
    )
    addBox(
        verts, faces,
        center = Vec3(0.75f, 0.35f, 2.05f),
        size = Vec3(0.35f, 0.18f, 0.05f),
        partId = PartId.HEADLIGHT,
        color = headlightColor,
        bottomColor = headlightColor,
    )

    // ── Фонари задние ───────────────────────────────────────────────
    addBox(
        verts, faces,
        center = Vec3(-0.75f, 0.45f, -2.0f),
        size = Vec3(0.35f, 0.18f, 0.05f),
        partId = PartId.TAILLIGHT,
        color = taillightColor,
        bottomColor = taillightColor,
    )
    addBox(
        verts, faces,
        center = Vec3(0.75f, 0.45f, -2.0f),
        size = Vec3(0.35f, 0.18f, 0.05f),
        partId = PartId.TAILLIGHT,
        color = taillightColor,
        bottomColor = taillightColor,
    )

    // ── 4 колеса (шестигранные "барабаны") ───────────────────────────
    val wheelR = 0.42f
    val wheelW = 0.32f
    val wheelY = -0.25f
    val wheelXOff = 1.02f
    val wheelZOffFront = 1.3f
    val wheelZOffRear = -1.3f

    addHexCylinder(verts, faces, Vec3(-wheelXOff, wheelY, wheelZOffFront), wheelR, wheelW, PartId.WHEEL_FL, wheelColor, hubColor)
    addHexCylinder(verts, faces, Vec3(wheelXOff, wheelY, wheelZOffFront), wheelR, wheelW, PartId.WHEEL_FR, wheelColor, hubColor)
    addHexCylinder(verts, faces, Vec3(-wheelXOff, wheelY, wheelZOffRear), wheelR, wheelW, PartId.WHEEL_RL, wheelColor, hubColor)
    addHexCylinder(verts, faces, Vec3(wheelXOff, wheelY, wheelZOffRear), wheelR, wheelW, PartId.WHEEL_RR, wheelColor, hubColor)

    return Mesh(verts, faces)
}

/** Добавляет параметрический кубоид в меш. */
private fun addBox(
    verts: MutableList<Vec3>,
    faces: MutableList<Face>,
    center: Vec3,
    size: Vec3,
    partId: String,
    color: Color,
    bottomColor: Color,
) {
    val hx = size.x / 2f
    val hy = size.y / 2f
    val hz = size.z / 2f
    val base = verts.size

    // 8 вершин куба
    verts += Vec3(center.x - hx, center.y - hy, center.z - hz) // 0 LDB
    verts += Vec3(center.x + hx, center.y - hy, center.z - hz) // 1 RDB
    verts += Vec3(center.x + hx, center.y + hy, center.z - hz) // 2 RUB
    verts += Vec3(center.x - hx, center.y + hy, center.z - hz) // 3 LUB
    verts += Vec3(center.x - hx, center.y - hy, center.z + hz) // 4 LDF
    verts += Vec3(center.x + hx, center.y - hy, center.z + hz) // 5 RDF
    verts += Vec3(center.x + hx, center.y + hy, center.z + hz) // 6 RUF
    verts += Vec3(center.x - hx, center.y + hy, center.z + hz) // 7 LUF

    // 12 треугольников, по 2 на грань. Порядок вершин CCW при взгляде снаружи,
    // чтобы нормаль, вычисленная через (b-a)×(c-a), смотрела наружу.
    // Верх (Y+): 3,6,2 & 3,7,6
    faces += Face(base + 3, base + 6, base + 2, partId, color)
    faces += Face(base + 3, base + 7, base + 6, partId, color)
    // Низ (Y-): 0,1,5 & 0,5,4
    faces += Face(base + 0, base + 1, base + 5, partId, bottomColor)
    faces += Face(base + 0, base + 5, base + 4, partId, bottomColor)
    // Перёд (Z+): 4,5,6 & 4,6,7
    faces += Face(base + 4, base + 5, base + 6, partId, color)
    faces += Face(base + 4, base + 6, base + 7, partId, color)
    // Зад (Z-): 1,0,3 & 1,3,2
    faces += Face(base + 1, base + 0, base + 3, partId, color)
    faces += Face(base + 1, base + 3, base + 2, partId, color)
    // Право (X+): 5,1,2 & 5,2,6
    faces += Face(base + 5, base + 1, base + 2, partId, color)
    faces += Face(base + 5, base + 2, base + 6, partId, color)
    // Лево (X-): 0,4,7 & 0,7,3
    faces += Face(base + 0, base + 4, base + 7, partId, color)
    faces += Face(base + 0, base + 7, base + 3, partId, color)
}

/**
 * Шестигранный «цилиндр» — грубая модель колеса. Ось цилиндра — X
 * (горизонтально, перпендикулярно направлению движения).
 */
private fun addHexCylinder(
    verts: MutableList<Vec3>,
    faces: MutableList<Face>,
    center: Vec3,
    radius: Float,
    width: Float,
    partId: String,
    tireColor: Color,
    hubColor: Color,
) {
    val segments = 8
    val base = verts.size
    val halfW = width / 2f

    // 16 вершин: два «обода» по 8 точек
    for (side in 0..1) {
        val x = center.x + (if (side == 0) -halfW else halfW)
        for (i in 0 until segments) {
            val a = (i.toFloat() / segments) * (2f * kotlin.math.PI.toFloat())
            val y = center.y + radius * cos(a)
            val z = center.z + radius * sin(a)
            verts += Vec3(x, y, z)
        }
    }
    // Центр каждого обода — для диска ступицы
    verts += Vec3(center.x - halfW, center.y, center.z) // center left
    verts += Vec3(center.x + halfW, center.y, center.z) // center right
    val leftCenter = base + 2 * segments
    val rightCenter = base + 2 * segments + 1

    // Боковая поверхность (резина)
    for (i in 0 until segments) {
        val i1 = i
        val i2 = (i + 1) % segments
        val a = base + i1
        val b = base + i2
        val c = base + segments + i2
        val d = base + segments + i1
        faces += Face(a, b, c, partId, tireColor)
        faces += Face(a, c, d, partId, tireColor)
    }
    // Диск (внешняя левая сторона): треугольники от центра к ободу
    for (i in 0 until segments) {
        val i1 = i
        val i2 = (i + 1) % segments
        faces += Face(leftCenter, base + i2, base + i1, partId, hubColor)
    }
    // Диск (внешняя правая сторона)
    for (i in 0 until segments) {
        val i1 = i
        val i2 = (i + 1) % segments
        faces += Face(rightCenter, base + segments + i1, base + segments + i2, partId, hubColor)
    }
}
