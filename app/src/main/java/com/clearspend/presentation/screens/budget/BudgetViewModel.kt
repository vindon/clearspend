package com.clearspend.presentation.screens.budget

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clearspend.domain.model.BudgetProgress
import com.clearspend.domain.model.Category
import com.clearspend.domain.repository.BudgetRepository
import com.clearspend.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class BudgetViewModel @Inject constructor(
    private val budgetRepo: BudgetRepository,
    private val transactionRepo: TransactionRepository
) : ViewModel() {

    private val today = LocalDate.now()
    private val month = today.monthValue
    private val year = today.year

    val uiState: StateFlow<BudgetUiState> = combine(
        budgetRepo.observeByMonth(month, year),
        transactionRepo.observeByMonth(month, year.toString())
    ) { budgets, transactions ->
        val totalSpent = transactions.sumOf { it.amount }
        val totalBudget = budgets.find { it.category == null }?.limitAmount

        // Category budgets progress
        val categoryProgress = Category.entries.map { cat ->
            val limit = budgets.find { it.category == cat }?.limitAmount ?: 0.0
            val spent = transactions.filter { it.category == cat }.sumOf { it.amount }
            val rollover = budgets.find { it.category == cat }?.rolloverBonus ?: 0.0
            BudgetProgress(
                category = cat,
                limit = limit,
                spent = spent,
                rollover = rollover
            )
        }

        BudgetUiState(
            totalSpent = totalSpent,
            totalBudget = totalBudget,
            categoryProgress = categoryProgress
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = BudgetUiState()
    )

    fun setTotalBudget(amount: Double) {
        viewModelScope.launch {
            budgetRepo.setTotalBudget(amount, month, year)
        }
    }

    fun setCategoryBudget(category: Category, amount: Double) {
        viewModelScope.launch {
            budgetRepo.setCategoryBudget(category, amount, month, year)
        }
    }
}
