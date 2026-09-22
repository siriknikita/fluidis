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
import kotlinx.coroutines.flow.map
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
        Triple(period, settings, today)
    }.flatMapLatest { (period, settings, today) ->
        val current = daysFor(period, settings.analyticsStartDate, today)
        val previous = previousDays(period, current)
        val allTime = daysFor(StatsPeriod.ALL_TIME, settings.analyticsStartDate, today)

        // One fetch covers every section: all time, the comparison period, and the six days
        // before the period that the first points of the rolling average reach back into.
        val fetchStart = listOfNotNull(
            allTime.start,
            previous?.start,
            current.start.minus(StatisticsReport.ROLLING_WINDOW - 1, DateTimeUnit.DAY),
        ).min()

        drinkEntryDao.getDailyDrinkTotalsInRange(fetchStart.toString(), today.toString()).map { rows ->
            StatisticsUiState.Success(
                period = period,
                chartMode = settings.selectedChartMode,
                goalMl = settings.dailyGoalMl,
                goalUpperMl = settings.dailyGoalUpperMl,
                report = StatisticsReport.build(
                    period = period,
                    current = current,
                    previous = previous,
                    allTime = allTime,
                    rows = rows,
                    goalMl = settings.dailyGoalMl,
                    upperMl = settings.dailyGoalUpperMl,
                ),
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

    companion object {
        /**
         * Week and month are rolling windows ending today. All time starts at the configured
         * analytics start date, or a year back when none has been set or detected.
         */
        fun daysFor(
            period: StatsPeriod,
            analyticsStartDate: LocalDate?,
            today: LocalDate,
        ): ClosedRange<LocalDate> {
            val start = when (period) {
                StatsPeriod.WEEK -> today.minus(DatePeriods.WEEK_OFFSET_DAYS, DateTimeUnit.DAY)
                StatsPeriod.MONTH -> today.minus(DatePeriods.MONTH_OFFSET_DAYS, DateTimeUnit.DAY)
                StatsPeriod.ALL_TIME ->
                    analyticsStartDate ?: today.minus(DatePeriods.DEFAULT_ALL_TIME_DAYS, DateTimeUnit.DAY)
            }
            // A start date set in the future would make an empty, inverted range.
            return minOf(start, today)..today
        }

        /** The same-length window ending the day before [current]. All time has none. */
        fun previousDays(period: StatsPeriod, current: ClosedRange<LocalDate>): ClosedRange<LocalDate>? {
            val length = when (period) {
                StatsPeriod.WEEK -> DatePeriods.WEEK_OFFSET_DAYS + 1
                StatsPeriod.MONTH -> DatePeriods.MONTH_OFFSET_DAYS + 1
                StatsPeriod.ALL_TIME -> return null
            }
            val end = current.start.minus(1, DateTimeUnit.DAY)
            return end.minus(length - 1, DateTimeUnit.DAY)..end
        }
    }
}
