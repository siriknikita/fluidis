package com.fluidis.app.core.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.WaterDrop
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.fluidis.app.core.navigation.HistoryRoute
import com.fluidis.app.core.navigation.HomeRoute
import com.fluidis.app.core.navigation.SettingsRoute
import com.fluidis.app.core.navigation.StatisticsRoute
import dev.chrisbanes.haze.HazeState
import dev.chrisbanes.haze.HazeTint
import dev.chrisbanes.haze.hazeEffect
import dev.chrisbanes.haze.hazeSource

private data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: Any,
)

private val bottomNavItems = listOf(
    BottomNavItem("Home", Icons.Outlined.WaterDrop, HomeRoute),
    BottomNavItem("Stats", Icons.Outlined.BarChart, StatisticsRoute),
    BottomNavItem("History", Icons.Outlined.CalendarMonth, HistoryRoute),
)

private val NavBarShape = RoundedCornerShape(28.dp)
private val NavItemPillShape = RoundedCornerShape(20.dp)

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

    val hazeState = remember { HazeState() }

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
                                Icon(Icons.Outlined.Settings, contentDescription = "Settings")
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
                        if (showBottomBar) {
                            Modifier
                                .hazeSource(state = hazeState)
                                .padding(bottom = 96.dp)
                        } else {
                            Modifier.padding(bottom = innerPadding.calculateBottomPadding())
                        }
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
                hazeState = hazeState,
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
    hazeState: HazeState,
    modifier: Modifier = Modifier,
) {
    val surfaceColor = MaterialTheme.colorScheme.surface
    val containerColor = MaterialTheme.colorScheme.surfaceContainerHigh

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(NavBarShape)
            .hazeEffect(state = hazeState) {
                backgroundColor = surfaceColor
                blurRadius = 24.dp
                tints = listOf(HazeTint(surfaceColor.copy(alpha = 0.4f)))
                noiseFactor = 0.1f
            }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        items.forEach { item ->
            val selected = currentDestination?.hasRoute(item.route::class) == true
            NavBarItem(
                icon = item.icon,
                label = item.label,
                selected = selected,
                onClick = { onItemClick(item.route) },
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun NavBarItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bgColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.surfaceContainerHighest.copy(alpha = 0.7f)
        } else {
            Color.Transparent
        },
        label = "navItemBg",
    )
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onSurface
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
    }

    Column(
        modifier = modifier
            .clip(NavItemPillShape)
            .background(bgColor)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = contentColor,
            modifier = Modifier.size(24.dp),
        )
        Text(
            text = label,
            color = contentColor,
            fontSize = 11.sp,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}
