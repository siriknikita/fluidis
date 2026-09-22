package com.fluidis.app.core.model

/**
 * Where one day's intake landed against the daily goal.
 *
 * The single definition of the classification, so the calendar rings, the month donut and the
 * Stats summary can't disagree about which days met the goal.
 */
enum class GoalStatus {
    BELOW,
    MET,

    /** Above the upper bound of a goal range. Only possible when the range is real. */
    OVER;

    companion object {
        fun of(totalMl: Int, goalMl: Int, upperMl: Int?): GoalStatus = when {
            upperMl != null && upperMl > goalMl && totalMl > upperMl -> OVER
            totalMl >= goalMl -> MET
            else -> BELOW
        }
    }
}
