package com.clearspend.domain.model

sealed class ScanResult {
    data class Success(
        val merchant: String,
        val amount: Double,
        val category: Category,
        val rawText: String,
        val confidenceScore: Float = 1.0f
    ) : ScanResult()

    data class Partial(
        val rawText: String,
        val possibleAmount: Double?,
        val possibleMerchant: String? = null,
        val suggestedCategory: Category = Category.OTHER,
        val confidenceScore: Float = 0.5f
    ) : ScanResult()

    data class Failure(val reason: String) : ScanResult()
}
