package com.clearspend.presentation.screens.home

import androidx.compose.ui.graphics.Color
import com.clearspend.domain.model.Category
import com.clearspend.domain.model.CoachInsight
import com.clearspend.domain.model.Transaction

data class HomeUiState(
    val isLoading: Boolean = false,
    val totalSpent: Double = 0.0,
    val totalBudget: Double? = null,
    val pendingReviewCount: Int = 0,
    val recentTransactions: List<Transaction> = emptyList(),
    val weeklyData: List<DaySpend> = emptyList(),
    val topCategories: List<CategorySpend> = emptyList(),
    val insights: List<SpendInsight> = emptyList(),
    val coachInsight: CoachInsight? = null
)

data class DaySpend(
    val dayLabel: String,
    val amount: Double,
    val isToday: Boolean
)

data class CategorySpend(
    val category: Category,
    val amount: Double,
    val percentage: Float
)

data class SpendInsight(
    val emoji: String,
    val text: String,
    val color: Color
)
