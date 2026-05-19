package ru.techgid.presentation.screen.viewer3d

import androidx.compose.ui.graphics.Color
import ru.techgid.domain.model.Difficulty
import kotlin.math.PI

data class CameraPreset(
    val yaw: Float,
    val pitch: Float,
    val zoom: Float,
    val focusX: Float = 0f,
    val focusY: Float = 0f,
    val focusZ: Float = 0f,
) {
    companion object {
        val DEFAULT = CameraPreset(
            yaw = (PI / 6).toFloat(),
            pitch = (-PI / 9).toFloat(),
            zoom = 1f,
        )
    }
}

data class SceneNode(
    val id: String,
    val label: String,
    val title: String,
    val location: String,
    val description: String,
    val difficulty: Difficulty,
    val highlightPartIds: Set<String>,
    val hidePartIds: Set<String> = emptySet(),
    val cameraPreset: CameraPreset = CameraPreset.DEFAULT,
    val highlightColor: Color = Color(0xFFFF8A00),
)

data class InstructionStep(
    val stepId: String,
    val label: String,
    val highlightPartIds: Set<String>,
    val hidePartIds: Set<String> = emptySet(),
    val cameraPreset: CameraPreset,
    val partOffsets: Map<String, Vec3> = emptyMap(),
)

data class CarSceneConfig(
    val carId: String,
    val displayName: String,
    val defaultCamera: CameraPreset = CameraPreset.DEFAULT,
    val nodes: List<SceneNode>,
    val instructions: Map<String, List<InstructionStep>> = emptyMap(),
    val ghostMode: Boolean = false,
)

fun buildSceneConfig(carName: String): CarSceneConfig = when {
    carName.contains("Audi Q3", ignoreCase = true) -> audiQ3SceneConfig()
    carName.contains("BMW", ignoreCase = true) -> genericSceneConfig(carName, "bmw")
    carName.contains("Mercedes", ignoreCase = true) -> genericSceneConfig(carName, "mercedes")
    carName.contains("Toyota", ignoreCase = true) -> genericSceneConfig(carName, "toyota")
    carName.contains("Volkswagen", ignoreCase = true) ||
        carName.contains("VW", ignoreCase = true) ||
        carName.contains("Golf", ignoreCase = true) -> genericSceneConfig(carName, "vw")
    carName.contains("Lada", ignoreCase = true) ||
        carName.contains("\u0412\u0410\u0417", ignoreCase = true) -> genericSceneConfig(carName, "lada")
    carName.contains("Hyundai", ignoreCase = true) ||
        carName.contains("Kia", ignoreCase = true) -> genericSceneConfig(carName, "hyundai")
    carName.contains("Skoda", ignoreCase = true) -> genericSceneConfig(carName, "skoda")
    carName.contains("Renault", ignoreCase = true) -> genericSceneConfig(carName, "renault")
    carName.contains("Mazda", ignoreCase = true) -> genericSceneConfig(carName, "mazda")
    carName.contains("Honda", ignoreCase = true) -> genericSceneConfig(carName, "honda")
    carName.contains("Nissan", ignoreCase = true) -> genericSceneConfig(carName, "nissan")
    carName.contains("Mitsubishi", ignoreCase = true) -> genericSceneConfig(carName, "mitsubishi")
    carName.contains("Ford", ignoreCase = true) -> genericSceneConfig(carName, "ford")
    carName.contains("Chevrolet", ignoreCase = true) -> genericSceneConfig(carName, "chevrolet")
    carName.contains("Lexus", ignoreCase = true) -> genericSceneConfig(carName, "lexus")
    carName.contains("Subaru", ignoreCase = true) -> genericSceneConfig(carName, "subaru")
    else -> genericSceneConfig(carName, "generic")
}

// ─────────────────────────────────────────────────────────────────────────────
// Audi Q3 2011 (8U) 2.0 TFSI — ghost mode with internal components
// ─────────────────────────────────────────────────────────────────────────────

private fun audiQ3SceneConfig(): CarSceneConfig {
    val nodes = listOf(
        SceneNode(
            id = "fuel_pump",
            label = "Топл. насос",
            title = "Модуль топливного насоса",
            location = "Правый фланец под задним сиденьем · в баке",
            description = "Доступ через правую крышку под подушкой заднего сиденья. В сцене показаны седловидный бак, модуль G6, стопорное кольцо, разъём, линия подачи, блок J538 и левый фланец с suction-jet pump.",
            difficulty = Difficulty.MEDIUM,
            highlightPartIds = setOf(
                PartId.FUEL_PUMP,
                PartId.FUEL_PUMP_COVER,
                PartId.FUEL_LOCKING_RING,
                PartId.FUEL_CONNECTOR,
            ),
            hidePartIds = setOf(PartId.REAR_SEAT),
            cameraPreset = CameraPreset(
                yaw = (-PI / 7).toFloat(),
                pitch = (-PI / 5).toFloat(),
                zoom = 1.85f,
                focusX = 0.28f,
                focusY = 0.64f,
                focusZ = -0.94f,
            ),
            highlightColor = Color(0xFF25B7FF),
        ),
        SceneNode(
            id = "engine",
            label = "Двигатель",
            title = "2.0 TFSI EA888",
            location = "Моторный отсек · центр",
            description = "Рядный 4-цилиндровый, 211 л.с., цепь ГРМ, турбонаддув, непосредственный впрыск. Блок чугунный, голова алюминиевая.",
            difficulty = Difficulty.MEDIUM,
            highlightPartIds = setOf(PartId.ENGINE_BLOCK),
            cameraPreset = CameraPreset(0.0f, -0.8f, 1.5f, focusY = 0.5f, focusZ = 1.1f),
        ),
        SceneNode(
            id = "oil_filter",
            label = "Масл. фильтр",
            title = "Масляный фильтр",
            location = "Верх двигателя · справа",
            description = "Картриджный фильтр в вертикальном корпусе, замена сверху. Ключ-съёмник 32 мм. Сливная пробка на поддоне.",
            difficulty = Difficulty.EASY,
            highlightPartIds = setOf(PartId.OIL_FILTER, PartId.OIL_PAN),
            cameraPreset = CameraPreset(-0.5f, -0.6f, 1.8f, focusX = 0.25f, focusY = 0.65f, focusZ = 1.3f),
        ),
        SceneNode(
            id = "air_filter",
            label = "Возд. фильтр",
            title = "Воздушный фильтр",
            location = "Правая сторона мот. отсека",
            description = "Бумажный элемент в пластиковом корпусе. Воздуховод от короба к турбине через расходомер.",
            difficulty = Difficulty.EASY,
            highlightPartIds = setOf(PartId.AIR_FILTER),
            cameraPreset = CameraPreset(-0.4f, -0.5f, 1.7f, focusX = 0.5f, focusY = 0.7f, focusZ = 1.3f),
        ),
        SceneNode(
            id = "turbo",
            label = "Турбо",
            title = "K03 турбонагнетатель",
            location = "Выпускной коллектор · сзади двигателя",
            description = "BorgWarner K03, интегрирован с выпускным коллектором. Давление наддува до 1.2 бар. Интеркулер — фронтальный.",
            difficulty = Difficulty.HARD,
            highlightPartIds = setOf(PartId.TURBO, PartId.INTERCOOLER),
            hidePartIds = setOf(PartId.HOOD),
            cameraPreset = CameraPreset(2.8f, -0.3f, 1.8f, focusY = 0.4f, focusZ = 0.85f),
        ),
        SceneNode(
            id = "cooling",
            label = "Охлаждение",
            title = "Система охлаждения",
            location = "Перед двигателем · радиатор",
            description = "Основной радиатор, расширительный бачок, термостат. Антифриз G12++ розовый, объём 7.4 л.",
            difficulty = Difficulty.MEDIUM,
            highlightPartIds = setOf(PartId.RADIATOR, PartId.COOLANT_TANK),
            cameraPreset = CameraPreset(0.0f, -0.3f, 1.4f, focusY = 0.55f, focusZ = 1.9f),
        ),
        SceneNode(
            id = "exhaust",
            label = "Выхлоп",
            title = "Выпускная система",
            location = "Под двигателем · вниз",
            description = "Катализатор после турбины, средний и задний глушители. Датчики O2 до и после ката.",
            difficulty = Difficulty.HARD,
            highlightPartIds = setOf(PartId.EXHAUST_MANIFOLD, PartId.CATALYTIC_CONVERTER),
            hidePartIds = setOf(PartId.BODY),
            cameraPreset = CameraPreset(0.3f, 0.4f, 1.5f, focusY = 0.25f, focusZ = 0.75f),
        ),
        SceneNode(
            id = "brakes",
            label = "Тормоза",
            title = "Тормозная система",
            location = "Колёсные арки",
            description = "Передние: вентил. диски 320 мм, суппорт FN3. Задние: 272 мм. ABS + ESP.",
            difficulty = Difficulty.EASY,
            highlightPartIds = setOf(PartId.BRAKE_CALIPER),
            cameraPreset = CameraPreset(-0.7f, -0.2f, 1.4f, focusX = 0.7f, focusY = 0.33f, focusZ = 1.3f),
        ),
        SceneNode(
            id = "transmission",
            label = "КПП",
            title = "S-tronic DSG DQ250",
            location = "Левая сторона двигателя",
            description = "6-ступ. DSG, мехатроник, двойное сцепление. Масло каждые 60 ткм. Приводные валы к передним колёсам.",
            difficulty = Difficulty.EXPERT,
            highlightPartIds = setOf(PartId.TRANSMISSION, PartId.DRIVE_SHAFT),
            cameraPreset = CameraPreset(0.8f, -0.4f, 1.7f, focusX = -0.4f, focusY = 0.38f, focusZ = 1.15f),
        ),
        SceneNode(
            id = "electrical",
            label = "Электрика",
            title = "Электросистема",
            location = "Левый отсек · АКБ",
            description = "АКБ 70 А·ч, генератор 140 А, стартер. CAN-шина, ЭБУ Bosch MED17.5.2.",
            difficulty = Difficulty.MEDIUM,
            highlightPartIds = setOf(PartId.BATTERY, PartId.ALTERNATOR, PartId.STARTER),
            cameraPreset = CameraPreset(0.6f, -0.5f, 1.7f, focusX = -0.5f, focusY = 0.6f, focusZ = 1.3f),
        ),
    )

    val instructions = mapOf(
        "fuel_pump" to listOf(
            InstructionStep(
                "fuel_1", "Сдвиньте передние сиденья вперёд",
                setOf(PartId.REAR_SEAT, PartId.ACCESS_MARKER),
                emptySet(),
                CameraPreset(
                    yaw = (-PI / 6).toFloat(),
                    pitch = (-PI / 5).toFloat(),
                    zoom = 1.65f,
                    focusX = 0.20f,
                    focusY = 0.92f,
                    focusZ = -0.98f,
                ),
            ),
            InstructionStep(
                "fuel_2", "Снимите подушку заднего сиденья",
                setOf(PartId.REAR_SEAT, PartId.ACCESS_MARKER),
                emptySet(),
                CameraPreset(
                    yaw = (-PI / 6).toFloat(),
                    pitch = (-PI / 5).toFloat(),
                    zoom = 1.72f,
                    focusX = 0.22f,
                    focusY = 0.84f,
                    focusZ = -0.98f,
                ),
                partOffsets = mapOf(
                    PartId.REAR_SEAT to Vec3(0f, 0.22f, -0.18f),
                ),
            ),
            InstructionStep(
                "fuel_3", "Отщёлкните правую крышку фланца",
                setOf(PartId.FUEL_PUMP_COVER),
                setOf(PartId.REAR_SEAT),
                CameraPreset(
                    yaw = (-PI / 7).toFloat(),
                    pitch = (-PI / 4.5).toFloat(),
                    zoom = 2.05f,
                    focusX = 0.30f,
                    focusY = 0.58f,
                    focusZ = -0.92f,
                ),
            ),
            InstructionStep(
                "fuel_4", "Отключите разъём и топливную линию",
                setOf(PartId.FUEL_CONNECTOR, PartId.FUEL_LINE, PartId.FUEL_PUMP_CONTROLLER),
                setOf(PartId.REAR_SEAT),
                CameraPreset(
                    yaw = (-PI / 7.5).toFloat(),
                    pitch = (-PI / 4.8).toFloat(),
                    zoom = 2.15f,
                    focusX = 0.42f,
                    focusY = 0.62f,
                    focusZ = -0.80f,
                ),
            ),
            InstructionStep(
                "fuel_5", "Ослабьте стопорное кольцо",
                setOf(PartId.FUEL_LOCKING_RING, PartId.FUEL_PUMP_COVER),
                setOf(PartId.REAR_SEAT),
                CameraPreset(
                    yaw = (-PI / 8).toFloat(),
                    pitch = (-PI / 4.2).toFloat(),
                    zoom = 2.25f,
                    focusX = 0.34f,
                    focusY = 0.60f,
                    focusZ = -0.92f,
                ),
            ),
            InstructionStep(
                "fuel_6", "Извлеките модуль насоса",
                setOf(PartId.FUEL_PUMP, PartId.FUEL_TANK),
                setOf(PartId.REAR_SEAT),
                CameraPreset(
                    yaw = (-PI / 8).toFloat(),
                    pitch = (-PI / 4).toFloat(),
                    zoom = 2.2f,
                    focusX = 0.34f,
                    focusY = 0.56f,
                    focusZ = -0.92f,
                ),
                partOffsets = mapOf(
                    PartId.FUEL_PUMP to Vec3(0f, 0.34f, 0f),
                ),
            ),
        ),
        "oil_filter" to listOf(
            InstructionStep(
                "oil_1", "Расположение фильтра",
                setOf(PartId.OIL_FILTER),
                emptySet(),
                CameraPreset(-0.5f, -0.6f, 1.8f, focusX = 0.25f, focusY = 0.65f, focusZ = 1.3f),
            ),
            InstructionStep(
                "oil_2", "Сливная пробка",
                setOf(PartId.OIL_PAN),
                setOf(PartId.BODY),
                CameraPreset(0.3f, 0.5f, 1.6f, focusY = 0.15f, focusZ = 1.0f),
            ),
            InstructionStep(
                "oil_3", "Замена фильтра",
                setOf(PartId.OIL_FILTER),
                emptySet(),
                CameraPreset(-0.3f, -0.9f, 2.0f, focusX = 0.25f, focusY = 0.72f, focusZ = 1.34f),
            ),
        ),
        "air_filter" to listOf(
            InstructionStep(
                "air_1", "Корпус фильтра",
                setOf(PartId.AIR_FILTER),
                emptySet(),
                CameraPreset(-0.4f, -0.5f, 1.7f, focusX = 0.5f, focusY = 0.7f, focusZ = 1.3f),
            ),
            InstructionStep(
                "air_2", "Замена элемента",
                setOf(PartId.AIR_FILTER),
                emptySet(),
                CameraPreset(-0.2f, -0.8f, 2.0f, focusX = 0.55f, focusY = 0.72f, focusZ = 1.28f),
            ),
        ),
        "turbo" to listOf(
            InstructionStep(
                "turbo_1", "Турбина",
                setOf(PartId.TURBO),
                setOf(PartId.HOOD),
                CameraPreset(2.8f, -0.3f, 1.8f, focusY = 0.4f, focusZ = 0.85f),
            ),
            InstructionStep(
                "turbo_2", "Интеркулер",
                setOf(PartId.INTERCOOLER),
                emptySet(),
                CameraPreset(0.0f, -0.3f, 1.5f, focusY = 0.44f, focusZ = 1.95f),
            ),
            InstructionStep(
                "turbo_3", "Выпускной коллектор",
                setOf(PartId.EXHAUST_MANIFOLD),
                setOf(PartId.HOOD, PartId.BODY),
                CameraPreset(2.5f, -0.2f, 1.7f, focusY = 0.36f, focusZ = 0.88f),
            ),
        ),
    )

    return CarSceneConfig(
        carId = "audi_q3_8u_2011",
        displayName = "Audi Q3 2011 · 2.0 TFSI",
        nodes = nodes,
        instructions = instructions,
        ghostMode = true,
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// Generic scene config for cars without internal component data
// ─────────────────────────────────────────────────────────────────────────────

private fun genericSceneConfig(carName: String, carId: String): CarSceneConfig {
    return CarSceneConfig(
        carId = carId,
        displayName = carName,
        nodes = genericNodes(),
        ghostMode = false,
    )
}

private fun genericNodes() = listOf(
    SceneNode(
        id = "engine", label = "Двигатель", title = "Двигатель",
        location = "Моторный отсек",
        description = "Двигатель внутреннего сгорания, привод ГРМ, система впрыска.",
        difficulty = Difficulty.MEDIUM,
        highlightPartIds = setOf(PartId.HOOD),
        cameraPreset = CameraPreset(0.3f, -0.6f, 1.3f, focusZ = 1.0f),
    ),
    SceneNode(
        id = "fuel", label = "Топливная", title = "Топливная система",
        location = "Моторный отсек · бак",
        description = "Топливный насос, инжекторы, бак.",
        difficulty = Difficulty.MEDIUM,
        highlightPartIds = setOf(PartId.HOOD),
        cameraPreset = CameraPreset(0.0f, -0.5f, 1.3f, focusZ = 1.2f),
    ),
    SceneNode(
        id = "brakes", label = "Тормоза", title = "Тормозная система",
        location = "Колёсные арки",
        description = "Дисковые или барабанные тормоза, суппорты, ABS.",
        difficulty = Difficulty.EASY,
        highlightPartIds = setOf(PartId.WHEEL_FL, PartId.WHEEL_FR, PartId.WHEEL_RL, PartId.WHEEL_RR),
        cameraPreset = CameraPreset(-0.5f, -0.2f, 1.3f, focusX = 0.8f, focusZ = 1.0f),
    ),
    SceneNode(
        id = "suspension", label = "Подвеска", title = "Подвеска",
        location = "Колёсные арки · днище",
        description = "Передняя и задняя подвеска, стабилизаторы.",
        difficulty = Difficulty.HARD,
        highlightPartIds = setOf(PartId.WHEEL_FL, PartId.WHEEL_FR, PartId.WHEEL_RL, PartId.WHEEL_RR),
        cameraPreset = CameraPreset(-0.3f, 0.2f, 1.2f),
    ),
    SceneNode(
        id = "electrical", label = "Электрика", title = "Электрооборудование",
        location = "Блок предохранителей",
        description = "Аккумулятор, генератор, блок предохранителей.",
        difficulty = Difficulty.MEDIUM,
        highlightPartIds = setOf(PartId.HOOD),
        cameraPreset = CameraPreset(0.5f, -0.4f, 1.3f, focusZ = 1.0f),
    ),
    SceneNode(
        id = "body", label = "Кузов", title = "Кузов",
        location = "Наружные панели",
        description = "Несущий кузов, антикоррозийная обработка.",
        difficulty = Difficulty.HARD,
        highlightPartIds = setOf(PartId.BODY),
        cameraPreset = CameraPreset.DEFAULT,
    ),
    SceneNode(
        id = "cabin", label = "Салон", title = "Салон",
        location = "Торпедо · центральная консоль",
        description = "Климат-контроль, мультимедиа, сиденья.",
        difficulty = Difficulty.EASY,
        highlightPartIds = setOf(PartId.CABIN, PartId.GLASS),
        cameraPreset = CameraPreset(0.8f, -0.5f, 1.2f),
    ),
)
