package com.fluidis.app.feature.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.fluidis.app.core.database.DrinkEntryDao
import com.fluidis.app.core.datastore.SettingsDataStore
import com.fluidis.app.core.model.ChartMode
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
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import com.fluidis.app.core.ui.DatePeriods
import javax.inject.Inject

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val drinkEntryDao: DrinkEntryDao,
    private val settingsDataStore: SettingsDataStore,
    private val todayProvider: TodayProvider,
) : ViewModel() {

    private val _period = MutableStateFlow(StatsPeriod.WEEK)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<StatisticsUiState> = combine(
        _period,
        settingsDataStore.settings,
        todayProvider.today,
    ) { period, settings, today ->
        Triple(period, settings, dateRangeFor(period, settings.analyticsStartDate, today))
    }.flatMapLatest { (period, settings, range) ->
        val (startDate, endDate) = range
        combine(
            drinkEntryDao.getDailyTotalsInRange(startDate, endDate),
            drinkEntryDao.getTotalsPerDrinkInRange(startDate, endDate),
        ) { dailyTotals, drinkBreakdown ->
            val daysTracked = dailyTotals.size
            val totalMl = dailyTotals.sumOf { it.total }
            val averageMl = if (daysTracked > 0) totalMl / daysTracked else 0
            val daysGoalMet = dailyTotals.count { it.total >= settings.dailyGoalMl }
            val daysOverUpper = if (settings.hasGoalRange) {
                dailyTotals.count { it.total > settings.effectiveUpperMl }
            } else 0

            StatisticsUiState.Success(
                period = period,
                chartMode = settings.selectedChartMode,
                dailyTotals = dailyTotals,
                drinkBreakdown = drinkBreakdown,
                averageMl = averageMl,
                daysTracked = daysTracked,
                daysGoalMet = daysGoalMet,
                goalMl = settings.dailyGoalMl,
                goalUpperMl = settings.dailyGoalUpperMl,
                daysOverUpper = daysOverUpper,
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), StatisticsUiState.Loading)

    fun setPeriod(period: StatsPeriod) {
        _period.value = period
    }

    fun setChartMode(mode: ChartMode) {
        viewModelScope.launch {
            settingsDataStore.updateSelectedChartMode(mode)
        }
    }

    private fun dateRangeFor(
        period: StatsPeriod,
        analyticsStartDate: LocalDate?,
        today: LocalDate,
    ): Pair<String, String> {
        val start = when (period) {
            StatsPeriod.WEEK -> today.minus(DatePeriods.WEEK_OFFSET_DAYS, DateTimeUnit.DAY)
            StatsPeriod.MONTH -> today.minus(DatePeriods.MONTH_OFFSET_DAYS, DateTimeUnit.DAY)
            StatsPeriod.ALL_TIME -> analyticsStartDate ?: today.minus(DatePeriods.DEFAULT_ALL_TIME_DAYS, DateTimeUnit.DAY)
        }
        return start.toString() to today.toString()
    }
}
