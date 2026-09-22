package com.fluidis.app.core.time

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

/** A clock the test moves by hand; defaults to the real today so date-agnostic tests are unaffected. */
class FakeTodayProvider(
    initial: LocalDate = Clock.System.todayIn(TimeZone.currentSystemDefault()),
) : TodayProvider {
    private val _today = MutableStateFlow(initial)
    override val today: StateFlow<LocalDate> = _today

    override fun refresh() = Unit

    fun set(date: LocalDate) {
        _today.value = date
    }
}
