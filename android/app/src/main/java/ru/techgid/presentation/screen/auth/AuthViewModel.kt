package ru.techgid.presentation.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.techgid.data.repository.AppPrefsRepository
import javax.inject.Inject

data class AuthUiState(
    val phone: String = "",
    val password: String = "",
    val displayName: String = "",
    val otpCode: String = "",
    val isLoginMode: Boolean = true,
    val showOtpField: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false,
)

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val prefs: AppPrefsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val loggedIn = prefs.isLoggedIn.first()
            _uiState.update { it.copy(isAuthenticated = loggedIn) }
        }
    }

    fun updatePhone(phone: String) {
        _uiState.update { it.copy(phone = phone, errorMessage = null) }
    }

    fun updatePassword(password: String) {
        _uiState.update { it.copy(password = password, errorMessage = null) }
    }

    fun updateDisplayName(name: String) {
        _uiState.update { it.copy(displayName = name, errorMessage = null) }
    }

    fun updateOtpCode(code: String) {
        _uiState.update { it.copy(otpCode = code, errorMessage = null) }
    }

    fun toggleMode() {
        _uiState.update {
            it.copy(
                isLoginMode = !it.isLoginMode,
                showOtpField = false,
                errorMessage = null,
                otpCode = "",
            )
        }
    }

    fun requestOtp() {
        val state = _uiState.value
        if (state.phone.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Введите номер телефона") }
            return
        }
        // Offline mode: skip real OTP, just show the code field
        _uiState.update { it.copy(showOtpField = true) }
    }

    fun register() {
        val state = _uiState.value
        if (state.phone.isBlank() || state.otpCode.isBlank() ||
            state.displayName.isBlank() || state.password.isBlank()
        ) {
            _uiState.update { it.copy(errorMessage = "Заполните все поля") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            // Offline local auth: accept any 6-digit code, save profile locally
            if (state.otpCode.length == 6 && state.password.length >= 8) {
                prefs.saveProfile(name = state.displayName, phone = state.phone)
                _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Неверный код или пароль")
                }
            }
        }
    }

    fun login() {
        val state = _uiState.value
        if (state.phone.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Введите телефон и пароль") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            // Offline local auth: accept any password >= 8 chars
            if (state.password.length >= 8) {
                prefs.saveProfile(name = state.phone, phone = state.phone)
                _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
            } else {
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = "Пароль должен быть не менее 8 символов")
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
