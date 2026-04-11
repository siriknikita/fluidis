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
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fluidis.app.core.model.DrinkType
import com.fluidis.app.core.model.DrinkTypeTotal

@Composable
fun DrinkBreakdownCard(
    breakdown: List<DrinkTypeTotal>,
    modifier: Modifier = Modifier,
) {
    val total = breakdown.sumOf { it.total }.coerceAtLeast(1)

    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Drink Breakdown",
                style = MaterialTheme.typography.titleMedium,
            )

            breakdown.forEach { item ->
                val drinkType = DrinkType.entries.find { it.key == item.drinkType } ?: return@forEach
                val fraction = item.total.toFloat() / total
                val percentage = (fraction * 100).toInt()

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(drinkType.color),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = drinkType.displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(60.dp),
                    )
                    LinearProgressIndicator(
                        progress = { fraction },
                        modifier = Modifier
                            .weight(1f)
                            .height(8.dp)
                            .clip(MaterialTheme.shapes.small),
                        color = drinkType.color,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${item.total} ml",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.width(64.dp),
                    )
                    Text(
                        text = "$percentage%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}
