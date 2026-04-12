package com.fluidis.app.feature.settings.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fluidis.app.core.ui.isValidAmountInput

@Composable
fun GoalSettingDialog(
    currentGoalMl: Int,
    currentGoalUpperMl: Int?,
    onConfirm: (goalMl: Int, upperMl: Int?) -> Unit,
    onDismiss: () -> Unit,
) {
    var goalText by remember { mutableStateOf(currentGoalMl.toString()) }
    var hasUpperBound by remember {
        mutableStateOf(currentGoalUpperMl != null && currentGoalUpperMl > currentGoalMl)
    }
    var upperText by remember {
        mutableStateOf(currentGoalUpperMl?.toString() ?: "")
    }

    val goalMl = goalText.toIntOrNull() ?: 0
    val upperMl = upperText.toIntOrNull() ?: 0
    val isValid = goalMl > 0 && (!hasUpperBound || upperMl > goalMl)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Daily Hydration Goal") },
        text = {
            Column {
                OutlinedTextField(
                    value = goalText,
                    onValueChange = { value ->
                        if (value.isValidAmountInput()) {
                            goalText = value
                        }
                    },
                    label = { Text(if (hasUpperBound) "Minimum goal" else "Goal") },
                    suffix = { Text("ml") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = hasUpperBound,
                        onCheckedChange = { hasUpperBound = it },
                    )
                    Text("Set upper bound")
                }

                AnimatedVisibility(visible = hasUpperBound) {
                    OutlinedTextField(
                        value = upperText,
                        onValueChange = { value ->
                            if (value.isValidAmountInput()) {
                                upperText = value
                            }
                        },
                        label = { Text("Maximum goal") },
                        suffix = { Text("ml") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(goalMl, if (hasUpperBound) upperMl else null) },
                enabled = isValid,
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
