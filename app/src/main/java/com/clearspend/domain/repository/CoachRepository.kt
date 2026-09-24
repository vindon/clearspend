package com.clearspend.domain.repository

import com.clearspend.domain.model.CoachInsight
import kotlinx.coroutines.flow.Flow

interface CoachRepository {
    fun observeRecent(): Flow<List<CoachInsight>>
    fun observeLatestInsight(): Flow<CoachInsight?>
    suspend fun saveInsight(insight: CoachInsight)
}
