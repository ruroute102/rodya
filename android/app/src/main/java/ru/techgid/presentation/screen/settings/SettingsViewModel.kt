package ru.techgid.presentation.screen.settings

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
import javax.inject.Inject

data class SettingsUiState(
    val darkTheme: Boolean = false,
    val notifications: Boolean = true,
    val maintenanceReminders: Boolean = true,
    val offlineSync: Boolean = false,
    val language: String = "ru",
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val prefs: AppPrefsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                prefs.darkTheme,
                prefs.notifications,
                prefs.maintenanceReminders,
                prefs.offlineSync,
                prefs.language,
            ) { dark, notif, maint, sync, lang ->
                SettingsUiState(
                    darkTheme = dark,
                    notifications = notif,
                    maintenanceReminders = maint,
                    offlineSync = sync,
                    language = lang,
                )
            }.collect { state -> _uiState.update { state } }
        }
    }

    fun toggleDarkTheme(value: Boolean) = viewModelScope.launch { prefs.setDarkTheme(value) }
    fun toggleNotifications(value: Boolean) = viewModelScope.launch { prefs.setNotifications(value) }
    fun toggleMaintenanceReminders(value: Boolean) = viewModelScope.launch { prefs.setMaintenanceReminders(value) }
    fun toggleOfflineSync(value: Boolean) = viewModelScope.launch { prefs.setOfflineSync(value) }
}
