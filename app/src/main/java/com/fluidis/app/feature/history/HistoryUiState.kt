package com.fluidis.app.feature.history

import com.fluidis.app.core.model.DailyTotal
import com.fluidis.app.core.model.DrinkEntry
import com.fluidis.app.core.model.DrinkType
import com.fluidis.app.core.model.GoalStatus
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.minus
import kotlinx.datetime.plus

data class MonthYear(val year: Int, val month: Month) {
    fun displayName(): String = "${monthName()} $year"

    fun monthName(): String = month.name.lowercase().replaceFirstChar { it.uppercase() }

    val firstDay: LocalDate get() = LocalDate(year, month, 1)
    val lastDay: LocalDate get() = firstDay.plus(1, DateTimeUnit.MONTH).minus(1, DateTimeUnit.DAY)

    fun plusMonths(delta: Int): MonthYear = firstDay.plus(delta, DateTimeUnit.MONTH).monthYear()
}

fun LocalDate.monthYear() = MonthYear(year, month)

sealed interface HistoryUiState {
    data object Loading : HistoryUiState

    data class Success(
        val currentMonth: MonthYear,
        val today: LocalDate,
        val datesWithEntries: Map<LocalDate, DailyTotal>,
        val goalMl: Int,
        val goalUpperMl: Int?,
        /** Always a day inside [currentMonth] — the day card never has nothing to show. */
        val selectedDate: LocalDate,
        val selectedDay: DaySummary,
        val month: MonthSummary,
        /** Whether the entries overlay for [selectedDate] is open. */
        val isEntriesOpen: Boolean,
    ) : HistoryUiState
}

/** One drink's share of a single day. */
data class DrinkDayTotal(
    val drinkType: DrinkType,
    val totalMl: Int,
    val servings: Int,
)

/** What was logged on the selected day, broken down per drink. */
data class DaySummary(
    /** Newest first, as the DAO returns them. */
    val entries: List<DrinkEntry>,
) {
    val totalMl: Int = entries.sumOf { it.amountMl }

    /**
     * Every drink type in declaration order, including ones with nothing logged, so the rows
     * don't reshuffle from day to day.
     */
    val drinks: List<DrinkDayTotal> = DrinkType.entries.map { type ->
        val matching = entries.filter { it.drinkType == type.key }
        DrinkDayTotal(type, matching.sumOf { it.amountMl }, matching.size)
    }
}

/** How the viewed month went, across the days that have entries. */
data class MonthSummary(
    val loggedDays: Int,
    val metDays: Int,
    val belowDays: Int,
    val overDays: Int,
    val totalMl: Int,
) {
    /** Averaged over logged days — an unlogged day is missing data, not a zero-intake day. */
    val averageMl: Int get() = if (loggedDays == 0) 0 else totalMl / loggedDays

    companion object {
        fun of(dailyTotals: Collection<DailyTotal>, goalMl: Int, upperMl: Int?): MonthSummary {
            val statuses = dailyTotals.map { GoalStatus.of(it.total, goalMl, upperMl) }
            return MonthSummary(
                loggedDays = dailyTotals.size,
                metDays = statuses.count { it == GoalStatus.MET },
                belowDays = statuses.count { it == GoalStatus.BELOW },
                overDays = statuses.count { it == GoalStatus.OVER },
                totalMl = dailyTotals.sumOf { it.total },
            )
        }
    }
}
