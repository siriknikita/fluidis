package com.fluidis.app.feature.home

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
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fluidis.app.core.model.DrinkType
import com.fluidis.app.feature.home.components.CustomAmountDialog
import com.fluidis.app.feature.home.components.DailySummary
import com.fluidis.app.feature.home.components.DrinkCard

@Composable
fun HomeScreen(
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    viewModel: HomeViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var customAmountDrinkType by remember { mutableStateOf<DrinkType?>(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is HomeEvent.EntryRemoved -> {
                    val result = snackbarHostState.showSnackbar(
                        message = "Removed ${event.entry.amountMl}ml ${DrinkType.fromKey(event.entry.drinkType).displayName}",
                        actionLabel = "UNDO",
                        duration = SnackbarDuration.Short,
                    )
                    if (result == SnackbarResult.ActionPerformed) {
                        viewModel.restoreEntry(event.entry)
                    }
                }
                is HomeEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    when (val state = uiState) {
        is HomeUiState.Loading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is HomeUiState.Success -> {
            HomeContent(
                state = state,
                onAdd = { drinkType ->
                    viewModel.addEntry(drinkType, state.servingSizes[drinkType] ?: drinkType.defaultServingMl)
                },
                onUndo = { drinkType -> viewModel.undoLastEntry(drinkType) },
                onLongPressAdd = { drinkType -> customAmountDrinkType = drinkType },
                modifier = modifier,
            )
        }
    }

    customAmountDrinkType?.let { drinkType ->
        CustomAmountDialog(
            drinkType = drinkType,
            onConfirm = { amountMl ->
                viewModel.addEntry(drinkType, amountMl)
                customAmountDrinkType = null
            },
            onDismiss = { customAmountDrinkType = null },
        )
    }
}

@Composable
private fun HomeContent(
    state: HomeUiState.Success,
    onAdd: (DrinkType) -> Unit,
    onUndo: (DrinkType) -> Unit,
    onLongPressAdd: (DrinkType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        DailySummary(
            totalMl = state.totalMl,
            goalMl = state.goalMl,
            progressFraction = state.progressFraction,
            goalReached = state.goalReached,
            remainingMl = state.remainingMl,
            overGoalMl = state.overGoalMl,
        )

        DrinkType.entries.forEach { drinkType ->
            DrinkCard(
                drinkType = drinkType,
                totalMl = state.drinkTotals[drinkType] ?: 0,
                servingMl = state.servingSizes[drinkType] ?: drinkType.defaultServingMl,
                onAdd = { onAdd(drinkType) },
                onUndo = { onUndo(drinkType) },
                onLongPressAdd = { onLongPressAdd(drinkType) },
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
