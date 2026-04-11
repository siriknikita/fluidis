package com.fluidis.app.core.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Coffee
import androidx.compose.material.icons.rounded.EmojiFoodBeverage
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.fluidis.app.core.theme.Blue40
import com.fluidis.app.core.theme.Brown40
import com.fluidis.app.core.theme.Teal40

enum class DrinkType(
    val key: String,
    val displayName: String,
    val defaultServingMl: Int,
    val icon: ImageVector,
    val color: Color,
) {
    WATER(
        key = "water",
        displayName = "Water",
        defaultServingMl = 500,
        icon = Icons.Rounded.WaterDrop,
        color = Blue40,
    ),
    TEA(
        key = "tea",
        displayName = "Tea",
        defaultServingMl = 350,
        icon = Icons.Rounded.EmojiFoodBeverage,
        color = Teal40,
    ),
    COFFEE(
        key = "coffee",
        displayName = "Coffee",
        defaultServingMl = 350,
        icon = Icons.Rounded.Coffee,
        color = Brown40,
    );

    companion object {
        fun fromKey(key: String): DrinkType = entries.first { it.key == key }
    }
}
