package com.clearspend.data.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.clearspend.domain.model.*
import java.time.LocalDate

@Entity(
    tableName = "transactions",
    indices = [
        Index("date"),
        Index("reviewStatus"),
        Index("cardId")
    ]
)
data class TransactionEntity(
    @PrimaryKey val id: String,
    val merchant: String,
    val amount: Double,
    val originalAmount: Double?,
    val originalCurrency: String?,
    val category: Category,
    val date: LocalDate,
    val note: String?,
    val source: TransactionSource,
    val isRecurring: Boolean,
    val receiptImagePath: String?,
    val cardId: String?,
    val confidenceScore: Float,
    val reviewStatus: ReviewStatus,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "budgets",
    indices = [Index(value = ["category", "month", "year"], unique = true)]
)
data class BudgetEntity(
    @PrimaryKey val id: String,
    val category: String?,          // null = monthly total budget
    val limitAmount: Double,
    val month: Int,
    val year: Int,
    val rolloverBonus: Double = 0.0
)

@Entity(tableName = "coach_insights")
data class CoachInsightEntity(
    @PrimaryKey val id: String,
    val weekStartDate: LocalDate,
    val summary: String,
    val topCategory: Category,
    val changeVsLastWeek: Double,
    val generatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "credit_cards")
data class CreditCardEntity(
    @PrimaryKey val id: String,
    val issuer: String,
    val productName: String,
    val last4Digits: String,
    val cardNetwork: CardNetwork,
    val creditLimit: Double?,
    val billingCycleDay: Int?
)

@Entity(
    tableName = "card_agreements",
    foreignKeys = [
        ForeignKey(
            entity = CreditCardEntity::class,
            parentColumns = ["id"],
            childColumns = ["cardId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["cardId"], unique = true)]
)
data class CardAgreementEntity(
    @PrimaryKey val id: String,
    val cardId: String,
    val issuerName: String?,
    val productName: String?,
    val agreementDate: String?,
    val purchaseAprMin: Double?,
    val purchaseAprMax: Double?,
    val aprType: String?,
    val cashAdvanceApr: Double?,
    val penaltyApr: Double?,
    val annualFee: Double?,
    val annualFeeWaiverSpend: Double?,
    val latePaymentFee: Double?,
    val foreignTxFeePercent: Double?,
    val gracePeriodDays: Int?,
    val balanceTransferFeePercent: Double?,
    val cashAdvanceFeePercent: Double?,
    val icsConfidenceScore: Float,
    val routingDecision: AgreementRouting,
    val lastAuditedAt: Long
)

@Entity(tableName = "audit_logs")
data class AuditLogEntity(
    @PrimaryKey val id: String,
    val eventType: AuditEventType,
    val details: String,
    val confidenceScore: Float?,
    val timestamp: Long = System.currentTimeMillis()
)
