package com.fluidis.app.feature.history.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.fluidis.app.core.model.DrinkEntry
import com.fluidis.app.core.model.DrinkType
import com.fluidis.app.feature.home.components.CustomAmountDialog
import kotlinx.datetime.LocalDate

private val PanelShape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)

@Composable
fun DayDetailPanel(
    date: LocalDate,
    entries: List<DrinkEntry>,
    totalMl: Int,
    onDeleteEntry: (DrinkEntry) -> Unit,
    onEditEntry: (DrinkEntry, Int, DrinkType) -> Unit,
    onAddEntry: (LocalDate, DrinkType, Int) -> Unit,
) {
    var editingEntry by remember { mutableStateOf<DrinkEntry?>(null) }
    var addingDrinkType by remember { mutableStateOf<DrinkType?>(null) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(1f),
    ) {
        // Panel surface with rounded top corners
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 76.dp),
            shape = PanelShape,
            color = MaterialTheme.colorScheme.surfaceContainerLow,
            tonalElevation = 2.dp,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 16.dp)
                    .padding(bottom = 96.dp),
            ) {
                // Date header
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                ) {
                    Text(
                        text = date.toString(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Total: $totalMl ml",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))
                Spacer(modifier = Modifier.height(8.dp))

                if (entries.isEmpty()) {
                    Text(
                        text = "No entries for this day",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 24.dp),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 16.dp),
                    ) {
                        items(entries, key = { it.id }) { entry ->
                            EntryItem(
                                entry = entry,
                                onEdit = { editingEntry = entry },
                                onDelete = { onDeleteEntry(entry) },
                            )
                        }
                    }
                }
            }
        }

        // Add button floating above the panel
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp, end = 20.dp),
            contentAlignment = androidx.compose.ui.Alignment.TopEnd,
        ) {
            IconButton(
                onClick = { addingDrinkType = DrinkType.WATER },
                modifier = Modifier
                    .size(56.dp)
                    .border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline,
                        shape = CircleShape,
                    ),
            ) {
                Icon(
                    Icons.Rounded.Add,
                    contentDescription = "Add entry",
                    tint = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }

    editingEntry?.let { entry ->
        EditEntryDialog(
            entry = entry,
            onConfirm = { amountMl, drinkType ->
                onEditEntry(entry, amountMl, drinkType)
                editingEntry = null
            },
            onDismiss = { editingEntry = null },
        )
    }

    addingDrinkType?.let { drinkType ->
        CustomAmountDialog(
            drinkType = drinkType,
            onConfirm = { amountMl ->
                onAddEntry(date, drinkType, amountMl)
                addingDrinkType = null
            },
            onDismiss = { addingDrinkType = null },
        )
    }
}
