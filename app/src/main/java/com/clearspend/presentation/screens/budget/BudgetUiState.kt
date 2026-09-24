package com.clearspend.presentation.screens.budget

import com.clearspend.domain.model.BudgetProgress

data class BudgetUiState(
    val totalSpent: Double = 0.0,
    val totalBudget: Double? = null,
    val categoryProgress: List<BudgetProgress> = emptyList(),
    val isEditingTotal: Boolean = false,
    val isEditingCategory: Boolean = false
)
