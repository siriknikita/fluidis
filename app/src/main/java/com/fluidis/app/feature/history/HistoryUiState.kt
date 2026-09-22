package com.fluidis.app.feature.history

import com.fluidis.app.core.model.DailyTotal
import com.fluidis.app.core.model.DrinkEntry
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month

data class MonthYear(val year: Int, val month: Month) {
    fun displayName(): String = "${month.name.lowercase().replaceFirstChar { it.uppercase() }} $year"
}

sealed interface HistoryUiState {
    data object Loading : HistoryUiState

    data class Success(
        val currentMonth: MonthYear,
        val today: LocalDate,
        val datesWithEntries: Map<LocalDate, DailyTotal>,
        val goalMl: Int,
        val goalUpperMl: Int?,
        val selectedDate: LocalDate?,
        val selectedDateEntries: List<DrinkEntry>,
        val selectedDateTotal: Int,
    ) : HistoryUiState
}
