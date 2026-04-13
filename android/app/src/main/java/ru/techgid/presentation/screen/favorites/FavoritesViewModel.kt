package ru.techgid.presentation.screen.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.techgid.data.local.entity.FavoriteEntity
import ru.techgid.data.repository.FavoriteRepository
import javax.inject.Inject

data class FavoritesUiState(
    val items: List<FavoriteEntity> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class FavoritesViewModel @Inject constructor(
    private val favorites: FavoriteRepository,
) : ViewModel() {

    val uiState: StateFlow<FavoritesUiState> = favorites.observeAll()
        .map { FavoritesUiState(items = it, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = FavoritesUiState(),
        )

    fun remove(guideId: Int) {
        viewModelScope.launch { favorites.remove(guideId) }
    }
}
