package com.fluidis.app.feature.history.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.fluidis.app.core.model.DailyTotal
import com.fluidis.app.core.model.GoalStatus
import com.fluidis.app.feature.history.MonthYear
import kotlinx.datetime.LocalDate

private val WEEKDAY_LABELS = listOf("Mo", "Tu", "We", "Th", "Fr", "Sa", "Su")
private val CELL_HEIGHT = 44.dp
private val RING_DIAMETER = 36.dp
private val RING_WIDTH = 3.dp

/** Horizontal drag distance past which a swipe pages the month. */
private const val SWIPE_THRESHOLD_PX = 120f

/** Month grid where every logged day carries a ring showing how much of the goal it reached. */
@Composable
fun CalendarView(
    currentMonth: MonthYear,
    today: LocalDate,
    datesWithEntries: Map<LocalDate, DailyTotal>,
    goalMl: Int,
    goalUpperMl: Int?,
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onToday: () -> Unit,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier
            .fillMaxWidth()
            // Horizontal swipes page the month; vertical drags still reach the enclosing scroll.
            .pointerInput(onPreviousMonth, onNextMonth) {
                var dragged = 0f
                detectHorizontalDragGestures(
                    onDragStart = { dragged = 0f },
                    onDragEnd = {
                        when {
                            dragged < -SWIPE_THRESHOLD_PX -> onNextMonth()
                            dragged > SWIPE_THRESHOLD_PX -> onPreviousMonth()
                        }
                    },
                    onHorizontalDrag = { _, amount -> dragged += amount },
                )
            },
    ) {
        Column(modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onPreviousMonth) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowLeft, contentDescription = "Previous month")
                }
                Text(
                    text = currentMonth.displayName(),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier
                        .weight(1f)
                        .padding(start = 4.dp),
                )
                AnimatedVisibility(
                    visible = selectedDate != today,
                    enter = fadeIn() + scaleIn(initialScale = 0.8f),
                    exit = fadeOut() + scaleOut(targetScale = 0.8f),
                ) {
                    FilledTonalButton(
                        onClick = onToday,
                        modifier = Modifier.height(32.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp),
                    ) {
                        Text("Today", style = MaterialTheme.typography.labelLarge)
                    }
                }
                IconButton(onClick = onNextMonth) {
                    Icon(Icons.AutoMirrored.Rounded.KeyboardArrowRight, contentDescription = "Next month")
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp),
            ) {
                WEEKDAY_LABELS.forEach { day ->
                    Text(
                        text = day,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            CalendarGrid(
                currentMonth = currentMonth,
                today = today,
                selectedDate = selectedDate,
                datesWithEntries = datesWithEntries,
                goalMl = goalMl,
                goalUpperMl = goalUpperMl,
                onDateSelected = onDateSelected,
            )
        }
    }
}

@Composable
private fun CalendarGrid(
    currentMonth: MonthYear,
    today: LocalDate,
    selectedDate: LocalDate,
    datesWithEntries: Map<LocalDate, DailyTotal>,
    goalMl: Int,
    goalUpperMl: Int?,
    onDateSelected: (LocalDate) -> Unit,
) {
    val daysInMonth = currentMonth.lastDay.dayOfMonth
    val startOffset = currentMonth.firstDay.dayOfWeek.ordinal // Monday = 0
    val rows = (startOffset + daysInMonth + 6) / 7

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val dayNumber = row * 7 + col - startOffset + 1
                    // Fixed-height cells, so rows of blanks and rows of days line up.
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(CELL_HEIGHT),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (dayNumber in 1..daysInMonth) {
                            val date = LocalDate(currentMonth.year, currentMonth.month, dayNumber)
                            CalendarDay(
                                date = date,
                                isToday = date == today,
                                isFuture = date > today,
                                isSelected = date == selectedDate,
                                dailyTotal = datesWithEntries[date],
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
}

@Composable
private fun CalendarDay(
    date: LocalDate,
    isToday: Boolean,
    isFuture: Boolean,
    isSelected: Boolean,
    dailyTotal: DailyTotal?,
    goalMl: Int,
    goalUpperMl: Int?,
    onClick: () -> Unit,
) {
    val status = dailyTotal?.let { GoalStatus.of(it.total, goalMl, goalUpperMl) }
    // Share of the goal reached, capped at a full ring.
    val progress = if (dailyTotal != null && goalMl > 0) {
        (dailyTotal.total.toFloat() / goalMl).coerceAtMost(1f)
    } else {
        0f
    }
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimaryContainer
        isToday -> MaterialTheme.colorScheme.primary
        isFuture -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        else -> MaterialTheme.colorScheme.onSurface
    }
    val spoken = buildString {
        append(date.dayOfMonth)
        if (isToday) append(", today")
        append(if (dailyTotal == null) ", no entries" else ", ${dailyTotal.total} millilitres")
    }

    Box(
        modifier = Modifier
            .size(RING_DIAMETER)
            .clip(CircleShape)
            .clickable(onClick = onClick)
            // The ISO day is a stable handle for UI tests; the spoken label stays human.
            .testTag(date.toString())
            .semantics {
                contentDescription = spoken
                selected = isSelected
            },
        contentAlignment = Alignment.Center,
    ) {
        if (isSelected) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(RING_WIDTH + 1.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
            )
        }

        // Only logged days get a ring; the faint track behind it lets a partial fill read as a
        // fraction of the goal. Unlogged days stay bare so the grid isn't busy.
        if (status != null) {
            val color = status.color
            Canvas(modifier = Modifier.fillMaxSize()) {
                val stroke = RING_WIDTH.toPx()
                val arcSize = Size(size.width - stroke, size.height - stroke)
                val topLeft = Offset(stroke / 2, stroke / 2)
                drawArc(
                    color = color.copy(alpha = 0.18f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke),
                )
                drawArc(
                    color = color,
                    startAngle = -90f,
                    sweepAngle = 360f * progress,
                    useCenter = false,
                    topLeft = topLeft,
                    size = arcSize,
                    style = Stroke(width = stroke, cap = StrokeCap.Round),
                )
            }
        }

        Text(
            text = "${date.dayOfMonth}",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isToday || isSelected) FontWeight.Bold else FontWeight.Normal,
            color = textColor,
        )
    }
}
