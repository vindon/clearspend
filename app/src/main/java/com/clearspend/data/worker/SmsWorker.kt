package com.clearspend.data.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.clearspend.data.service.SmsBankParser
import com.clearspend.domain.model.AuditEventType
import com.clearspend.domain.model.Transaction
import com.clearspend.domain.model.TransactionSource
import com.clearspend.domain.repository.AuditRepository
import com.clearspend.domain.repository.TransactionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.time.Instant
import java.time.ZoneId

@HiltWorker
class SmsWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val smsParser: SmsBankParser,
    private val transactionRepo: TransactionRepository,
    private val auditRepo: AuditRepository
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val sender = inputData.getString("sender") ?: return Result.failure()
        val body = inputData.getString("body") ?: return Result.failure()
        val timestamp = inputData.getLong("timestamp", System.currentTimeMillis())

        val isBank = smsParser.isBankSms(sender) || sender.contains("paisa") || sender.contains("paytm")
        if (!isBank) return Result.success()

        val parsed = smsParser.parseDebit(body, timestamp) ?: return Result.success()

        // Dedup check: same merchant + amount within 5 minutes
        val fiveMinMs = 5 * 60 * 1000L
        val dups = transactionRepo.countDuplicates(
            amount = parsed.amount,
            merchant = parsed.merchant,
            minTime = timestamp - fiveMinMs,
            maxTime = timestamp + fiveMinMs
        )
        if (dups > 0) return Result.success()

        val localDate = Instant.ofEpochMilli(timestamp)
            .atZone(ZoneId.systemDefault())
            .toLocalDate()

        val transaction = Transaction(
            merchant = parsed.merchant,
            amount = parsed.amount,
            category = parsed.category,
            date = localDate,
            source = TransactionSource.SMS_IMPORT,
            confidenceScore = parsed.confidenceScore,
            reviewStatus = parsed.reviewStatus,
            createdAt = timestamp
        )

        transactionRepo.save(transaction)
        auditRepo.logEvent(
            eventType = AuditEventType.SMS_PARSED,
            details = "Parsed ₹${parsed.amount} at ${parsed.merchant}",
            confidenceScore = parsed.confidenceScore
        )

        return Result.success()
    }
}
