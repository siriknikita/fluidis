package com.fluidis.app.core.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.fluidis.app.feature.home.HomeScreen
import com.fluidis.app.feature.history.HistoryScreen
import com.fluidis.app.feature.settings.SettingsScreen
import com.fluidis.app.feature.statistics.StatisticsScreen

@Composable
fun FluidisNavHost(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
    onHistoryDetailStateChanged: (isOpen: Boolean, dismiss: (() -> Unit)?, addEntry: (() -> Unit)?) -> Unit = { _, _, _ -> },
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
    ) {
        composable<HomeRoute> {
            HomeScreen()
        }
        composable<StatisticsRoute> {
            StatisticsScreen()
        }
        composable<HistoryRoute> {
            HistoryScreen(
                onDetailStateChanged = onHistoryDetailStateChanged,
            )
        }
        composable<SettingsRoute> {
            SettingsScreen(onNavigateBack = { navController.popBackStack() })
        }
    }
}
