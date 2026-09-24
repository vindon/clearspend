package com.clearspend.domain.model

import java.util.UUID

/**
 * Local audit trail tracking ingestion events, security actions, and parser metrics.
 */
data class AuditLog(
    val id: String = UUID.randomUUID().toString(),
    val eventType: AuditEventType,
    val details: String,
    val confidenceScore: Float? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class AuditEventType {
    SMS_PARSED,
    RECEIPT_SCANNED,
    NOTIFICATION_PROCESSED,
    TRANSACTION_REVIEWED,
    CARD_AGREEMENT_AUDITED,
    DATA_WIPED
}
