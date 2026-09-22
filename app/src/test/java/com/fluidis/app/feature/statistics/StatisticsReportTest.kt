package com.fluidis.app.feature.statistics

import com.fluidis.app.core.model.DailyDrinkTotal
import com.fluidis.app.core.model.DrinkType
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class StatisticsReportTest {

    private val today = LocalDate(2026, 9, 22) // a Tuesday

    private fun row(daysAgo: Int, drink: String, ml: Int) =
        DailyDrinkTotal(today.minus(daysAgo, DateTimeUnit.DAY).toString(), drink, ml)

    private fun report(period: StatsPeriod = StatsPeriod.WEEK, rows: List<DailyDrinkTotal>): StatisticsReport {
        val current = StatisticsViewModel.daysFor(period, null, today)
        return StatisticsReport.build(
            period = period,
            current = current,
            previous = StatisticsViewModel.previousDays(period, current),
            allTime = StatisticsViewModel.daysFor(StatsPeriod.ALL_TIME, null, today),
            rows = rows,
            goalMl = 2000,
            upperMl = null,
        )
    }

    @Test
    fun `the rolling average covers the trailing seven days, logged days only`() {
        val report = report(
            rows = listOf(
                row(10, "water", 3000), // outside every window that matters here
                row(7, "water", 1000),
                row(1, "water", 2000),
                row(0, "water", 3000),
            ),
        )
        assertEquals(listOf(2000, 3000), report.dailyTotals.map { it.total })
        // Yesterday's window reaches back to 7 days ago; today's no longer does.
        assertEquals(listOf(1500, 2500), report.rollingAverage)
    }

    @Test
    fun `the mix splits a week by day and keeps last week's shares for comparison`() {
        val report = report(rows = listOf(row(0, "water", 1500), row(0, "coffee", 500), row(8, "water", 1000)))

        assertEquals(7, report.mix.buckets.size)
        assertEquals("Tu", report.mix.buckets.last().label)
        assertEquals(mapOf(DrinkType.WATER to 1500, DrinkType.COFFEE to 500), report.mix.buckets.last().totals)
        assertEquals(0.25, report.mix.share(DrinkType.COFFEE), 0.0)
        assertEquals(1.0, report.mix.previousShare(DrinkType.WATER)!!, 0.0)
        assertEquals(0.0, report.mix.previousShare(DrinkType.COFFEE)!!, 0.0)
        assertEquals(
            mapOf(DrinkType.WATER to 1500, DrinkType.COFFEE to 500),
            report.drinkSplits[today.toString()],
        )
    }

    @Test
    fun `a month splits into calendar weeks, clipped to the window`() {
        val report = report(period = StatsPeriod.MONTH, rows = emptyList())
        // 24 Aug (Mon) … 22 Sep (Tue): five buckets, the last a two-day stub.
        assertEquals(5, report.mix.buckets.size)
        assertEquals("2026-08-24", report.mix.buckets.first().id)
        assertEquals("2026-09-21", report.mix.buckets.last().id)
    }

    @Test
    fun `the weekday pattern needs two weeks before claiming a weekend habit`() {
        val sparse = report(rows = listOf(row(3, "water", 1000), row(1, "water", 2000))) // Sat, Mon
        assertEquals(1000, sparse.weekdays.averages[5])
        assertEquals(2000, sparse.weekdays.averages[0])
        assertNull(sparse.weekdays.weekendChange)

        // Two weeks: weekdays at 2000, weekends at 1000.
        val rows = (0 until 14).map { daysAgo ->
            val weekday = today.minus(daysAgo, DateTimeUnit.DAY).dayOfWeek.ordinal
            row(daysAgo, "water", if (weekday >= 5) 1000 else 2000)
        }
        val full = report(rows = rows)
        assertEquals(2000, full.weekdays.weekdayAverage)
        assertEquals(1000, full.weekdays.weekendAverage)
        assertEquals(-0.5, full.weekdays.weekendChange!!, 0.0)
    }

    @Test
    fun `the trend has no average change when the previous period is empty`() {
        val report = report(rows = listOf(row(0, "water", 2000)))
        assertEquals(0, report.trend.previous?.daysTracked)
        assertNull(report.trend.averageChange)
    }
}
