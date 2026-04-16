package ru.techgid.presentation.screen.viewer3d

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import ru.techgid.data.repository.AppPrefsRepository
import javax.inject.Inject

@HiltViewModel
class Viewer3DViewModel @Inject constructor(
    prefs: AppPrefsRepository,
) : ViewModel() {
    val carName: StateFlow<String> = prefs.selectedCarName
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "Audi Q3 2011 · 2.0 TFSI")

    val mesh: StateFlow<Mesh> = prefs.selectedCarName
        .map { buildMeshForCar(it) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), buildMeshForCar("Audi Q3 2011 · 2.0 TFSI"))
}
