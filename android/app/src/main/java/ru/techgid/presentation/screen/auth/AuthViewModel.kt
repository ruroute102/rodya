package ru.techgid.presentation.screen.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.techgid.domain.repository.AuthRepository
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
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        _uiState.update { it.copy(isAuthenticated = authRepository.isLoggedIn()) }
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
            _uiState.update { it.copy(errorMessage = "Please enter phone number") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val result = authRepository.requestOtp(state.phone)
                if (result != null) {
                    _uiState.update {
                        it.copy(isLoading = false, showOtpField = true)
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "Failed to send OTP")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Failed to request OTP",
                    )
                }
            }
        }
    }

    fun register() {
        val state = _uiState.value
        if (state.phone.isBlank() || state.otpCode.isBlank() ||
            state.displayName.isBlank() || state.password.isBlank()
        ) {
            _uiState.update { it.copy(errorMessage = "Please fill in all fields") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val success = authRepository.register(
                    phone = state.phone,
                    code = state.otpCode,
                    displayName = state.displayName,
                    password = state.password,
                )
                if (success) {
                    _uiState.update {
                        it.copy(isLoading = false, isAuthenticated = true)
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "Registration failed")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Registration failed",
                    )
                }
            }
        }
    }

    fun login() {
        val state = _uiState.value
        if (state.phone.isBlank() || state.password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter phone and password") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val success = authRepository.login(
                    phone = state.phone,
                    password = state.password,
                )
                if (success) {
                    _uiState.update {
                        it.copy(isLoading = false, isAuthenticated = true)
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = "Invalid credentials")
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.message ?: "Login failed",
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }
}
