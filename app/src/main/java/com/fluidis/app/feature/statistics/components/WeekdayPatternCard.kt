package com.fluidis.app.feature.statistics.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fluidis.app.core.theme.Teal40
import com.fluidis.app.feature.statistics.WeekdayPattern
import java.util.Locale
import kotlin.math.abs
import kotlin.math.roundToInt

private val CHART_HEIGHT = 80.dp
private val VALUE_LABEL_HEIGHT = 18.dp
private val WEEKDAY_NAMES = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")

/** Average intake per weekday across all history, with a one-line read of the weekend habit. */
@Composable
fun WeekdayPatternCard(pattern: WeekdayPattern, goalMl: Int, modifier: Modifier = Modifier) {
    val peak = maxOf(pattern.averages.filterNotNull().maxOrNull() ?: 0, goalMl, 1)
    val goalLineColor = MaterialTheme.colorScheme.error.copy(alpha = 0.4f)

    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column {
                Text(text = "Weekday Pattern", style = MaterialTheme.typography.titleMedium)
                Text(
                    text = insight(pattern),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Box {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    pattern.averages.forEachIndexed { index, average ->
                        WeekdayColumn(index, average, peak, Modifier.weight(1f))
                    }
                }
                // The goal as a faint rule across the columns, so each bar reads against it.
                Canvas(modifier = Modifier.matchParentSize()) {
                    val y = VALUE_LABEL_HEIGHT.toPx() + CHART_HEIGHT.toPx() * (1f - goalMl.toFloat() / peak)
                    drawLine(
                        color = goalLineColor,
                        start = Offset(0f, y),
                        end = Offset(size.width, y),
                        strokeWidth = 1.5f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f)),
                    )
                }
            }
        }
    }
}

@Composable
private fun WeekdayColumn(index: Int, average: Int?, peak: Int, modifier: Modifier) {
    val isWeekend = index >= 5
    val color = if (isWeekend) Teal40 else MaterialTheme.colorScheme.primary
    val barHeight = average?.let { CHART_HEIGHT * (it.toFloat() / peak) }

    Column(
        modifier = modifier.clearAndSetSemantics {
            contentDescription = "${WEEKDAY_NAMES[index]}, " +
                (average?.let { "average $it millilitres" } ?: "no entries")
        },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(modifier = Modifier.height(VALUE_LABEL_HEIGHT), contentAlignment = Alignment.TopCenter) {
            Text(
                text = average?.let(::compact) ?: "–",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(CHART_HEIGHT),
            contentAlignment = Alignment.BottomCenter,
        ) {
            if (barHeight != null) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(maxOf(barHeight, 3.dp))
                        .clip(RoundedCornerShape(5.dp))
                        .background(color.copy(alpha = 0.85f)),
                )
            }
        }
        Text(
            text = WeekdayPattern.LABELS[index],
            style = MaterialTheme.typography.labelSmall,
            fontWeight = if (isWeekend) FontWeight.SemiBold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}

private fun insight(pattern: WeekdayPattern): String {
    val change = pattern.weekendChange ?: return if (pattern.loggedDays < WeekdayPattern.MINIMUM_DAYS_FOR_INSIGHT) {
        "Log two weeks to see your pattern"
    } else {
        "Average per weekday, all time"
    }
    val percent = (abs(change) * 100).roundToInt()
    return when {
        percent < 10 -> "Steady through the week"
        change < 0 -> "You drink $percent% less at weekends"
        else -> "You drink $percent% more at weekends"
    }
}

/** "1.8k" rather than "1800", so seven labels fit side by side. */
private fun compact(ml: Int): String =
    if (ml >= 1000) String.format(Locale.US, "%.1fk", ml / 1000f) else "$ml"
