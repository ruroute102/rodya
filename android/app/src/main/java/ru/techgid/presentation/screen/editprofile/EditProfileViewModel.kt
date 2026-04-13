package ru.techgid.presentation.screen.editprofile

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

data class EditProfileUiState(
    val name: String = "",
    val phone: String = "",
    val saved: Boolean = false,
    val isLoaded: Boolean = false,
)

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val prefs: AppPrefsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditProfileUiState())
    val uiState: StateFlow<EditProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val name = prefs.profileName.first()
            val phone = prefs.profilePhone.first()
            _uiState.update { it.copy(name = name, phone = phone, isLoaded = true) }
        }
    }

    fun onNameChange(value: String) {
        _uiState.update { it.copy(name = value, saved = false) }
    }

    fun onPhoneChange(value: String) {
        _uiState.update { it.copy(phone = value, saved = false) }
    }

    fun save() {
        viewModelScope.launch {
            prefs.saveProfile(_uiState.value.name.trim(), _uiState.value.phone.trim())
            _uiState.update { it.copy(saved = true) }
        }
    }

    fun consumeSaved() {
        _uiState.update { it.copy(saved = false) }
    }
}
