package com.clearspend.domain.model

import java.util.UUID

/**
 * Domain models for Credit Cards and Credit Card Agreements.
 * Incorporates the 18 structured fields extracted by the Credit Agreement Intelligence engine.
 */
data class CreditCard(
    val id: String = UUID.randomUUID().toString(),
    val issuer: String,                     // e.g. "HDFC Bank", "ICICI Bank", "Citibank"
    val productName: String,                // e.g. "Regalia Gold", "Amazon Pay ICICI"
    val last4Digits: String,                // e.g. "4590"
    val cardNetwork: CardNetwork = CardNetwork.VISA,
    val creditLimit: Double? = null,
    val billingCycleDay: Int? = null,
    val agreement: CreditCardAgreement? = null
)

enum class CardNetwork { VISA, MASTERCARD, RUPAY, AMEX, OTHER }

/**
 * 18 Structured fields from Credit Card Agreements (CFPB / Bank Agreements).
 */
data class CreditCardAgreement(
    val id: String = UUID.randomUUID().toString(),
    val cardId: String,
    // 1-3 Identity
    val issuerName: String?,
    val productName: String?,
    val agreementDate: String?,
    // 4-8 Pricing & APRs
    val purchaseAprMin: Double?,
    val purchaseAprMax: Double?,
    val aprType: String?,                   // "variable" or "fixed"
    val cashAdvanceApr: Double?,
    val penaltyApr: Double?,
    // 9-11 Fees
    val annualFee: Double?,
    val annualFeeWaiverSpend: Double?,      // Spend required to waive annual fee
    val latePaymentFee: Double?,
    // 12-14 Transactions & Grace Period
    val foreignTxFeePercent: Double?,       // Forex markup %
    val gracePeriodDays: Int?,              // e.g. 21-25 days
    val balanceTransferFeePercent: Double?,
    val cashAdvanceFeePercent: Double?,
    // 15-18 Quality & Governance
    val icsConfidenceScore: Float = 0.85f,   // Iterative Consensus Score (0.0 to 1.0)
    val routingDecision: AgreementRouting = AgreementRouting.AUTO_APPROVE,
    val lastAuditedAt: Long = System.currentTimeMillis()
)

enum class AgreementRouting {
    AUTO_APPROVE,    // High confidence (>0.85)
    FLAGGED_FIELDS,  // Medium confidence (0.65 - 0.85)
    HUMAN_REVIEW     // Low confidence (<0.65)
}
