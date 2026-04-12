package com.fluidis.app.feature.statistics

import com.fluidis.app.core.model.ChartMode
import com.fluidis.app.core.model.DailyTotal
import com.fluidis.app.core.model.DrinkTypeTotal

enum class StatsPeriod(val displayName: String) {
    WEEK("Week"),
    MONTH("Month"),
    ALL_TIME("All Time"),
}

sealed interface StatisticsUiState {
    data object Loading : StatisticsUiState

    data class Success(
        val period: StatsPeriod,
        val chartMode: ChartMode,
        val dailyTotals: List<DailyTotal>,
        val drinkBreakdown: List<DrinkTypeTotal>,
        val averageMl: Int,
        val daysTracked: Int,
        val daysGoalMet: Int,
        val goalMl: Int,
        val goalUpperMl: Int?,
        val daysOverUpper: Int,
    ) : StatisticsUiState
}
