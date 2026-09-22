package com.fluidis.app.feature.history

import com.fluidis.app.core.model.DailyTotal
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import org.junit.Assert.assertEquals
import org.junit.Test

class HistorySummaryTest {

    @Test
    fun `month summary counts outcomes, total and average across logged days`() {
        val totals = listOf(2000, 2100, 2500, 3500, 2000, 2200, 2000, 800)
            .mapIndexed { index, ml -> DailyTotal("2026-09-${(index + 1).toString().padStart(2, '0')}", ml) }
        val summary = MonthSummary.of(totals, goalMl = 2000, upperMl = 3000)

        assertEquals(8, summary.loggedDays)
        assertEquals(6, summary.metDays)
        assertEquals(1, summary.overDays)
        assertEquals(1, summary.belowDays)
        assertEquals(17100, summary.totalMl)
        assertEquals(2137, summary.averageMl)
    }

    @Test
    fun `an empty month has zero average rather than dividing by zero`() {
        assertEquals(0, MonthSummary.of(emptyList(), 2000, null).averageMl)
    }

    @Test
    fun `nearest day is today, the last day of a past month or the first of a future one`() {
        val today = LocalDate(2026, 9, 22)
        assertEquals(today, nearestDay(today, MonthYear(2026, Month.SEPTEMBER)))
        assertEquals(LocalDate(2026, 8, 31), nearestDay(today, MonthYear(2026, Month.AUGUST)))
        assertEquals(LocalDate(2026, 10, 1), nearestDay(today, MonthYear(2026, Month.OCTOBER)))
    }
}
