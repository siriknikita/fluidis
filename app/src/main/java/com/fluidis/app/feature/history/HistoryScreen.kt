package com.fluidis.app.feature.history

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fluidis.app.feature.history.components.CalendarView
import com.fluidis.app.feature.history.components.DayDetailSheet

@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is HistoryUiState.Loading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is HistoryUiState.Success -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                CalendarView(
                    currentMonth = state.currentMonth,
                    datesWithEntries = state.datesWithEntries,
                    goalMl = state.goalMl,
                    goalUpperMl = state.goalUpperMl,
                    selectedDate = state.selectedDate,
                    onDateSelected = viewModel::selectDate,
                    onPreviousMonth = { viewModel.navigateMonth(-1) },
                    onNextMonth = { viewModel.navigateMonth(1) },
                )
            }

            state.selectedDate?.let { date ->
                DayDetailSheet(
                    date = date,
                    entries = state.selectedDateEntries,
                    totalMl = state.selectedDateTotal,
                    onDismiss = viewModel::dismissDetail,
                    onDeleteEntry = viewModel::deleteEntry,
                    onEditEntry = { entry, amount, type ->
                        viewModel.updateEntry(entry, amount, type)
                    },
                    onAddEntry = viewModel::addEntryToDate,
                )
            }
        }
    }
}
