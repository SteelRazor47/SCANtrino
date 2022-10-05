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
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.steelrazor47.scantrino.ui.camera.CameraScreen
import com.steelrazor47.scantrino.ui.camera.ReceiptReviewViewModel
import com.steelrazor47.scantrino.ui.camera.ReceiptReviewScreen
import com.steelrazor47.scantrino.ui.overview.OverviewScreen
import com.steelrazor47.scantrino.ui.theme.ScantrinoTheme
import com.steelrazor47.scantrino.utils.Routes
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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
                navController.navigateUpTo(Routes.Camera.route)
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
                        selected = currentDestination?.hierarchy?.any { it.route == Routes.Overview.route } == true,
                        onClick = {
                            navController.navigateUpTo(Routes.Overview.route)
                        }
                    )
                    BottomNavigationItem(
                        icon = { Icon(Icons.Filled.PhotoCamera, "") },
                        selected = currentDestination?.hierarchy?.any { it.route == Routes.Camera.route } == true,
                        onClick = {
                            when (PackageManager.PERMISSION_GRANTED) {
                                ContextCompat.checkSelfPermission(
                                    context,
                                    Manifest.permission.CAMERA
                                ) -> navController.navigateUpTo(Routes.Camera.route)
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
                startDestination = Routes.Overview.route,
                modifier = Modifier.padding(padding)
            ) {
                composable(route = Routes.Overview.route) { OverviewScreen() }
                composable(route = Routes.Camera.route) {
                    CameraScreen(
                        onReviewReceipt = {
                            navController.navigate(Routes.ReviewReceipt.route) {
                                launchSingleTop = true
                            }
                        },
                    )
                }
                composable(route = Routes.ReviewReceipt.route) { backStackEntry ->
                    val parentEntry = remember(backStackEntry) {
                        navController.getBackStackEntry(Routes.Camera.route)
                    }
                    val reviewViewModel = hiltViewModel<ReceiptReviewViewModel>(parentEntry)
                    ReceiptReviewScreen(reviewViewModel, onReceiptSaved = {
                        navController.navigateUpTo(Routes.Overview.route)
                    })
                }

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
    }
}
