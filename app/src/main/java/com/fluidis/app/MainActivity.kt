package com.fluidis.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.remember
import androidx.navigation.compose.rememberNavController
import com.fluidis.app.core.navigation.FluidisNavHost
import com.fluidis.app.core.theme.FluidisTheme
import com.fluidis.app.core.ui.FluidisScaffold
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FluidisTheme {
                val navController = rememberNavController()
                val snackbarHostState = remember { SnackbarHostState() }
                FluidisScaffold(
                    navController = navController,
                    snackbarHostState = snackbarHostState,
                ) { modifier ->
                    FluidisNavHost(
                        navController = navController,
                        snackbarHostState = snackbarHostState,
                        modifier = modifier,
                    )
                }
            }
        }
    }
}
