package com.fluidis.app.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.navigation.compose.rememberNavController
import com.fluidis.app.core.navigation.FluidisNavHost
import com.fluidis.app.core.theme.FluidisTheme
import com.fluidis.app.core.ui.FluidisScaffold
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@HiltAndroidTest
class NavigationTest {

    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @get:Rule(order = 1)
    val composeTestRule = createComposeRule()

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @Test
    fun bottomNav_homeIsDefaultDestination() {
        composeTestRule.setContent {
            FluidisTheme {
                val navController = rememberNavController()
                val snackbarHostState = SnackbarHostState()
                FluidisScaffold(navController = navController, snackbarHostState = snackbarHostState) { modifier ->
                    FluidisNavHost(navController = navController, snackbarHostState = snackbarHostState, modifier = modifier)
                }
            }
        }

        // Home tab should be selected by default — water drop icon visible
        composeTestRule.onNodeWithText("Home").assertIsDisplayed()
        composeTestRule.onNodeWithText("Stats").assertIsDisplayed()
        composeTestRule.onNodeWithText("History").assertIsDisplayed()
    }

    @Test
    fun bottomNav_navigateToStats() {
        composeTestRule.setContent {
            FluidisTheme {
                val navController = rememberNavController()
                val snackbarHostState = SnackbarHostState()
                FluidisScaffold(navController = navController, snackbarHostState = snackbarHostState) { modifier ->
                    FluidisNavHost(navController = navController, snackbarHostState = snackbarHostState, modifier = modifier)
                }
            }
        }

        composeTestRule.onNodeWithText("Stats").performClick()
        composeTestRule.onNodeWithText("Week").assertIsDisplayed()
        composeTestRule.onNodeWithText("Month").assertIsDisplayed()
        composeTestRule.onNodeWithText("All Time").assertIsDisplayed()
    }

    @Test
    fun bottomNav_navigateToHistory() {
        composeTestRule.setContent {
            FluidisTheme {
                val navController = rememberNavController()
                val snackbarHostState = SnackbarHostState()
                FluidisScaffold(navController = navController, snackbarHostState = snackbarHostState) { modifier ->
                    FluidisNavHost(navController = navController, snackbarHostState = snackbarHostState, modifier = modifier)
                }
            }
        }

        composeTestRule.onNodeWithText("History").performClick()
        // Calendar should show day-of-week headers
        composeTestRule.onNodeWithText("Mo").assertIsDisplayed()
        composeTestRule.onNodeWithText("Su").assertIsDisplayed()
    }

    @Test
    fun topBar_navigateToSettings() {
        composeTestRule.setContent {
            FluidisTheme {
                val navController = rememberNavController()
                val snackbarHostState = SnackbarHostState()
                FluidisScaffold(navController = navController, snackbarHostState = snackbarHostState) { modifier ->
                    FluidisNavHost(navController = navController, snackbarHostState = snackbarHostState, modifier = modifier)
                }
            }
        }

        composeTestRule.onNodeWithContentDescription("Settings").performClick()
        composeTestRule.onNodeWithText("Daily goal").assertIsDisplayed()
        composeTestRule.onNodeWithText("Serving Sizes").assertIsDisplayed()
    }

    @Test
    fun settings_backNavigatesToPreviousScreen() {
        composeTestRule.setContent {
            FluidisTheme {
                val navController = rememberNavController()
                val snackbarHostState = SnackbarHostState()
                FluidisScaffold(navController = navController, snackbarHostState = snackbarHostState) { modifier ->
                    FluidisNavHost(navController = navController, snackbarHostState = snackbarHostState, modifier = modifier)
                }
            }
        }

        // Go to settings
        composeTestRule.onNodeWithContentDescription("Settings").performClick()
        composeTestRule.onNodeWithText("Settings").assertIsDisplayed()

        // Go back
        composeTestRule.onNodeWithContentDescription("Back").performClick()

        // Should be back on Home — bottom nav visible
        composeTestRule.onNodeWithText("Home").assertIsDisplayed()
    }
}
