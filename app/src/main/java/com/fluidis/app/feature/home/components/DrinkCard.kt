package com.fluidis.app.feature.home.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedIconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fluidis.app.core.model.DrinkType

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DrinkCard(
    drinkType: DrinkType,
    totalMl: Int,
    servingMl: Int,
    servingCount: Int,
    maxServings: Int,
    onAdd: () -> Unit,
    onUndo: () -> Unit,
    onLongPressUndo: () -> Unit,
    onLongPressAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            // First line: icon, name, total
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = drinkType.icon,
                    contentDescription = null,
                    tint = drinkType.color,
                    modifier = Modifier.size(24.dp),
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = drinkType.displayName,
                    style = MaterialTheme.typography.titleMedium,
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = "$totalMl ml",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = drinkType.color,
                )
            }

            // Second line: -, count, +
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val limitText = if (maxServings > 0) " · $servingCount/$maxServings" else ""
                Text(
                    text = "${servingMl}ml/serving$limitText",
                    style = MaterialTheme.typography.labelMedium,
                    color = if (maxServings > 0 && servingCount >= maxServings) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                )

                Spacer(modifier = Modifier.weight(1f))

                OutlinedIconButton(
                    onClick = onUndo,
                    enabled = totalMl > 0,
                    modifier = Modifier
                        .size(36.dp)
                        .then(
                            if (totalMl > 0) {
                                Modifier.combinedClickable(
                                    onClick = onUndo,
                                    onLongClick = onLongPressUndo,
                                )
                            } else {
                                Modifier
                            }
                        ),
                ) {
                    Icon(
                        Icons.Rounded.Remove,
                        contentDescription = "Undo last ${drinkType.displayName}",
                        modifier = Modifier.size(18.dp),
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                val count = if (servingMl > 0) totalMl / servingMl else 0
                Text(
                    text = "$count",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                )

                Spacer(modifier = Modifier.width(16.dp))

                val atLimit = maxServings > 0 && servingCount >= maxServings
                FilledIconButton(
                    onClick = onAdd,
                    enabled = !atLimit,
                    modifier = Modifier
                        .size(36.dp)
                        .then(
                            if (!atLimit) {
                                Modifier.combinedClickable(
                                    onClick = onAdd,
                                    onLongClick = onLongPressAdd,
                                )
                            } else {
                                Modifier
                            }
                        ),
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = drinkType.color,
                    ),
                ) {
                    Icon(
                        Icons.Rounded.Add,
                        contentDescription = "Add ${drinkType.displayName}",
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}
