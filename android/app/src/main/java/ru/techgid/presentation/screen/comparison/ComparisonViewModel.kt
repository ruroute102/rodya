package ru.techgid.presentation.screen.comparison

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

data class RepairVariant(
    val label: String,
    val partName: String,
    val partNumber: String,
    val priceRange: String,
    val warrantyMonths: Int?,
    val durabilityKm: String?,
    val pros: List<String>,
    val cons: List<String>,
    val rating: Float,
)

data class ComparisonItem(
    val id: Int,
    val title: String,
    val componentName: String,
    val oem: RepairVariant,
    val aftermarket: RepairVariant,
)

data class ComparisonUiState(
    val items: List<ComparisonItem> = emptyList(),
    val selectedIndex: Int = 0,
) {
    val current: ComparisonItem? get() = items.getOrNull(selectedIndex)
}

@HiltViewModel
class ComparisonViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(ComparisonUiState(items = demoComparisons()))
    val uiState: StateFlow<ComparisonUiState> = _uiState.asStateFlow()

    fun selectItem(index: Int) {
        _uiState.update { it.copy(selectedIndex = index) }
    }

    private fun demoComparisons(): List<ComparisonItem> = listOf(
        ComparisonItem(
            id = 1,
            title = "Тормозные колодки передние",
            componentName = "Тормозная система",
            oem = RepairVariant(
                label = "OEM",
                partName = "Audi Original",
                partNumber = "8U0 698 151",
                priceRange = "4 500 – 6 000 ₽",
                warrantyMonths = 24,
                durabilityKm = "40 000 – 60 000 км",
                pros = listOf("Идеальная совместимость", "Гарантия производителя", "Тихая работа"),
                cons = listOf("Высокая цена", "Не всегда в наличии"),
                rating = 4.8f,
            ),
            aftermarket = RepairVariant(
                label = "Аналог",
                partName = "TRW GDB1550",
                partNumber = "GDB1550",
                priceRange = "1 800 – 2 500 ₽",
                warrantyMonths = 12,
                durabilityKm = "30 000 – 50 000 км",
                pros = listOf("Доступная цена", "Широко доступны", "Хорошее качество"),
                cons = listOf("Чуть больше пыли", "Возможен начальный скрип"),
                rating = 4.3f,
            ),
        ),
        ComparisonItem(
            id = 2,
            title = "Масляный фильтр",
            componentName = "Двигатель",
            oem = RepairVariant(
                label = "OEM",
                partName = "VAG Original",
                partNumber = "06L 115 562",
                priceRange = "900 – 1 300 ₽",
                warrantyMonths = 12,
                durabilityKm = "15 000 км (до замены масла)",
                pros = listOf("Точные размеры", "Качественный фильтрующий элемент", "Гарантия"),
                cons = listOf("Цена выше аналогов"),
                rating = 4.9f,
            ),
            aftermarket = RepairVariant(
                label = "Аналог",
                partName = "MANN-FILTER HU 7020 z",
                partNumber = "HU 7020 z",
                priceRange = "400 – 600 ₽",
                warrantyMonths = 6,
                durabilityKm = "15 000 км",
                pros = listOf("Проверенный бренд", "Низкая цена", "Широко доступен"),
                cons = listOf("Иногда чуть тугая посадка"),
                rating = 4.6f,
            ),
        ),
        ComparisonItem(
            id = 3,
            title = "Стойка стабилизатора",
            componentName = "Подвеска",
            oem = RepairVariant(
                label = "OEM",
                partName = "VAG Original",
                partNumber = "5Q0 411 315 A",
                priceRange = "3 200 – 4 500 ₽",
                warrantyMonths = 24,
                durabilityKm = "60 000 – 80 000 км",
                pros = listOf("Максимальный ресурс", "Идеальная геометрия", "Гарантия 2 года"),
                cons = listOf("Высокая цена"),
                rating = 4.7f,
            ),
            aftermarket = RepairVariant(
                label = "Аналог",
                partName = "Lemförder 37096 01",
                partNumber = "37096 01",
                priceRange = "1 200 – 1 800 ₽",
                warrantyMonths = 12,
                durabilityKm = "40 000 – 60 000 км",
                pros = listOf("OEM-поставщик Lemförder", "Хорошее соотношение цена/качество"),
                cons = listOf("Ресурс чуть ниже OEM", "Пыльник может быть тоньше"),
                rating = 4.4f,
            ),
        ),
        ComparisonItem(
            id = 4,
            title = "Воздушный фильтр",
            componentName = "Двигатель",
            oem = RepairVariant(
                label = "OEM",
                partName = "Audi Original",
                partNumber = "8U0 129 620 D",
                priceRange = "1 500 – 2 200 ₽",
                warrantyMonths = 12,
                durabilityKm = "30 000 км",
                pros = listOf("Точные размеры", "Оптимальная фильтрация"),
                cons = listOf("Высокая цена для расходника"),
                rating = 4.7f,
            ),
            aftermarket = RepairVariant(
                label = "Аналог",
                partName = "MAHLE LX 2831",
                partNumber = "LX 2831",
                priceRange = "500 – 800 ₽",
                warrantyMonths = 6,
                durabilityKm = "30 000 км",
                pros = listOf("Надёжный бренд", "Доступная цена", "Лёгкая установка"),
                cons = listOf("Минимальные отличия в пропускной способности"),
                rating = 4.5f,
            ),
        ),
    )
}
