package com.fluidis.app.core.ui

import androidx.compose.ui.unit.dp

object AnimationConstants {
    const val PROGRESS_DURATION_MS = 600
}

object RepeatClickDefaults {
    const val INITIAL_DELAY_MS = 400L
    const val MAX_DELAY_MS = 300L
    const val MIN_DELAY_MS = 50L
}

object ChartDefaults {
    const val Y_AXIS_ROUND_INTERVAL = 500
    val CHART_HEIGHT = 200.dp
    const val LEFT_PADDING = 40f
    const val BOTTOM_PADDING = 24f
    const val BAR_WIDTH_FRACTION = 0.6f
    const val GAP_FRACTION = 0.4f
    const val BAR_CORNER_RADIUS = 4f
    const val LINE_STROKE_WIDTH = 3f
    const val DOT_RADIUS = 4f
}

object DatePeriods {
    const val WEEK_OFFSET_DAYS = 6
    const val MONTH_OFFSET_DAYS = 29
    const val DEFAULT_ALL_TIME_DAYS = 365
}
