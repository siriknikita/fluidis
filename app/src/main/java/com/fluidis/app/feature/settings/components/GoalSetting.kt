package com.fluidis.app.feature.settings.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.fluidis.app.core.ui.isValidAmountInput

@Composable
fun GoalSettingDialog(
    currentGoalMl: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var goalText by remember { mutableStateOf(currentGoalMl.toString()) }
    val goalMl = goalText.toIntOrNull() ?: 0
    val isValid = goalMl > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Daily Hydration Goal") },
        text = {
            OutlinedTextField(
                value = goalText,
                onValueChange = { value ->
                    if (value.isValidAmountInput()) {
                        goalText = value
                    }
                },
                label = { Text("Goal") },
                suffix = { Text("ml") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(goalMl) }, enabled = isValid) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
