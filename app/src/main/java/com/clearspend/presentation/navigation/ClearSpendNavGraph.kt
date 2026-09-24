package com.clearspend.presentation.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.clearspend.presentation.common.ClearSpendBottomNav
import com.clearspend.presentation.screens.budget.BudgetScreen
import com.clearspend.presentation.screens.cards.CardIntelligenceScreen
import com.clearspend.presentation.screens.coach.CoachScreen
import com.clearspend.presentation.screens.home.HomeScreen
import com.clearspend.presentation.screens.onboarding.OnboardingScreen
import com.clearspend.presentation.screens.scan.ScanScreen
import com.clearspend.presentation.screens.settings.SettingsScreen
import com.clearspend.presentation.screens.transactions.TransactionListScreen

@Composable
fun ClearSpendNavGraph(
    navController: NavHostController = rememberNavController(),
    startDestination: String = NavRoutes.Home.route
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    // Show bottom bar on primary tabs, hide on Scan and Onboarding
    val showBottomBar = currentRoute in listOf(
        NavRoutes.Home.route,
        NavRoutes.Transactions.route,
        NavRoutes.Budget.route,
        NavRoutes.Cards.route,
        NavRoutes.Settings.route
    )

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                ClearSpendBottomNav(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(NavRoutes.Home.route) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = startDestination,
            modifier = Modifier.padding(padding)
        ) {
            composable(NavRoutes.Home.route) {
                HomeScreen(
                    onNavigateToTransactions = { navController.navigate(NavRoutes.Transactions.route) },
                    onNavigateToScan = { navController.navigate(NavRoutes.Scan.route) },
                    onNavigateToBudget = { navController.navigate(NavRoutes.Budget.route) },
                    onNavigateToCoach = { navController.navigate(NavRoutes.Coach.route) },
                    onNavigateToCards = { navController.navigate(NavRoutes.Cards.route) }
                )
            }

            composable(NavRoutes.Scan.route) {
                ScanScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onTransactionSaved = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.Budget.route) {
                BudgetScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.Cards.route) {
                CardIntelligenceScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.Transactions.route) {
                TransactionListScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.Coach.route) {
                CoachScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.Settings.route) {
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable(NavRoutes.Onboarding.route) {
                OnboardingScreen(
                    onComplete = { _ ->
                        navController.navigate(NavRoutes.Home.route) {
                            popUpTo(NavRoutes.Onboarding.route) { inclusive = true }
                        }
                    }
                )
            }
        }
    }
}
