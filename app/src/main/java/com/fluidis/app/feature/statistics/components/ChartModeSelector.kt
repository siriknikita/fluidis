package com.fluidis.app.feature.statistics.components

import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.ShowChart
import androidx.compose.material.icons.rounded.StackedBarChart
import androidx.compose.material.icons.rounded.AreaChart
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.vector.ImageVector
import com.fluidis.app.core.model.ChartMode

private fun ChartMode.icon(): ImageVector = when (this) {
    ChartMode.BAR -> Icons.Rounded.BarChart
    ChartMode.STACKED_BAR -> Icons.Rounded.StackedBarChart
    ChartMode.LINE -> Icons.Rounded.ShowChart
    ChartMode.AREA -> Icons.Rounded.AreaChart
}

@Composable
fun ChartModeSelector(
    selectedMode: ChartMode,
    onModeSelected: (ChartMode) -> Unit,
) {
    var expanded by remember { mutableStateOf(false) }

    IconButton(onClick = { expanded = true }) {
        Icon(
            imageVector = selectedMode.icon(),
            contentDescription = "Chart type: ${selectedMode.displayName}",
        )
    }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { expanded = false },
    ) {
        ChartMode.entries.forEach { mode ->
            DropdownMenuItem(
                text = { Text(mode.displayName) },
                leadingIcon = {
                    Icon(
                        imageVector = mode.icon(),
                        contentDescription = null,
                    )
                },
                onClick = {
                    onModeSelected(mode)
                    expanded = false
                },
            )
        }
    }
}
