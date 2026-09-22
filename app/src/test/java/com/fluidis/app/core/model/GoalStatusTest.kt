package com.fluidis.app.core.model

import org.junit.Assert.assertEquals
import org.junit.Test

class GoalStatusTest {

    @Test
    fun `classification respects the goal and a real upper bound only`() {
        assertEquals(GoalStatus.BELOW, GoalStatus.of(1999, 2000, null))
        assertEquals(GoalStatus.MET, GoalStatus.of(2000, 2000, null))
        assertEquals(GoalStatus.MET, GoalStatus.of(5000, 2000, null))
        assertEquals(GoalStatus.MET, GoalStatus.of(3000, 2000, 3000))
        assertEquals(GoalStatus.OVER, GoalStatus.of(3001, 2000, 3000))
        // An upper bound at or below the goal isn't a range, so nothing is "over".
        assertEquals(GoalStatus.MET, GoalStatus.of(3001, 2000, 1500))
    }
}
