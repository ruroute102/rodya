package ru.techgid.presentation.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.techgid.data.local.entity.ReminderEntity
import ru.techgid.data.local.entity.ServiceRecordEntity
import ru.techgid.data.repository.AppPrefsRepository
import ru.techgid.data.repository.FavoriteRepository
import ru.techgid.data.repository.ReminderRepository
import ru.techgid.data.repository.ServiceRecordRepository
import javax.inject.Inject

data class HomePopularGuide(
    val id: Int,
    val title: String,
    val meta: String,
    val difficulty: String,
)

data class HomeUiState(
    val carName: String = "Audi Q3 2011 · 2.0 TFSI",
    val configId: Int = 1,
    val favoritesCount: Int = 0,
    val historyCount: Int = 0,
    val totalSpentRub: Int = 0,
    val maxMileage: Int = 0,
    val lastRecord: ServiceRecordEntity? = null,
    val nextReminder: ReminderEntity? = null,
    val popularGuides: List<HomePopularGuide> = DEFAULT_POPULAR,
) {
    companion object {
        val DEFAULT_POPULAR = listOf(
            HomePopularGuide(1, "Замена топливного насоса", "6 шагов · 1.5 ч", "Средняя"),
            HomePopularGuide(3, "Замена масла и фильтра", "3 шага · 30 мин", "Лёгкая"),
            HomePopularGuide(4, "Замена тормозных колодок", "5 шагов · 1 ч", "Средняя"),
        )
    }
}

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val prefs: AppPrefsRepository,
    private val favorites: FavoriteRepository,
    private val history: ServiceRecordRepository,
    private val reminders: ReminderRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        val carInfoFlow = combine(
            prefs.selectedCarName,
            prefs.selectedConfigId,
            favorites.observeCount(),
            history.observeCount(),
            history.observeTotalCost(),
        ) { name, configId, favCount, histCount, total ->
            CarInfo(name, configId, favCount, histCount, total)
        }
        val activityFlow = combine(
            history.observeMaxMileage(),
            history.observeAll(),
            reminders.observeAll(),
        ) { maxKm, records, reminderList ->
            Activity(maxKm, records.firstOrNull(), reminderList.firstOrNull { !it.isDone })
        }
        viewModelScope.launch {
            combine(carInfoFlow, activityFlow) { info, activity ->
                HomeUiState(
                    carName = info.carName,
                    configId = info.configId,
                    favoritesCount = info.favoritesCount,
                    historyCount = info.historyCount,
                    totalSpentRub = info.totalSpentRub,
                    maxMileage = activity.maxMileage,
                    lastRecord = activity.lastRecord,
                    nextReminder = activity.nextReminder,
                )
            }.collect { state -> _uiState.update { state } }
        }
    }

    private data class CarInfo(
        val carName: String,
        val configId: Int,
        val favoritesCount: Int,
        val historyCount: Int,
        val totalSpentRub: Int,
    )

    private data class Activity(
        val maxMileage: Int,
        val lastRecord: ServiceRecordEntity?,
        val nextReminder: ReminderEntity?,
    )
}
