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

// ─────────────────────────────────────────────────────────────────────────────
// Audi Q3 2011 (8U) — low-poly procedural mesh matching the real car's
// proportions: L=4.39m, W=1.83m, H=1.58m, WB=2.60m.
// Coordinate origin at ground-centre of wheelbase.
// ─────────────────────────────────────────────────────────────────────────────
fun buildAudiQ3Mesh(): Mesh {
    val verts = mutableListOf<Vec3>()
    val faces = mutableListOf<Face>()

    // ── Palette ──────────────────────────────────────────────────────────
    val body      = Color(0xFFF2F2F0)  // glacier white metallic
    val bodyDark  = Color(0xFFCCCCCA)
    val bump      = Color(0xFFE0E0DE)  // bumper plastic
    val glass     = Color(0xFF1A2535)
    val pillar    = Color(0xFF1A1A1A)  // black A/B/C-pillars
    val tire      = Color(0xFF181818)
    val hub       = Color(0xFFCECECE)  // silver 18" alloys
    val grille    = Color(0xFF0C0C0C)
    val chrome    = Color(0xFFBEBEBE)
    val headL     = Color(0xFFF0F6FF)
    val tailR     = Color(0xFFCC1100)
    val sill      = Color(0xFF5A5A5A)
    val roofRail  = Color(0xFFAAAAAA)

    // ── Key geometry ─────────────────────────────────────────────────────
    val hw   = 0.915f   // half-width
    val fa   =  1.30f   // front axle Z
    val ra   = -1.30f   // rear axle Z
    val wx   =  0.785f  // wheel-centre X offset
    val wr   =  0.330f  // wheel radius (18" ≈ 660 mm diam)
    val ww   =  0.230f  // wheel width
    val bb   =  0.19f   // body bottom Y
    val belt =  0.87f   // beltline / bottom-of-glass Y
    val wtop =  1.37f   // top-of-glass Y
    val roof =  1.58f   // roof Y

    // ── 1. Main lower body ───────────────────────────────────────────────
    val bodyMidY = (bb + belt) / 2f
    val bodyH    = belt - bb
    addBox(verts, faces, Vec3(0f, bodyMidY, 0f),
        Vec3(hw * 2f, bodyH, 4.34f), PartId.BODY, body, bodyDark)

    // ── 2. Front bumper fascia ───────────────────────────────────────────
    addBox(verts, faces, Vec3(0f, 0.445f, 2.14f),
        Vec3(hw * 2f, 0.51f, 0.10f), PartId.BODY, bump, bodyDark)

    // ── 3. Lower front apron ─────────────────────────────────────────────
    addBox(verts, faces, Vec3(0f, bb + 0.04f, 2.13f),
        Vec3(1.68f, 0.09f, 0.12f), PartId.BODY, sill, bodyDark)

    // ── 4. Audi single-frame grille (wide trapezoid, inset) ──────────────
    // Main dark area
    addBox(verts, faces, Vec3(0f, 0.595f, 2.165f),
        Vec3(1.34f, 0.47f, 0.055f), PartId.HOOD, grille, grille)
    // Chrome surround top bar
    addBox(verts, faces, Vec3(0f, 0.845f, 2.175f),
        Vec3(1.46f, 0.065f, 0.05f), PartId.HOOD, chrome, chrome)
    // Chrome surround bottom bar
    addBox(verts, faces, Vec3(0f, 0.365f, 2.175f),
        Vec3(1.46f, 0.065f, 0.05f), PartId.HOOD, chrome, chrome)

    // ── 5. Headlights — semi-vertical, at grille corners ─────────────────
    for (sign in listOf(-1f, 1f)) {
        val cx = sign * 0.790f
        addBox(verts, faces, Vec3(cx, 0.805f, 2.12f),
            Vec3(0.24f, 0.43f, 0.10f), PartId.HEADLIGHT, headL, headL)
        // DRL strip (thin bright bar below main lens)
        addBox(verts, faces, Vec3(cx, 0.605f, 2.175f),
            Vec3(0.20f, 0.06f, 0.03f), PartId.HEADLIGHT, Color(0xFFFFFFEE), Color(0xFFFFFFEE))
    }

    // ── 6. Hood (slight ramp: front edge lower, firewall edge higher) ─────
    val hdW = 1.76f
    addPrism(verts, faces,
        Vec3(-hdW/2f, belt,  2.09f), Vec3(hdW/2f, belt,  2.09f),
        Vec3( hdW/2f, 0.92f, 2.09f), Vec3(-hdW/2f, 0.92f, 2.09f),
        Vec3(-hdW/2f, belt,  0.52f), Vec3(hdW/2f, belt,  0.52f),
        Vec3( hdW/2f, 0.97f, 0.52f), Vec3(-hdW/2f, 0.97f, 0.52f),
        PartId.HOOD, body, bodyDark)

    // ── 7. Windshield (raked ~55° from vertical) ──────────────────────────
    val wsW = 1.66f
    addPrism(verts, faces,
        Vec3(-wsW/2f, 0.97f, 0.52f), Vec3(wsW/2f, 0.97f, 0.52f),
        Vec3( wsW/2f, wtop,  0.07f), Vec3(-wsW/2f, wtop,  0.07f),
        Vec3(-wsW/2f, 0.97f, 0.48f), Vec3(wsW/2f, 0.97f, 0.48f),
        Vec3( wsW/2f, wtop,  0.03f), Vec3(-wsW/2f, wtop,  0.03f),
        PartId.GLASS, glass, glass)

    // ── 8. Roof ───────────────────────────────────────────────────────────
    val roofMidZ = (0.07f + (-1.63f)) / 2f
    val roofLen  = 0.07f - (-1.63f)
    addBox(verts, faces, Vec3(0f, (wtop + roof) / 2f, roofMidZ),
        Vec3(1.70f, roof - wtop, roofLen), PartId.CABIN, body, bodyDark)

    // ── 9. A-pillars (body-coloured strip beside windshield) ──────────────
    for (sign in listOf(-1f, 1f)) {
        addBox(verts, faces, Vec3(sign * (hw - 0.07f), (belt + wtop) / 2f, 0.295f),
            Vec3(0.10f, wtop - belt, 0.44f), PartId.CABIN, body, bodyDark)
    }

    // ── 10. B-pillars (black centre post) ────────────────────────────────
    for (sign in listOf(-1f, 1f)) {
        addBox(verts, faces, Vec3(sign * (hw - 0.06f), (belt + wtop) / 2f, 0.055f),
            Vec3(0.08f, wtop - belt, 0.13f), PartId.CABIN, pillar, pillar)
    }

    // ── 11. C-pillars ────────────────────────────────────────────────────
    for (sign in listOf(-1f, 1f)) {
        addBox(verts, faces, Vec3(sign * (hw - 0.07f), (belt + wtop) / 2f, -0.87f),
            Vec3(0.10f, wtop - belt, 0.13f), PartId.CABIN, body, bodyDark)
    }

    // ── 12. D-pillars (rear uprights, prominent on Q3) ───────────────────
    for (sign in listOf(-1f, 1f)) {
        addBox(verts, faces, Vec3(sign * (hw - 0.07f), (belt + roof) / 2f + 0.02f, -1.62f),
            Vec3(0.11f, roof - belt + 0.06f, 0.14f), PartId.CABIN, body, bodyDark)
    }

    // ── 13. Side glass — front door ───────────────────────────────────────
    for (sign in listOf(-1f, 1f)) {
        addBox(verts, faces, Vec3(sign * hw, (belt + wtop) / 2f, 0.525f),
            Vec3(0.05f, wtop - belt - 0.04f, 0.82f), PartId.GLASS, glass, glass)
    }

    // ── 14. Side glass — rear door ────────────────────────────────────────
    for (sign in listOf(-1f, 1f)) {
        addBox(verts, faces, Vec3(sign * hw, (belt + wtop) / 2f, -0.415f),
            Vec3(0.05f, wtop - belt - 0.04f, 0.76f), PartId.GLASS, glass, glass)
    }

    // ── 15. Quarter glass (C-pillar window) ──────────────────────────────
    for (sign in listOf(-1f, 1f)) {
        addBox(verts, faces, Vec3(sign * hw, (belt + wtop) / 2f + 0.02f, -1.275f),
            Vec3(0.05f, wtop - belt - 0.18f, 0.48f), PartId.GLASS, glass, glass)
    }

    // ── 16. Rear window (nearly upright, slight rake) ─────────────────────
    // Outside faces -Z; we swap front/back so the "back face" is the outside.
    val rwW = 1.62f
    addPrism(verts, faces,
        // inside (p0-p3): slightly toward +Z from outside
        Vec3(-rwW/2f, belt + 0.06f, -2.04f), Vec3(rwW/2f, belt + 0.06f, -2.04f),
        Vec3( rwW/2f, wtop - 0.02f, -1.65f), Vec3(-rwW/2f, wtop - 0.02f, -1.65f),
        // outside (p4-p7)
        Vec3(-rwW/2f, belt + 0.06f, -2.08f), Vec3(rwW/2f, belt + 0.06f, -2.08f),
        Vec3( rwW/2f, wtop - 0.02f, -1.69f), Vec3(-rwW/2f, wtop - 0.02f, -1.69f),
        PartId.GLASS, glass, glass)

    // ── 17. Rear hatch upper panel (above rear window) ────────────────────
    addBox(verts, faces, Vec3(0f, (wtop + roof) / 2f, -2.10f),
        Vec3(1.72f, roof - wtop + 0.06f, 0.09f), PartId.TRUNK, body, bodyDark)

    // ── 18. Rear hatch lower panel (below rear window) ────────────────────
    addBox(verts, faces, Vec3(0f, (bb + belt + 0.06f) / 2f, -2.16f),
        Vec3(1.72f, belt + 0.06f - bb, 0.09f), PartId.TRUNK, body, bodyDark)

    // ── 19. Taillights — wide horizontal Q3-style ─────────────────────────
    for (sign in listOf(-1f, 1f)) {
        addBox(verts, faces, Vec3(sign * 0.595f, 0.795f, -2.115f),
            Vec3(0.70f, 0.37f, 0.11f), PartId.TAILLIGHT, tailR, tailR)
        // Inner amber strip
        addBox(verts, faces, Vec3(sign * 0.245f, 0.795f, -2.118f),
            Vec3(0.25f, 0.28f, 0.06f), PartId.TAILLIGHT, Color(0xFFFF5500), Color(0xFFFF5500))
    }

    // ── 20. Rear bumper ───────────────────────────────────────────────────
    addBox(verts, faces, Vec3(0f, 0.335f, -2.14f),
        Vec3(hw * 2f, 0.31f, 0.09f), PartId.TRUNK, bump, bodyDark)

    // ── 21. Side sills (rocker panels) ───────────────────────────────────
    for (sign in listOf(-1f, 1f)) {
        addBox(verts, faces, Vec3(sign * (hw + 0.015f), 0.275f, -0.02f),
            Vec3(0.065f, 0.17f, 3.52f), PartId.BODY, sill, bodyDark)
    }

    // ── 22. Wheel-arch flares (fender overhang) ───────────────────────────
    for (sign in listOf(-1f, 1f)) {
        // Front
        addBox(verts, faces, Vec3(sign * (hw + 0.02f), 0.50f, fa),
            Vec3(0.07f, 0.62f, 1.00f), PartId.BODY, body, bodyDark)
        // Rear
        addBox(verts, faces, Vec3(sign * (hw + 0.02f), 0.50f, ra),
            Vec3(0.07f, 0.62f, 1.00f), PartId.BODY, body, bodyDark)
    }

    // ── 23. Roof rails ────────────────────────────────────────────────────
    for (sign in listOf(-1f, 1f)) {
        addBox(verts, faces, Vec3(sign * 0.73f, roof + 0.035f, roofMidZ),
            Vec3(0.045f, 0.045f, roofLen - 0.30f), PartId.CABIN, roofRail, roofRail)
    }

    // ── 24. Wheels (12 segments for smooth look) ──────────────────────────
    for ((xSign, zPos, pid) in listOf(
        Triple(-1f, fa, PartId.WHEEL_FL), Triple(1f, fa, PartId.WHEEL_FR),
        Triple(-1f, ra, PartId.WHEEL_RL), Triple(1f, ra, PartId.WHEEL_RR),
    )) {
        addCylinder(verts, faces, Vec3(xSign * wx, wr, zPos), wr, ww, 12, pid, tire, hub)
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
