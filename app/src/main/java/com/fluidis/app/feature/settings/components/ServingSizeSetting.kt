package com.fluidis.app.feature.settings.components

import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.input.KeyboardType
import com.fluidis.app.core.model.DrinkType

@Composable
fun ServingSizeDialog(
    drinkType: DrinkType,
    currentSizeMl: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var sizeText by remember { mutableStateOf(currentSizeMl.toString()) }
    val sizeMl = sizeText.toIntOrNull() ?: 0
    val isValid = sizeMl > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = drinkType.icon,
                contentDescription = null,
                tint = drinkType.color,
            )
        },
        title = { Text("${drinkType.displayName} Serving Size") },
        text = {
            OutlinedTextField(
                value = sizeText,
                onValueChange = { value ->
                    if (value.all { it.isDigit() } && value.length <= 5) {
                        sizeText = value
                    }
                },
                label = { Text("Serving size") },
                suffix = { Text("ml") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            )
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(sizeMl) }, enabled = isValid) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
    )
}
