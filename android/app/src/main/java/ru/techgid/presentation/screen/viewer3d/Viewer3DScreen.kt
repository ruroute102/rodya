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

    val parts = remember(carName) { buildPartsForCar(carName) }

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

// ─────────────────────────────────────────────────────────────────────────────
// Per-car part descriptions
// ─────────────────────────────────────────────────────────────────────────────

private fun buildPartsForCar(carName: String): List<PartInfo> = when {
    carName.contains("Audi Q3", ignoreCase = true) -> audiQ3Parts()
    carName.contains("BMW", ignoreCase = true) -> bmwParts()
    carName.contains("Toyota", ignoreCase = true) -> toyotaParts()
    carName.contains("Volkswagen", ignoreCase = true) || carName.contains("VW", ignoreCase = true) -> vwGolfParts()
    carName.contains("Lada", ignoreCase = true) || carName.contains("\u0412\u0410\u0417", ignoreCase = true) -> ladaVestaParts()
    carName.contains("Mercedes", ignoreCase = true) -> mercedesParts()
    carName.contains("Hyundai", ignoreCase = true) || carName.contains("Kia", ignoreCase = true) -> hyundaiParts()
    else -> defaultParts()
}

private fun audiQ3Parts() = listOf(
    PartInfo("Двигатель", "2.0 TFSI", "Моторный отсек", "2.0 TFSI, 4 цилиндра, 211 л.с., непосредственный впрыск, турбонаддув. Цепь ГРМ, интеркулер.", Difficulty.MEDIUM),
    PartInfo("Топливная система", "Топливный насос", "Заднее сиденье · доступ снизу", "Электробензонасос в баке, давление 4.5 бар, инжекторы непосредственного впрыска. Бак 60 л.", Difficulty.MEDIUM),
    PartInfo("Тормоза", "Тормозные колодки", "Колёсные арки", "Дисковые передние/задние, ABS, ESP, диаметр переднего диска 320 мм. Суппорт однопоршневый.", Difficulty.EASY),
    PartInfo("Подвеска", "McPherson / Многорычажная", "Колёсные арки · днище", "Передняя — McPherson, задняя — многорычажная. Стабилизаторы поперечной устойчивости.", Difficulty.HARD),
    PartInfo("Электрика", "CAN-шина", "Блок предохранителей", "Аккумулятор 70 А·ч, генератор 140 А, CAN-шина. Блок предохранителей под капотом и в салоне.", Difficulty.MEDIUM),
    PartInfo("Кузов", "Несущий кузов", "Наружные панели", "Несущий кузов, оцинковка, лакокрасочное покрытие в 4 слоя. Зоны программируемой деформации.", Difficulty.EXPERT),
    PartInfo("Салон", "Климат-контроль", "Торпедо · центральная консоль", "Климат-контроль, мультимедиа MMI, электрорегулировка сидений, подогрев передних сидений.", Difficulty.EASY),
)

private fun bmwParts() = listOf(
    PartInfo("Двигатель", "B58 3.0 R6", "Моторный отсек", "3.0 R6 Twin-Scroll турбо, 340 л.с., цепь ГРМ, Valvetronic + Double-VANOS, алюминиевый блок.", Difficulty.HARD),
    PartInfo("Топливная система", "ТНВД + форсунки", "Моторный отсек · правая сторона", "ТНВД Bosch, 350 бар, инжекторы прямого впрыска. Бак 83 л, два топливных насоса.", Difficulty.HARD),
    PartInfo("Тормоза", "Тормозная система", "Колёсные арки", "Передние: 374 мм вентил. диски, 4-поршн. суппорт. Задние: 345 мм. ABS, DSC, DTC.", Difficulty.EASY),
    PartInfo("Подвеска", "Двухрычажная / Многорычажная", "Колёсные арки · днище", "Передняя — двухрычажная алюминиевая, задняя — 5-рычажная. Adaptive M Sport опционально.", Difficulty.EXPERT),
    PartInfo("Электрика", "iDrive 7 / CAN-FD", "Блок предохранителей · багажник", "Аккумулятор AGM 90 А·ч в багажнике, генератор 220 А. Две CAN-FD шины.", Difficulty.HARD),
    PartInfo("Кузов", "Несущий кузов", "Наружные панели", "Стальной несущий кузов, алюминиевые капот и крылья, горячая оцинковка. Зоны деформации.", Difficulty.EXPERT),
    PartInfo("Салон", "4-зонный климат", "Торпедо · центральная консоль", "4-зонный климат-контроль, Live Cockpit Professional, Harman Kardon, панорамная крыша.", Difficulty.EASY),
)

private fun toyotaParts() = listOf(
    PartInfo("Двигатель", "2.5 Dynamic Force", "Моторный отсек", "2.5 R4, 209 л.с., цикл Аткинсона, D-4S (комбинир. впрыск), цепь ГРМ, VVT-iE.", Difficulty.MEDIUM),
    PartInfo("Топливная система", "Инжекторы D-4S", "Моторный отсек", "Комбинированный впрыск: прямой + распределённый. Бак 60 л, электронасос в баке.", Difficulty.MEDIUM),
    PartInfo("Тормоза", "Дисковые тормоза", "Колёсные арки", "Передние: вентил. диски 297 мм. Задние: сплошные 281 мм. ABS, VSC, BA.", Difficulty.EASY),
    PartInfo("Подвеска", "McPherson / Двухрычажная", "Колёсные арки · днище", "Передняя — McPherson, задняя — двухрычажная. Платформа TNGA-K.", Difficulty.MEDIUM),
    PartInfo("Электрика", "Toyota Safety Sense", "Блок предохранителей", "Аккумулятор 60 А·ч, генератор 130 А. Toyota Safety Sense 2.5+: PCS, LDA, DRCC.", Difficulty.MEDIUM),
    PartInfo("Кузов", "Несущий TNGA-K", "Наружные панели", "Платформа TNGA-K, высокопрочная сталь до 1500 МПа, коррозийная защита.", Difficulty.HARD),
    PartInfo("Салон", "Климат-контроль", "Торпедо · центральная консоль", "2-зонный климат, 9\" мультимедиа, беспроводной CarPlay, подогрев руля и сидений.", Difficulty.EASY),
)

private fun vwGolfParts() = listOf(
    PartInfo("Двигатель", "1.4 TSI EA211", "Моторный отсек", "1.4 R4 TSI, 150 л.с., турбонаддув, непосредственный впрыск, ремень ГРМ. ACT (отключение цилиндров).", Difficulty.MEDIUM),
    PartInfo("Топливная система", "ТНВД + форсунки", "Моторный отсек", "ТНВД 150 бар, пьезоинжекторы. Бак 50 л, электронасос в баке, давление 3.5 бар.", Difficulty.MEDIUM),
    PartInfo("Тормоза", "Дисковые тормоза", "Колёсные арки", "Передние: вентил. диски 312 мм. Задние: сплошные 272 мм. ABS, ESC, XDS+.", Difficulty.EASY),
    PartInfo("Подвеска", "McPherson / Многорычажная", "Колёсные арки · днище", "Передняя — McPherson, задняя — многорычажная (опция). Платформа MQB.", Difficulty.MEDIUM),
    PartInfo("Электрика", "CAN / MIB II", "Блок предохранителей", "Аккумулятор 59 А·ч, генератор 140 А. Две CAN-шины, мультимедиа MIB II.", Difficulty.MEDIUM),
    PartInfo("Кузов", "Несущий MQB", "Наружные панели", "Платформа MQB, горячая оцинковка, 12-летняя гарантия от сквозной коррозии.", Difficulty.HARD),
    PartInfo("Салон", "Climatronic", "Торпедо · центральная консоль", "2-зонный Climatronic, цифровая приборка Active Info, подогрев сидений и руля.", Difficulty.EASY),
)

private fun ladaVestaParts() = listOf(
    PartInfo("Двигатель", "1.6 ВАЗ-21129", "Моторный отсек", "1.6 R4, 106 л.с., распредвпрыск, цепь ГРМ. 16-клапанный, DOHC.", Difficulty.EASY),
    PartInfo("Топливная система", "Форсунки Bosch", "Моторный отсек · бак", "Многоточечный впрыск, давление рампы 3.8 бар. Бак 55 л, погружной насос.", Difficulty.EASY),
    PartInfo("Тормоза", "Дисковые / Барабанные", "Колёсные арки", "Передние: вентил. диски 260 мм. Задние: барабанные. ABS, BAS.", Difficulty.EASY),
    PartInfo("Подвеска", "McPherson / Балка", "Колёсные арки · днище", "Передняя — McPherson, задняя — полузависимая балка. Стабилизатор спереди.", Difficulty.EASY),
    PartInfo("Электрика", "CAN-шина", "Блок предохранителей", "Аккумулятор 62 А·ч, генератор 115 А. ЭБУ Bosch ME17.9.7.", Difficulty.EASY),
    PartInfo("Кузов", "Несущий кузов", "Наружные панели", "Несущий стальной кузов, катафорезный грунт, антикоррозийная мастика.", Difficulty.MEDIUM),
    PartInfo("Салон", "Климат-контроль", "Торпедо · центральная консоль", "Кондиционер или климат-контроль, мультимедиа 7\", подогрев сидений и лобового.", Difficulty.EASY),
)

private fun mercedesParts() = listOf(
    PartInfo("Двигатель", "M264 2.0 Turbo", "Моторный отсек", "2.0 R4 турбо, 258 л.с., NANOSLIDE-покрытие цилиндров, CAMTRONIC. Цепь ГРМ.", Difficulty.HARD),
    PartInfo("Топливная система", "Пьезоинжекторы", "Моторный отсек", "Непосредственный впрыск 200+ бар, пьезоинжекторы Bosch. Бак 66 л.", Difficulty.HARD),
    PartInfo("Тормоза", "Тормозная система", "Колёсные арки", "Передние: вентил. диски 330 мм. Задние: 300 мм. ABS, ESP, Brake Assist Plus.", Difficulty.EASY),
    PartInfo("Подвеска", "4-рычажная / Многорычажная", "Колёсные арки · днище", "Передняя — 4-рычажная, задняя — 5-рычажная. AGILITY CONTROL (адаптивные амортизаторы).", Difficulty.EXPERT),
    PartInfo("Электрика", "MBUX / CAN", "Блок предохранителей", "Аккумулятор AGM 80 А·ч, генератор 200 А. MBUX, EQ Boost 48В (мягкий гибрид).", Difficulty.HARD),
    PartInfo("Кузов", "Несущий кузов", "Наружные панели", "Алюминиево-стальной несущий кузов, катафорезная оцинковка, 30-летняя гарантия.", Difficulty.EXPERT),
    PartInfo("Салон", "THERMOTRONIC", "Торпедо · центральная консоль", "3-зонный THERMOTRONIC, MBUX 10.25\", Burmester, массаж сидений, подсветка 64 цвета.", Difficulty.EASY),
)

private fun hyundaiParts() = listOf(
    PartInfo("Двигатель", "2.0 Nu MPI", "Моторный отсек", "2.0 R4, 150 л.с., распредвпрыск MPI, цепь ГРМ. D-CVVT, алюминиевый блок.", Difficulty.EASY),
    PartInfo("Топливная система", "Инжекторы MPI", "Моторный отсек · бак", "Многоточечный впрыск, давление рампы 3.5 бар. Бак 50 л, погружной насос.", Difficulty.EASY),
    PartInfo("Тормоза", "Дисковые тормоза", "Колёсные арки", "Передние: вентил. диски 280 мм. Задние: сплошные 262 мм. ABS, ESC, HAC.", Difficulty.EASY),
    PartInfo("Подвеска", "McPherson / Многорычажная", "Колёсные арки · днище", "Передняя — McPherson, задняя — многорычажная. Платформа третьего поколения.", Difficulty.MEDIUM),
    PartInfo("Электрика", "SmartSense", "Блок предохранителей", "Аккумулятор 60 А·ч, генератор 130 А. Hyundai SmartSense: FCA, LKA, BCW.", Difficulty.MEDIUM),
    PartInfo("Кузов", "Несущий кузов", "Наружные панели", "Платформа 3-го поколения, 54% сталь повышенной прочности, горячая штамповка.", Difficulty.HARD),
    PartInfo("Салон", "Климат-контроль", "Торпедо · центральная консоль", "2-зонный климат, 10.25\" навигация, Bose-аудио, подогрев и вентиляция сидений.", Difficulty.EASY),
)

private fun defaultParts() = listOf(
    PartInfo("Двигатель", "Бензиновый", "Моторный отсек", "Бензиновый 4-цилиндровый двигатель, ременной/цепной привод ГРМ.", Difficulty.MEDIUM),
    PartInfo("Топливная система", "Топливный насос", "Бак · моторный отсек", "Электробензонасос в баке, инжекторная система подачи топлива.", Difficulty.MEDIUM),
    PartInfo("Тормоза", "Тормозные колодки", "Колёсные арки", "Дисковые передние, дисковые или барабанные задние. ABS.", Difficulty.EASY),
    PartInfo("Подвеска", "Независимая подвеска", "Колёсные арки · днище", "Передняя независимая, задняя полузависимая или многорычажная.", Difficulty.MEDIUM),
    PartInfo("Электрика", "CAN-шина", "Блок предохранителей", "Аккумулятор 60 А·ч, генератор, блок предохранителей.", Difficulty.MEDIUM),
    PartInfo("Кузов", "Несущий кузов", "Наружные панели", "Несущий стальной кузов, антикоррозийная обработка.", Difficulty.HARD),
    PartInfo("Салон", "Климат-контроль", "Торпедо · центральная консоль", "Кондиционер или климат-контроль, мультимедиа-система.", Difficulty.EASY),
)
