package ru.techgid.presentation.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.techgid.data.local.dao.GuideDao
import ru.techgid.data.repository.AppPrefsRepository
import ru.techgid.data.repository.FavoriteRepository
import ru.techgid.data.repository.ServiceRecordRepository
import javax.inject.Inject

data class ProfileUiState(
    val userName: String = "",
    val userPhone: String = "",
    val userRole: String = "Пользователь",
    val userCar: String = "",
    val offlineCount: Int = 0,
    val commentsCount: Int = 0,
    val favoritesCount: Int = 0,
    val historyCount: Int = 0,
    val isLoggedIn: Boolean = false,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val prefs: AppPrefsRepository,
    private val favorites: FavoriteRepository,
    private val history: ServiceRecordRepository,
    private val guideDao: GuideDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                prefs.profileName,
                prefs.profilePhone,
                prefs.isLoggedIn,
                prefs.selectedCarName,
                favorites.observeCount(),
                history.observeCount(),
            ) { values ->
                val name = values[0] as String
                val phone = values[1] as String
                val loggedIn = values[2] as Boolean
                val car = values[3] as String
                @Suppress("UNCHECKED_CAST")
                val favCount = values[4] as Int
                @Suppress("UNCHECKED_CAST")
                val histCount = values[5] as Int
                val offlineCount = try { guideDao.getOfflineCount() } catch (_: Exception) { 0 }
                ProfileUiState(
                    userName = name.ifBlank { if (loggedIn) "Пользователь" else "" },
                    userPhone = phone,
                    isLoggedIn = loggedIn,
                    userCar = car,
                    offlineCount = offlineCount,
                    favoritesCount = favCount,
                    historyCount = histCount,
                )
            }.collect { state -> _uiState.update { state } }
        }
    }

    fun logout() {
        viewModelScope.launch {
            prefs.logout()
        }
    }
}
