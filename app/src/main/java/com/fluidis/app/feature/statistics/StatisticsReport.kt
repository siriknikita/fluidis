package com.fluidis.app.feature.statistics

import com.fluidis.app.core.model.DailyDrinkTotal
import com.fluidis.app.core.model.DailyTotal
import com.fluidis.app.core.model.DrinkType
import com.fluidis.app.core.model.GoalStatus
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.roundToInt

/**
 * Everything the Statistics screen shows, computed from one fetch of per-day, per-drink totals.
 *
 * Statistics answers "what are my patterns?" — History already covers "what happened on this day
 * or in this month". So the top of the screen compares the chosen period against the one before
 * it, and the bottom looks across all history for weekday habits.
 *
 * Pure, so every number here is testable without a DAO.
 */
data class StatisticsReport(
    /** Logged days in the period, ascending — one chart slot each. */
    val dailyTotals: List<DailyTotal>,
    /** The trailing 7-day average at each of [dailyTotals], index-aligned. */
    val rollingAverage: List<Int>,
    /** Each logged day's intake split by drink, keyed by ISO date — what the stacked chart draws. */
    val drinkSplits: Map<String, Map<DrinkType, Int>>,
    val trend: PeriodTrend,
    val mix: DrinkMix,
    val weekdays: WeekdayPattern,
) {
    companion object {
        const val ROLLING_WINDOW = 7

        /**
         * @param rows per-day, per-drink totals covering at least [allTime], [previous] and the six
         *   days before [current] (for the rolling average).
         */
        fun build(
            period: StatsPeriod,
            current: ClosedRange<LocalDate>,
            previous: ClosedRange<LocalDate>?,
            allTime: ClosedRange<LocalDate>,
            rows: List<DailyDrinkTotal>,
            goalMl: Int,
            upperMl: Int?,
        ): StatisticsReport {
            val totals = mutableMapOf<LocalDate, Int>()
            val splits = mutableMapOf<LocalDate, MutableMap<DrinkType, Int>>()
            for (row in rows) {
                val day = runCatching { LocalDate.parse(row.date) }.getOrNull() ?: continue
                totals[day] = (totals[day] ?: 0) + row.total
                val type = DrinkType.entries.find { it.key == row.drinkType } ?: continue
                val split = splits.getOrPut(day) { mutableMapOf() }
                split[type] = (split[type] ?: 0) + row.total
            }

            fun days(range: ClosedRange<LocalDate>) = totals.keys.filter { it in range }.sorted()

            fun drinkTotals(range: ClosedRange<LocalDate>): Map<DrinkType, Int> {
                val result = mutableMapOf<DrinkType, Int>()
                splits.filterKeys { it in range }.values.forEach { split ->
                    split.forEach { (type, ml) -> result[type] = (result[type] ?: 0) + ml }
                }
                return result
            }

            fun summary(range: ClosedRange<LocalDate>) =
                PeriodSummary.of(days(range).map { totals.getValue(it) }, goalMl, upperMl)

            val currentDays = days(current)
            return StatisticsReport(
                dailyTotals = currentDays.map { DailyTotal(it.toString(), totals.getValue(it)) },
                rollingAverage = currentDays.map { day ->
                    val window = day.minus(ROLLING_WINDOW - 1, DateTimeUnit.DAY)..day
                    val values = totals.filterKeys { it in window }.values
                    if (values.isEmpty()) 0 else values.sum() / values.size
                },
                drinkSplits = currentDays.associate { it.toString() to (splits[it] ?: emptyMap()) },
                trend = PeriodTrend(summary(current), previous?.let(::summary)),
                mix = DrinkMix(
                    buckets = buckets(period, current).map { (range, label) ->
                        MixBucket(range.start.toString(), label, drinkTotals(range))
                    },
                    totals = drinkTotals(current),
                    previousTotals = previous?.let(::drinkTotals),
                ),
                weekdays = WeekdayPattern.of(days(allTime).map { it to totals.getValue(it) }),
            )
        }

        /**
         * A week splits into days, a month into calendar weeks, all time into calendar months —
         * each clipped to the period so the first and last buckets can be partial.
         */
        private fun buckets(
            period: StatsPeriod,
            range: ClosedRange<LocalDate>,
        ): List<Pair<ClosedRange<LocalDate>, String>> {
            val result = mutableListOf<Pair<ClosedRange<LocalDate>, String>>()
            var start = range.start
            while (start <= range.endInclusive) {
                val next = when (period) {
                    StatsPeriod.WEEK -> start.plus(1, DateTimeUnit.DAY)
                    StatsPeriod.MONTH -> start.plus(7 - start.dayOfWeek.ordinal, DateTimeUnit.DAY)
                    StatsPeriod.ALL_TIME -> LocalDate(start.year, start.month, 1).plus(1, DateTimeUnit.MONTH)
                }
                val end = minOf(next.minus(1, DateTimeUnit.DAY), range.endInclusive)
                result += (start..end) to label(start, period)
                start = next
            }
            return result
        }

        private fun label(day: LocalDate, period: StatsPeriod): String = when (period) {
            StatsPeriod.WEEK -> WeekdayPattern.LABELS[day.dayOfWeek.ordinal]
            StatsPeriod.MONTH -> day.toJavaLocalDate().format(DateTimeFormatter.ofPattern("d MMM", Locale.getDefault()))
            StatsPeriod.ALL_TIME -> day.toJavaLocalDate().format(DateTimeFormatter.ofPattern("MMM", Locale.getDefault()))
        }
    }
}

/** Headline numbers for one span of days. */
data class PeriodSummary(
    val daysTracked: Int,
    /** Over logged days — an unlogged day is missing data, not a zero-intake day. */
    val averageMl: Int,
    /** Days inside the goal (or the goal range, when one is set). */
    val metDays: Int,
    val overDays: Int,
) {
    companion object {
        fun of(totals: List<Int>, goalMl: Int, upperMl: Int?): PeriodSummary {
            val statuses = totals.map { GoalStatus.of(it, goalMl, upperMl) }
            return PeriodSummary(
                daysTracked = totals.size,
                averageMl = if (totals.isEmpty()) 0 else totals.sum() / totals.size,
                metDays = statuses.count { it == GoalStatus.MET },
                overDays = statuses.count { it == GoalStatus.OVER },
            )
        }
    }
}

/** The chosen period against the same-length period just before it. */
data class PeriodTrend(
    val current: PeriodSummary,
    /** Null for all time, which has nothing before it to compare with. */
    val previous: PeriodSummary?,
) {
    /**
     * Relative change of the daily average, e.g. 0.12 for +12%. Null when either side has no
     * logged days, since a change from or to nothing isn't a trend.
     */
    val averageChange: Double?
        get() {
            val previous = previous ?: return null
            if (previous.daysTracked == 0 || current.daysTracked == 0 || previous.averageMl == 0) return null
            return (current.averageMl - previous.averageMl).toDouble() / previous.averageMl
        }
}

data class MixBucket(
    val id: String,
    val label: String,
    val totals: Map<DrinkType, Int>,
) {
    val total: Int get() = totals.values.sum()
}

/** How the drinks shared the period, bucket by bucket, and against the previous period. */
data class DrinkMix(
    val buckets: List<MixBucket>,
    val totals: Map<DrinkType, Int>,
    val previousTotals: Map<DrinkType, Int>?,
) {
    val total: Int get() = totals.values.sum()

    fun share(type: DrinkType): Double = share(type, totals)

    /** The drink's share in the previous period. Null when that period had nothing logged. */
    fun previousShare(type: DrinkType): Double? {
        val previous = previousTotals ?: return null
        if (previous.values.sum() == 0) return null
        return share(type, previous)
    }

    private fun share(type: DrinkType, totals: Map<DrinkType, Int>): Double {
        val sum = totals.values.sum()
        return if (sum > 0) (totals[type] ?: 0).toDouble() / sum else 0.0
    }
}

/** Average intake per weekday across all history — the habit a calendar hides. */
data class WeekdayPattern(
    /** Monday first; null for a weekday that has never been logged. */
    val averages: List<Int?>,
    val weekdayAverage: Int?,
    val weekendAverage: Int?,
    val loggedDays: Int,
) {
    /**
     * Weekend average relative to weekdays, e.g. -0.3 for "30% less at weekends". Null until
     * there's enough history on both sides to say something.
     */
    val weekendChange: Double?
        get() {
            if (loggedDays < MINIMUM_DAYS_FOR_INSIGHT) return null
            val weekday = weekdayAverage ?: return null
            val weekend = weekendAverage ?: return null
            if (weekday == 0) return null
            return (weekend - weekday).toDouble() / weekday
        }

    companion object {
        val LABELS = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")

        /** Below this, one unusual day would swing the comparison, so no insight is claimed. */
        const val MINIMUM_DAYS_FOR_INSIGHT = 14

        fun of(days: List<Pair<LocalDate, Int>>): WeekdayPattern {
            val buckets = List(7) { mutableListOf<Int>() }
            days.forEach { (day, total) -> buckets[day.dayOfWeek.ordinal] += total }

            fun mean(values: List<Int>) = if (values.isEmpty()) null else values.sum() / values.size
            return WeekdayPattern(
                averages = buckets.map(::mean),
                weekdayAverage = mean(buckets.subList(0, 5).flatten()),
                weekendAverage = mean(buckets.subList(5, 7).flatten()),
                loggedDays = days.size,
            )
        }
    }
}

/** Percentage for display, e.g. 0.253 → "25%". */
fun Double.asPercent(): String = "${(this * 100).roundToInt()}%"
