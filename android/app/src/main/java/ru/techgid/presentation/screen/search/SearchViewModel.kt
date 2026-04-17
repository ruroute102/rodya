package ru.techgid.presentation.screen.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.techgid.data.repository.AppPrefsRepository
import ru.techgid.domain.model.GuideListItem
import ru.techgid.domain.repository.GuideRepository
import javax.inject.Inject

private val DEFAULT_RECENT = listOf(
    "топливный насос",
    "замена масла",
    "тормозные колодки",
    "воздушный фильтр",
)

data class SearchUiState(
    val query: String = "",
    val results: List<GuideListItem> = emptyList(),
    val recentQueries: List<String> = DEFAULT_RECENT,
    val isSearching: Boolean = false,
)

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val guideRepository: GuideRepository,
    private val prefs: AppPrefsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            prefs.recentSearches.collect { saved ->
                val queries = saved.ifEmpty { DEFAULT_RECENT }
                _uiState.update { it.copy(recentQueries = queries) }
            }
        }
    }

    fun search(query: String) {
        _uiState.update { it.copy(query = query) }
        if (query.isBlank()) {
            _uiState.update { it.copy(results = emptyList(), isSearching = false) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            prefs.addRecentSearch(query)
            try {
                val result = guideRepository.getGuides(search = query)
                _uiState.update { it.copy(results = result.items, isSearching = false) }
            } catch (_: Exception) {
                _uiState.update { it.copy(results = emptyList(), isSearching = false) }
            }
        }
    }

    fun selectRecent(query: String) {
        search(query)
    }
}
