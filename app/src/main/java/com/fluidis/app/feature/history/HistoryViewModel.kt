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
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val drinkEntryDao: DrinkEntryDao,
    private val settingsDataStore: SettingsDataStore,
    private val todayProvider: TodayProvider,
) : ViewModel() {

    private val _currentMonth = MutableStateFlow(todayProvider.today.value.monthYear())
    private val _selectedDate = MutableStateFlow(todayProvider.today.value)
    private val _entriesOpen = MutableStateFlow(false)

    init {
        followRollover()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<HistoryUiState> = combine(
        _currentMonth,
        _selectedDate,
        _entriesOpen,
        settingsDataStore.settings,
        todayProvider.today,
    ) { month, selectedDate, entriesOpen, settings, today ->
        Query(month, selectedDate, entriesOpen, settings, today)
    }.flatMapLatest { (month, selectedDate, entriesOpen, settings, today) ->
        combine(
            drinkEntryDao.getDailyTotalsInRange(month.firstDay.toString(), month.lastDay.toString()),
            drinkEntryDao.getEntriesForDate(selectedDate.toString()),
        ) { dailyTotals, selectedEntries ->
            HistoryUiState.Success(
                currentMonth = month,
                today = today,
                datesWithEntries = dailyTotals.associateBy { LocalDate.parse(it.date) },
                goalMl = settings.dailyGoalMl,
                goalUpperMl = settings.dailyGoalUpperMl,
                selectedDate = selectedDate,
                selectedDay = DaySummary(selectedEntries),
                month = MonthSummary.of(dailyTotals, settings.dailyGoalMl, settings.dailyGoalUpperMl),
                isEntriesOpen = entriesOpen,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HistoryUiState.Loading)

    /**
     * When the day rolls over, follow it — but only if the calendar was showing the month that
     * was current until now. Someone browsing an older month stays where they are, and so does
     * a deliberately picked other day in the current month.
     */
    private fun followRollover() {
        viewModelScope.launch {
            var previous = todayProvider.today.value
            todayProvider.today.collect { today ->
                val yesterday = previous
                previous = today
                if (today == yesterday || _currentMonth.value != yesterday.monthYear()) return@collect
                _currentMonth.value = today.monthYear()
                val selected = _selectedDate.value
                if (selected == yesterday || selected.monthYear() != today.monthYear()) {
                    _selectedDate.value = today
                }
            }
        }
    }

    private data class Query(
        val month: MonthYear,
        val selectedDate: LocalDate,
        val entriesOpen: Boolean,
        val settings: Settings,
        val today: LocalDate,
    )

    /**
     * Moving month selects the new month's day nearest today: today itself in the current
     * month, the last day of a past month, the first day of a future one.
     */
    fun navigateMonth(delta: Int) {
        val month = _currentMonth.value.plusMonths(delta)
        _currentMonth.value = month
        _selectedDate.value = nearestDay(todayProvider.today.value, month)
        _entriesOpen.value = false
    }

    /** There is always a selected day; tapping one moves the selection, never clears it. */
    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
        _currentMonth.value = date.monthYear()
    }

    fun goToToday() {
        val today = todayProvider.today.value
        _currentMonth.value = today.monthYear()
        _selectedDate.value = today
    }

    fun showEntries() {
        _entriesOpen.value = true
    }

    /** Closes the entries overlay; the day stays selected. */
    fun dismissDetail() {
        _entriesOpen.value = false
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

internal fun nearestDay(today: LocalDate, month: MonthYear): LocalDate = when {
    today.monthYear() == month -> today
    month.firstDay > today -> month.firstDay
    else -> month.lastDay
}
