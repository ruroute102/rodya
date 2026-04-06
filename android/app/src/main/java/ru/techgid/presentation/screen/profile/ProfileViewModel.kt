package ru.techgid.presentation.screen.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.techgid.data.local.dao.GuideDao
import ru.techgid.domain.repository.AuthRepository
import javax.inject.Inject

data class ProfileUiState(
    val userName: String = "",
    val userPhone: String = "",
    val userRole: String = "Пользователь",
    val userCar: String = "",
    val offlineCount: Int = 0,
    val commentsCount: Int = 0,
    val isLoggedIn: Boolean = false,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val guideDao: GuideDao,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadProfile()
    }

    private fun loadProfile() {
        viewModelScope.launch {
            val isLoggedIn = authRepository.isLoggedIn()
            val offlineCount = try { guideDao.getOfflineCount() } catch (_: Exception) { 0 }

            _uiState.update {
                it.copy(
                    isLoggedIn = isLoggedIn,
                    offlineCount = offlineCount,
                    userName = if (isLoggedIn) "Пользователь" else "",
                    userCar = "Audi Q3 2011 · 2.0 TFSI",
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                authRepository.logout()
            } catch (_: Exception) {
                // Even if network fails, clear local state
            }
            _uiState.update { ProfileUiState(isLoggedIn = false) }
        }
    }
}
