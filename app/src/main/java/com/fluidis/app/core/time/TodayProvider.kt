package com.fluidis.app.core.time

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import javax.inject.Inject
import javax.inject.Singleton

/**
 * The single source of "today" for every screen.
 *
 * View models outlive a night in the background — Home is the nav start destination, so its
 * view model lives as long as the activity — so a date captured once goes stale. Collecting
 * [today] instead makes a day rollover re-run the date-keyed queries like any other change.
 */
interface TodayProvider {
    val today: StateFlow<LocalDate>

    /** Re-read the clock. Emits only if the date actually changed. */
    fun refresh()
}

/**
 * Follows the system clock: the date/time/time-zone broadcasts cover a rollover while the app
 * is running, and [MainActivity][com.fluidis.app.MainActivity] calls [refresh] on every start
 * because a cached process isn't guaranteed to receive broadcasts sent while it was frozen.
 */
@Singleton
class SystemTodayProvider @Inject constructor(
    @ApplicationContext context: Context,
) : TodayProvider {

    private val _today = MutableStateFlow(currentDate())
    override val today: StateFlow<LocalDate> = _today.asStateFlow()

    init {
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_DATE_CHANGED)
            addAction(Intent.ACTION_TIME_CHANGED)
            addAction(Intent.ACTION_TIMEZONE_CHANGED)
        }
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) = refresh()
        }
        // Singleton on the application context, so never unregistered — it lives with the process.
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)
    }

    // StateFlow drops equal values, so a same-day refresh doesn't re-run any query.
    override fun refresh() {
        _today.value = currentDate()
    }

    private fun currentDate(): LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault())
}
