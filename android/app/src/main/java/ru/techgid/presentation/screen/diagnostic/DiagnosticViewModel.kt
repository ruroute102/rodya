package ru.techgid.presentation.screen.diagnostic

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.techgid.domain.model.DiagnosticResult
import ru.techgid.domain.model.SymptomCategory
import ru.techgid.domain.repository.DiagnosticRepository
import javax.inject.Inject

data class DiagnosticUiState(
    val symptomCategories: List<SymptomCategory> = emptyList(),
    val selectedSymptomIds: Set<Int> = emptySet(),
    val results: List<DiagnosticResult> = emptyList(),
    val showResults: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)

@HiltViewModel
class DiagnosticViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val diagnosticRepository: DiagnosticRepository,
) : ViewModel() {

    private val configurationId: Int = savedStateHandle.get<Int>("configurationId") ?: 0

    private val _uiState = MutableStateFlow(DiagnosticUiState(isLoading = true))
    val uiState: StateFlow<DiagnosticUiState> = _uiState.asStateFlow()

    init {
        loadSymptoms()
    }

    private fun loadSymptoms() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val categories = diagnosticRepository.getSymptomCategories()
                _uiState.update { it.copy(symptomCategories = categories, isLoading = false) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Ошибка загрузки симптомов")
                }
            }
        }
    }

    fun toggleSymptom(symptomId: Int) {
        _uiState.update { state ->
            val newSet = state.selectedSymptomIds.toMutableSet()
            if (symptomId in newSet) newSet.remove(symptomId) else newSet.add(symptomId)
            state.copy(selectedSymptomIds = newSet)
        }
    }

    fun diagnose() {
        val state = _uiState.value
        if (state.selectedSymptomIds.isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val results = diagnosticRepository.diagnose(
                    configurationId = configurationId,
                    symptomIds = state.selectedSymptomIds.toList(),
                )
                _uiState.update {
                    it.copy(results = results, showResults = true, isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message ?: "Ошибка диагностики")
                }
            }
        }
    }

    fun goBackToSymptoms() {
        _uiState.update { it.copy(showResults = false) }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
