package com.fluidis.app.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fluidis.app.core.database.DrinkEntryDao
import com.fluidis.app.core.datastore.SettingsDataStore
import com.fluidis.app.core.model.DrinkEntry
import com.fluidis.app.core.model.DrinkType
import com.fluidis.app.core.model.Settings
import com.fluidis.app.core.time.TodayProvider
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val drinkEntryDao: DrinkEntryDao,
    private val settingsDataStore: SettingsDataStore,
    private val todayProvider: TodayProvider,
) : ViewModel() {

    private val _currentMonth = MutableStateFlow(todayProvider.today.value.monthYear())
    private val _selectedDate = MutableStateFlow<LocalDate?>(null)

    init {
        followMonthRollover()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HistoryUiState> = combine(
        _currentMonth,
        _selectedDate,
        settingsDataStore.settings,
        todayProvider.today,
    ) { month, selectedDate, settings, today ->
        MonthQuery(month, selectedDate, settings, today)
    }.flatMapLatest { (month, selectedDate, settings, today) ->
        val goalMl = settings.dailyGoalMl
        val firstDay = LocalDate(month.year, month.month, 1)
        val lastDay = firstDay.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)

        val entriesFlow = if (selectedDate != null) {
            drinkEntryDao.getEntriesForDate(selectedDate.toString())
        } else {
            kotlinx.coroutines.flow.flowOf(emptyList())
        }

        combine(
            drinkEntryDao.getDailyTotalsInRange(firstDay.toString(), lastDay.toString()),
            entriesFlow,
        ) { dailyTotals, selectedEntries ->
            val datesMap = dailyTotals.associate { daily ->
                LocalDate.parse(daily.date) to daily
            }
            HistoryUiState.Success(
                currentMonth = month,
                today = today,
                datesWithEntries = datesMap,
                goalMl = goalMl,
                goalUpperMl = settings.dailyGoalUpperMl,
                selectedDate = selectedDate,
                selectedDateEntries = selectedEntries,
                selectedDateTotal = selectedEntries.sumOf { it.amountMl },
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState.Loading)

    /**
     * When the day rolls into a new month, follow it — but only if the calendar was showing the
     * month that was current until now. Someone browsing an older month stays where they are.
     */
    private fun followMonthRollover() {
        viewModelScope.launch {
            var previous = todayProvider.today.value
            todayProvider.today.collect { today ->
                val previousMonth = previous.monthYear()
                previous = today
                if (_currentMonth.value == previousMonth && today.monthYear() != previousMonth) {
                    _currentMonth.value = today.monthYear()
                    _selectedDate.value = null
                }
            }
        }
    }

    private data class MonthQuery(
        val month: MonthYear,
        val selectedDate: LocalDate?,
        val settings: Settings,
        val today: LocalDate,
    )

    fun navigateMonth(delta: Int) {
        _currentMonth.update { current ->
            val monthOrdinal = current.month.ordinal + delta
            val newYear = current.year + monthOrdinal.floorDiv(12)
            val newMonth = Month.entries[((monthOrdinal % 12) + 12) % 12]
            MonthYear(newYear, newMonth)
        }
        _selectedDate.value = null
    }

    fun selectDate(date: LocalDate) {
        _selectedDate.value = if (_selectedDate.value == date) null else date
    }

    fun dismissDetail() {
        _selectedDate.value = null
    }

    fun deleteEntry(entry: DrinkEntry) {
        viewModelScope.launch {
            drinkEntryDao.delete(entry)
        }
    }

    fun updateEntry(entry: DrinkEntry, newAmountMl: Int, newDrinkType: DrinkType) {
        viewModelScope.launch {
            drinkEntryDao.update(
                entry.copy(
                    amountMl = newAmountMl,
                    drinkType = newDrinkType.key,
                )
            )
        }
    }

    fun addEntryToDate(date: LocalDate, drinkType: DrinkType, amountMl: Int) {
        if (amountMl <= 0) return
        viewModelScope.launch {
            drinkEntryDao.insert(
                DrinkEntry(
                    drinkType = drinkType.key,
                    amountMl = amountMl,
                    date = date.toString(),
                    createdAt = Clock.System.now().toEpochMilliseconds(),
                )
            )
        }
    }
}

private fun LocalDate.monthYear() = MonthYear(year, month)
