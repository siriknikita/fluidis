package com.fluidis.app.feature.settings.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.fluidis.app.core.model.DrinkType
import com.fluidis.app.core.ui.isValidAmountInput

@Composable
fun MaxServingsDialog(
    drinkType: DrinkType,
    currentMax: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var maxText by remember { mutableStateOf(if (currentMax > 0) currentMax.toString() else "") }
    val maxServings = maxText.toIntOrNull() ?: 0

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = drinkType.icon,
                contentDescription = null,
                tint = drinkType.color,
            )
        },
        title = { Text("${drinkType.displayName} Daily Limit") },
        text = {
            Column {
                Text(
                    text = "Set maximum servings per day. Leave empty or set to 0 for unlimited.",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = maxText,
                    onValueChange = { value ->
                        if (value.isValidAmountInput(maxLength = 3)) {
                            maxText = value
                        }
                    },
                    label = { Text("Max servings") },
                    placeholder = { Text("Unlimited") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(maxServings) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
