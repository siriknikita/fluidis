package com.fluidis.app.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.fluidis.app.BuildConfig
import com.fluidis.app.core.model.DrinkType
import com.fluidis.app.feature.settings.components.GoalSettingDialog
import com.fluidis.app.feature.settings.components.MaxServingsDialog
import com.fluidis.app.feature.settings.components.ServingSizeDialog
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showGoalDialog by remember { mutableStateOf(false) }
    var editingServingDrinkType by remember { mutableStateOf<DrinkType?>(null) }
    var editingMaxServingsDrinkType by remember { mutableStateOf<DrinkType?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Settings") },
            navigationIcon = {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                }
            },
        )

        when (val state = uiState) {
            is SettingsUiState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is SettingsUiState.Success -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                ) {
                    // Daily Goal
                    SectionHeader("Hydration")
                    ListItem(
                        headlineContent = { Text("Daily goal") },
                        supportingContent = {
                            val goalText = if (state.goalUpperMl != null && state.goalUpperMl > state.goalMl) {
                                "${state.goalMl} – ${state.goalUpperMl} ml"
                            } else {
                                "${state.goalMl} ml"
                            }
                            Text(goalText)
                        },
                        modifier = Modifier.clickable { showGoalDialog = true },
                    )

                    HorizontalDivider()

                    // Serving Sizes
                    SectionHeader("Serving Sizes")
                    DrinkType.entries.forEach { drinkType ->
                        val currentSize = state.servingSizes[drinkType] ?: drinkType.defaultServingMl
                        ListItem(
                            headlineContent = { Text(drinkType.displayName) },
                            supportingContent = { Text("$currentSize ml per serving") },
                            leadingContent = {
                                Icon(
                                    imageVector = drinkType.icon,
                                    contentDescription = null,
                                    tint = drinkType.color,
                                )
                            },
                            modifier = Modifier.clickable { editingServingDrinkType = drinkType },
                        )
                    }

                    HorizontalDivider()

                    // Daily Limits
                    SectionHeader("Daily Limits")
                    DrinkType.entries.forEach { drinkType ->
                        val currentMax = state.maxServings[drinkType] ?: 0
                        val limitText = if (currentMax > 0) "$currentMax servings/day" else "Unlimited"
                        ListItem(
                            headlineContent = { Text(drinkType.displayName) },
                            supportingContent = { Text(limitText) },
                            leadingContent = {
                                Icon(
                                    imageVector = drinkType.icon,
                                    contentDescription = null,
                                    tint = drinkType.color,
                                )
                            },
                            modifier = Modifier.clickable { editingMaxServingsDrinkType = drinkType },
                        )
                    }

                    HorizontalDivider()

                    // Analytics
                    SectionHeader("Analytics")
                    val startDateText = state.analyticsStartDate?.toString()
                        ?: state.detectedStartDate?.let { "$it (auto)" }
                        ?: "Not set"
                    ListItem(
                        headlineContent = { Text("Analytics start date") },
                        supportingContent = { Text(startDateText) },
                        modifier = Modifier.clickable { showDatePicker = true },
                    )

                    HorizontalDivider()

                    // About
                    SectionHeader("About")
                    ListItem(
                        headlineContent = { Text("Fluidis") },
                        supportingContent = { Text("Version ${BuildConfig.VERSION_NAME}") },
                    )
                }

                // Dialogs
                if (showGoalDialog) {
                    GoalSettingDialog(
                        currentGoalMl = state.goalMl,
                        currentGoalUpperMl = state.goalUpperMl,
                        onConfirm = { goal, upper ->
                            viewModel.updateDailyGoal(goal, upper)
                            showGoalDialog = false
                        },
                        onDismiss = { showGoalDialog = false },
                    )
                }

                editingMaxServingsDrinkType?.let { drinkType ->
                    MaxServingsDialog(
                        drinkType = drinkType,
                        currentMax = state.maxServings[drinkType] ?: 0,
                        onConfirm = { max ->
                            viewModel.updateMaxServings(drinkType, max)
                            editingMaxServingsDrinkType = null
                        },
                        onDismiss = { editingMaxServingsDrinkType = null },
                    )
                }

                editingServingDrinkType?.let { drinkType ->
                    ServingSizeDialog(
                        drinkType = drinkType,
                        currentSizeMl = state.servingSizes[drinkType] ?: drinkType.defaultServingMl,
                        onConfirm = { size ->
                            viewModel.updateServingSize(drinkType, size)
                            editingServingDrinkType = null
                        },
                        onDismiss = { editingServingDrinkType = null },
                    )
                }

                if (showDatePicker) {
                    val initialMillis = (state.analyticsStartDate ?: state.detectedStartDate)
                        ?.atStartOfDayIn(TimeZone.UTC)
                        ?.toEpochMilliseconds()
                    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialMillis)

                    DatePickerDialog(
                        onDismissRequest = { showDatePicker = false },
                        confirmButton = {
                            TextButton(onClick = {
                                datePickerState.selectedDateMillis?.let { millis ->
                                    val date = Instant.fromEpochMilliseconds(millis)
                                        .toLocalDateTime(TimeZone.UTC)
                                        .date
                                    viewModel.updateAnalyticsStartDate(date)
                                }
                                showDatePicker = false
                            }) {
                                Text("Set")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDatePicker = false }) {
                                Text("Cancel")
                            }
                        },
                    ) {
                        DatePicker(state = datePickerState)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
    )
}
