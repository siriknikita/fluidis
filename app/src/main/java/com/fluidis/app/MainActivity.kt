package com.fluidis.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
                var isDetailPanelOpen by remember { mutableStateOf(false) }
                var dismissDetailPanel by remember { mutableStateOf<(() -> Unit)?>(null) }
                var addDetailEntry by remember { mutableStateOf<(() -> Unit)?>(null) }

                FluidisScaffold(
                    navController = navController,
                    snackbarHostState = snackbarHostState,
                    isDetailPanelOpen = isDetailPanelOpen,
                    onDismissDetailPanel = { dismissDetailPanel?.invoke() },
                    onAddDetailEntry = { addDetailEntry?.invoke() },
                ) { modifier ->
                    FluidisNavHost(
                        navController = navController,
                        snackbarHostState = snackbarHostState,
                        modifier = modifier,
                        onHistoryDetailStateChanged = { isOpen, dismiss, add ->
                            isDetailPanelOpen = isOpen
                            dismissDetailPanel = dismiss
                            addDetailEntry = add
                        },
                    )
                }
            }
        }
    }
}
