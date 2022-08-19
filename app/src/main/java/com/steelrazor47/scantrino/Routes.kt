package com.steelrazor47.scantrino

sealed class Routes(val route: String) {
    object Overview : Routes("overview")
    object Camera : Routes("camera")
    object ReviewReceipt : Routes("reviewReceipt")
}