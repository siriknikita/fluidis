package com.fluidis.app.core.model

import kotlinx.datetime.LocalDate

data class Settings(
    val dailyGoalMl: Int = 2000,
    val dailyGoalUpperMl: Int? = null,
    val waterServingMl: Int = 500,
    val teaServingMl: Int = 350,
    val coffeeServingMl: Int = 350,
    val waterMaxServings: Int = 0,
    val teaMaxServings: Int = 0,
    val coffeeMaxServings: Int = 0,
    val analyticsStartDate: LocalDate? = null,
    val selectedChartMode: ChartMode = ChartMode.BAR,
) {
    val hasGoalRange: Boolean
        get() = dailyGoalUpperMl != null && dailyGoalUpperMl > dailyGoalMl

    val effectiveUpperMl: Int
        get() = if (hasGoalRange) dailyGoalUpperMl!! else dailyGoalMl

    fun servingMlFor(drinkType: DrinkType): Int = when (drinkType) {
        DrinkType.WATER -> waterServingMl
        DrinkType.TEA -> teaServingMl
        DrinkType.COFFEE -> coffeeServingMl
    }

    /** 0 means unlimited */
    fun maxServingsFor(drinkType: DrinkType): Int = when (drinkType) {
        DrinkType.WATER -> waterMaxServings
        DrinkType.TEA -> teaMaxServings
        DrinkType.COFFEE -> coffeeMaxServings
    }
}

enum class ChartMode(val key: String, val displayName: String) {
    BAR("bar", "Bar"),
    STACKED_BAR("stacked_bar", "Stacked"),
    LINE("line", "Line"),
    AREA("area", "Area");

    companion object {
        fun fromKey(key: String): ChartMode = entries.find { it.key == key } ?: BAR
    }
}
