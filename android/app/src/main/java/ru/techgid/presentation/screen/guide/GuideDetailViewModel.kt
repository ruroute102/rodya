package ru.techgid.presentation.screen.guide

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.techgid.data.repository.FavoriteRepository
import ru.techgid.data.repository.StepProgressRepository
import ru.techgid.domain.model.Comment
import ru.techgid.domain.model.GuideDetail
import ru.techgid.domain.repository.GuideRepository
import javax.inject.Inject

data class GuideDetailUiState(
    val guideDetail: GuideDetail? = null,
    val currentStepIndex: Int = 0,
    val comments: List<Comment> = emptyList(),
    val commentText: String = "",
    val isLoading: Boolean = false,
    val isSavedOffline: Boolean = false,
    val isFavorite: Boolean = false,
    val doneStepIds: Set<Int> = emptySet(),
    val error: String? = null,
) {
    val totalSteps: Int get() = guideDetail?.steps?.size ?: 0
    val currentStep get() = guideDetail?.steps?.getOrNull(currentStepIndex)
    val canGoNext: Boolean get() = currentStepIndex < totalSteps - 1
    val canGoPrev: Boolean get() = currentStepIndex > 0
}

@HiltViewModel
class GuideDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val guideRepository: GuideRepository,
    private val favoriteRepository: FavoriteRepository,
    private val stepProgressRepository: StepProgressRepository,
) : ViewModel() {

    private val guideId: Int = checkNotNull(savedStateHandle.get<Int>("guideId"))

    private val _uiState = MutableStateFlow(GuideDetailUiState(isLoading = true))
    val uiState: StateFlow<GuideDetailUiState> = _uiState.asStateFlow()

    init {
        loadGuideDetail()
        checkOfflineStatus()
        observeFavorite()
        observeStepProgress()
    }

    private fun loadGuideDetail() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val detail = guideRepository.getGuide(guideId)
                _uiState.update { it.copy(guideDetail = detail, isLoading = false) }
                loadComments()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load guide",
                    )
                }
            }
        }
    }

    private fun loadComments() {
        viewModelScope.launch {
            try {
                val currentStep = _uiState.value.currentStep
                val comments = guideRepository.getComments(
                    guideId = guideId,
                    stepId = currentStep?.id,
                )
                _uiState.update { it.copy(comments = comments) }
            } catch (_: Exception) {
                // Comments are non-critical; silently ignore
            }
        }
    }

    private fun checkOfflineStatus() {
        viewModelScope.launch {
            try {
                val saved = guideRepository.isGuideSavedOffline(guideId)
                _uiState.update { it.copy(isSavedOffline = saved) }
            } catch (_: Exception) {
                // Ignore
            }
        }
    }

    private fun observeFavorite() {
        viewModelScope.launch {
            favoriteRepository.observeIsFavorite(guideId).collect { isFav ->
                _uiState.update { it.copy(isFavorite = isFav) }
            }
        }
    }

    private fun observeStepProgress() {
        viewModelScope.launch {
            stepProgressRepository.observeForGuide(guideId).collect { progressList ->
                val doneIds = progressList.filter { it.isDone }.map { it.stepId }.toSet()
                _uiState.update { it.copy(doneStepIds = doneIds) }
            }
        }
    }

    fun toggleFavorite() {
        viewModelScope.launch {
            val detail = _uiState.value.guideDetail ?: return@launch
            favoriteRepository.toggle(
                guideId = guideId,
                title = detail.title,
                componentName = detail.componentName,
                difficulty = detail.difficulty.label,
                estimatedTimeMin = detail.estimatedTimeMin,
            )
        }
    }

    fun toggleStepDone(stepId: Int) {
        viewModelScope.launch {
            val isDone = stepId in _uiState.value.doneStepIds
            stepProgressRepository.setStepDone(guideId, stepId, !isDone)
        }
    }

    fun nextStep() {
        val state = _uiState.value
        if (state.canGoNext) {
            _uiState.update { it.copy(currentStepIndex = state.currentStepIndex + 1) }
            loadComments()
        }
    }

    fun prevStep() {
        val state = _uiState.value
        if (state.canGoPrev) {
            _uiState.update { it.copy(currentStepIndex = state.currentStepIndex - 1) }
            loadComments()
        }
    }

    fun updateCommentText(text: String) {
        _uiState.update { it.copy(commentText = text) }
    }

    fun addComment() {
        val state = _uiState.value
        val text = state.commentText.trim()
        if (text.isBlank()) return

        viewModelScope.launch {
            try {
                val comment = guideRepository.addComment(
                    guideId = guideId,
                    stepId = state.currentStep?.id,
                    parentId = null,
                    text = text,
                )
                _uiState.update {
                    it.copy(
                        comments = it.comments + comment,
                        commentText = "",
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to add comment")
                }
            }
        }
    }

    fun toggleOfflineSave() {
        viewModelScope.launch {
            val state = _uiState.value
            try {
                if (state.isSavedOffline) {
                    guideRepository.removeGuideOffline(guideId)
                    _uiState.update { it.copy(isSavedOffline = false) }
                } else {
                    guideRepository.saveGuideOffline(guideId, configurationId = 0)
                    _uiState.update { it.copy(isSavedOffline = true) }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(error = e.message ?: "Failed to toggle offline save")
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
