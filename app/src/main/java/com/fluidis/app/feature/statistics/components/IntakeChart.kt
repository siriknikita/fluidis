package com.fluidis.app.feature.statistics.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.fluidis.app.core.model.ChartMode
import com.fluidis.app.core.model.DailyTotal
import com.fluidis.app.core.model.DrinkType
import com.fluidis.app.core.theme.GoalGreen
import com.fluidis.app.core.ui.ChartDefaults

@Composable
fun IntakeChart(
    dailyTotals: List<DailyTotal>,
    goalMl: Int,
    goalUpperMl: Int?,
    chartMode: ChartMode,
    onChartModeChange: (ChartMode) -> Unit,
    modifier: Modifier = Modifier,
) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val surfaceVariant = MaterialTheme.colorScheme.surfaceVariant
    val onSurfaceVariant = MaterialTheme.colorScheme.onSurfaceVariant
    val goalLineColor = MaterialTheme.colorScheme.error.copy(alpha = 0.6f)
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelSmall.copy(
        color = onSurfaceVariant,
    )

    val effectiveUpper = goalUpperMl?.takeIf { it > goalMl } ?: goalMl
    val maxValue = remember(dailyTotals, goalMl, effectiveUpper) {
        (dailyTotals.maxOfOrNull { it.total } ?: effectiveUpper).coerceAtLeast(effectiveUpper).let {
            ((it / ChartDefaults.Y_AXIS_ROUND_INTERVAL) + 1) * ChartDefaults.Y_AXIS_ROUND_INTERVAL
        }
    }

    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "Daily Intake",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f),
                )
                ChartModeSelector(
                    selectedMode = chartMode,
                    onModeSelected = onChartModeChange,
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (dailyTotals.isEmpty()) {
                Text(
                    text = "No data for this period",
                    style = MaterialTheme.typography.bodyMedium,
                    color = onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 40.dp),
                )
            } else {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(ChartDefaults.CHART_HEIGHT),
                ) {
                    val leftPadding = ChartDefaults.LEFT_PADDING
                    val bottomPadding = ChartDefaults.BOTTOM_PADDING
                    val chartWidth = size.width - leftPadding
                    val chartHeight = size.height - bottomPadding

                    // Goal line(s)
                    val dashEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f))
                    val goalY = chartHeight * (1f - goalMl.toFloat() / maxValue)

                    if (goalUpperMl != null && goalUpperMl > goalMl) {
                        val upperY = chartHeight * (1f - goalUpperMl.toFloat() / maxValue)
                        // Shaded band between lower and upper
                        drawRect(
                            color = GoalGreen.copy(alpha = 0.1f),
                            topLeft = Offset(leftPadding, upperY),
                            size = Size(chartWidth, goalY - upperY),
                        )
                        // Lower bound line
                        drawLine(
                            color = goalLineColor,
                            start = Offset(leftPadding, goalY),
                            end = Offset(size.width, goalY),
                            strokeWidth = 1.5f,
                            pathEffect = dashEffect,
                        )
                        // Upper bound line
                        drawLine(
                            color = goalLineColor,
                            start = Offset(leftPadding, upperY),
                            end = Offset(size.width, upperY),
                            strokeWidth = 1.5f,
                            pathEffect = dashEffect,
                        )
                    } else {
                        drawLine(
                            color = goalLineColor,
                            start = Offset(leftPadding, goalY),
                            end = Offset(size.width, goalY),
                            strokeWidth = 1.5f,
                            pathEffect = dashEffect,
                        )
                    }

                    when (chartMode) {
                        ChartMode.BAR -> drawBarChart(
                            dailyTotals, maxValue, chartWidth, chartHeight,
                            leftPadding, primaryColor, surfaceVariant,
                        )
                        ChartMode.STACKED_BAR -> drawStackedBarChart(
                            dailyTotals, maxValue, chartWidth, chartHeight,
                            leftPadding,
                        )
                        ChartMode.LINE -> drawLineChart(
                            dailyTotals, maxValue, chartWidth, chartHeight,
                            leftPadding, primaryColor, filled = false,
                        )
                        ChartMode.AREA -> drawLineChart(
                            dailyTotals, maxValue, chartWidth, chartHeight,
                            leftPadding, primaryColor, filled = true,
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawBarChart(
    dailyTotals: List<DailyTotal>,
    maxValue: Int,
    chartWidth: Float,
    chartHeight: Float,
    leftPadding: Float,
    barColor: Color,
    trackColor: Color,
) {
    val barWidth = (chartWidth / dailyTotals.size) * ChartDefaults.BAR_WIDTH_FRACTION
    val gap = (chartWidth / dailyTotals.size) * ChartDefaults.GAP_FRACTION

    dailyTotals.forEachIndexed { index, daily ->
        val x = leftPadding + index * (barWidth + gap) + gap / 2
        val barHeight = chartHeight * (daily.total.toFloat() / maxValue)
        val y = chartHeight - barHeight

        drawRoundRect(
            color = barColor,
            topLeft = Offset(x, y),
            size = Size(barWidth, barHeight),
            cornerRadius = CornerRadius(ChartDefaults.BAR_CORNER_RADIUS, ChartDefaults.BAR_CORNER_RADIUS),
        )
    }
}

private fun DrawScope.drawStackedBarChart(
    dailyTotals: List<DailyTotal>,
    maxValue: Int,
    chartWidth: Float,
    chartHeight: Float,
    leftPadding: Float,
) {
    val barWidth = (chartWidth / dailyTotals.size) * ChartDefaults.BAR_WIDTH_FRACTION
    val gap = (chartWidth / dailyTotals.size) * ChartDefaults.GAP_FRACTION
    val colors = DrinkType.entries.map { it.color }

    dailyTotals.forEachIndexed { index, daily ->
        val x = leftPadding + index * (barWidth + gap) + gap / 2
        val totalHeight = chartHeight * (daily.total.toFloat() / maxValue)

        // Split into proportional segments based on drink type count
        val segmentCount = colors.size
        val segmentHeight = totalHeight / segmentCount

        var currentY = chartHeight

        colors.forEachIndexed { colorIndex, color ->
            val h = if (colorIndex == segmentCount - 1) {
                chartHeight - (chartHeight - totalHeight) - (segmentHeight * colorIndex)
            } else {
                segmentHeight
            }
            currentY -= h
            drawRoundRect(
                color = color,
                topLeft = Offset(x, currentY),
                size = Size(barWidth, h),
                cornerRadius = if (colorIndex == segmentCount - 1) CornerRadius(ChartDefaults.BAR_CORNER_RADIUS) else CornerRadius.Zero,
            )
        }
    }
}

private fun DrawScope.drawLineChart(
    dailyTotals: List<DailyTotal>,
    maxValue: Int,
    chartWidth: Float,
    chartHeight: Float,
    leftPadding: Float,
    lineColor: Color,
    filled: Boolean,
) {
    if (dailyTotals.size < 2) return

    val stepX = chartWidth / (dailyTotals.size - 1).coerceAtLeast(1)

    val points = dailyTotals.mapIndexed { index, daily ->
        val x = leftPadding + index * stepX
        val y = chartHeight * (1f - daily.total.toFloat() / maxValue)
        Offset(x, y)
    }

    // Draw area fill
    if (filled && points.size >= 2) {
        val areaPath = Path().apply {
            moveTo(points.first().x, chartHeight)
            points.forEach { lineTo(it.x, it.y) }
            lineTo(points.last().x, chartHeight)
            close()
        }
        drawPath(
            path = areaPath,
            brush = Brush.verticalGradient(
                colors = listOf(lineColor.copy(alpha = 0.3f), lineColor.copy(alpha = 0.05f)),
            ),
        )
    }

    // Draw line
    val linePath = Path().apply {
        points.forEachIndexed { index, point ->
            if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
        }
    }
    drawPath(
        path = linePath,
        color = lineColor,
        style = Stroke(width = ChartDefaults.LINE_STROKE_WIDTH, cap = StrokeCap.Round),
    )

    // Draw dots
    points.forEach { point ->
        drawCircle(color = lineColor, radius = ChartDefaults.DOT_RADIUS, center = point)
    }
}
