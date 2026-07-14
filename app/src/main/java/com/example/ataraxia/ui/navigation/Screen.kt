package com.example.ataraxia.ui.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Welcome : Screen("welcome")
    object Home : Screen("home")
    object Journal : Screen("journal")
    object Breathe : Screen("breathe")
    object Focus : Screen("focus")
    object Me : Screen("me")
    object PinLock : Screen("pinLock")
}
