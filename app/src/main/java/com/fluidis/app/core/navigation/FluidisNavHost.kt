package com.fluidis.app.core.navigation

import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.fluidis.app.feature.home.HomeScreen

@Composable
fun FluidisNavHost(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = HomeRoute,
        modifier = modifier,
    ) {
        composable<HomeRoute> {
            HomeScreen(snackbarHostState = snackbarHostState)
        }
        composable<StatisticsRoute> {
            Text("Statistics Screen")
        }
        composable<HistoryRoute> {
            Text("History Screen")
        }
        composable<SettingsRoute> {
            Text("Settings Screen")
        }
    }
}
