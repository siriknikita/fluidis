package com.fluidis.app.feature.statistics.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.fluidis.app.core.model.DrinkType
import com.fluidis.app.feature.statistics.DrinkMix
import com.fluidis.app.feature.statistics.MixBucket
import com.fluidis.app.feature.statistics.asPercent

private val COLUMN_HEIGHT = 72.dp

/**
 * What the period's intake was made of, and how that mix moved: one 100% column per bucket
 * (day, week or month), then each drink's total and share, with last period's share beside it.
 */
@Composable
fun DrinkMixCard(mix: DrinkMix, modifier: Modifier = Modifier) {
    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(text = "Drink Mix", style = MaterialTheme.typography.titleMedium)

            if (mix.total == 0) {
                Text(
                    text = "No data for this period",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 12.dp),
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clearAndSetSemantics { },
                    horizontalArrangement = Arrangement.spacedBy(if (mix.buckets.size > 8) 4.dp else 8.dp),
                ) {
                    mix.buckets.forEach { bucket ->
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            MixColumn(bucket)
                            Text(
                                text = bucket.label,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Clip,
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    DrinkType.entries.forEach { type -> DrinkRow(type, mix) }
                }
            }
        }
    }
}

/**
 * One bucket as a 100% column, drinks stacked in declaration order from the bottom. An empty
 * bucket is a faint placeholder, so gaps in logging stay visible.
 */
@Composable
private fun MixColumn(bucket: MixBucket) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(COLUMN_HEIGHT)
            .clip(RoundedCornerShape(5.dp))
            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)),
        verticalArrangement = Arrangement.Bottom,
    ) {
        val total = bucket.total
        if (total > 0) {
            DrinkType.entries.reversed().forEach { type ->
                val ml = bucket.totals[type] ?: 0
                if (ml > 0) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(COLUMN_HEIGHT * (ml.toFloat() / total))
                            .background(type.color),
                    )
                }
            }
        }
    }
}

@Composable
private fun DrinkRow(type: DrinkType, mix: DrinkMix) {
    val ml = mix.totals[type] ?: 0
    val share = mix.share(type).asPercent()
    val previous = mix.previousShare(type)?.asPercent()

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .alpha(if (ml == 0) 0.5f else 1f)
            .clearAndSetSemantics {
                contentDescription = buildString {
                    append("${type.displayName}, $ml millilitres, $share")
                    if (previous != null) append(", previously $previous")
                }
            },
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(type.color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(type.icon, contentDescription = null, tint = type.color, modifier = Modifier.size(16.dp))
        }
        Spacer(modifier = Modifier.width(10.dp))
        Text(text = type.displayName, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
        Text(
            text = "$ml ml",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = share,
            style = MaterialTheme.typography.labelLarge,
            textAlign = TextAlign.End,
            modifier = Modifier.width(44.dp),
        )
        Text(
            text = previous?.let { "was $it" } ?: "",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
            textAlign = TextAlign.End,
            modifier = Modifier.width(60.dp),
        )
    }
}

