package ru.techgid.presentation.screen.reminders

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.techgid.data.local.entity.ReminderEntity
import ru.techgid.data.repository.ReminderRepository
import javax.inject.Inject

data class RemindersUiState(
    val items: List<ReminderEntity> = emptyList(),
    val isLoading: Boolean = true,
)

@HiltViewModel
class RemindersViewModel @Inject constructor(
    private val repository: ReminderRepository,
) : ViewModel() {

    val uiState: StateFlow<RemindersUiState> = repository.observeAll()
        .map { RemindersUiState(items = it, isLoading = false) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = RemindersUiState(),
        )

    fun add(title: String, dueMileage: Int?, dueDateIso: String?) {
        viewModelScope.launch {
            repository.add(title, dueMileage, dueDateIso)
        }
    }

    fun toggleDone(reminder: ReminderEntity) {
        viewModelScope.launch { repository.toggleDone(reminder) }
    }

    fun delete(id: Long) {
        viewModelScope.launch { repository.delete(id) }
    }
}
