package com.fluidis.app.feature.history.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fluidis.app.core.model.DailyTotal
import com.fluidis.app.core.theme.DotBelowGoal
import com.fluidis.app.core.theme.DotGoalMet
import com.fluidis.app.core.theme.DotOverUpper
import com.fluidis.app.feature.history.MonthYear
import kotlinx.datetime.Clock
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import java.time.YearMonth

@Composable
fun CalendarView(
    currentMonth: MonthYear,
    datesWithEntries: Map<LocalDate, DailyTotal>,
    goalMl: Int,
    goalUpperMl: Int?,
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val today = remember { Clock.System.todayIn(TimeZone.currentSystemDefault()) }
    val firstDayOfMonth = remember(currentMonth) {
        LocalDate(currentMonth.year, currentMonth.month, 1)
    }
    val daysInMonth = remember(currentMonth) {
        YearMonth.of(currentMonth.year, currentMonth.month.value).lengthOfMonth()
    }
    val firstDayOfWeek = remember(firstDayOfMonth) {
        firstDayOfMonth.dayOfWeek
    }
    val startOffset = remember(firstDayOfWeek) {
        (firstDayOfWeek.ordinal - DayOfWeek.MONDAY.ordinal + 7) % 7
    }

    Column(modifier = modifier) {
        // Month navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onPreviousMonth) {
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Previous month")
            }
            Text(
                text = currentMonth.displayName(),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            IconButton(onClick = onNextMonth) {
                Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Next month")
            }
        }

        // Day of week headers
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        // Calendar grid
        CalendarGrid(
            currentMonth = currentMonth,
            daysInMonth = daysInMonth,
            startOffset = startOffset,
            today = today,
            selectedDate = selectedDate,
            datesWithEntries = datesWithEntries,
            goalMl = goalMl,
            goalUpperMl = goalUpperMl,
            onDateSelected = onDateSelected,
        )
    }
}

@Composable
private fun CalendarGrid(
    currentMonth: MonthYear,
    daysInMonth: Int,
    startOffset: Int,
    today: LocalDate,
    selectedDate: LocalDate?,
    datesWithEntries: Map<LocalDate, DailyTotal>,
    goalMl: Int,
    goalUpperMl: Int?,
    onDateSelected: (LocalDate) -> Unit,
) {
    val totalCells = startOffset + daysInMonth
    val rows = (totalCells + 6) / 7

    for (row in 0 until rows) {
        Row(modifier = Modifier.fillMaxWidth()) {
            for (col in 0 until 7) {
                val cellIndex = row * 7 + col
                val dayNumber = cellIndex - startOffset + 1

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    if (dayNumber in 1..daysInMonth) {
                        val date = LocalDate(currentMonth.year, currentMonth.month, dayNumber)
                        val dailyTotal = datesWithEntries[date]
                        val isToday = date == today
                        val isSelected = date == selectedDate

                        CalendarDay(
                            dayNumber = dayNumber,
                            isToday = isToday,
                            isSelected = isSelected,
                            dailyTotal = dailyTotal,
                            goalMl = goalMl,
                            goalUpperMl = goalUpperMl,
                            onClick = { onDateSelected(date) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CalendarDay(
    dayNumber: Int,
    isToday: Boolean,
    isSelected: Boolean,
    dailyTotal: DailyTotal?,
    goalMl: Int,
    goalUpperMl: Int?,
    onClick: () -> Unit,
) {
    val backgroundColor = when {
        isSelected -> MaterialTheme.colorScheme.primaryContainer
        isToday -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surface
    }

    Column(
        modifier = Modifier
            .clip(CircleShape)
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "$dayNumber",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            },
        )

        if (dailyTotal != null) {
            val dotColor = when {
                goalUpperMl != null && goalUpperMl > goalMl && dailyTotal.total > goalUpperMl -> DotOverUpper
                dailyTotal.total >= goalMl -> DotGoalMet
                else -> DotBelowGoal
            }
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor),
            )
        }
    }
}
