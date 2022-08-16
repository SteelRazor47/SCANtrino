package com.steelrazor47.scantrino

sealed class Screen(val route: String) {
    object Overview : Screen("overview")
    object Camera : Screen("camera")
}