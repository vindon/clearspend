package com.clearspend.domain.repository

import com.clearspend.domain.model.AuditEventType
import com.clearspend.domain.model.AuditLog
import kotlinx.coroutines.flow.Flow

interface AuditRepository {
    fun observeRecentLogs(limit: Int = 50): Flow<List<AuditLog>>
    suspend fun logEvent(eventType: AuditEventType, details: String, confidenceScore: Float? = null)
    suspend fun clearLogs()
}
