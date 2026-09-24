package com.clearspend.presentation.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clearspend.domain.model.ReviewStatus
import com.clearspend.domain.model.Transaction
import com.clearspend.domain.repository.BudgetRepository
import com.clearspend.domain.repository.CoachRepository
import com.clearspend.domain.repository.TransactionRepository
import com.clearspend.presentation.theme.ClearSpendColors
import com.clearspend.presentation.theme.categoryColor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.*
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val transactionRepo: TransactionRepository,
    private val budgetRepo: BudgetRepository,
    private val coachRepo: CoachRepository
) : ViewModel() {

    private val today = LocalDate.now()

    val uiState: StateFlow<HomeUiState> = combine(
        transactionRepo.observeByMonth(today.monthValue, today.year.toString()),
        budgetRepo.observeByMonth(today.monthValue, today.year),
        coachRepo.observeLatestInsight(),
        transactionRepo.observePendingReview()
    ) { transactions, budgets, latestCoachInsight, pendingList ->

        val totalSpent = transactions.sumOf { it.amount }
        val totalBudget = budgets.find { it.category == null }?.limitAmount

        // 7-day spend chart data
        val weekStart = today.minusDays(6)
        val weeklyData = (0..6).map { offset ->
            val day = weekStart.plusDays(offset.toLong())
            val dayTotal = transactions.filter { it.date == day }.sumOf { it.amount }
            DaySpend(
                dayLabel = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(2).uppercase(),
                amount = dayTotal,
                isToday = day == today
            )
        }

        // Top categories
        val catTotals = transactions
            .groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
            .entries.sortedByDescending { it.value }
            .take(4)
            .map {
                CategorySpend(
                    category = it.key,
                    amount = it.value,
                    percentage = (it.value / totalSpent.coerceAtLeast(1.0) * 100).toFloat()
                )
            }

        // Dynamic quick insights
        val insights = buildList {
            catTotals.firstOrNull()?.let { topCat ->
                add(SpendInsight(topCat.category.emoji, "Top: ${topCat.category.displayName}", categoryColor(topCat.category)))
            }
            if (totalBudget != null && totalBudget > 0) {
                val pct = ((totalSpent / totalBudget) * 100).toInt()
                val color = if (pct >= 80) ClearSpendColors.YellowWarn else ClearSpendColors.GreenSuccess
                add(SpendInsight("📊", "$pct% of Budget Used", color))
            }
            if (pendingList.isNotEmpty()) {
                add(SpendInsight("⚡", "${pendingList.size} Needs Review", ClearSpendColors.Amber400))
            }
        }

        HomeUiState(
            isLoading = false,
            totalSpent = totalSpent,
            totalBudget = totalBudget,
            pendingReviewCount = pendingList.size,
            recentTransactions = transactions.take(15),
            weeklyData = weeklyData,
            topCategories = catTotals,
            insights = insights,
            coachInsight = latestCoachInsight
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState(isLoading = true)
    )

    fun confirmTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepo.updateReviewStatus(transaction.id, ReviewStatus.APPROVED)
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            transactionRepo.delete(transaction.id)
        }
    }
}
