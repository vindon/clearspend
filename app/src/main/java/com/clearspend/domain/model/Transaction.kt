package com.clearspend.domain.model

import java.time.LocalDate
import java.util.UUID

/**
 * Core domain model for a single financial transaction.
 * Supports on-device confidence scoring and human-in-the-loop (HITL) review states.
 */
data class Transaction(
    val id: String = UUID.randomUUID().toString(),
    val merchant: String,
    val amount: Double,
    val originalAmount: Double? = null,
    val originalCurrency: String? = null,
    val category: Category,
    val date: LocalDate,
    val note: String? = null,
    val source: TransactionSource,
    val isRecurring: Boolean = false,
    val receiptImagePath: String? = null,
    val cardId: String? = null,
    val confidenceScore: Float = 1.0f,
    val reviewStatus: ReviewStatus = ReviewStatus.APPROVED,
    val createdAt: Long = System.currentTimeMillis()
)

enum class TransactionSource {
    RECEIPT_SCAN,
    SMS_IMPORT,
    NOTIFICATION_IMPORT,
    MANUAL
}

enum class ReviewStatus {
    APPROVED,         // High confidence (>=85%) or confirmed by user
    PENDING_REVIEW,   // Medium confidence (65%-84%), flagged for quick user confirmation
    REJECTED          // Flagged as incorrect/duplicate by user
}
