// ─── domain/model/Transaction.kt ─────────────────────────────────────────────
package com.clearspend.domain.model

import java.time.LocalDate
import java.util.UUID

/**
 * Core domain model for a single financial transaction.
 * Source-agnostic: could come from receipt OCR, SMS parser, or manual entry.
 */
data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val merchant: String,
    val amount: Double,              // Always stored in user's home currency
    val originalAmount: Double? = null,   // If multi-currency, original amount
    val originalCurrency: String? = null, // e.g. "USD", "EUR"
    val category: Category,
    val date: LocalDate,
    val note: String? = null,
    val source: TransactionSource,
    val isRecurring: Boolean = false,
    val receiptImagePath: String? = null  // Local file path if scanned
)

enum class TransactionSource {
    RECEIPT_SCAN,   // CameraX + ML Kit + Gemini
    SMS_IMPORT,     // Bank SMS BroadcastReceiver
    MANUAL          // User typed it in
}

// ─── domain/model/Category.kt ────────────────────────────────────────────────
enum class Category(
    val displayName: String,
    val emoji: String,
    val colorHex: String
) {
    FOOD("Food & Dining", "🍽️", "#F97316"),
    TRANSPORT("Transport", "🚗", "#3B82F6"),
    SHOPPING("Shopping", "🛍️", "#EC4899"),
    HEALTH("Health", "💊", "#22C55E"),
    BILLS("Bills & Utilities", "⚡", "#EAB308"),
    ENTERTAINMENT("Entertainment", "🎬", "#A855F7"),
    GROCERIES("Groceries", "🛒", "#14B8A6"),
    TRAVEL("Travel", "✈️", "#06B6D4"),
    EMI("EMI & Loans", "🏦", "#EF4444"),
    OTHER("Other", "📦", "#6B7280");

    companion object {
        fun fromDisplayName(name: String): Category =
            entries.firstOrNull { it.displayName.equals(name, ignoreCase = true) } ?: OTHER

        /** Called after Gemini returns a category string */
        fun fromGeminiResponse(raw: String): Category =
            entries.firstOrNull {
                raw.contains(it.name, ignoreCase = true) ||
                raw.contains(it.displayName, ignoreCase = true)
            } ?: OTHER
    }
}

// ─── domain/model/Budget.kt ──────────────────────────────────────────────────
data class Budget(
    val id: String = UUID.randomUUID().toString(),
    val category: Category?,          // null = overall total budget
    val limitAmount: Double,
    val month: Int,                   // 1-12
    val year: Int,
    val rolloverBonus: Double = 0.0   // Pro: unused budget from last month
)

// ─── domain/model/BudgetProgress.kt ─────────────────────────────────────────
/** Computed at query time — never persisted */
data class BudgetProgress(
    val category: Category?,
    val limit: Double,
    val spent: Double,
    val rollover: Double = 0.0
) {
    val remaining: Double get() = (limit + rollover) - spent
    val percentUsed: Float get() = ((spent / (limit + rollover).coerceAtLeast(1.0)) * 100f).toFloat()
    val status: BudgetStatus get() = when {
        percentUsed >= 100f -> BudgetStatus.OVER
        percentUsed >= 80f  -> BudgetStatus.WARNING
        else                -> BudgetStatus.OK
    }
}

enum class BudgetStatus { OK, WARNING, OVER }

// ─── domain/model/ScanResult.kt ──────────────────────────────────────────────
/** Intermediate result from the OCR + Gemini pipeline */
sealed class ScanResult {
    data class Success(
        val merchant: String,
        val amount: Double,
        val category: Category,
        val rawText: String        // Keep for debugging/fallback edits
    ) : ScanResult()

    data class Partial(
        val rawText: String,       // OCR succeeded but Gemini parse was uncertain
        val possibleAmount: Double?
    ) : ScanResult()

    data class Failure(val reason: String) : ScanResult()
}

// ─── domain/model/CoachInsight.kt ────────────────────────────────────────────
data class CoachInsight(
    val id: String = UUID.randomUUID().toString(),
    val weekStartDate: LocalDate,
    val summary: String,            // 2–3 sentence AI insight
    val topCategory: Category,
    val changeVsLastWeek: Double,   // % change in total spend
    val generatedAt: Long = System.currentTimeMillis()
)
