package com.fluidis.app.feature.history.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.fluidis.app.core.model.DailyTotal
import com.fluidis.app.core.theme.DotBelowGoal
import com.fluidis.app.core.theme.DotGoalMet
import com.fluidis.app.core.theme.DotOverUpper
import kotlinx.datetime.LocalDate

private const val GAP_DEGREES = 2f
private val DONUT_SIZE = 120.dp
private val STROKE_WIDTH = 24.dp

@Composable
fun GoalDonutChart(
    datesWithEntries: Map<LocalDate, DailyTotal>,
    goalMl: Int,
    goalUpperMl: Int?,
    modifier: Modifier = Modifier,
) {
    val counts = remember(datesWithEntries, goalMl, goalUpperMl) {
        var belowGoal = 0
        var goalMet = 0
        var overUpper = 0

        for ((_, daily) in datesWithEntries) {
            when {
                goalUpperMl != null && goalUpperMl > goalMl && daily.total > goalUpperMl -> overUpper++
                daily.total >= goalMl -> goalMet++
                else -> belowGoal++
            }
        }

        Triple(belowGoal, goalMet, overUpper)
    }

    val (belowGoal, goalMet, overUpper) = counts
    val total = belowGoal + goalMet + overUpper

    if (total == 0) return

    val segments = remember(counts, goalUpperMl) {
        buildList {
            if (goalMet > 0) add(Segment(DotGoalMet, "Goal met", goalMet))
            if (belowGoal > 0) add(Segment(DotBelowGoal, "Below goal", belowGoal))
            if (goalUpperMl != null && overUpper > 0) add(Segment(DotOverUpper, "Over limit", overUpper))
        }
    }

    Row(
        modifier = modifier.padding(top = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Canvas(modifier = Modifier.size(DONUT_SIZE)) {
            val strokePx = STROKE_WIDTH.toPx()
            val arcSize = Size(size.width - strokePx, size.height - strokePx)
            val topLeft = Offset(strokePx / 2, strokePx / 2)
            val style = Stroke(width = strokePx, cap = StrokeCap.Butt)

            if (segments.size == 1) {
                drawArc(
                    color = segments[0].color,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = style,
                )
            } else {
                val totalGap = GAP_DEGREES * segments.size
                val available = 360f - totalGap
                var currentAngle = -90f

                for (segment in segments) {
                    val sweep = (segment.count.toFloat() / total) * available
                    drawArc(
                        color = segment.color,
                        startAngle = currentAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = style,
                    )
                    currentAngle += sweep + GAP_DEGREES
                }
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            for (segment in segments) {
                LegendItem(color = segment.color, label = segment.label, count = segment.count)
            }
        }
    }
}

private data class Segment(val color: Color, val label: String, val count: Int)

@Composable
private fun LegendItem(color: Color, label: String, count: Int) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "$label ($count)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
