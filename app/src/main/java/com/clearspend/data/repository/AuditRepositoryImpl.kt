package com.clearspend.data.repository

import com.clearspend.data.db.AuditLogDao
import com.clearspend.data.db.AuditLogEntity
import com.clearspend.domain.model.AuditEventType
import com.clearspend.domain.model.AuditLog
import com.clearspend.domain.repository.AuditRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuditRepositoryImpl @Inject constructor(
    private val dao: AuditLogDao
) : AuditRepository {

    override fun observeRecentLogs(limit: Int): Flow<List<AuditLog>> =
        dao.observeRecent(limit).map { list -> list.map { it.toDomain() } }

    override suspend fun logEvent(eventType: AuditEventType, details: String, confidenceScore: Float?) {
        dao.insert(
            AuditLogEntity(
                id = UUID.randomUUID().toString(),
                eventType = eventType,
                details = details,
                confidenceScore = confidenceScore,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    override suspend fun clearLogs() {
        dao.deleteAll()
    }

    private fun AuditLogEntity.toDomain() = AuditLog(
        id = id,
        eventType = eventType,
        details = details,
        confidenceScore = confidenceScore,
        timestamp = timestamp
    )
}
