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
import androidx.compose.material3.MaterialTheme
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
import com.fluidis.app.feature.statistics.components.DrinkMixCard
import com.fluidis.app.feature.statistics.components.IntakeChart
import com.fluidis.app.feature.statistics.components.TrendCard
import com.fluidis.app.feature.statistics.components.WeekdayPatternCard

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

                val report = state.report
                TrendCard(
                    period = state.period,
                    trend = report.trend,
                    hasGoalRange = (state.goalUpperMl ?: 0) > state.goalMl,
                )

                IntakeChart(
                    dailyTotals = report.dailyTotals,
                    rollingAverage = report.rollingAverage,
                    drinkSplits = report.drinkSplits,
                    goalMl = state.goalMl,
                    goalUpperMl = state.goalUpperMl,
                    chartMode = state.chartMode,
                    onChartModeChange = viewModel::setChartMode,
                )

                DrinkMixCard(mix = report.mix)

                // Everything below ignores the period picker: a weekday habit only shows up
                // across all of history.
                if (report.weekdays.loggedDays > 0) {
                    Text(
                        text = "ALL TIME",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 12.dp, start = 4.dp),
                    )
                    WeekdayPatternCard(pattern = report.weekdays, goalMl = state.goalMl)
                }

                Spacer(modifier = Modifier.height(84.dp))
            }
        }
    }
}
