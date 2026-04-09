package ru.techgid.presentation.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.techgid.data.repository.AppPrefsRepository
import ru.techgid.data.repository.FavoriteRepository
import ru.techgid.data.repository.ServiceRecordRepository
import javax.inject.Inject

data class HomePopularGuide(
    val id: Int,
    val title: String,
    val meta: String,
    val difficulty: String,
)

data class HomeUiState(
    val carName: String = "Audi Q3 2011 · 2.0 TFSI",
    val configId: Int = 1,
    val favoritesCount: Int = 0,
    val historyCount: Int = 0,
    val totalSpentRub: Int = 0,
    val popularGuides: List<HomePopularGuide> = DEFAULT_POPULAR,
) {
    companion object {
        val DEFAULT_POPULAR = listOf(
            HomePopularGuide(1, "Замена топливного насоса", "6 шагов · 1.5 ч", "Средняя"),
            HomePopularGuide(3, "Замена масла и фильтра", "3 шага · 30 мин", "Лёгкая"),
            HomePopularGuide(4, "Замена тормозных колодок", "5 шагов · 1 ч", "Средняя"),
        )
    }
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val prefs: AppPrefsRepository,
    private val favorites: FavoriteRepository,
    private val history: ServiceRecordRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                prefs.selectedCarName,
                prefs.selectedConfigId,
                favorites.observeCount(),
                history.observeCount(),
                history.observeTotalCost(),
            ) { name, configId, favCount, hist, total ->
                HomeUiState(
                    carName = name,
                    configId = configId,
                    favoritesCount = favCount,
                    historyCount = hist,
                    totalSpentRub = total,
                )
            }.collect { state -> _uiState.update { state } }
        }
    }
}
