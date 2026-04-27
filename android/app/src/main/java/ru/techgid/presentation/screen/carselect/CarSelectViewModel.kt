package ru.techgid.presentation.screen.carselect

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.techgid.data.repository.AppPrefsRepository
import ru.techgid.domain.model.CarBrand
import ru.techgid.domain.model.CarEngine
import ru.techgid.domain.model.CarGeneration
import ru.techgid.domain.model.CarModel
import ru.techgid.domain.repository.CarRepository
import javax.inject.Inject

data class CarSelectUiState(
    val brands: List<CarBrand> = emptyList(),
    val models: List<CarModel> = emptyList(),
    val generations: List<CarGeneration> = emptyList(),
    val engines: List<CarEngine> = emptyList(),
    val selectedBrand: CarBrand? = null,
    val selectedModel: CarModel? = null,
    val selectedGeneration: CarGeneration? = null,
    val selectedEngine: CarEngine? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
) {
    val isComplete: Boolean
        get() = selectedBrand != null &&
            selectedModel != null &&
            selectedGeneration != null &&
            selectedEngine != null
}

@HiltViewModel
class CarSelectViewModel @Inject constructor(
    private val carRepository: CarRepository,
    private val prefs: AppPrefsRepository,
) : ViewModel() {

    private val _selectedBrand = MutableStateFlow<CarBrand?>(null)
    private val _selectedModel = MutableStateFlow<CarModel?>(null)
    private val _selectedGeneration = MutableStateFlow<CarGeneration?>(null)
    private val _selectedEngine = MutableStateFlow<CarEngine?>(null)
    private val _brands = MutableStateFlow<List<CarBrand>>(emptyList())
    private val _models = MutableStateFlow<List<CarModel>>(emptyList())
    private val _generations = MutableStateFlow<List<CarGeneration>>(emptyList())
    private val _engines = MutableStateFlow<List<CarEngine>>(emptyList())
    private val _isLoading = MutableStateFlow(false)
    private val _error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CarSelectUiState> = combine(
        combine(_brands, _models, _generations, _engines) { brands, models, generations, engines ->
            CarSelectUiState(
                brands = brands,
                models = models,
                generations = generations,
                engines = engines,
            )
        },
        combine(_selectedBrand, _selectedModel, _selectedGeneration, _selectedEngine) { brand, model, gen, engine ->
            CarSelectUiState(
                selectedBrand = brand,
                selectedModel = model,
                selectedGeneration = gen,
                selectedEngine = engine,
            )
        },
        _isLoading,
        _error,
    ) { lists, selections, loading, error ->
        CarSelectUiState(
            brands = lists.brands,
            models = lists.models,
            generations = lists.generations,
            engines = lists.engines,
            selectedBrand = selections.selectedBrand,
            selectedModel = selections.selectedModel,
            selectedGeneration = selections.selectedGeneration,
            selectedEngine = selections.selectedEngine,
            isLoading = loading,
            error = error,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CarSelectUiState(isLoading = true),
    )

    init {
        loadBrands()
    }

    private fun loadBrands() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                // Trigger network refresh in background, ignore failure (offline-first)
                launch {
                    try {
                        carRepository.refreshBrands()
                    } catch (_: Exception) {
                        // Offline — rely on cached data
                    }
                }
                carRepository.getBrands().collect { brands ->
                    _brands.value = brands
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load brands"
                _isLoading.value = false
            }
        }
    }

    fun selectBrand(brand: CarBrand) {
        _selectedBrand.value = brand
        _selectedModel.value = null
        _selectedGeneration.value = null
        _selectedEngine.value = null
        _models.value = emptyList()
        _generations.value = emptyList()
        _engines.value = emptyList()
        loadModels(brand.id)
    }

    private fun loadModels(brandId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                launch {
                    try {
                        carRepository.refreshModels(brandId)
                    } catch (_: Exception) {
                        // Offline — rely on cached data
                    }
                }
                carRepository.getModels(brandId).collect { models ->
                    _models.value = models
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load models"
                _isLoading.value = false
            }
        }
    }

    fun selectModel(model: CarModel) {
        _selectedModel.value = model
        _selectedGeneration.value = null
        _selectedEngine.value = null
        _generations.value = emptyList()
        _engines.value = emptyList()
        loadGenerations(model.id)
    }

    private fun loadGenerations(modelId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                launch {
                    try {
                        carRepository.refreshGenerations(modelId)
                    } catch (_: Exception) {
                        // Offline — rely on cached data
                    }
                }
                carRepository.getGenerations(modelId).collect { generations ->
                    _generations.value = generations
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load generations"
                _isLoading.value = false
            }
        }
    }

    fun selectGeneration(generation: CarGeneration) {
        _selectedGeneration.value = generation
        _selectedEngine.value = null
        _engines.value = emptyList()
        loadEngines(generation.id)
    }

    private fun loadEngines(generationId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                launch {
                    try {
                        carRepository.refreshEngines(generationId)
                    } catch (_: Exception) {
                        // Offline — rely on cached data
                    }
                }
                carRepository.getEngines(generationId).collect { engines ->
                    _engines.value = engines
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to load engines"
                _isLoading.value = false
            }
        }
    }

    fun selectEngine(engine: CarEngine) {
        _selectedEngine.value = engine
        saveSelection()
    }

    private fun saveSelection() {
        val brand = _selectedBrand.value
        val model = _selectedModel.value
        val gen = _selectedGeneration.value
        val engine = _selectedEngine.value
        if (brand != null && model != null && gen != null && engine != null) {
            val displayName = buildString {
                append(brand.name)
                append(' ')
                append(model.name)
                append(' ')
                append(gen.yearStart)
                engine.displacementLabel?.let { append(" · $it") }
            }
            viewModelScope.launch {
                prefs.setSelectedCar(configId = engine.id, displayName = displayName)
            }
        }
    }

    fun clearError() {
        _error.value = null
    }

    fun reset() {
        _selectedBrand.value = null
        _selectedModel.value = null
        _selectedGeneration.value = null
        _selectedEngine.value = null
        _models.value = emptyList()
        _generations.value = emptyList()
        _engines.value = emptyList()
    }
}
