package com.fluidis.app.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.BarChart
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.WaterDrop
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination
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

private val NavBarShape = RoundedCornerShape(28.dp)

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

    Box(modifier = Modifier.fillMaxSize()) {
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
            snackbarHost = { SnackbarHost(snackbarHostState) },
        ) { innerPadding ->
            content(
                Modifier
                    .padding(top = innerPadding.calculateTopPadding())
                    .then(
                        if (showBottomBar) Modifier.padding(bottom = 96.dp)
                        else Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                    )
            )
        }

        if (showBottomBar) {
            FloatingNavBar(
                items = bottomNavItems,
                currentDestination = currentDestination,
                onItemClick = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(start = 24.dp, end = 24.dp, bottom = 16.dp)
                    .navigationBarsPadding(),
            )
        }
    }
}

@Composable
private fun FloatingNavBar(
    items: List<BottomNavItem>,
    currentDestination: NavDestination?,
    onItemClick: (Any) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .shadow(elevation = 8.dp, shape = NavBarShape)
            .clip(NavBarShape)
            .background(MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.85f))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        items.forEach { item ->
            val selected = currentDestination?.hasRoute(item.route::class) == true
            NavigationBarItem(
                selected = selected,
                onClick = { onItemClick(item.route) },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) },
            )
        }
    }
}
