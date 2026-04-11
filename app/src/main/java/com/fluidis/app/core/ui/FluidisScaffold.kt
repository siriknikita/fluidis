package com.fluidis.app.core.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.fluidis.app.core.navigation.HistoryRoute
import com.fluidis.app.core.navigation.HomeRoute
import com.fluidis.app.core.navigation.SettingsRoute
import com.fluidis.app.core.navigation.StatisticsRoute

private data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: Any,
)

private val bottomNavItems = listOf(
    BottomNavItem("Home", Icons.Rounded.WaterDrop, HomeRoute),
    BottomNavItem("Stats", Icons.Rounded.BarChart, StatisticsRoute),
    BottomNavItem("History", Icons.Rounded.CalendarMonth, HistoryRoute),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FluidisScaffold(
    navController: NavHostController,
    snackbarHostState: SnackbarHostState = remember { SnackbarHostState() },
    content: @Composable (Modifier) -> Unit,
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val showBottomBar = currentDestination != null && bottomNavItems.any { item ->
        currentDestination.hasRoute(item.route::class)
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (showBottomBar) {
                CenterAlignedTopAppBar(
                    title = { Text("Fluidis") },
                    navigationIcon = {
                        IconButton(onClick = {
                            navController.navigate(SettingsRoute) {
                                launchSingleTop = true
                            }
                        }) {
                            Icon(Icons.Rounded.Settings, contentDescription = "Settings")
                        }
                    },
                )
            }
        },
        bottomBar = {
            if (showBottomBar) {
                NavigationBar {
                    bottomNavItems.forEach { item ->
                        val selected = currentDestination?.hasRoute(item.route::class) == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(item.icon, contentDescription = item.label) },
                            label = { Text(item.label) },
                        )
                    }
                }
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        content(Modifier.padding(innerPadding))
    }
}
