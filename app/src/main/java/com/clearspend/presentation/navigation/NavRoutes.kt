package com.clearspend.presentation.navigation

sealed class NavRoutes(val route: String) {
    object Home : NavRoutes("home")
    object Scan : NavRoutes("scan")
    object Budget : NavRoutes("budget")
    object Cards : NavRoutes("cards")
    object Transactions : NavRoutes("transactions")
    object Coach : NavRoutes("coach")
    object Settings : NavRoutes("settings")
    object Onboarding : NavRoutes("onboarding")
}
