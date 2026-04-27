package ru.techgid.presentation.screen.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.techgid.data.local.entity.ServiceRecordEntity
import ru.techgid.data.repository.ServiceRecordRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

enum class ServiceSortMode(val label: String) {
    DATE_DESC("По дате"),
    COST_DESC("По стоимости"),
    MILEAGE_DESC("По пробегу"),
}

data class ServiceHistoryUiState(
    val records: List<ServiceRecordEntity> = emptyList(),
    val totalCost: Int = 0,
    val maxMileage: Int = 0,
    val count: Int = 0,
    val sortMode: ServiceSortMode = ServiceSortMode.DATE_DESC,
)

@HiltViewModel
class ServiceHistoryViewModel @Inject constructor(
    private val repository: ServiceRecordRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServiceHistoryUiState())
    val uiState: StateFlow<ServiceHistoryUiState> = _uiState.asStateFlow()

    init {
        observe()
        ensureSeed()
    }

    private fun observe() {
        viewModelScope.launch {
            combine(
                repository.observeAll(),
                repository.observeTotalCost(),
                repository.observeMaxMileage(),
                repository.observeCount(),
            ) { records, total, mileage, count ->
                Quad(records, total, mileage, count)
            }.collect { (records, total, mileage, count) ->
                _uiState.update { current ->
                    current.copy(
                        records = applySort(records, current.sortMode),
                        totalCost = total,
                        maxMileage = mileage,
                        count = count,
                    )
                }
            }
        }
    }

    fun setSortMode(mode: ServiceSortMode) {
        _uiState.update { it.copy(sortMode = mode, records = applySort(it.records, mode)) }
    }

    private fun applySort(records: List<ServiceRecordEntity>, mode: ServiceSortMode): List<ServiceRecordEntity> =
        when (mode) {
            ServiceSortMode.DATE_DESC -> records.sortedByDescending { it.dateIso }
            ServiceSortMode.COST_DESC -> records.sortedByDescending { it.costRub }
            ServiceSortMode.MILEAGE_DESC -> records.sortedByDescending { it.mileageKm }
        }

    private data class Quad(
        val records: List<ServiceRecordEntity>,
        val total: Int,
        val mileage: Int,
        val count: Int,
    )

    private fun ensureSeed() {
        viewModelScope.launch {
            val count = repository.observeCount().first()
            if (count == 0) {
                listOf(
                    ServiceRecordEntity(
                        title = "Замена масла и фильтра",
                        dateIso = "2026-03-15",
                        mileageKm = 152340,
                        notes = "Mobil 1 5W-30, фильтр Mahle",
                        costRub = 4200,
                        category = "engine",
                    ),
                    ServiceRecordEntity(
                        title = "Замена тормозных колодок (перед)",
                        dateIso = "2026-02-10",
                        mileageKm = 151200,
                        notes = "ATE Ceramic, оригинал",
                        costRub = 8500,
                        category = "brakes",
                    ),
                    ServiceRecordEntity(
                        title = "Замена воздушного фильтра",
                        dateIso = "2026-01-20",
                        mileageKm = 150100,
                        notes = "Mann C 30 005",
                        costRub = 1200,
                        category = "engine",
                    ),
                    ServiceRecordEntity(
                        title = "Замена свечей зажигания",
                        dateIso = "2025-12-10",
                        mileageKm = 148500,
                        notes = "NGK PFR7S8EG, момент 25 Нм",
                        costRub = 3600,
                        category = "ignition",
                    ),
                ).forEach { repository.add(it) }
            }
        }
    }

    fun add(title: String, mileageKm: Int, costRub: Int, notes: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            repository.add(
                ServiceRecordEntity(
                    title = title.trim(),
                    dateIso = today,
                    mileageKm = mileageKm,
                    notes = notes.trim(),
                    costRub = costRub,
                )
            )
        }
    }

    fun delete(id: Long) {
        viewModelScope.launch { repository.delete(id) }
    }
}
