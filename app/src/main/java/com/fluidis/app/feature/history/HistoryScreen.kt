package com.fluidis.app.feature.history

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fluidis.app.core.model.DrinkType
import com.fluidis.app.feature.history.components.CalendarView
import com.fluidis.app.feature.history.components.DayDetailCard
import com.fluidis.app.feature.history.components.DayDetailPanel
import com.fluidis.app.feature.history.components.MonthSummaryCard
import com.fluidis.app.feature.home.components.CustomAmountDialog

@Composable
fun HistoryScreen(
    modifier: Modifier = Modifier,
    viewModel: HistoryViewModel = hiltViewModel(),
    onDetailStateChanged: (isOpen: Boolean, dismiss: (() -> Unit)?, addEntry: (() -> Unit)?) -> Unit = { _, _, _ -> },
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is HistoryUiState.Loading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is HistoryUiState.Success -> {
            // A day is always selected and summarised inline; the overlay is only the entry list.
            val isEntriesOpen = state.isEntriesOpen
            var addRequested by remember { mutableStateOf(false) }
            var addingDrinkType by remember { mutableStateOf<DrinkType?>(null) }

            BackHandler(enabled = isEntriesOpen) {
                viewModel.dismissDetail()
            }

            LaunchedEffect(isEntriesOpen) {
                onDetailStateChanged(
                    isEntriesOpen,
                    if (isEntriesOpen) viewModel::dismissDetail else null,
                    if (isEntriesOpen) ({ addRequested = true }) else null,
                )
            }

            DisposableEffect(Unit) {
                onDispose {
                    onDetailStateChanged(false, null, null)
                }
            }

            val scale by animateFloatAsState(
                targetValue = if (isEntriesOpen) 0.92f else 1f,
                animationSpec = tween(300),
                label = "bgScale",
            )
            val bgAlpha by animateFloatAsState(
                targetValue = if (isEntriesOpen) 0.5f else 1f,
                animationSpec = tween(300),
                label = "bgAlpha",
            )

            Box(modifier = modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = scale
                            scaleY = scale
                            alpha = bgAlpha
                        }
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    CalendarView(
                        currentMonth = state.currentMonth,
                        today = state.today,
                        datesWithEntries = state.datesWithEntries,
                        goalMl = state.goalMl,
                        goalUpperMl = state.goalUpperMl,
                        selectedDate = state.selectedDate,
                        onDateSelected = viewModel::selectDate,
                        onPreviousMonth = { viewModel.navigateMonth(-1) },
                        onNextMonth = { viewModel.navigateMonth(1) },
                        onToday = viewModel::goToToday,
                    )

                    DayDetailCard(
                        date = state.selectedDate,
                        isToday = state.selectedDate == state.today,
                        summary = state.selectedDay,
                        goalMl = state.goalMl,
                        goalUpperMl = state.goalUpperMl,
                        onAddEntry = { addingDrinkType = it },
                        onShowEntries = viewModel::showEntries,
                    )

                    // Nothing logged this month — the summary would be a ring of nothing.
                    if (state.month.loggedDays > 0) {
                        MonthSummaryCard(
                            month = state.currentMonth,
                            summary = state.month,
                            hasGoalRange = (state.goalUpperMl ?: 0) > state.goalMl,
                        )
                    }

                    Spacer(modifier = Modifier.height(84.dp))
                }

                // Entries overlay
                AnimatedVisibility(
                    visible = isEntriesOpen,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(350),
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(300),
                    ),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f)),
                        contentAlignment = Alignment.BottomCenter,
                    ) {
                        DayDetailPanel(
                            date = state.selectedDate,
                            entries = state.selectedDay.entries,
                            totalMl = state.selectedDay.totalMl,
                            addRequested = addRequested,
                            onAddConsumed = { addRequested = false },
                            onDeleteEntry = viewModel::deleteEntry,
                            onEditEntry = { entry, amount, type ->
                                viewModel.updateEntry(entry, amount, type)
                            },
                            onAddEntry = viewModel::addEntryToDate,
                        )
                    }
                }
            }

            addingDrinkType?.let { drinkType ->
                CustomAmountDialog(
                    drinkType = drinkType,
                    onConfirm = { amountMl ->
                        viewModel.addEntryToDate(state.selectedDate, drinkType, amountMl)
                        addingDrinkType = null
                    },
                    onDismiss = { addingDrinkType = null },
                )
            }
        }
    }
}
