package com.fluidis.app.feature.statistics

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fluidis.app.feature.statistics.components.AveragesCard
import com.fluidis.app.feature.statistics.components.DrinkBreakdownCard
import com.fluidis.app.feature.statistics.components.IntakeChart

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    modifier: Modifier = Modifier,
    viewModel: StatisticsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    when (val state = uiState) {
        is StatisticsUiState.Loading -> {
            Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is StatisticsUiState.Success -> {
            Column(
                modifier = modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                // Period selector
                SingleChoiceSegmentedButtonRow(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    StatsPeriod.entries.forEachIndexed { index, period ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(
                                index = index,
                                count = StatsPeriod.entries.size,
                            ),
                            onClick = { viewModel.setPeriod(period) },
                            selected = state.period == period,
                        ) {
                            Text(period.displayName)
                        }
                    }
                }

                IntakeChart(
                    dailyTotals = state.dailyTotals,
                    goalMl = state.goalMl,
                    chartMode = state.chartMode,
                    onChartModeChange = viewModel::setChartMode,
                )

                AveragesCard(
                    averageMl = state.averageMl,
                    daysTracked = state.daysTracked,
                    daysGoalMet = state.daysGoalMet,
                    goalMl = state.goalMl,
                )

                DrinkBreakdownCard(
                    breakdown = state.drinkBreakdown,
                )

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}
