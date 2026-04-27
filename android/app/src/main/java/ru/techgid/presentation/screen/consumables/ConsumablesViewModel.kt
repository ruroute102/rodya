package ru.techgid.presentation.screen.consumables

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.techgid.data.repository.AppPrefsRepository
import javax.inject.Inject

enum class FluidType(val title: String, val shortName: String) {
    ENGINE_OIL("Моторное масло", "масло"),
    COOLANT("Охлаждающая жидкость", "антифриз"),
    BRAKE_FLUID("Тормозная жидкость", "тормозная"),
    TRANSMISSION_OIL("Трансмиссионное масло", "АКПП"),
    POWER_STEERING("Жидкость ГУР", "ГУР"),
    WASHER_FLUID("Жидкость омывателя", "омыватель"),
}

data class FluidRecommendation(
    val type: FluidType,
    val volumeLiters: Double,
    val viscosity: String? = null,
    val specification: String? = null,
    val note: String? = null,
)

data class ConsumablesUiState(
    val carName: String = "",
    val displacementLiters: Double = 2.0,
    val selectedType: FluidType = FluidType.ENGINE_OIL,
    val recommendation: FluidRecommendation = FluidRecommendation(
        type = FluidType.ENGINE_OIL,
        volumeLiters = 4.5,
        viscosity = "5W-30",
        specification = "VW 502.00 / API SN",
    ),
)

@HiltViewModel
class ConsumablesViewModel @Inject constructor(
    private val prefs: AppPrefsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ConsumablesUiState())
    val uiState: StateFlow<ConsumablesUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val carName = prefs.selectedCarName.first()
            val displacement = parseDisplacement(carName)
            _uiState.update {
                it.copy(
                    carName = carName,
                    displacementLiters = displacement,
                    recommendation = computeRecommendation(it.selectedType, displacement),
                )
            }
        }
    }

    fun selectFluid(type: FluidType) {
        _uiState.update {
            it.copy(
                selectedType = type,
                recommendation = computeRecommendation(type, it.displacementLiters),
            )
        }
    }

    fun setDisplacement(liters: Double) {
        val clamped = liters.coerceIn(0.6, 8.0)
        _uiState.update {
            it.copy(
                displacementLiters = clamped,
                recommendation = computeRecommendation(it.selectedType, clamped),
            )
        }
    }

    private fun parseDisplacement(carName: String): Double {
        val regex = Regex("""(\d[.,]\d)""")
        val match = regex.find(carName) ?: return 2.0
        return match.value.replace(',', '.').toDoubleOrNull() ?: 2.0
    }

    private fun computeRecommendation(type: FluidType, displacement: Double): FluidRecommendation {
        return when (type) {
            FluidType.ENGINE_OIL -> FluidRecommendation(
                type = type,
                volumeLiters = roundHalf(3.5 + (displacement - 1.0) * 0.7),
                viscosity = recommendOilViscosity(displacement),
                specification = "API SN/SP, ACEA A3/B4",
                note = "Точный объём указан в сервисной книжке. Проверяйте уровень щупом после заливки.",
            )
            FluidType.COOLANT -> FluidRecommendation(
                type = type,
                volumeLiters = roundHalf(5.0 + (displacement - 1.0) * 0.8),
                specification = "G12++/G13 (для VAG), концентрат разбавлять дистиллятом 50/50",
                note = "Перед заменой полностью слейте старый антифриз и промойте систему.",
            )
            FluidType.BRAKE_FLUID -> FluidRecommendation(
                type = type,
                volumeLiters = 1.0,
                specification = "DOT 4 / DOT 4+ (low viscosity для ESP)",
                note = "Меняйте раз в 2 года независимо от пробега. Гигроскопична.",
            )
            FluidType.TRANSMISSION_OIL -> FluidRecommendation(
                type = type,
                volumeLiters = roundHalf(4.0 + (displacement - 1.0) * 0.5),
                specification = "ATF DEXRON VI / DCT для роботов",
                note = "Объём при частичной замене, для полной — в 2 раза больше.",
            )
            FluidType.POWER_STEERING -> FluidRecommendation(
                type = type,
                volumeLiters = 1.2,
                specification = "PSF / Pentosin CHF 11S",
                note = "Прокачайте систему: руль до упора влево/вправо при работающем двигателе.",
            )
            FluidType.WASHER_FLUID -> FluidRecommendation(
                type = type,
                volumeLiters = 4.5,
                specification = "Незамерзайка до -25°C зимой / летняя летом",
                note = "Объём бачка стандартный 4-5 л.",
            )
        }
    }

    private fun recommendOilViscosity(displacement: Double): String = when {
        displacement < 1.4 -> "0W-20 / 5W-30"
        displacement < 2.0 -> "5W-30 / 5W-40"
        displacement < 3.0 -> "5W-40"
        else -> "5W-40 / 10W-40"
    }

    private fun roundHalf(value: Double): Double {
        return (Math.round(value * 2.0) / 2.0).coerceAtLeast(0.5)
    }
}
