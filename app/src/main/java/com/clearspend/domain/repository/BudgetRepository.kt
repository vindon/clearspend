package com.clearspend.domain.repository

import com.clearspend.domain.model.Budget
import com.clearspend.domain.model.Category
import kotlinx.coroutines.flow.Flow

interface BudgetRepository {
    fun observeByMonth(month: Int, year: Int): Flow<List<Budget>>
    suspend fun getTotalBudget(month: Int, year: Int): Budget?
    suspend fun getCategoryBudget(month: Int, year: Int, category: Category): Budget?
    suspend fun setTotalBudget(amount: Double, month: Int, year: Int)
    suspend fun setCategoryBudget(category: Category, amount: Double, month: Int, year: Int)
    suspend fun save(budget: Budget)
    suspend fun delete(id: String)
}
