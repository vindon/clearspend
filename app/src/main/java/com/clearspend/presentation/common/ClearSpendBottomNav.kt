package com.clearspend.presentation.common

import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.clearspend.presentation.navigation.NavRoutes
import com.clearspend.presentation.theme.ClearSpendColors

sealed class BottomNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    object Home : BottomNavItem(NavRoutes.Home.route, "Home", Icons.Rounded.Home)
    object Transactions : BottomNavItem(NavRoutes.Transactions.route, "Expenses", Icons.Rounded.ReceiptLong)
    object Budget : BottomNavItem(NavRoutes.Budget.route, "Budget", Icons.Rounded.PieChart)
    object Cards : BottomNavItem(NavRoutes.Cards.route, "Cards", Icons.Rounded.CreditCard)
    object Settings : BottomNavItem(NavRoutes.Settings.route, "Settings", Icons.Rounded.Settings)
}

@Composable
fun ClearSpendBottomNav(
    currentRoute: String?,
    onNavigate: (String) -> Unit
) {
    val items = listOf(
        BottomNavItem.Home,
        BottomNavItem.Transactions,
        BottomNavItem.Budget,
        BottomNavItem.Cards,
        BottomNavItem.Settings
    )

    NavigationBar(
        containerColor = ClearSpendColors.Surface1,
        contentColor = ClearSpendColors.TextPrimary,
        tonalElevation = 8.dp,
        modifier = Modifier.height(72.dp)
    ) {
        items.forEach { item ->
            val isSelected = currentRoute == item.route
            NavigationBarItem(
                selected = isSelected,
                onClick = {
                    if (currentRoute != item.route) {
                        onNavigate(item.route)
                    }
                },
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = item.label
                    )
                },
                label = {
                    Text(
                        text = item.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ClearSpendColors.Amber500,
                    selectedTextColor = ClearSpendColors.Amber500,
                    unselectedIconColor = ClearSpendColors.TextMuted,
                    unselectedTextColor = ClearSpendColors.TextMuted,
                    indicatorColor = ClearSpendColors.Amber500.copy(alpha = 0.15f)
                )
            )
        }
    }
}
