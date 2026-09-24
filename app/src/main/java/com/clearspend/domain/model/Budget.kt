package com.clearspend.domain.model

import java.util.UUID

data class Budget(
    val id: String = UUID.randomUUID().toString(),
    val category: Category?,          // null = overall monthly total budget
    val limitAmount: Double,
    val month: Int,                   // 1-12
    val year: Int,
    val rolloverBonus: Double = 0.0   // Unused budget carried forward from previous month
)

/**
 * Real-time computed progress for monthly envelope budgeting.
 */
data class BudgetProgress(
    val category: Category?,
    val limit: Double,
    val spent: Double,
    val rollover: Double = 0.0
) {
    val totalAvailable: Double get() = limit + rollover
    val remaining: Double get() = totalAvailable - spent
    val percentUsed: Float get() = ((spent / totalAvailable.coerceAtLeast(1.0)) * 100f).toFloat()
    val status: BudgetStatus get() = when {
        percentUsed >= 100f -> BudgetStatus.OVER
        percentUsed >= 80f  -> BudgetStatus.WARNING
        else                -> BudgetStatus.OK
    }
}

enum class BudgetStatus { OK, WARNING, OVER }
