package com.fluidis.app.feature.home

import com.fluidis.app.core.model.DrinkEntry
import com.fluidis.app.core.model.DrinkType

sealed interface HomeUiState {
    data object Loading : HomeUiState

    data class Success(
        val totalMl: Int,
        val goalMl: Int,
        val drinkTotals: Map<DrinkType, Int>,
        val drinkServingCounts: Map<DrinkType, Int>,
        val maxServings: Map<DrinkType, Int>,
        val recentEntries: List<DrinkEntry>,
        val servingSizes: Map<DrinkType, Int>,
    ) : HomeUiState {

        fun isAtLimit(drinkType: DrinkType): Boolean {
            val max = maxServings[drinkType] ?: 0
            if (max <= 0) return false
            return (drinkServingCounts[drinkType] ?: 0) >= max
        }
        val progressFraction: Float
            get() = if (goalMl > 0) (totalMl.toFloat() / goalMl).coerceAtLeast(0f) else 0f

        val goalReached: Boolean
            get() = totalMl >= goalMl

        val remainingMl: Int
            get() = (goalMl - totalMl).coerceAtLeast(0)

        val overGoalMl: Int
            get() = (totalMl - goalMl).coerceAtLeast(0)
    }
}