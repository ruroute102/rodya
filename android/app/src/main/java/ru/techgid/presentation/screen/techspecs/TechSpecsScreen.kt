package ru.techgid.presentation.screen.techspecs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import ru.techgid.presentation.theme.TechGidTheme

private data class TechSpecCategory(
    val icon: ImageVector,
    val name: String,
    val count: Int,
    val specs: List<TechSpec>,
)

private data class TechSpec(
    val key: String,
    val value: String,
    val unit: String,
    val range: String? = null,
    val notes: String? = null,
)

@Composable
fun TechSpecsScreen(
    viewModel: TechSpecsViewModel = hiltViewModel(),
    onBack: () -> Unit = {},
) {
    val carName by viewModel.carName.collectAsState()
    val categories = remember(carName) { buildTechSpecs(carName) }

    var selectedCategory by remember { mutableStateOf<TechSpecCategory?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = {
                if (selectedCategory != null) selectedCategory = null else onBack()
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Назад",
                    tint = MaterialTheme.colorScheme.onBackground,
                )
            }
            Column(modifier = Modifier.padding(start = 4.dp)) {
                Text(
                    text = selectedCategory?.name ?: "Технические данные",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onBackground,
                )
                Text(
                    text = carName,
                    style = MaterialTheme.typography.labelSmall,
                    color = TechGidTheme.extendedColors.textTertiary,
                )
            }
        }

        if (selectedCategory == null) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(categories) { category ->
                    CategoryCard(
                        category = category,
                        onClick = { selectedCategory = category },
                    )
                }
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(selectedCategory!!.specs) { spec ->
                    SpecRow(spec = spec)
                }
            }
        }
    }
}

@Composable
private fun CategoryCard(category: TechSpecCategory, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = TechGidTheme.extendedColors.cardBackground,
        ),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, TechGidTheme.extendedColors.cardBorder),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = category.icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp),
                )
            }
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = category.name,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${category.count} параметров",
                    style = MaterialTheme.typography.labelSmall,
                    color = TechGidTheme.extendedColors.textTertiary,
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                contentDescription = null,
                tint = TechGidTheme.extendedColors.textTertiary,
                modifier = Modifier.size(12.dp),
            )
        }
    }
}

@Composable
private fun SpecRow(spec: TechSpec) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = TechGidTheme.extendedColors.cardBackground,
        ),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, TechGidTheme.extendedColors.cardBorder),
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = spec.key,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f),
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                ) {
                    Text(
                        text = "${spec.value} ${spec.unit}",
                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
            spec.notes?.let {
                Spacer(Modifier.height(4.dp))
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelSmall,
                    color = TechGidTheme.extendedColors.textTertiary,
                )
            }
            spec.range?.let {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Норма: $it",
                    style = MaterialTheme.typography.labelSmall,
                    color = TechGidTheme.extendedColors.textTertiary,
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Per-car tech specs data
// ─────────────────────────────────────────────────────────────────────────────

private fun buildTechSpecs(carName: String): List<TechSpecCategory> = when {
    carName.contains("Audi Q3", ignoreCase = true) -> audiQ3Specs()
    carName.contains("BMW", ignoreCase = true) -> bmwSpecs()
    carName.contains("Toyota", ignoreCase = true) -> toyotaSpecs()
    carName.contains("Volkswagen", ignoreCase = true) || carName.contains("VW", ignoreCase = true) -> vwGolfSpecs()
    carName.contains("Lada", ignoreCase = true) || carName.contains("\u0412\u0410\u0417", ignoreCase = true) -> ladaVestaSpecs()
    carName.contains("Mercedes", ignoreCase = true) -> mercedesSpecs()
    carName.contains("Hyundai", ignoreCase = true) || carName.contains("Kia", ignoreCase = true) -> hyundaiSpecs()
    else -> audiQ3Specs()
}

private fun specs(
    icon: ImageVector,
    name: String,
    items: List<TechSpec>,
) = TechSpecCategory(icon, name, items.size, items)

private fun audiQ3Specs() = listOf(
    specs(Icons.Filled.Info, "Жидкости и объёмы", listOf(
        TechSpec("Масло двигателя", "4.6", "л", "5W-30 / 5W-40"),
        TechSpec("Масло АКПП", "7.0", "л", "VAG G 060 162"),
        TechSpec("Антифриз", "8.5", "л", "G12++ / G13"),
        TechSpec("Тормозная жидкость", "1.0", "л", "DOT 4"),
        TechSpec("Бак", "60", "л"),
        TechSpec("Омыватель", "5.5", "л"),
    )),
    specs(Icons.Filled.Speed, "Давления", listOf(
        TechSpec("Топливная рампа", "4.5", "бар", "4.0–5.0 бар"),
        TechSpec("Давление масла (хол. ход.)", "1.5", "бар"),
        TechSpec("Давление масла (3000 об/мин)", "3.5", "бар"),
        TechSpec("Давление в шинах", "2.3", "бар", "Передние/задние"),
    )),
    specs(Icons.Filled.Build, "Моменты затяжки", listOf(
        TechSpec("Болты ГБЦ (1 этап)", "40", "Нм"),
        TechSpec("Болты ГБЦ (2 этап)", "+90°", "поворот"),
        TechSpec("Болты колеса", "120", "Нм"),
        TechSpec("Свечи зажигания", "25", "Нм"),
        TechSpec("Сливная пробка масла", "30", "Нм"),
    )),
    specs(Icons.Filled.Settings, "Зазоры", listOf(
        TechSpec("Зазор свечей зажигания", "0.8", "мм"),
        TechSpec("Зазор клапанов (впуск)", "0.20", "мм", "холодный двигатель"),
        TechSpec("Зазор клапанов (выпуск)", "0.30", "мм", "холодный двигатель"),
    )),
    specs(Icons.Filled.Star, "Электрика", listOf(
        TechSpec("Аккумулятор", "70", "А·ч", "EN 680A"),
        TechSpec("Генератор", "140", "А"),
        TechSpec("Напряжение зарядки", "14.2", "В", "13.8–14.6 В"),
    )),
)

private fun bmwSpecs() = listOf(
    specs(Icons.Filled.Info, "Жидкости и объёмы", listOf(
        TechSpec("Масло двигателя", "6.5", "л", "0W-30 BMW LL-04"),
        TechSpec("Масло АКПП ZF 8HP", "9.0", "л", "ZF Lifeguard 8"),
        TechSpec("Антифриз", "11.0", "л", "BMW LLC"),
        TechSpec("Тормозная жидкость", "1.2", "л", "DOT 4 Low Viscosity"),
        TechSpec("Бак", "83", "л"),
        TechSpec("Омыватель", "6.5", "л"),
    )),
    specs(Icons.Filled.Speed, "Давления", listOf(
        TechSpec("Давление масла (хол. ход.)", "1.0", "бар", "мин. 0.7 бар"),
        TechSpec("Давление масла (3000 об/мин)", "4.0", "бар"),
        TechSpec("Давление в шинах (перед)", "2.5", "бар"),
        TechSpec("Давление в шинах (зад)", "2.8", "бар"),
    )),
    specs(Icons.Filled.Build, "Моменты затяжки", listOf(
        TechSpec("Болты ГБЦ (1 этап)", "30", "Нм"),
        TechSpec("Болты ГБЦ (2 этап)", "+90° + 90°", "поворот"),
        TechSpec("Болты колеса", "140", "Нм"),
        TechSpec("Свечи накала", "10", "Нм"),
        TechSpec("Сливная пробка масла", "25", "Нм"),
    )),
    specs(Icons.Filled.Settings, "Зазоры", listOf(
        TechSpec("Зазор клапанов", "—", "", notes = "гидрокомпенсаторы, регулировка не требуется"),
    )),
    specs(Icons.Filled.Star, "Электрика", listOf(
        TechSpec("Аккумулятор", "90", "А·ч", "AGM 900A"),
        TechSpec("Генератор", "220", "А"),
        TechSpec("Напряжение зарядки", "14.4", "В", "14.0–14.8 В"),
    )),
)

private fun toyotaSpecs() = listOf(
    specs(Icons.Filled.Info, "Жидкости и объёмы", listOf(
        TechSpec("Масло двигателя", "4.8", "л", "0W-20 ILSAC GF-6A"),
        TechSpec("Масло вариатора CVT", "7.4", "л", "Toyota CVT Fluid TC"),
        TechSpec("Антифриз", "6.9", "л", "Toyota Super LLC"),
        TechSpec("Тормозная жидкость", "0.8", "л", "DOT 3"),
        TechSpec("Бак", "60", "л"),
        TechSpec("Омыватель", "4.7", "л"),
    )),
    specs(Icons.Filled.Speed, "Давления", listOf(
        TechSpec("Топливная рампа", "3.5", "бар", "3.1–3.9 бар"),
        TechSpec("Давление масла (хол. ход.)", "0.3", "бар", "мин. 0.2 бар"),
        TechSpec("Давление масла (3000 об/мин)", "3.0", "бар"),
        TechSpec("Давление в шинах", "2.3", "бар", "Передние/задние"),
    )),
    specs(Icons.Filled.Build, "Моменты затяжки", listOf(
        TechSpec("Болты ГБЦ (1 этап)", "27", "Нм"),
        TechSpec("Болты ГБЦ (2 этап)", "+90° + 90°", "поворот"),
        TechSpec("Гайки колеса", "103", "Нм"),
        TechSpec("Свечи зажигания (иридий)", "18", "Нм"),
        TechSpec("Сливная пробка масла", "29", "Нм"),
    )),
    specs(Icons.Filled.Settings, "Зазоры", listOf(
        TechSpec("Зазор свечей зажигания", "1.1", "мм", "иридиевые, не регулируются"),
        TechSpec("Зазор клапанов", "—", "", notes = "гидрокомпенсаторы, регулировка не требуется"),
    )),
    specs(Icons.Filled.Star, "Электрика", listOf(
        TechSpec("Аккумулятор", "60", "А·ч", "CCA 590A"),
        TechSpec("Генератор", "130", "А"),
        TechSpec("Напряжение зарядки", "14.0", "В", "13.6–14.4 В"),
    )),
)

private fun vwGolfSpecs() = listOf(
    specs(Icons.Filled.Info, "Жидкости и объёмы", listOf(
        TechSpec("Масло двигателя", "3.6", "л", "5W-30 VW 504.00"),
        TechSpec("Масло DSG DQ200", "6.0", "л", "VAG G 052 182"),
        TechSpec("Антифриз", "5.6", "л", "G13"),
        TechSpec("Тормозная жидкость", "0.7", "л", "DOT 4"),
        TechSpec("Бак", "50", "л"),
        TechSpec("Омыватель", "5.5", "л"),
    )),
    specs(Icons.Filled.Speed, "Давления", listOf(
        TechSpec("Топливная рампа", "150", "бар", "непоср. впрыск TSI"),
        TechSpec("Давление масла (хол. ход.)", "2.0", "бар"),
        TechSpec("Давление масла (3000 об/мин)", "4.0", "бар"),
        TechSpec("Давление в шинах", "2.1", "бар", "Передние/задние"),
    )),
    specs(Icons.Filled.Build, "Моменты затяжки", listOf(
        TechSpec("Болты ГБЦ (1 этап)", "40", "Нм"),
        TechSpec("Болты ГБЦ (2 этап)", "+90° + 90°", "поворот"),
        TechSpec("Болты колеса", "120", "Нм"),
        TechSpec("Свечи зажигания", "25", "Нм"),
        TechSpec("Сливная пробка масла", "30", "Нм"),
    )),
    specs(Icons.Filled.Settings, "Зазоры", listOf(
        TechSpec("Зазор свечей зажигания", "0.7", "мм"),
        TechSpec("Зазор клапанов", "—", "", notes = "гидрокомпенсаторы, регулировка не требуется"),
    )),
    specs(Icons.Filled.Star, "Электрика", listOf(
        TechSpec("Аккумулятор", "59", "А·ч", "EN 540A"),
        TechSpec("Генератор", "140", "А"),
        TechSpec("Напряжение зарядки", "14.2", "В", "13.8–14.6 В"),
    )),
)

private fun ladaVestaSpecs() = listOf(
    specs(Icons.Filled.Info, "Жидкости и объёмы", listOf(
        TechSpec("Масло двигателя", "4.4", "л", "5W-40 API SN"),
        TechSpec("Масло МКПП", "2.25", "л", "TAD-17И / 75W-85 GL-4"),
        TechSpec("Антифриз", "7.0", "л", "Coolstream NRC"),
        TechSpec("Тормозная жидкость", "0.5", "л", "DOT 4"),
        TechSpec("Бак", "55", "л"),
        TechSpec("Омыватель", "4.7", "л"),
    )),
    specs(Icons.Filled.Speed, "Давления", listOf(
        TechSpec("Топливная рампа", "3.8", "бар", "3.6–4.0 бар"),
        TechSpec("Давление масла (хол. ход.)", "0.8", "бар"),
        TechSpec("Давление масла (3000 об/мин)", "2.5", "бар"),
        TechSpec("Давление в шинах", "2.1", "бар", "185/65 R15"),
    )),
    specs(Icons.Filled.Build, "Моменты затяжки", listOf(
        TechSpec("Болты ГБЦ (1 этап)", "20", "Нм"),
        TechSpec("Болты ГБЦ (2 этап)", "+90° + 90°", "поворот"),
        TechSpec("Болты колеса", "90", "Нм"),
        TechSpec("Свечи зажигания", "25", "Нм"),
        TechSpec("Сливная пробка масла", "35", "Нм"),
    )),
    specs(Icons.Filled.Settings, "Зазоры", listOf(
        TechSpec("Зазор свечей зажигания", "1.0", "мм"),
        TechSpec("Зазор клапанов (впуск)", "0.20", "мм", "холодный двигатель"),
        TechSpec("Зазор клапанов (выпуск)", "0.35", "мм", "холодный двигатель"),
    )),
    specs(Icons.Filled.Star, "Электрика", listOf(
        TechSpec("Аккумулятор", "62", "А·ч", "EN 600A"),
        TechSpec("Генератор", "115", "А"),
        TechSpec("Напряжение зарядки", "13.9", "В", "13.6–14.4 В"),
    )),
)

private fun mercedesSpecs() = listOf(
    specs(Icons.Filled.Info, "Жидкости и объёмы", listOf(
        TechSpec("Масло двигателя", "5.5", "л", "5W-30 MB 229.52"),
        TechSpec("Масло АКПП 9G-Tronic", "8.5", "л", "MB 236.17"),
        TechSpec("Антифриз", "9.5", "л", "MB 325.6"),
        TechSpec("Тормозная жидкость", "1.0", "л", "DOT 4+"),
        TechSpec("Бак", "66", "л"),
        TechSpec("Омыватель", "5.0", "л"),
    )),
    specs(Icons.Filled.Speed, "Давления", listOf(
        TechSpec("Давление масла (хол. ход.)", "1.2", "бар"),
        TechSpec("Давление масла (3000 об/мин)", "3.8", "бар"),
        TechSpec("Давление в шинах (перед)", "2.3", "бар"),
        TechSpec("Давление в шинах (зад)", "2.6", "бар"),
    )),
    specs(Icons.Filled.Build, "Моменты затяжки", listOf(
        TechSpec("Болты ГБЦ (1 этап)", "40", "Нм"),
        TechSpec("Болты ГБЦ (2 этап)", "+90° + 90°", "поворот"),
        TechSpec("Болты колеса", "130", "Нм"),
        TechSpec("Свечи зажигания", "22", "Нм"),
        TechSpec("Сливная пробка масла", "25", "Нм"),
    )),
    specs(Icons.Filled.Settings, "Зазоры", listOf(
        TechSpec("Зазор клапанов", "—", "", notes = "гидрокомпенсаторы, регулировка не требуется"),
    )),
    specs(Icons.Filled.Star, "Электрика", listOf(
        TechSpec("Аккумулятор", "80", "А·ч", "AGM 800A"),
        TechSpec("Генератор", "200", "А"),
        TechSpec("Напряжение зарядки", "14.3", "В", "14.0–14.7 В"),
    )),
)

private fun hyundaiSpecs() = listOf(
    specs(Icons.Filled.Info, "Жидкости и объёмы", listOf(
        TechSpec("Масло двигателя", "4.0", "л", "5W-30 API SP"),
        TechSpec("Масло АКПП", "6.8", "л", "ATF SP-IV"),
        TechSpec("Антифриз", "6.3", "л", "Hyundai LLC"),
        TechSpec("Тормозная жидкость", "0.8", "л", "DOT 4"),
        TechSpec("Бак", "50", "л"),
        TechSpec("Омыватель", "4.0", "л"),
    )),
    specs(Icons.Filled.Speed, "Давления", listOf(
        TechSpec("Топливная рампа", "3.5", "бар", "3.0–4.0 бар"),
        TechSpec("Давление масла (хол. ход.)", "1.0", "бар"),
        TechSpec("Давление масла (3000 об/мин)", "3.0", "бар"),
        TechSpec("Давление в шинах", "2.3", "бар"),
    )),
    specs(Icons.Filled.Build, "Моменты затяжки", listOf(
        TechSpec("Болты ГБЦ (1 этап)", "22", "Нм"),
        TechSpec("Болты ГБЦ (2 этап)", "+90° + 90°", "поворот"),
        TechSpec("Гайки колеса", "110", "Нм"),
        TechSpec("Свечи зажигания", "15", "Нм"),
        TechSpec("Сливная пробка масла", "35", "Нм"),
    )),
    specs(Icons.Filled.Settings, "Зазоры", listOf(
        TechSpec("Зазор свечей зажигания", "1.0", "мм"),
        TechSpec("Зазор клапанов (впуск)", "0.17", "мм", "холодный двигатель"),
        TechSpec("Зазор клапанов (выпуск)", "0.27", "мм", "холодный двигатель"),
    )),
    specs(Icons.Filled.Star, "Электрика", listOf(
        TechSpec("Аккумулятор", "60", "А·ч", "CCA 550A"),
        TechSpec("Генератор", "130", "А"),
        TechSpec("Напряжение зарядки", "14.1", "В", "13.7–14.5 В"),
    )),
)
