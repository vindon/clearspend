package com.clearspend.domain.model

import org.junit.Assert.assertEquals
import org.junit.Test

class BudgetCalculationTest {

    @Test
    fun testBudgetProgressStatusOk() {
        val progress = BudgetProgress(
            category = Category.FOOD,
            limit = 10_000.0,
            spent = 6_500.0,
            rollover = 0.0
        )
        assertEquals(BudgetStatus.OK, progress.status)
        assertEquals(3_500.0, progress.remaining, 0.01)
        assertEquals(65.0f, progress.percentUsed, 0.1f)
    }

    @Test
    fun testBudgetProgressStatusWarning() {
        val progress = BudgetProgress(
            category = Category.SHOPPING,
            limit = 10_000.0,
            spent = 8_500.0,
            rollover = 0.0
        )
        assertEquals(BudgetStatus.WARNING, progress.status)
        assertEquals(1_500.0, progress.remaining, 0.01)
        assertEquals(85.0f, progress.percentUsed, 0.1f)
    }

    @Test
    fun testBudgetProgressStatusOver() {
        val progress = BudgetProgress(
            category = Category.BILLS,
            limit = 5_000.0,
            spent = 5_200.0,
            rollover = 0.0
        )
        assertEquals(BudgetStatus.OVER, progress.status)
        assertEquals(-200.0, progress.remaining, 0.01)
        assertEquals(104.0f, progress.percentUsed, 0.1f)
    }

    @Test
    fun testBudgetWithRolloverBonus() {
        val progress = BudgetProgress(
            category = Category.FOOD,
            limit = 10_000.0,
            spent = 11_000.0,
            rollover = 2_000.0 // Rollover extends total limit to 12,000
        )
        assertEquals(12_000.0, progress.totalAvailable, 0.01)
        assertEquals(1_000.0, progress.remaining, 0.01)
        assertEquals(BudgetStatus.WARNING, progress.status) // 11000/12000 = 91.6%
    }
}
