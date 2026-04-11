package com.fluidis.app.feature.settings

import com.fluidis.app.core.model.DrinkType
import kotlinx.datetime.LocalDate

sealed interface SettingsUiState {
    data object Loading : SettingsUiState

    data class Success(
        val goalMl: Int,
        val servingSizes: Map<DrinkType, Int>,
        val analyticsStartDate: LocalDate?,
        val detectedStartDate: LocalDate?,
    ) : SettingsUiState
}
