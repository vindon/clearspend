package com.clearspend.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.clearspend.data.service.GeminiReceiptParser
import com.clearspend.domain.model.Category
import com.clearspend.domain.model.CoachInsight
import com.clearspend.domain.model.Transaction
import com.clearspend.domain.repository.CoachRepository
import com.clearspend.domain.repository.TransactionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.LocalDate

@HiltWorker
class WeeklyCoachWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val transactionRepo: TransactionRepository,
    private val coachRepo: CoachRepository,
    private val geminiParser: GeminiReceiptParser
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val today = LocalDate.now()
        val weekStart = today.minusDays(6)
        val lastWeekStart = today.minusDays(13)
        val lastWeekEnd = today.minusDays(7)

        val thisWeekTx = transactionRepo.getForDateRange(weekStart, today)
        val lastWeekTx = transactionRepo.getForDateRange(lastWeekStart, lastWeekEnd)

        if (thisWeekTx.isEmpty()) return Result.success()

        val thisWeekSummary = buildSummary(thisWeekTx)
        val lastWeekSummary = buildSummary(lastWeekTx)

        val insightText = geminiParser.generateWeeklyInsight(thisWeekSummary, lastWeekSummary)
            ?: "Great job tracking your expenses this week. Keep an eye on non-essential spending to stay aligned with your monthly goals."

        val topCategory = thisWeekTx
            .groupBy { it.category }
            .maxByOrNull { it.value.sumOf { tx -> tx.amount } }
            ?.key ?: Category.OTHER

        val thisTotal = thisWeekTx.sumOf { it.amount }
        val lastTotal = lastWeekTx.sumOf { it.amount }
        val change = if (lastTotal > 0) ((thisTotal - lastTotal) / lastTotal * 100) else 0.0

        coachRepo.saveInsight(
            CoachInsight(
                weekStartDate = weekStart,
                summary = insightText,
                topCategory = topCategory,
                changeVsLastWeek = change
            )
        )

        return Result.success()
    }

    private fun buildSummary(transactions: List<Transaction>): String {
        val total = transactions.sumOf { it.amount }
        val byCat = transactions.groupBy { it.category }
            .mapValues { it.value.sumOf { tx -> tx.amount } }
            .entries.sortedByDescending { it.value }
            .joinToString("\n") { "- ${it.key.displayName}: ₹${it.value.toInt()}" }
        return "Total Spend: ₹${total.toInt()}\n$byCat"
    }
}
