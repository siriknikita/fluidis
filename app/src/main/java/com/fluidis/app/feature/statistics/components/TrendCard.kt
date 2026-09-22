package com.fluidis.app.feature.statistics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fluidis.app.core.theme.DotBelowGoal
import com.fluidis.app.core.theme.DotGoalMet
import com.fluidis.app.core.ui.DatePeriods
import com.fluidis.app.feature.statistics.PeriodSummary
import com.fluidis.app.feature.statistics.PeriodTrend
import com.fluidis.app.feature.statistics.StatsPeriod
import kotlin.math.abs
import kotlin.math.roundToInt

/** The period's headline numbers, each set against the same-length period before it. */
@Composable
fun TrendCard(
    period: StatsPeriod,
    trend: PeriodTrend,
    hasGoalRange: Boolean,
    modifier: Modifier = Modifier,
) {
    // The previous period only when it has something in it — "was 0 ml" against an empty week
    // isn't a comparison, it's noise.
    val previous: PeriodSummary? = trend.previous?.takeIf { it.daysTracked > 0 }
    val periodLength = when (period) {
        StatsPeriod.WEEK -> DatePeriods.WEEK_OFFSET_DAYS + 1
        StatsPeriod.MONTH -> DatePeriods.MONTH_OFFSET_DAYS + 1
        StatsPeriod.ALL_TIME -> null
    }
    val current = trend.current

    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Column {
                Text(text = "Summary", style = MaterialTheme.typography.titleMedium)
                period.comparisonName?.let { comparison ->
                    Text(
                        text = if (previous == null) {
                            "Nothing logged in the $comparison to compare with"
                        } else {
                            "Compared with the $comparison"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth()) {
                val cell = Modifier.weight(1f)
                StatItem(
                    value = "${current.averageMl} ml",
                    label = "Avg/day",
                    previous = previous?.let { "${it.averageMl} ml" },
                    change = trend.averageChange,
                    modifier = cell,
                )
                StatItem(
                    value = "${current.metDays}/${current.daysTracked}",
                    label = if (hasGoalRange) "In range" else "Goal met",
                    previous = previous?.let { "${it.metDays}/${it.daysTracked}" },
                    modifier = cell,
                )
                if (hasGoalRange) {
                    StatItem(
                        value = "${current.overDays}",
                        label = "Over limit",
                        previous = previous?.let { "${it.overDays}" },
                        modifier = cell,
                    )
                } else {
                    StatItem(
                        value = periodLength?.let { "${current.daysTracked}/$it" } ?: "${current.daysTracked}",
                        label = "Days logged",
                        previous = previous?.let { "${it.daysTracked}" },
                        modifier = cell,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    value: String,
    label: String,
    previous: String?,
    modifier: Modifier = Modifier,
    change: Double? = null,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        when {
            change != null -> ChangeBadge(change)
            previous != null -> Text(
                text = "was $previous",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            )
        }
    }
}

/** "↑ 12%" / "↓ 8%" — a relative change, tinted by direction. */
@Composable
private fun ChangeBadge(change: Double) {
    val percent = (abs(change) * 100).roundToInt()
    val isFlat = percent == 0
    val isUp = change > 0
    val color = when {
        isFlat -> MaterialTheme.colorScheme.onSurfaceVariant
        isUp -> DotGoalMet
        else -> DotBelowGoal
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 6.dp, vertical = 2.dp)
            .clearAndSetSemantics {
                contentDescription = if (isFlat) "No change" else "${if (isUp) "Up" else "Down"} $percent percent"
            },
    ) {
        if (!isFlat) {
            Icon(
                imageVector = if (isUp) Icons.Rounded.ArrowUpward else Icons.Rounded.ArrowDownward,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(11.dp),
            )
        }
        Text(
            text = if (isFlat) "no change" else "$percent%",
            style = MaterialTheme.typography.labelSmall,
            color = color,
        )
    }
}
