package com.fluidis.app.feature.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fluidis.app.core.database.DrinkEntryDao
import com.fluidis.app.core.datastore.SettingsDataStore
import com.fluidis.app.core.model.DrinkEntry
import com.fluidis.app.core.model.DrinkType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val drinkEntryDao: DrinkEntryDao,
    private val settingsDataStore: SettingsDataStore,
) : ViewModel() {

    private fun currentDate(): String =
        Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()

    private val _today = MutableStateFlow(currentDate())

    private fun refreshDate() {
        _today.value = currentDate()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HomeUiState> = _today.flatMapLatest { date ->
        combine(
            drinkEntryDao.getEntriesForDate(date),
            drinkEntryDao.getTotalsPerDrinkForDate(date),
            settingsDataStore.settings,
        ) { entries, drinkTotals, settings ->
            val totalsMap = DrinkType.entries.associateWith { type ->
                drinkTotals.find { it.drinkType == type.key }?.total ?: 0
            }
            val servingCounts = DrinkType.entries.associateWith { type ->
                entries.count { it.drinkType == type.key }
            }
            HomeUiState.Success(
                totalMl = entries.sumOf { it.amountMl },
                goalMl = settings.dailyGoalMl,
                goalUpperMl = settings.dailyGoalUpperMl,
                drinkTotals = totalsMap,
                drinkServingCounts = servingCounts,
                maxServings = DrinkType.entries.associateWith { settings.maxServingsFor(it) },
                recentEntries = entries.take(5),
                servingSizes = DrinkType.entries.associateWith { settings.servingMlFor(it) },
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState.Loading)

    fun addEntry(drinkType: DrinkType, amountMl: Int) {
        if (amountMl <= 0) return
        refreshDate()
        viewModelScope.launch {
            drinkEntryDao.insert(
                DrinkEntry(
                    drinkType = drinkType.key,
                    amountMl = amountMl,
                    date = _today.value,
                    createdAt = Clock.System.now().toEpochMilliseconds(),
                )
            )
        }
    }

    fun undoLastEntry(drinkType: DrinkType) {
        refreshDate()
        viewModelScope.launch {
            val lastEntry = drinkEntryDao.getLastEntryForDrinkOnDate(_today.value, drinkType.key)
            if (lastEntry != null) {
                drinkEntryDao.delete(lastEntry)
            }
        }
    }

    fun clearAllEntries(drinkType: DrinkType) {
        refreshDate()
        viewModelScope.launch {
            drinkEntryDao.deleteAllForDrinkOnDate(_today.value, drinkType.key)
        }
    }
}
