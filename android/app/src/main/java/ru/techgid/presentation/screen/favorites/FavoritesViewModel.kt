package ru.techgid.presentation.screen.favorites

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.techgid.data.local.entity.FavoriteEntity
import ru.techgid.data.repository.FavoriteRepository
import javax.inject.Inject

data class FavoritesUiState(
    val items: List<FavoriteEntity> = emptyList(),
    val isLoading: Boolean = true,
)

sealed interface FavoritesEvent {
    data class Removed(val favorite: FavoriteEntity) : FavoritesEvent
}

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

    private val _events = Channel<FavoritesEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    fun remove(guideId: Int) {
        viewModelScope.launch {
            val target = favorites.observeAll().first().firstOrNull { it.guideId == guideId }
            favorites.remove(guideId)
            if (target != null) {
                _events.trySend(FavoritesEvent.Removed(target))
            }
        }
    }

    fun restore(favorite: FavoriteEntity) {
        viewModelScope.launch { favorites.restore(favorite) }
    }
}
