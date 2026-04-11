package com.fluidis.app.feature.history.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MenuAnchorType
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
import androidx.compose.ui.unit.dp
import com.fluidis.app.core.model.DrinkEntry
import com.fluidis.app.core.model.DrinkType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditEntryDialog(
    entry: DrinkEntry,
    onConfirm: (amountMl: Int, drinkType: DrinkType) -> Unit,
    onDismiss: () -> Unit,
) {
    var amountText by remember { mutableStateOf(entry.amountMl.toString()) }
    var selectedDrinkType by remember { mutableStateOf(DrinkType.fromKey(entry.drinkType)) }
    var dropdownExpanded by remember { mutableStateOf(false) }

    val amountMl = amountText.toIntOrNull() ?: 0
    val isValid = amountMl > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Entry") },
        text = {
            Column {
                // Drink type selector
                ExposedDropdownMenuBox(
                    expanded = dropdownExpanded,
                    onExpandedChange = { dropdownExpanded = it },
                ) {
                    OutlinedTextField(
                        value = selectedDrinkType.displayName,
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        label = { Text("Drink type") },
                        leadingIcon = {
                            Icon(
                                imageVector = selectedDrinkType.icon,
                                contentDescription = null,
                                tint = selectedDrinkType.color,
                            )
                        },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = dropdownExpanded) },
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownExpanded,
                        onDismissRequest = { dropdownExpanded = false },
                    ) {
                        DrinkType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.displayName) },
                                leadingIcon = {
                                    Icon(
                                        imageVector = type.icon,
                                        contentDescription = null,
                                        tint = type.color,
                                    )
                                },
                                onClick = {
                                    selectedDrinkType = type
                                    dropdownExpanded = false
                                },
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Amount input
                OutlinedTextField(
                    value = amountText,
                    onValueChange = { value ->
                        if (value.all { it.isDigit() } && value.length <= 5) {
                            amountText = value
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Amount") },
                    suffix = { Text("ml") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(amountMl, selectedDrinkType) },
                enabled = isValid,
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
}
