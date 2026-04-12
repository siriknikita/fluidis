package com.fluidis.app.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fluidis.app.core.database.DrinkEntryDao
import com.fluidis.app.core.datastore.SettingsDataStore
import com.fluidis.app.core.model.DrinkType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore,
    private val drinkEntryDao: DrinkEntryDao,
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        settingsDataStore.settings,
        drinkEntryDao.getEarliestEntryDate(),
    ) { settings, earliestDate ->
        val detectedDate = earliestDate?.let { LocalDate.parse(it) }
        SettingsUiState.Success(
            goalMl = settings.dailyGoalMl,
            goalUpperMl = settings.dailyGoalUpperMl,
            servingSizes = DrinkType.entries.associateWith { settings.servingMlFor(it) },
            maxServings = DrinkType.entries.associateWith { settings.maxServingsFor(it) },
            analyticsStartDate = settings.analyticsStartDate,
            detectedStartDate = detectedDate,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), SettingsUiState.Loading)

    fun updateDailyGoal(goalMl: Int, upperMl: Int? = null) {
        if (goalMl <= 0) return
        viewModelScope.launch {
            settingsDataStore.updateDailyGoal(goalMl, upperMl)
        }
    }

    fun updateServingSize(drinkType: DrinkType, sizeMl: Int) {
        if (sizeMl <= 0) return
        viewModelScope.launch {
            settingsDataStore.updateServingSize(drinkType, sizeMl)
        }
    }

    fun updateMaxServings(drinkType: DrinkType, maxServings: Int) {
        viewModelScope.launch {
            settingsDataStore.updateMaxServings(drinkType, maxServings.coerceAtLeast(0))
        }
    }

    fun updateAnalyticsStartDate(date: LocalDate?) {
        viewModelScope.launch {
            settingsDataStore.updateAnalyticsStartDate(date)
        }
    }
}
