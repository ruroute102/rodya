package ru.techgid.presentation.screen.catalog

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.techgid.domain.model.Difficulty
import ru.techgid.domain.model.GuideListItem
import ru.techgid.domain.repository.GuideRepository
import javax.inject.Inject

data class CatalogUiState(
    val guides: List<GuideListItem> = emptyList(),
    val searchQuery: String = "",
    val selectedDifficulty: Difficulty? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val currentPage: Int = 1,
    val totalPages: Int = 1,
)

@HiltViewModel
class CatalogViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val guideRepository: GuideRepository,
) : ViewModel() {

    private val configurationId: Int? = savedStateHandle.get<Int>("configurationId")

    private val _uiState = MutableStateFlow(CatalogUiState())
    val uiState: StateFlow<CatalogUiState> = _uiState.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadGuides()
    }

    fun search(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(300) // debounce
            loadGuides(resetPage = true)
        }
    }

    fun setDifficulty(difficulty: Difficulty?) {
        _uiState.update { it.copy(selectedDifficulty = difficulty) }
        loadGuides(resetPage = true)
    }

    fun loadNextPage() {
        val state = _uiState.value
        if (state.currentPage < state.totalPages && !state.isLoading) {
            loadGuides(page = state.currentPage + 1)
        }
    }

    private fun loadGuides(resetPage: Boolean = false, page: Int = 1) {
        viewModelScope.launch {
            val state = _uiState.value
            val targetPage = if (resetPage) 1 else page
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val result = guideRepository.getGuides(
                    configurationId = configurationId,
                    difficulty = state.selectedDifficulty?.name?.lowercase(),
                    search = state.searchQuery.ifBlank { null },
                    page = targetPage,
                )
                _uiState.update { current ->
                    val newGuides = if (targetPage == 1) {
                        result.items
                    } else {
                        current.guides + result.items
                    }
                    current.copy(
                        guides = newGuides,
                        isLoading = false,
                        currentPage = result.page,
                        totalPages = result.totalPages,
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load guides",
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
