package com.fluidis.app.feature.home.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fluidis.app.core.model.DrinkType
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.ln
import kotlin.math.max

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

                // Minus button — tap removes one, hold repeats with acceleration
                val minusEnabled = totalMl > 0
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .border(
                            width = 1.dp,
                            color = MaterialTheme.colorScheme.outline.copy(
                                alpha = if (minusEnabled) 1f else 0.38f,
                            ),
                            shape = CircleShape,
                        )
                        .then(
                            if (minusEnabled) {
                                Modifier.repeatingClickable(
                                    onClick = onUndo,
                                    initialDelayMs = 400L,
                                    maxDelayMs = 300L,
                                    minDelayMs = 50L,
                                )
                            } else {
                                Modifier
                            }
                        )
                        .alpha(if (minusEnabled) 1f else 0.38f)
                        .semantics { role = Role.Button },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Rounded.Remove,
                        contentDescription = "Remove ${drinkType.displayName}",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onSurface,
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

                // Plus button — tap adds serving, long-press opens custom amount
                val atLimit = maxServings > 0 && servingCount >= maxServings
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(
                            color = drinkType.color.copy(
                                alpha = if (atLimit) 0.38f else 1f,
                            ),
                        )
                        .then(
                            if (!atLimit) {
                                Modifier.combinedClickable(
                                    onClick = onAdd,
                                    onLongClick = onLongPressAdd,
                                )
                            } else {
                                Modifier
                            }
                        )
                        .semantics { role = Role.Button },
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Rounded.Add,
                        contentDescription = "Add ${drinkType.displayName}",
                        modifier = Modifier.size(18.dp),
                        tint = MaterialTheme.colorScheme.onPrimary,
                    )
                }
            }
        }
    }
}

/**
 * Modifier that fires [onClick] on tap, and when held down, repeats [onClick]
 * with logarithmic acceleration — starts slow, speeds up the longer you hold.
 *
 * @param initialDelayMs delay before repeating starts (long-press threshold)
 * @param maxDelayMs delay between first few repeats
 * @param minDelayMs fastest repeat rate (floor)
 */
private fun Modifier.repeatingClickable(
    onClick: () -> Unit,
    initialDelayMs: Long = 400L,
    maxDelayMs: Long = 300L,
    minDelayMs: Long = 50L,
): Modifier = this
    .clickable(onClick = onClick)
    .pointerInput(onClick) {
        coroutineScope {
            awaitEachGesture {
                awaitFirstDown()
                val repeatingJob = launch {
                    delay(initialDelayMs)
                    var step = 1
                    while (true) {
                        onClick()
                        // Logarithmic decay: delay = maxDelay / ln(step + e)
                        // At step 1: ~maxDelay, decays toward minDelay
                        val delay = max(
                            minDelayMs,
                            (maxDelayMs / ln(step.toDouble() + Math.E)).toLong(),
                        )
                        delay(delay)
                        step++
                    }
                }
                waitForUpOrCancellation()
                repeatingJob.cancel()
            }
        }
    }
