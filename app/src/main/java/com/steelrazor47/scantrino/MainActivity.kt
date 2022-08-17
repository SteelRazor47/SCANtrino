package com.steelrazor47.scantrino

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.padding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
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
        val context = LocalContext.current
        val launcher = rememberLauncherForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted)
                navController.navigateUpTo(Screen.Camera.route)
            else
                Toast.makeText(context, "Camera permission denied", Toast.LENGTH_SHORT).show()
        }

        Scaffold(
            bottomBar = {
                BottomNavigation {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination
                    BottomNavigationItem(
                        icon = { Icon(Icons.Filled.List, "") },
                        selected = currentDestination?.hierarchy?.any { it.route == Screen.Overview.route } == true,
                        onClick = {
                            navController.navigateUpTo(Screen.Overview.route)
                        }
                    )
                    BottomNavigationItem(
                        icon = { Icon(Icons.Filled.PhotoCamera, "") },
                        selected = currentDestination?.hierarchy?.any { it.route == Screen.Camera.route } == true,
                        onClick = {
                            when (PackageManager.PERMISSION_GRANTED) {
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) -> navController.navigateUpTo(Screen.Camera.route)
                                else -> {
                                    launcher.launch(Manifest.permission.CAMERA)
                                }
                            }
                        }
                    )
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

fun NavHostController.navigateUpTo(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }
}