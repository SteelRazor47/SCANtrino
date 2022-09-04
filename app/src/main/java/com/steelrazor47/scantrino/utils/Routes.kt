package com.steelrazor47.scantrino.utils

sealed class Routes(val route: String) {
    object Overview : Routes("overview")
    object Camera : Routes("camera")
    object ReviewReceipt : Routes("reviewReceipt")
}