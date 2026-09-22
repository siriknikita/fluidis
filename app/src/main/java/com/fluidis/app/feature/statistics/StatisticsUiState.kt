package com.fluidis.app.feature.statistics

import com.fluidis.app.core.model.ChartMode

enum class StatsPeriod(
    val displayName: String,
    /** How the summary names the comparison period. Null for all time, which has none. */
    val comparisonName: String?,
) {
    WEEK("Week", "previous 7 days"),
    MONTH("Month", "previous 30 days"),
    ALL_TIME("All Time", null),
}

sealed interface StatisticsUiState {
    data object Loading : StatisticsUiState

    data class Success(
        val period: StatsPeriod,
        val chartMode: ChartMode,
        val goalMl: Int,
        val goalUpperMl: Int?,
        val report: StatisticsReport,
    ) : StatisticsUiState
}
