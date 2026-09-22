package com.fluidis.app.feature.history.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fluidis.app.core.model.GoalStatus
import com.fluidis.app.feature.history.MonthSummary
import com.fluidis.app.feature.history.MonthYear
import java.util.Locale

private const val GAP_DEGREES = 3f
private val DONUT_SIZE = 104.dp
private val STROKE_WIDTH = 14.dp

/**
 * How the viewed month went: a donut of goal outcomes across logged days, next to the headline
 * numbers. Unlike the Statistics tab, which counts back from today, this follows the calendar.
 */
@Composable
fun MonthSummaryCard(
    month: MonthYear,
    summary: MonthSummary,
    hasGoalRange: Boolean,
    modifier: Modifier = Modifier,
) {
    val segments = buildList {
        if (summary.metDays > 0) {
            add(Segment(GoalStatus.MET, if (hasGoalRange) "In range" else "Goal met", summary.metDays))
        }
        if (summary.belowDays > 0) add(Segment(GoalStatus.BELOW, "Below goal", summary.belowDays))
        if (summary.overDays > 0) add(Segment(GoalStatus.OVER, "Over limit", summary.overDays))
    }

    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(text = month.monthName(), style = MaterialTheme.typography.titleMedium)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Donut(segments = segments, summary = summary)
                Spacer(modifier = Modifier.width(20.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    segments.forEach { segment ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(segment.status.color),
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = segment.label,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f),
                            )
                            Text(text = "${segment.count}", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
            }

            HorizontalDivider()

            Row(modifier = Modifier.fillMaxWidth()) {
                Stat(value = "${summary.averageMl} ml", label = "Avg/day", modifier = Modifier.weight(1f))
                Stat(
                    value = String.format(Locale.US, "%.1f L", summary.totalMl / 1000f),
                    label = "Total",
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

private data class Segment(val status: GoalStatus, val label: String, val count: Int)

@Composable
private fun Donut(segments: List<Segment>, summary: MonthSummary) {
    val total = segments.sumOf { it.count }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(DONUT_SIZE)
            .clearAndSetSemantics {
                contentDescription = "Goal met on ${summary.metDays} of ${summary.loggedDays} logged days"
            },
    ) {
        Canvas(modifier = Modifier.size(DONUT_SIZE)) {
            if (total == 0) return@Canvas
            val strokePx = STROKE_WIDTH.toPx()
            val arcSize = Size(size.width - strokePx, size.height - strokePx)
            val topLeft = Offset(strokePx / 2, strokePx / 2)
            val style = Stroke(width = strokePx, cap = StrokeCap.Butt)

            // A single segment is a full ring — inserting a gap would leave a stray notch.
            if (segments.size == 1) {
                drawArc(segments[0].status.color, -90f, 360f, false, topLeft, arcSize, style = style)
                return@Canvas
            }
            val available = 360f - GAP_DEGREES * segments.size
            var angle = -90f
            segments.forEach { segment ->
                val sweep = segment.count.toFloat() / total * available
                drawArc(segment.status.color, angle, sweep, false, topLeft, arcSize, style = style)
                angle += sweep + GAP_DEGREES
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${summary.metDays}/${summary.loggedDays}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "days",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun Stat(value: String, label: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
