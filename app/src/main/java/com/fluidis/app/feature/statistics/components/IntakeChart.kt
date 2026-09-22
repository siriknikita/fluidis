package com.fluidis.app.feature.statistics.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeJoin
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

/**
 * Daily intake, drawn four ways. The goal line (or the shaded band, when a range is set) sits
 * behind whichever series is selected, and a 7-day rolling average is drawn over it — daily
 * totals jump around, and the average is what shows which way things are actually heading.
 */
@Composable
fun IntakeChart(
    dailyTotals: List<DailyTotal>,
    /** Index-aligned with [dailyTotals]. */
    rollingAverage: List<Int>,
    /** Each day's intake split by drink, keyed by ISO date, for the stacked mode. */
    drinkSplits: Map<String, Map<DrinkType, Int>>,
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
    val averageColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.75f)
    val haloColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
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

                    val xs = when (chartMode) {
                        ChartMode.BAR -> drawBarChart(
                            dailyTotals, maxValue, chartWidth, chartHeight,
                            leftPadding, primaryColor, surfaceVariant,
                        )
                        ChartMode.STACKED_BAR -> drawStackedBarChart(
                            dailyTotals, drinkSplits, maxValue, chartWidth, chartHeight,
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
                    val averagePoints = xs.zip(rollingAverage) { x, average ->
                        Offset(x, chartHeight * (1f - average.toFloat() / maxValue))
                    }
                    drawAverage(averagePoints, averageColor, haloColor)
                }

                Spacer(modifier = Modifier.height(8.dp))
                ChartLegend(
                    showDrinks = chartMode == ChartMode.STACKED_BAR,
                    showAverage = rollingAverage.size >= 2,
                    averageColor = averageColor,
                )
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
): List<Float> {
    val barWidth = (chartWidth / dailyTotals.size) * ChartDefaults.BAR_WIDTH_FRACTION
    val gap = (chartWidth / dailyTotals.size) * ChartDefaults.GAP_FRACTION

    return dailyTotals.mapIndexed { index, daily ->
        val x = leftPadding + index * (barWidth + gap) + gap / 2
        val barHeight = chartHeight * (daily.total.toFloat() / maxValue)
        val y = chartHeight - barHeight

        drawRoundRect(
            color = barColor,
            topLeft = Offset(x, y),
            size = Size(barWidth, barHeight),
            cornerRadius = CornerRadius(ChartDefaults.BAR_CORNER_RADIUS, ChartDefaults.BAR_CORNER_RADIUS),
        )
        x + barWidth / 2
    }
}

/** Returns the x centre of each bar, where the rolling average is plotted. */
private fun DrawScope.drawStackedBarChart(
    dailyTotals: List<DailyTotal>,
    drinkSplits: Map<String, Map<DrinkType, Int>>,
    maxValue: Int,
    chartWidth: Float,
    chartHeight: Float,
    leftPadding: Float,
): List<Float> {
    val barWidth = (chartWidth / dailyTotals.size) * ChartDefaults.BAR_WIDTH_FRACTION
    val gap = (chartWidth / dailyTotals.size) * ChartDefaults.GAP_FRACTION

    return dailyTotals.mapIndexed { index, daily ->
        val x = leftPadding + index * (barWidth + gap) + gap / 2
        val totalHeight = chartHeight * (daily.total.toFloat() / maxValue)

        // Each drink's real share of the day, in declaration order from the bottom. Rows for
        // drinks this build doesn't know are in the total but not the split, so the bands are
        // scaled to the known part and the stack still reaches the day's total.
        val split = drinkSplits[daily.date].orEmpty()
        val knownTotal = split.values.sum()
        val bands = DrinkType.entries.mapNotNull { type ->
            val ml = split[type] ?: 0
            if (ml > 0 && knownTotal > 0) type.color to totalHeight * ml / knownTotal else null
        }

        var currentY = chartHeight
        bands.forEachIndexed { bandIndex, (color, height) ->
            currentY -= height
            val isTop = bandIndex == bands.lastIndex
            drawRoundRect(
                color = color,
                topLeft = Offset(x, currentY),
                size = Size(barWidth, height),
                cornerRadius = if (isTop) CornerRadius(ChartDefaults.BAR_CORNER_RADIUS) else CornerRadius.Zero,
            )
        }
        x + barWidth / 2
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
): List<Float> {
    if (dailyTotals.size < 2) return emptyList()

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
    return points.map { it.x }
}

private fun DrawScope.drawAverage(points: List<Offset>, color: Color, halo: Color) {
    if (points.size < 2) return
    val path = Path().apply {
        points.forEachIndexed { index, point ->
            if (index == 0) moveTo(point.x, point.y) else lineTo(point.x, point.y)
        }
    }
    // A halo first, so the line stays legible where it crosses bars of any colour.
    drawPath(path, halo, style = Stroke(width = AVERAGE_WIDTH + 6f, cap = StrokeCap.Round, join = StrokeJoin.Round))
    drawPath(path, color, style = Stroke(width = AVERAGE_WIDTH, cap = StrokeCap.Round, join = StrokeJoin.Round))
}

private const val AVERAGE_WIDTH = 5f

@Composable
private fun ChartLegend(showDrinks: Boolean, showAverage: Boolean, averageColor: Color) {
    if (!showDrinks && !showAverage) return
    val labelStyle = MaterialTheme.typography.labelSmall
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showDrinks) {
            DrinkType.entries.forEach { type ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(type.color),
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(type.displayName, style = labelStyle, color = labelColor)
                }
            }
        }
        if (showAverage) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(14.dp)
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(averageColor),
                )
                Spacer(modifier = Modifier.width(5.dp))
                Text("7-day average", style = labelStyle, color = labelColor)
            }
        }
    }
}
