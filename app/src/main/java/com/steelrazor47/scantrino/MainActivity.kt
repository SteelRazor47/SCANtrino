package com.steelrazor47.scantrino

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.steelrazor47.scantrino.ui.camera.CameraScreen
import com.steelrazor47.scantrino.ui.overview.OverviewScreen
import com.steelrazor47.scantrino.ui.theme.ScantrinoTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ScantrinoApp()
        }
    }
}

@Composable
fun ScantrinoApp() {
    ScantrinoTheme {
        val navController = rememberNavController()
        val items = listOf(Screen.Overview, Screen.Camera)
        Scaffold(
            bottomBar = {
                BottomNavigation {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    items.forEach { screen ->
                        BottomNavigationItem(
                            icon = { Icon(Icons.Filled.PhotoCamera, "") },
                            //label = { Text(stringResource(screen.resourceId)) },
                            selected = currentDestination?.hierarchy?.any { it.route == screen.route } == true,
                            onClick = {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            }
                        )
                    }
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = Screen.Overview.route,
                modifier = Modifier.padding(padding)
            ) {
                composable(route = Screen.Overview.route) { OverviewScreen() }
                composable(route = Screen.Camera.route) { CameraScreen() }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AppPreview() {
    ScantrinoApp()
}