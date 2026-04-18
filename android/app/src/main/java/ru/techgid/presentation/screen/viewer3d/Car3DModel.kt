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
    const val ENGINE_BLOCK = "engine_block"
    const val OIL_FILTER = "oil_filter"
    const val AIR_FILTER = "air_filter"
    const val TURBO = "turbo"
    const val INTERCOOLER = "intercooler"
    const val RADIATOR = "radiator"
    const val EXHAUST_MANIFOLD = "exhaust_manifold"
    const val BATTERY = "battery"
    const val COOLANT_TANK = "coolant_tank"
    const val TRANSMISSION = "transmission"
    const val CATALYTIC_CONVERTER = "cat_converter"
    const val DRIVE_SHAFT = "drive_shaft"
    const val BRAKE_CALIPER = "brake_caliper"
    const val OIL_PAN = "oil_pan"
    const val ALTERNATOR = "alternator"
    const val STARTER = "starter"
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

private fun addCylinder(
    verts: MutableList<Vec3>,
    faces: MutableList<Face>,
    center: Vec3,
    radius: Float,
    width: Float,
    segments: Int,
    partId: String,
    tireColor: Color,
    hubColor: Color,
) {
    val base = verts.size
    val halfW = width / 2f
    for (side in 0..1) {
        val x = center.x + (if (side == 0) -halfW else halfW)
        for (i in 0 until segments) {
            val a = (i.toFloat() / segments) * (2f * kotlin.math.PI.toFloat())
            verts += Vec3(x, center.y + radius * cos(a), center.z + radius * sin(a))
        }
    }
    verts += Vec3(center.x - halfW, center.y, center.z)
    verts += Vec3(center.x + halfW, center.y, center.z)
    val lc = base + 2 * segments
    val rc = base + 2 * segments + 1
    for (i in 0 until segments) {
        val i2 = (i + 1) % segments
        faces += Face(base + i, base + i2, base + segments + i2, partId, tireColor)
        faces += Face(base + i, base + segments + i2, base + segments + i, partId, tireColor)
        faces += Face(lc, base + i2, base + i, partId, hubColor)
        faces += Face(rc, base + segments + i, base + segments + i2, partId, hubColor)
    }
}

private fun addVerticalCylinder(
    verts: MutableList<Vec3>,
    faces: MutableList<Face>,
    center: Vec3,
    radius: Float,
    height: Float,
    segments: Int,
    partId: String,
    sideColor: Color,
    capColor: Color,
) {
    val base = verts.size
    val halfH = height / 2f
    for (i in 0 until segments) {
        val a = (i.toFloat() / segments) * (2f * kotlin.math.PI.toFloat())
        verts += Vec3(center.x + radius * cos(a), center.y - halfH, center.z + radius * sin(a))
    }
    for (i in 0 until segments) {
        val a = (i.toFloat() / segments) * (2f * kotlin.math.PI.toFloat())
        verts += Vec3(center.x + radius * cos(a), center.y + halfH, center.z + radius * sin(a))
    }
    verts += Vec3(center.x, center.y - halfH, center.z)
    verts += Vec3(center.x, center.y + halfH, center.z)
    val bc = base + 2 * segments
    val tc = base + 2 * segments + 1
    for (i in 0 until segments) {
        val i2 = (i + 1) % segments
        faces += Face(base + i, base + i2, base + segments + i2, partId, sideColor)
        faces += Face(base + i, base + segments + i2, base + segments + i, partId, sideColor)
        faces += Face(bc, base + i2, base + i, partId, capColor)
        faces += Face(tc, base + segments + i, base + segments + i2, partId, capColor)
    }
}

// Arbitrary 8-point solid. Vertex order (mirrors addBox vertex layout):
//   p0=FBL  p1=FBR  p2=FTR  p3=FTL   (front / Z+ face)
//   p4=BBL  p5=BBR  p6=BTR  p7=BTL   (back  / Z- face)
// Winding is derived from the verified addBox winding — outward normals guaranteed
// for convex shapes.
private fun addPrism(
    verts: MutableList<Vec3>,
    faces: MutableList<Face>,
    p0: Vec3, p1: Vec3, p2: Vec3, p3: Vec3,
    p4: Vec3, p5: Vec3, p6: Vec3, p7: Vec3,
    partId: String,
    color: Color,
    bottomColor: Color = color,
) {
    val b = verts.size
    verts += p0; verts += p1; verts += p2; verts += p3
    verts += p4; verts += p5; verts += p6; verts += p7
    faces += Face(b+7, b+2, b+6, partId, color)   // top
    faces += Face(b+7, b+3, b+2, partId, color)
    faces += Face(b+4, b+5, b+1, partId, bottomColor) // bottom
    faces += Face(b+4, b+1, b+0, partId, bottomColor)
    faces += Face(b+0, b+1, b+2, partId, color)   // front
    faces += Face(b+0, b+2, b+3, partId, color)
    faces += Face(b+5, b+4, b+7, partId, color)   // back
    faces += Face(b+5, b+7, b+6, partId, color)
    faces += Face(b+1, b+5, b+6, partId, color)   // right
    faces += Face(b+1, b+6, b+2, partId, color)
    faces += Face(b+4, b+0, b+3, partId, color)   // left
    faces += Face(b+4, b+3, b+7, partId, color)
}

private fun bodyProfile(
    z: Float, bY: Float, bW: Float,
    lW: Float, lY: Float, mW: Float,
    sY: Float, sW: Float,
    uY: Float, uW: Float,
    rY: Float, rW: Float, pY: Float,
): List<Vec3> {
    val mY = (lY + sY) / 2f
    return listOf(
        Vec3(0f, bY, z), Vec3(bW, bY, z),
        Vec3(lW, lY, z), Vec3(mW, mY, z),
        Vec3(sW, sY, z), Vec3(uW, uY, z),
        Vec3(rW, rY, z), Vec3(0f, pY, z),
        Vec3(-rW, rY, z), Vec3(-uW, uY, z),
        Vec3(-sW, sY, z), Vec3(-mW, mY, z),
        Vec3(-lW, lY, z), Vec3(-bW, bY, z),
    )
}

private fun addLoftedBody(
    verts: MutableList<Vec3>,
    faces: MutableList<Face>,
    profiles: List<List<Vec3>>,
    from: Int, to: Int,
    partId: String,
    color: Color,
    bottomColor: Color = color,
    capFront: Boolean = false,
    capBack: Boolean = false,
) {
    val n = profiles[0].size
    val base = verts.size
    for (s in from..to) verts.addAll(profiles[s])
    val sCount = to - from
    for (s in 0 until sCount) {
        for (i in 0 until n) {
            val i2 = (i + 1) % n
            val a = base + s * n + i
            val b = base + s * n + i2
            val c = base + (s + 1) * n + i2
            val d = base + (s + 1) * n + i
            val fc = if (i == 0 || i == n - 1) bottomColor else color
            faces += Face(a, d, c, partId, fc)
            faces += Face(a, c, b, partId, fc)
        }
    }
    if (capFront) {
        val p = profiles[from]
        val cx = p.map { it.x }.average().toFloat()
        val cy = p.map { it.y }.average().toFloat()
        val ci = verts.size
        verts += Vec3(cx, cy, p[0].z)
        for (i in 0 until n) {
            faces += Face(ci, base + i, base + (i + 1) % n, partId, color)
        }
    }
    if (capBack) {
        val p = profiles[to]
        val cx = p.map { it.x }.average().toFloat()
        val cy = p.map { it.y }.average().toFloat()
        val ci = verts.size
        verts += Vec3(cx, cy, p[0].z)
        val lastBase = base + sCount * n
        for (i in 0 until n) {
            faces += Face(ci, lastBase + (i + 1) % n, lastBase + i, partId, color)
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Audi Q3 2011 (8U) — low-poly procedural mesh matching the real car's
// proportions: L=4.39m, W=1.83m, H=1.58m, WB=2.60m.
// Coordinate origin at ground-centre of wheelbase.
// ─────────────────────────────────────────────────────────────────────────────
fun buildAudiQ3Mesh(): Mesh {
    val verts = mutableListOf<Vec3>()
    val faces = mutableListOf<Face>()

    // ── Palette ──────────────────────────────────────────────────────────
    val body      = Color(0xFFE8E8E6)
    val bodyDark  = Color(0xFFC0C0BE)
    val glass     = Color(0xFF1A2535)
    val pillar    = Color(0xFF1A1A1A)
    val tire      = Color(0xFF181818)
    val hub       = Color(0xFFCECECE)
    val grille    = Color(0xFF0C0C0C)
    val chrome    = Color(0xFFBEBEBE)
    val headL     = Color(0xFFF0F6FF)
    val tailR     = Color(0xFFCC1100)
    val roofRail  = Color(0xFFAAAAAA)

    // ── Key geometry ─────────────────────────────────────────────────────
    val fa   =  1.30f
    val ra   = -1.30f
    val wx   =  0.785f
    val wr   =  0.330f
    val ww   =  0.230f
    val belt =  0.87f
    val wtop =  1.37f
    val roof =  1.58f

    // ── 1. Body shell (profile-lofted smooth surface) ───────────────────
    val profiles = listOf(
        bodyProfile(2.20f, 0.22f,0.84f, 0.86f,0.35f, 0.88f, 0.65f,0.84f, 0.74f,0.72f, 0.76f,0.45f, 0.77f),
        bodyProfile(2.05f, 0.19f,0.88f, 0.90f,0.38f, 0.91f, 0.78f,0.90f, 0.87f,0.82f, 0.90f,0.55f, 0.92f),
        bodyProfile(1.60f, 0.19f,0.89f, 0.91f,0.40f, 0.92f, 0.84f,0.91f, 0.91f,0.85f, 0.94f,0.55f, 0.95f),
        bodyProfile(1.30f, 0.19f,0.90f, 0.94f,0.42f, 0.95f, 0.86f,0.92f, 0.94f,0.86f, 0.97f,0.55f, 0.98f),
        bodyProfile(0.52f, 0.19f,0.90f, 0.93f,0.42f, 0.93f, 0.87f,0.91f, 1.10f,0.86f, 1.38f,0.82f, 1.42f),
        bodyProfile(0.05f, 0.19f,0.90f, 0.93f,0.42f, 0.93f, 0.87f,0.91f, 1.20f,0.86f, 1.52f,0.82f, 1.58f),
        bodyProfile(-0.50f,0.19f,0.90f, 0.93f,0.42f, 0.93f, 0.87f,0.91f, 1.18f,0.86f, 1.50f,0.82f, 1.57f),
        bodyProfile(-1.00f,0.19f,0.90f, 0.93f,0.42f, 0.93f, 0.87f,0.91f, 1.15f,0.85f, 1.45f,0.80f, 1.53f),
        bodyProfile(-1.30f,0.19f,0.90f, 0.94f,0.42f, 0.95f, 0.87f,0.91f, 1.12f,0.85f, 1.42f,0.80f, 1.50f),
        bodyProfile(-1.70f,0.19f,0.89f, 0.92f,0.40f, 0.92f, 0.87f,0.90f, 1.08f,0.84f, 1.35f,0.78f, 1.44f),
        bodyProfile(-2.05f,0.22f,0.86f, 0.90f,0.40f, 0.90f, 0.85f,0.88f, 1.05f,0.82f, 1.28f,0.76f, 1.38f),
        bodyProfile(-2.20f,0.22f,0.83f, 0.86f,0.38f, 0.86f, 0.78f,0.84f, 0.82f,0.76f, 0.84f,0.50f, 0.86f),
    )
    addLoftedBody(verts, faces, profiles, 0, 4, PartId.HOOD, body, bodyDark, capFront = true)
    addLoftedBody(verts, faces, profiles, 4, 9, PartId.BODY, body, bodyDark)
    addLoftedBody(verts, faces, profiles, 9, 11, PartId.TRUNK, body, bodyDark, capBack = true)

    // ── 2. Grille ───────────────────────────────────────────────────────
    addBox(verts, faces, Vec3(0f, 0.50f, 2.22f),
        Vec3(1.30f, 0.32f, 0.04f), PartId.HOOD, grille, grille)
    addBox(verts, faces, Vec3(0f, 0.68f, 2.23f),
        Vec3(1.40f, 0.05f, 0.03f), PartId.HOOD, chrome, chrome)
    addBox(verts, faces, Vec3(0f, 0.35f, 2.23f),
        Vec3(1.40f, 0.05f, 0.03f), PartId.HOOD, chrome, chrome)

    // ── 3. Headlights ───────────────────────────────────────────────────
    for (sign in listOf(-1f, 1f)) {
        addBox(verts, faces, Vec3(sign * 0.76f, 0.62f, 2.18f),
            Vec3(0.22f, 0.28f, 0.06f), PartId.HEADLIGHT, headL, headL)
        addBox(verts, faces, Vec3(sign * 0.76f, 0.50f, 2.22f),
            Vec3(0.18f, 0.05f, 0.03f), PartId.HEADLIGHT, Color(0xFFFFFFEE), Color(0xFFFFFFEE))
    }

    // ── 4. Windshield ───────────────────────────────────────────────────
    val wsW = 1.60f
    addPrism(verts, faces,
        Vec3(-wsW/2f, 0.95f, 0.52f), Vec3(wsW/2f, 0.95f, 0.52f),
        Vec3( wsW/2f, wtop,  0.07f), Vec3(-wsW/2f, wtop,  0.07f),
        Vec3(-wsW/2f, 0.95f, 0.48f), Vec3(wsW/2f, 0.95f, 0.48f),
        Vec3( wsW/2f, wtop,  0.03f), Vec3(-wsW/2f, wtop,  0.03f),
        PartId.GLASS, glass, glass)

    // ── 5. B-pillars ────────────────────────────────────────────────────
    for (sign in listOf(-1f, 1f)) {
        addBox(verts, faces, Vec3(sign * 0.87f, (belt + wtop) / 2f, 0.05f),
            Vec3(0.06f, wtop - belt, 0.10f), PartId.CABIN, pillar, pillar)
    }

    // ── 6. Side glass — front door ──────────────────────────────────────
    for (sign in listOf(-1f, 1f)) {
        addBox(verts, faces, Vec3(sign * 0.88f, (belt + wtop) / 2f, 0.50f),
            Vec3(0.04f, wtop - belt - 0.04f, 0.80f), PartId.GLASS, glass, glass)
    }

    // ── 7. Side glass — rear door ───────────────────────────────────────
    for (sign in listOf(-1f, 1f)) {
        addBox(verts, faces, Vec3(sign * 0.88f, (belt + wtop) / 2f, -0.42f),
            Vec3(0.04f, wtop - belt - 0.04f, 0.74f), PartId.GLASS, glass, glass)
    }

    // ── 8. Quarter glass ────────────────────────────────────────────────
    for (sign in listOf(-1f, 1f)) {
        addBox(verts, faces, Vec3(sign * 0.87f, (belt + wtop) / 2f + 0.02f, -1.27f),
            Vec3(0.04f, wtop - belt - 0.18f, 0.46f), PartId.GLASS, glass, glass)
    }

    // ── 9. Rear window ──────────────────────────────────────────────────
    val rwW = 1.56f
    addPrism(verts, faces,
        Vec3(-rwW/2f, belt + 0.06f, -2.04f), Vec3(rwW/2f, belt + 0.06f, -2.04f),
        Vec3( rwW/2f, wtop - 0.02f, -1.65f), Vec3(-rwW/2f, wtop - 0.02f, -1.65f),
        Vec3(-rwW/2f, belt + 0.06f, -2.08f), Vec3(rwW/2f, belt + 0.06f, -2.08f),
        Vec3( rwW/2f, wtop - 0.02f, -1.69f), Vec3(-rwW/2f, wtop - 0.02f, -1.69f),
        PartId.GLASS, glass, glass)

    // ── 10. Taillights ──────────────────────────────────────────────────
    for (sign in listOf(-1f, 1f)) {
        addBox(verts, faces, Vec3(sign * 0.58f, 0.72f, -2.18f),
            Vec3(0.52f, 0.28f, 0.06f), PartId.TAILLIGHT, tailR, tailR)
        addBox(verts, faces, Vec3(sign * 0.24f, 0.72f, -2.19f),
            Vec3(0.20f, 0.22f, 0.04f), PartId.TAILLIGHT, Color(0xFFFF5500), Color(0xFFFF5500))
    }

    // ── 11. Roof rails ──────────────────────────────────────────────────
    for (sign in listOf(-1f, 1f)) {
        addBox(verts, faces, Vec3(sign * 0.75f, roof + 0.03f, -0.35f),
            Vec3(0.04f, 0.04f, 1.90f), PartId.CABIN, roofRail, roofRail)
    }

    // ── INTERNAL COMPONENTS — EA888 Gen 2 (CDNC) 2.0 TFSI ─────────────
    // Engine is transverse-mounted. Intake side faces front (+Z), exhaust
    // side faces firewall (-Z).  All positions based on real Q3 8U layout.

    val engineCol   = Color(0xFF708090)
    val headCol     = Color(0xFF7A8A9A)
    val oilFiltCol  = Color(0xFFFF9800)
    val airFiltCol  = Color(0xFF4CAF50)
    val turboCol    = Color(0xFF00BCD4)
    val interCol    = Color(0xFFB0BEC5)
    val radCol      = Color(0xFF2196F3)
    val exhaustCol  = Color(0xFFE64A19)
    val battCol     = Color(0xFFFFC107)
    val coolantCol  = Color(0xFF29B6F6)
    val transCol    = Color(0xFF546E7A)
    val catCol      = Color(0xFF8D6E63)
    val oilPanCol   = Color(0xFF455A64)
    val altCol      = Color(0xFF78909C)
    val startCol    = Color(0xFFBF8040)
    val brakeCol    = Color(0xFFF44336)
    val shaftCol    = Color(0xFF9E9E9E)

    // 25. Engine block (transverse I4, ~60cm long x ~40cm deep x ~35cm tall)
    addBox(verts, faces, Vec3(0.05f, 0.42f, 1.15f),
        Vec3(0.80f, 0.40f, 0.38f), PartId.ENGINE_BLOCK, engineCol, engineCol)

    // 26. Cylinder head + valve cover (sits on top of block)
    addBox(verts, faces, Vec3(0.05f, 0.68f, 1.15f),
        Vec3(0.75f, 0.14f, 0.34f), PartId.ENGINE_BLOCK, headCol, headCol)

    // 27. Intake manifold (front-top of head, facing radiator)
    addBox(verts, faces, Vec3(0.05f, 0.72f, 1.42f),
        Vec3(0.60f, 0.12f, 0.16f), PartId.ENGINE_BLOCK, Color(0xFF607D8B), Color(0xFF607D8B))

    // 28. Oil filter housing (top-front of engine, slightly right of centre)
    // EA888 has a cartridge filter in a vertical housing, accessed from above
    addVerticalCylinder(verts, faces, Vec3(0.28f, 0.72f, 1.34f),
        0.045f, 0.14f, 10, PartId.OIL_FILTER, oilFiltCol, oilFiltCol)

    // 29. Oil pan (below engine block, extends slightly further back)
    addBox(verts, faces, Vec3(0.05f, 0.19f, 1.10f),
        Vec3(0.72f, 0.08f, 0.44f), PartId.OIL_PAN, oilPanCol, oilPanCol)

    // 30. Oil drain plug (small indicator at bottom-rear of oil pan)
    addBox(verts, faces, Vec3(0.15f, 0.14f, 0.95f),
        Vec3(0.06f, 0.04f, 0.06f), PartId.OIL_PAN, oilFiltCol, oilFiltCol)

    // 31. Air filter box (passenger side, large rectangular box)
    addBox(verts, faces, Vec3(0.58f, 0.72f, 1.28f),
        Vec3(0.36f, 0.20f, 0.32f), PartId.AIR_FILTER, airFiltCol, airFiltCol)
    // Air intake duct from filter box to turbo
    addBox(verts, faces, Vec3(0.30f, 0.68f, 1.00f),
        Vec3(0.20f, 0.10f, 0.12f), PartId.AIR_FILTER, Color(0xFF388E3C), Color(0xFF388E3C))

    // 32. Turbocharger K03 (exhaust side, rear-bottom of engine near firewall)
    addBox(verts, faces, Vec3(0.08f, 0.38f, 0.82f),
        Vec3(0.18f, 0.16f, 0.16f), PartId.TURBO, turboCol, turboCol)
    // Wastegate actuator
    addBox(verts, faces, Vec3(-0.04f, 0.44f, 0.78f),
        Vec3(0.06f, 0.08f, 0.06f), PartId.TURBO, Color(0xFF0097A7), Color(0xFF0097A7))

    // 33. Exhaust manifold (integrated with turbo, 4 runners merge into turbo)
    addBox(verts, faces, Vec3(0.05f, 0.36f, 0.88f),
        Vec3(0.55f, 0.08f, 0.06f), PartId.EXHAUST_MANIFOLD, exhaustCol, exhaustCol)
    // Downpipe from turbo
    addBox(verts, faces, Vec3(0.08f, 0.28f, 0.72f),
        Vec3(0.10f, 0.12f, 0.10f), PartId.EXHAUST_MANIFOLD, Color(0xFFBF360C), Color(0xFFBF360C))

    // 34. Catalytic converter (below, after turbo/downpipe)
    addBox(verts, faces, Vec3(0.08f, 0.16f, 0.58f),
        Vec3(0.14f, 0.10f, 0.28f), PartId.CATALYTIC_CONVERTER, catCol, catCol)

    // 35. Intercooler (front-mounted, wide and thin behind bumper)
    addBox(verts, faces, Vec3(0.0f, 0.44f, 1.98f),
        Vec3(1.10f, 0.18f, 0.05f), PartId.INTERCOOLER, interCol, interCol)
    // Charge pipe from intercooler to intake manifold
    addBox(verts, faces, Vec3(0.30f, 0.54f, 1.70f),
        Vec3(0.08f, 0.08f, 0.30f), PartId.INTERCOOLER, Color(0xFF90A4AE), Color(0xFF90A4AE))

    // 36. Radiator (main cooling, behind grille above intercooler)
    addBox(verts, faces, Vec3(0.0f, 0.62f, 1.96f),
        Vec3(1.05f, 0.30f, 0.04f), PartId.RADIATOR, radCol, radCol)
    // Radiator hoses (upper and lower)
    addBox(verts, faces, Vec3(-0.30f, 0.74f, 1.60f),
        Vec3(0.06f, 0.06f, 0.40f), PartId.COOLANT_TANK, coolantCol, coolantCol)
    addBox(verts, faces, Vec3(0.30f, 0.44f, 1.60f),
        Vec3(0.06f, 0.06f, 0.40f), PartId.COOLANT_TANK, coolantCol, coolantCol)

    // 37. Coolant expansion tank (high, left side of engine bay)
    addBox(verts, faces, Vec3(-0.52f, 0.80f, 0.92f),
        Vec3(0.12f, 0.14f, 0.10f), PartId.COOLANT_TANK, coolantCol, coolantCol)

    // 38. Transmission (6-speed DSG, bolted to left side of engine block)
    addBox(verts, faces, Vec3(-0.48f, 0.38f, 1.15f),
        Vec3(0.32f, 0.34f, 0.36f), PartId.TRANSMISSION, transCol, transCol)

    // 39. Battery (right-front of engine bay on Q3 8U)
    addBox(verts, faces, Vec3(-0.58f, 0.62f, 1.40f),
        Vec3(0.24f, 0.18f, 0.18f), PartId.BATTERY, battCol, battCol)
    // Battery terminals
    addBox(verts, faces, Vec3(-0.52f, 0.72f, 1.42f),
        Vec3(0.06f, 0.02f, 0.04f), PartId.BATTERY, Color(0xFFFF8F00), Color(0xFFFF8F00))

    // 40. Alternator (front-left of engine, belt-driven)
    addVerticalCylinder(verts, faces, Vec3(-0.30f, 0.40f, 1.38f),
        0.06f, 0.10f, 8, PartId.ALTERNATOR, altCol, altCol)

    // 41. Starter motor (rear-bottom of engine, near transmission bell housing)
    addVerticalCylinder(verts, faces, Vec3(-0.32f, 0.28f, 0.96f),
        0.05f, 0.12f, 8, PartId.STARTER, startCol, startCol)

    // 42. Brake calipers (at each wheel, behind rim)
    for ((xSign, zPos) in listOf(
        Pair(-1f, fa), Pair(1f, fa), Pair(-1f, ra), Pair(1f, ra),
    )) {
        addBox(verts, faces, Vec3(xSign * (wx - 0.08f), wr + 0.06f, zPos),
            Vec3(0.08f, 0.14f, 0.12f), PartId.BRAKE_CALIPER, brakeCol, brakeCol)
    }

    // 43. Front drive shafts (from transmission to front wheels)
    addBox(verts, faces, Vec3(-0.65f, 0.26f, fa),
        Vec3(0.40f, 0.04f, 0.04f), PartId.DRIVE_SHAFT, shaftCol, shaftCol)
    addBox(verts, faces, Vec3(0.55f, 0.26f, fa),
        Vec3(0.50f, 0.04f, 0.04f), PartId.DRIVE_SHAFT, shaftCol, shaftCol)

    // ── 12. Wheels (16 segments for smooth look) ──────────────────────────
    for ((xSign, zPos, pid) in listOf(
        Triple(-1f, fa, PartId.WHEEL_FL), Triple(1f, fa, PartId.WHEEL_FR),
        Triple(-1f, ra, PartId.WHEEL_RL), Triple(1f, ra, PartId.WHEEL_RR),
    )) {
        addCylinder(verts, faces, Vec3(xSign * wx, wr, zPos), wr, ww, 16, pid, tire, hub)
    }

    return Mesh(verts, faces)
}

// ─────────────────────────────────────────────────────────────────────────────
// Sedan — Toyota Camry-like proportions: L≈4.88m W≈1.84m H≈1.45m WB≈2.82m
// ─────────────────────────────────────────────────────────────────────────────
fun buildSedanMesh(): Mesh {
    val verts = mutableListOf<Vec3>()
    val faces = mutableListOf<Face>()

    val body     = Color(0xFFCCCCCC)
    val bodyDark = Color(0xFFAAAAAA)
    val glass    = Color(0xFF1A2535)
    val pillar   = Color(0xFF1A1A1A)
    val tire     = Color(0xFF181818)
    val hub      = Color(0xFFBBBBBB)
    val grille   = Color(0xFF1A1A1A)
    val chrome   = Color(0xFFCCCCCC)
    val headL    = Color(0xFFF0F6FF)
    val tailR    = Color(0xFFCC1100)
    val sill     = Color(0xFF5A5A5A)

    val hw   = 0.92f
    val fa   = 1.41f
    val ra   = -1.41f
    val wx   = 0.78f
    val wr   = 0.31f
    val ww   = 0.21f
    val bb   = 0.17f
    val belt = 0.78f
    val wtop = 1.28f
    val roof = 1.45f

    // 1. Lower body
    addBox(verts, faces, Vec3(0f, (bb + belt) / 2f, 0f),
        Vec3(hw * 2f, belt - bb, 4.84f), PartId.BODY, body, bodyDark)

    // 2. Front bumper
    addBox(verts, faces, Vec3(0f, 0.40f, 2.38f),
        Vec3(hw * 2f, 0.46f, 0.10f), PartId.BODY, body, bodyDark)

    // 3. Grille
    addBox(verts, faces, Vec3(0f, 0.50f, 2.40f),
        Vec3(1.20f, 0.30f, 0.05f), PartId.HOOD, grille, grille)
    addBox(verts, faces, Vec3(0f, 0.66f, 2.42f),
        Vec3(1.30f, 0.04f, 0.04f), PartId.HOOD, chrome, chrome)

    // 4. Headlights
    for (sign in listOf(-1f, 1f)) {
        addBox(verts, faces, Vec3(sign * 0.74f, 0.62f, 2.36f),
            Vec3(0.30f, 0.22f, 0.10f), PartId.HEADLIGHT, headL, headL)
    }

    // 5. Hood (gentle slope)
    val hdW = 1.72f
    addPrism(verts, faces,
        Vec3(-hdW/2f, belt, 2.32f),   Vec3(hdW/2f, belt, 2.32f),
        Vec3(hdW/2f, 0.82f, 2.32f),   Vec3(-hdW/2f, 0.82f, 2.32f),
        Vec3(-hdW/2f, belt, 0.60f),    Vec3(hdW/2f, belt, 0.60f),
        Vec3(hdW/2f, 0.86f, 0.60f),    Vec3(-hdW/2f, 0.86f, 0.60f),
        PartId.HOOD, body, bodyDark)

    // 6. Windshield
    val wsW = 1.60f
    addPrism(verts, faces,
        Vec3(-wsW/2f, 0.86f, 0.60f),  Vec3(wsW/2f, 0.86f, 0.60f),
        Vec3(wsW/2f, wtop, 0.10f),     Vec3(-wsW/2f, wtop, 0.10f),
        Vec3(-wsW/2f, 0.86f, 0.56f),  Vec3(wsW/2f, 0.86f, 0.56f),
        Vec3(wsW/2f, wtop, 0.06f),     Vec3(-wsW/2f, wtop, 0.06f),
        PartId.GLASS, glass, glass)

    // 7. Roof
    addBox(verts, faces, Vec3(0f, (wtop + roof) / 2f, -0.50f),
        Vec3(1.62f, roof - wtop, 1.20f), PartId.CABIN, body, bodyDark)

    // 8. A/B/C pillars
    for (sign in listOf(-1f, 1f)) {
        addBox(verts, faces, Vec3(sign * (hw - 0.06f), (belt + wtop) / 2f, 0.35f),
            Vec3(0.08f, wtop - belt, 0.40f), PartId.CABIN, pillar, pillar)
        addBox(verts, faces, Vec3(sign * (hw - 0.06f), (belt + wtop) / 2f, -0.10f),
            Vec3(0.08f, wtop - belt, 0.10f), PartId.CABIN, pillar, pillar)
        addBox(verts, faces, Vec3(sign * (hw - 0.06f), (belt + wtop) / 2f, -1.00f),
            Vec3(0.08f, wtop - belt, 0.10f), PartId.CABIN, pillar, pillar)
    }

    // 9. Side glass
    for (sign in listOf(-1f, 1f)) {
        addBox(verts, faces, Vec3(sign * hw, (belt + wtop) / 2f, 0.57f),
            Vec3(0.04f, wtop - belt - 0.04f, 0.80f), PartId.GLASS, glass, glass)
        addBox(verts, faces, Vec3(sign * hw, (belt + wtop) / 2f, -0.50f),
            Vec3(0.04f, wtop - belt - 0.04f, 0.80f), PartId.GLASS, glass, glass)
    }

    // 10. Rear window
    val rwW = 1.56f
    addPrism(verts, faces,
        Vec3(-rwW/2f, belt + 0.04f, -1.78f), Vec3(rwW/2f, belt + 0.04f, -1.78f),
        Vec3(rwW/2f, wtop - 0.02f, -1.12f),  Vec3(-rwW/2f, wtop - 0.02f, -1.12f),
        Vec3(-rwW/2f, belt + 0.04f, -1.82f), Vec3(rwW/2f, belt + 0.04f, -1.82f),
        Vec3(rwW/2f, wtop - 0.02f, -1.16f),  Vec3(-rwW/2f, wtop - 0.02f, -1.16f),
        PartId.GLASS, glass, glass)

    // 11. Trunk lid (slopes down)
    addPrism(verts, faces,
        Vec3(-0.82f, belt, -1.82f),   Vec3(0.82f, belt, -1.82f),
        Vec3(0.82f, 0.72f, -1.82f),   Vec3(-0.82f, 0.72f, -1.82f),
        Vec3(-0.82f, belt, -2.40f),   Vec3(0.82f, belt, -2.40f),
        Vec3(0.82f, 0.58f, -2.40f),   Vec3(-0.82f, 0.58f, -2.40f),
        PartId.TRUNK, body, bodyDark)

    // 12. Trunk rear panel
    addBox(verts, faces, Vec3(0f, (bb + 0.58f) / 2f, -2.40f),
        Vec3(1.64f, 0.58f - bb, 0.06f), PartId.TRUNK, body, bodyDark)

    // 13. Taillights
    for (sign in listOf(-1f, 1f)) {
        addBox(verts, faces, Vec3(sign * 0.62f, 0.60f, -2.38f),
            Vec3(0.50f, 0.22f, 0.10f), PartId.TAILLIGHT, tailR, tailR)
    }

    // 14. Rear bumper
    addBox(verts, faces, Vec3(0f, 0.29f, -2.40f),
        Vec3(hw * 2f, 0.24f, 0.08f), PartId.TRUNK, body, bodyDark)

    // 15. Side sills
    for (sign in listOf(-1f, 1f)) {
        addBox(verts, faces, Vec3(sign * (hw + 0.01f), 0.25f, 0f),
            Vec3(0.05f, 0.14f, 3.80f), PartId.BODY, sill, bodyDark)
    }

    // 16. Wheels (10-segment)
    for ((xSign, zPos, pid) in listOf(
        Triple(-1f, fa, PartId.WHEEL_FL), Triple(1f, fa, PartId.WHEEL_FR),
        Triple(-1f, ra, PartId.WHEEL_RL), Triple(1f, ra, PartId.WHEEL_RR),
    )) {
        addCylinder(verts, faces, Vec3(xSign * wx, wr, zPos), wr, ww, 10, pid, tire, hub)
    }

    return Mesh(verts, faces)
}

// ─────────────────────────────────────────────────────────────────────────────
// Hatchback — VW Golf-like proportions: L≈4.26m W≈1.80m H≈1.45m WB≈2.63m
// ─────────────────────────────────────────────────────────────────────────────
fun buildHatchbackMesh(): Mesh {
    val verts = mutableListOf<Vec3>()
    val faces = mutableListOf<Face>()

    val body     = Color(0xFFB0B0B0)
    val bodyDark = Color(0xFF8A8A8A)
    val glass    = Color(0xFF1A2535)
    val pillar   = Color(0xFF1A1A1A)
    val tire     = Color(0xFF181818)
    val hub      = Color(0xFFBBBBBB)
    val grille   = Color(0xFF1A1A1A)
    val headL    = Color(0xFFF0F6FF)
    val tailR    = Color(0xFFCC1100)
    val sill     = Color(0xFF5A5A5A)

    val hw   = 0.90f
    val fa   = 1.31f
    val ra   = -1.31f
    val wx   = 0.77f
    val wr   = 0.31f
    val ww   = 0.21f
    val bb   = 0.17f
    val belt = 0.78f
    val wtop = 1.28f
    val roof = 1.45f

    // 1. Lower body (shorter than sedan)
    addBox(verts, faces, Vec3(0f, (bb + belt) / 2f, 0.10f),
        Vec3(hw * 2f, belt - bb, 4.22f), PartId.BODY, body, bodyDark)

    // 2. Front bumper
    addBox(verts, faces, Vec3(0f, 0.40f, 2.18f),
        Vec3(hw * 2f, 0.46f, 0.10f), PartId.BODY, body, bodyDark)

    // 3. Grille
    addBox(verts, faces, Vec3(0f, 0.50f, 2.20f),
        Vec3(1.30f, 0.26f, 0.05f), PartId.HOOD, grille, grille)

    // 4. Headlights
    for (sign in listOf(-1f, 1f)) {
        addBox(verts, faces, Vec3(sign * 0.72f, 0.62f, 2.16f),
            Vec3(0.28f, 0.22f, 0.10f), PartId.HEADLIGHT, headL, headL)
    }

    // 5. Hood
    val hdW = 1.68f
    addPrism(verts, faces,
        Vec3(-hdW/2f, belt, 2.12f),   Vec3(hdW/2f, belt, 2.12f),
        Vec3(hdW/2f, 0.82f, 2.12f),   Vec3(-hdW/2f, 0.82f, 2.12f),
        Vec3(-hdW/2f, belt, 0.55f),    Vec3(hdW/2f, belt, 0.55f),
        Vec3(hdW/2f, 0.86f, 0.55f),    Vec3(-hdW/2f, 0.86f, 0.55f),
        PartId.HOOD, body, bodyDark)

    // 6. Windshield
    val wsW = 1.58f
    addPrism(verts, faces,
        Vec3(-wsW/2f, 0.86f, 0.55f),  Vec3(wsW/2f, 0.86f, 0.55f),
        Vec3(wsW/2f, wtop, 0.08f),     Vec3(-wsW/2f, wtop, 0.08f),
        Vec3(-wsW/2f, 0.86f, 0.51f),  Vec3(wsW/2f, 0.86f, 0.51f),
        Vec3(wsW/2f, wtop, 0.04f),     Vec3(-wsW/2f, wtop, 0.04f),
        PartId.GLASS, glass, glass)

    // 7. Roof (shorter than sedan)
    addBox(verts, faces, Vec3(0f, (wtop + roof) / 2f, -0.42f),
        Vec3(1.60f, roof - wtop, 1.00f), PartId.CABIN, body, bodyDark)

    // 8. A/B pillars
    for (sign in listOf(-1f, 1f)) {
        addBox(verts, faces, Vec3(sign * (hw - 0.06f), (belt + wtop) / 2f, 0.31f),
            Vec3(0.08f, wtop - belt, 0.38f), PartId.CABIN, pillar, pillar)
        addBox(verts, faces, Vec3(sign * (hw - 0.06f), (belt + wtop) / 2f, -0.12f),
            Vec3(0.08f, wtop - belt, 0.10f), PartId.CABIN, pillar, pillar)
    }

    // 9. Side glass
    for (sign in listOf(-1f, 1f)) {
        addBox(verts, faces, Vec3(sign * hw, (belt + wtop) / 2f, 0.52f),
            Vec3(0.04f, wtop - belt - 0.04f, 0.76f), PartId.GLASS, glass, glass)
        addBox(verts, faces, Vec3(sign * hw, (belt + wtop) / 2f, -0.42f),
            Vec3(0.04f, wtop - belt - 0.04f, 0.50f), PartId.GLASS, glass, glass)
    }

    // 10. Rear window (steep ~70°)
    val rwW = 1.54f
    addPrism(verts, faces,
        Vec3(-rwW/2f, belt + 0.04f, -2.00f), Vec3(rwW/2f, belt + 0.04f, -2.00f),
        Vec3(rwW/2f, wtop - 0.02f, -0.94f),  Vec3(-rwW/2f, wtop - 0.02f, -0.94f),
        Vec3(-rwW/2f, belt + 0.04f, -2.04f), Vec3(rwW/2f, belt + 0.04f, -2.04f),
        Vec3(rwW/2f, wtop - 0.02f, -0.98f),  Vec3(-rwW/2f, wtop - 0.02f, -0.98f),
        PartId.GLASS, glass, glass)

    // 11. Rear hatch panel (short, nearly vertical)
    addBox(verts, faces, Vec3(0f, (bb + belt + 0.04f) / 2f, -2.06f),
        Vec3(1.60f, belt + 0.04f - bb, 0.08f), PartId.TRUNK, body, bodyDark)

    // 12. Taillights
    for (sign in listOf(-1f, 1f)) {
        addBox(verts, faces, Vec3(sign * 0.58f, 0.68f, -2.04f),
            Vec3(0.48f, 0.30f, 0.10f), PartId.TAILLIGHT, tailR, tailR)
    }

    // 13. Rear bumper
    addBox(verts, faces, Vec3(0f, 0.28f, -2.06f),
        Vec3(hw * 2f, 0.22f, 0.08f), PartId.TRUNK, body, bodyDark)

    // 14. Side sills
    for (sign in listOf(-1f, 1f)) {
        addBox(verts, faces, Vec3(sign * (hw + 0.01f), 0.25f, 0.10f),
            Vec3(0.05f, 0.14f, 3.40f), PartId.BODY, sill, bodyDark)
    }

    // 15. Wheels (10-segment)
    for ((xSign, zPos, pid) in listOf(
        Triple(-1f, fa, PartId.WHEEL_FL), Triple(1f, fa, PartId.WHEEL_FR),
        Triple(-1f, ra, PartId.WHEEL_RL), Triple(1f, ra, PartId.WHEEL_RR),
    )) {
        addCylinder(verts, faces, Vec3(xSign * wx, wr, zPos), wr, ww, 10, pid, tire, hub)
    }

    return Mesh(verts, faces)
}

// ─────────────────────────────────────────────────────────────────────────────
// SUV — BMW X5-like proportions: L≈4.92m W≈2.00m H≈1.75m WB≈2.97m
// ─────────────────────────────────────────────────────────────────────────────
fun buildSUVMesh(): Mesh {
    val verts = mutableListOf<Vec3>()
    val faces = mutableListOf<Face>()

    val body     = Color(0xFFF5F5F5)
    val bodyDark = Color(0xFFD0D0D0)
    val glass    = Color(0xFF1A2535)
    val pillar   = Color(0xFF1A1A1A)
    val tire     = Color(0xFF181818)
    val hub      = Color(0xFFCCCCCC)
    val grille   = Color(0xFF0A0A0A)
    val chrome   = Color(0xFFBEBEBE)
    val headL    = Color(0xFFF0F6FF)
    val tailR    = Color(0xFFCC1100)
    val sill     = Color(0xFF5A5A5A)
    val roofRail = Color(0xFFAAAAAA)

    val hw   = 1.00f
    val fa   = 1.49f
    val ra   = -1.49f
    val wx   = 0.85f
    val wr   = 0.35f
    val ww   = 0.25f
    val bb   = 0.25f
    val belt = 0.95f
    val wtop = 1.52f
    val roof = 1.75f

    // 1. Lower body (tall, wide)
    addBox(verts, faces, Vec3(0f, (bb + belt) / 2f, 0f),
        Vec3(hw * 2f, belt - bb, 4.88f), PartId.BODY, body, bodyDark)

    // 2. Front bumper
    addBox(verts, faces, Vec3(0f, 0.50f, 2.40f),
        Vec3(hw * 2f, 0.50f, 0.10f), PartId.BODY, body, bodyDark)

    // 3. BMW-style kidney grilles (two separate dark boxes)
    addBox(verts, faces, Vec3(-0.28f, 0.70f, 2.44f),
        Vec3(0.42f, 0.34f, 0.05f), PartId.HOOD, grille, grille)
    addBox(verts, faces, Vec3(0.28f, 0.70f, 2.44f),
        Vec3(0.42f, 0.34f, 0.05f), PartId.HOOD, grille, grille)
    addBox(verts, faces, Vec3(0f, 0.88f, 2.46f),
        Vec3(1.00f, 0.04f, 0.04f), PartId.HOOD, chrome, chrome)

    // 4. Headlights
    for (sign in listOf(-1f, 1f)) {
        addBox(verts, faces, Vec3(sign * 0.80f, 0.82f, 2.38f),
            Vec3(0.30f, 0.20f, 0.10f), PartId.HEADLIGHT, headL, headL)
    }

    // 5. Hood
    val hdW = 1.88f
    addPrism(verts, faces,
        Vec3(-hdW/2f, belt, 2.34f),   Vec3(hdW/2f, belt, 2.34f),
        Vec3(hdW/2f, 1.00f, 2.34f),   Vec3(-hdW/2f, 1.00f, 2.34f),
        Vec3(-hdW/2f, belt, 0.60f),    Vec3(hdW/2f, belt, 0.60f),
        Vec3(hdW/2f, 1.05f, 0.60f),    Vec3(-hdW/2f, 1.05f, 0.60f),
        PartId.HOOD, body, bodyDark)

    // 6. Windshield
    val wsW = 1.78f
    addPrism(verts, faces,
        Vec3(-wsW/2f, 1.05f, 0.60f),  Vec3(wsW/2f, 1.05f, 0.60f),
        Vec3(wsW/2f, wtop, 0.10f),     Vec3(-wsW/2f, wtop, 0.10f),
        Vec3(-wsW/2f, 1.05f, 0.56f),  Vec3(wsW/2f, 1.05f, 0.56f),
        Vec3(wsW/2f, wtop, 0.06f),     Vec3(-wsW/2f, wtop, 0.06f),
        PartId.GLASS, glass, glass)

    // 7. Roof
    val roofMidZ = (0.10f + (-1.80f)) / 2f
    val roofLen  = 0.10f - (-1.80f)
    addBox(verts, faces, Vec3(0f, (wtop + roof) / 2f, roofMidZ),
        Vec3(1.82f, roof - wtop, roofLen), PartId.CABIN, body, bodyDark)

    // 8. A/B/C/D pillars
    for (sign in listOf(-1f, 1f)) {
        addBox(verts, faces, Vec3(sign * (hw - 0.06f), (belt + wtop) / 2f, 0.35f),
            Vec3(0.08f, wtop - belt, 0.42f), PartId.CABIN, body, bodyDark)
        addBox(verts, faces, Vec3(sign * (hw - 0.06f), (belt + wtop) / 2f, -0.05f),
            Vec3(0.08f, wtop - belt, 0.10f), PartId.CABIN, pillar, pillar)
        addBox(verts, faces, Vec3(sign * (hw - 0.06f), (belt + wtop) / 2f, -0.98f),
            Vec3(0.08f, wtop - belt, 0.10f), PartId.CABIN, body, bodyDark)
        addBox(verts, faces, Vec3(sign * (hw - 0.06f), (belt + roof) / 2f, -1.78f),
            Vec3(0.10f, roof - belt + 0.04f, 0.12f), PartId.CABIN, body, bodyDark)
    }

    // 9. Side glass
    for (sign in listOf(-1f, 1f)) {
        addBox(verts, faces, Vec3(sign * hw, (belt + wtop) / 2f, 0.58f),
            Vec3(0.04f, wtop - belt - 0.04f, 0.86f), PartId.GLASS, glass, glass)
        addBox(verts, faces, Vec3(sign * hw, (belt + wtop) / 2f, -0.48f),
            Vec3(0.04f, wtop - belt - 0.04f, 0.80f), PartId.GLASS, glass, glass)
        addBox(verts, faces, Vec3(sign * hw, (belt + wtop) / 2f + 0.02f, -1.35f),
            Vec3(0.04f, wtop - belt - 0.14f, 0.50f), PartId.GLASS, glass, glass)
    }

    // 10. Rear window
    val rwW = 1.74f
    addPrism(verts, faces,
        Vec3(-rwW/2f, belt + 0.06f, -2.24f), Vec3(rwW/2f, belt + 0.06f, -2.24f),
        Vec3(rwW/2f, wtop - 0.02f, -1.82f),  Vec3(-rwW/2f, wtop - 0.02f, -1.82f),
        Vec3(-rwW/2f, belt + 0.06f, -2.28f), Vec3(rwW/2f, belt + 0.06f, -2.28f),
        Vec3(rwW/2f, wtop - 0.02f, -1.86f),  Vec3(-rwW/2f, wtop - 0.02f, -1.86f),
        PartId.GLASS, glass, glass)

    // 11. Rear hatch
    addBox(verts, faces, Vec3(0f, (wtop + roof) / 2f, -2.30f),
        Vec3(1.84f, roof - wtop + 0.04f, 0.08f), PartId.TRUNK, body, bodyDark)
    addBox(verts, faces, Vec3(0f, (bb + belt + 0.06f) / 2f, -2.36f),
        Vec3(1.84f, belt + 0.06f - bb, 0.08f), PartId.TRUNK, body, bodyDark)

    // 12. Taillights
    for (sign in listOf(-1f, 1f)) {
        addBox(verts, faces, Vec3(sign * 0.65f, 0.90f, -2.34f),
            Vec3(0.58f, 0.34f, 0.10f), PartId.TAILLIGHT, tailR, tailR)
    }

    // 13. Rear bumper
    addBox(verts, faces, Vec3(0f, 0.38f, -2.36f),
        Vec3(hw * 2f, 0.26f, 0.08f), PartId.TRUNK, body, bodyDark)

    // 14. Side sills
    for (sign in listOf(-1f, 1f)) {
        addBox(verts, faces, Vec3(sign * (hw + 0.01f), 0.33f, 0f),
            Vec3(0.06f, 0.16f, 3.90f), PartId.BODY, sill, bodyDark)
    }

    // 15. Fender flares
    for (sign in listOf(-1f, 1f)) {
        addBox(verts, faces, Vec3(sign * (hw + 0.02f), 0.56f, fa),
            Vec3(0.07f, 0.62f, 1.06f), PartId.BODY, body, bodyDark)
        addBox(verts, faces, Vec3(sign * (hw + 0.02f), 0.56f, ra),
            Vec3(0.07f, 0.62f, 1.06f), PartId.BODY, body, bodyDark)
    }

    // 16. Roof rails
    for (sign in listOf(-1f, 1f)) {
        addBox(verts, faces, Vec3(sign * 0.80f, roof + 0.035f, roofMidZ),
            Vec3(0.04f, 0.04f, roofLen - 0.30f), PartId.CABIN, roofRail, roofRail)
    }

    // 17. Wheels (12-segment, larger)
    for ((xSign, zPos, pid) in listOf(
        Triple(-1f, fa, PartId.WHEEL_FL), Triple(1f, fa, PartId.WHEEL_FR),
        Triple(-1f, ra, PartId.WHEEL_RL), Triple(1f, ra, PartId.WHEEL_RR),
    )) {
        addCylinder(verts, faces, Vec3(xSign * wx, wr, zPos), wr, ww, 12, pid, tire, hub)
    }

    return Mesh(verts, faces)
}

// ─────────────────────────────────────────────────────────────────────────────
// Factory: returns the appropriate Mesh for a given car name string.
// ─────────────────────────────────────────────────────────────────────────────
fun buildMeshForCar(carName: String): Mesh = when {
    carName.contains("Audi Q3", ignoreCase = true) -> buildAudiQ3Mesh()
    carName.contains("BMW", ignoreCase = true) -> buildSUVMesh()
    carName.contains("Mercedes", ignoreCase = true) -> buildSedanMesh()
    carName.contains("Toyota", ignoreCase = true) -> buildSedanMesh()
    carName.contains("Volkswagen", ignoreCase = true) || carName.contains("VW", ignoreCase = true) -> buildHatchbackMesh()
    carName.contains("Golf", ignoreCase = true) -> buildHatchbackMesh()
    carName.contains("Lada", ignoreCase = true) || carName.contains("\u0412\u0410\u0417", ignoreCase = true) -> buildSedanMesh()
    carName.contains("Hyundai", ignoreCase = true) || carName.contains("Kia", ignoreCase = true) -> buildSedanMesh()
    carName.contains("Skoda", ignoreCase = true) -> buildHatchbackMesh()
    carName.contains("Renault", ignoreCase = true) -> buildHatchbackMesh()
    carName.contains("Mazda", ignoreCase = true) -> buildSedanMesh()
    carName.contains("Honda", ignoreCase = true) -> buildSedanMesh()
    carName.contains("Nissan", ignoreCase = true) -> buildSUVMesh()
    carName.contains("Mitsubishi", ignoreCase = true) -> buildSUVMesh()
    carName.contains("Ford", ignoreCase = true) -> buildSUVMesh()
    carName.contains("Chevrolet", ignoreCase = true) -> buildSUVMesh()
    carName.contains("Lexus", ignoreCase = true) -> buildSUVMesh()
    carName.contains("Subaru", ignoreCase = true) -> buildSUVMesh()
    else -> buildCarMesh()
}
