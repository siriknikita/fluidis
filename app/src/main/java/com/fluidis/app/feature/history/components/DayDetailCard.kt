package com.fluidis.app.feature.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fluidis.app.core.model.DrinkType
import com.fluidis.app.core.model.GoalStatus
import com.fluidis.app.feature.history.DaySummary
import com.fluidis.app.feature.history.DrinkDayTotal
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.roundToInt

/**
 * The selected day at a glance: total against the goal and what it was made of, per drink.
 * Editing individual entries stays in [DayDetailPanel], one tap away.
 */
@Composable
fun DayDetailCard(
    date: LocalDate,
    isToday: Boolean,
    summary: DaySummary,
    goalMl: Int,
    goalUpperMl: Int?,
    onAddEntry: (DrinkType) -> Unit,
    onShowEntries: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val status = GoalStatus.of(summary.totalMl, goalMl, goalUpperMl)
    val goalPercent = if (goalMl > 0) (summary.totalMl * 100f / goalMl).roundToInt() else 0

    ElevatedCard(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Header(date = date, isToday = isToday, onAddEntry = onAddEntry)

            if (summary.entries.isEmpty()) {
                Text(
                    text = if (isToday) "Nothing logged yet today." else "Nothing logged on this day.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = 4.dp),
                )
            } else {
                Column(
                    verticalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.clearAndSetSemantics {
                        contentDescription =
                            "${summary.totalMl} of $goalMl millilitres, $goalPercent percent of goal"
                    },
                ) {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = "${summary.totalMl}",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.SemiBold,
                        )
                        Text(
                            text = " / $goalMl ml",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        Text(
                            text = "$goalPercent%",
                            style = MaterialTheme.typography.labelLarge,
                            color = status.color,
                            modifier = Modifier.padding(bottom = 4.dp),
                        )
                    }
                    LinearProgressIndicator(
                        progress = { (goalPercent / 100f).coerceAtMost(1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = status.color,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        drawStopIndicator = {},
                        gapSize = 0.dp,
                    )
                }

                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    summary.drinks.forEach { drink -> DrinkRow(drink, summary.totalMl) }
                }

                EntriesButton(count = summary.entries.size, onClick = onShowEntries)
            }
        }
    }
}

@Composable
private fun Header(date: LocalDate, isToday: Boolean, onAddEntry: (DrinkType) -> Unit) {
    val javaDate = date.toJavaLocalDate()
    var menuOpen by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (isToday) "TODAY" else {
                    javaDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault()).uppercase()
                },
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = javaDate.format(DateTimeFormatter.ofPattern("d MMMM", Locale.getDefault())),
                style = MaterialTheme.typography.titleLarge,
            )
        }
        Box {
            FilledIconButton(onClick = { menuOpen = true }) {
                Icon(Icons.Rounded.Add, contentDescription = "Add entry")
            }
            DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
                DrinkType.entries.forEach { type ->
                    DropdownMenuItem(
                        text = { Text(type.displayName) },
                        leadingIcon = { Icon(type.icon, contentDescription = null, tint = type.color) },
                        onClick = {
                            menuOpen = false
                            onAddEntry(type)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun DrinkRow(drink: DrinkDayTotal, dayTotalMl: Int) {
    val type = drink.drinkType
    val fraction = if (dayTotalMl > 0) drink.totalMl.toFloat() / dayTotalMl else 0f
    val servings = if (drink.servings == 1) "1 serving" else "${drink.servings} servings"

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .alpha(if (drink.servings == 0) 0.5f else 1f)
            .clearAndSetSemantics {
                contentDescription = "${type.displayName}, ${drink.totalMl} millilitres, $servings"
            },
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(type.color.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(type.icon, contentDescription = null, tint = type.color, modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(5.dp)) {
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = type.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = servings,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 1.dp),
                )
                Spacer(modifier = Modifier.weight(1f))
                Text(text = "${drink.totalMl} ml", style = MaterialTheme.typography.labelLarge)
            }
            LinearProgressIndicator(
                progress = { fraction },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = type.color,
                trackColor = MaterialTheme.colorScheme.surfaceVariant,
                drawStopIndicator = {},
                gapSize = 0.dp,
            )
        }
    }
}

@Composable
private fun EntriesButton(count: Int, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
    ) {
        Text(
            text = if (count == 1) "View 1 entry" else "View $count entries",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f),
        )
        Icon(
            Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp),
        )
    }
}

