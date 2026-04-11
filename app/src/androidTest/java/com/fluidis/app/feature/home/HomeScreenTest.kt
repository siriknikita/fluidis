package com.fluidis.app.feature.home

import androidx.compose.material3.SnackbarHostState
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.fluidis.app.core.model.DrinkType
import com.fluidis.app.core.theme.FluidisTheme
import com.fluidis.app.feature.home.components.CustomAmountDialog
import com.fluidis.app.feature.home.components.DailySummary
import com.fluidis.app.feature.home.components.DrinkCard
import org.junit.Rule
import org.junit.Test

class HomeScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun dailySummary_displaysProgressAndGoal() {
        composeTestRule.setContent {
            FluidisTheme {
                DailySummary(
                    totalMl = 1250,
                    goalMl = 2000,
                    progressFraction = 0.625f,
                    goalReached = false,
                    remainingMl = 750,
                    overGoalMl = 0,
                )
            }
        }

        composeTestRule.onNodeWithText("1250 / 2000 ml").assertIsDisplayed()
        composeTestRule.onNodeWithText("750 ml remaining").assertIsDisplayed()
    }

    @Test
    fun dailySummary_displaysGoalReached() {
        composeTestRule.setContent {
            FluidisTheme {
                DailySummary(
                    totalMl = 2500,
                    goalMl = 2000,
                    progressFraction = 1.25f,
                    goalReached = true,
                    remainingMl = 0,
                    overGoalMl = 500,
                )
            }
        }

        composeTestRule.onNodeWithText("2500 / 2000 ml").assertIsDisplayed()
        composeTestRule.onNodeWithText("Goal reached! +500 ml").assertIsDisplayed()
    }

    @Test
    fun drinkCard_displaysCorrectInfo() {
        composeTestRule.setContent {
            FluidisTheme {
                DrinkCard(
                    drinkType = DrinkType.WATER,
                    totalMl = 1000,
                    servingMl = 500,
                    onAdd = {},
                    onUndo = {},
                    onLongPressAdd = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Water").assertIsDisplayed()
        composeTestRule.onNodeWithText("1000 ml").assertIsDisplayed()
        composeTestRule.onNodeWithText("500ml/serving").assertIsDisplayed()
        composeTestRule.onNodeWithText("2").assertIsDisplayed() // 1000/500
    }

    @Test
    fun drinkCard_addButtonCallsOnAdd() {
        var addCalled = false
        composeTestRule.setContent {
            FluidisTheme {
                DrinkCard(
                    drinkType = DrinkType.TEA,
                    totalMl = 350,
                    servingMl = 350,
                    onAdd = { addCalled = true },
                    onUndo = {},
                    onLongPressAdd = {},
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Add Tea").performClick()
        assert(addCalled)
    }

    @Test
    fun drinkCard_undoButtonCallsOnUndo() {
        var undoCalled = false
        composeTestRule.setContent {
            FluidisTheme {
                DrinkCard(
                    drinkType = DrinkType.COFFEE,
                    totalMl = 350,
                    servingMl = 350,
                    onAdd = {},
                    onUndo = { undoCalled = true },
                    onLongPressAdd = {},
                )
            }
        }

        composeTestRule.onNodeWithContentDescription("Undo last Coffee").performClick()
        assert(undoCalled)
    }

    @Test
    fun customAmountDialog_displaysAndAcceptsInput() {
        var confirmedAmount: Int? = null
        composeTestRule.setContent {
            FluidisTheme {
                CustomAmountDialog(
                    drinkType = DrinkType.WATER,
                    onConfirm = { confirmedAmount = it },
                    onDismiss = {},
                )
            }
        }

        composeTestRule.onNodeWithText("Add Water").assertIsDisplayed()
        composeTestRule.onNodeWithText("Enter amount in milliliters:").assertIsDisplayed()
        composeTestRule.onNodeWithText("Cancel").assertIsDisplayed()
        composeTestRule.onNodeWithText("Add").assertIsDisplayed()
    }
}
