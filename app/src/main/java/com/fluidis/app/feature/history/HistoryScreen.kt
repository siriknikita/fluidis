package com.fluidis.app.feature.history

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
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
import com.fluidis.app.feature.history.components.CalendarView
import com.fluidis.app.feature.history.components.DayDetailPanel
import com.fluidis.app.feature.history.components.GoalDonutChart

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
            val isDetailOpen = state.selectedDate != null
            var addRequested by remember { mutableStateOf(false) }

            BackHandler(enabled = isDetailOpen) {
                viewModel.dismissDetail()
            }

            LaunchedEffect(isDetailOpen) {
                onDetailStateChanged(
                    isDetailOpen,
                    if (isDetailOpen) viewModel::dismissDetail else null,
                    if (isDetailOpen) ({ addRequested = true }) else null,
                )
            }

            DisposableEffect(Unit) {
                onDispose {
                    onDetailStateChanged(false, null, null)
                }
            }

            val scale by animateFloatAsState(
                targetValue = if (isDetailOpen) 0.92f else 1f,
                animationSpec = tween(300),
                label = "bgScale",
            )
            val bgAlpha by animateFloatAsState(
                targetValue = if (isDetailOpen) 0.5f else 1f,
                animationSpec = tween(300),
                label = "bgAlpha",
            )

            Box(modifier = modifier.fillMaxSize()) {
                // Background content with scale-down animation
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

                    GoalDonutChart(
                        datesWithEntries = state.datesWithEntries,
                        goalMl = state.goalMl,
                        goalUpperMl = state.goalUpperMl,
                    )

                    Spacer(modifier = Modifier.height(96.dp))
                }

                // Detail panel overlay
                AnimatedVisibility(
                    visible = isDetailOpen,
                    enter = slideInVertically(
                        initialOffsetY = { it },
                        animationSpec = tween(350),
                    ),
                    exit = slideOutVertically(
                        targetOffsetY = { it },
                        animationSpec = tween(300),
                    ),
                ) {
                    state.selectedDate?.let { date ->
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.BottomCenter,
                        ) {
                            DayDetailPanel(
                                date = date,
                                entries = state.selectedDateEntries,
                                totalMl = state.selectedDateTotal,
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
            }
        }
    }
}
