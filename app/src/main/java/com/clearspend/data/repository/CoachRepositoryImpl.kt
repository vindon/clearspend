package com.clearspend.data.repository

import com.clearspend.data.db.CoachInsightDao
import com.clearspend.data.db.CoachInsightEntity
import com.clearspend.domain.model.CoachInsight
import com.clearspend.domain.repository.CoachRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoachRepositoryImpl @Inject constructor(
    private val dao: CoachInsightDao
) : CoachRepository {

    override fun observeRecent(): Flow<List<CoachInsight>> =
        dao.observeRecent().map { list -> list.map { it.toDomain() } }

    override fun observeLatestInsight(): Flow<CoachInsight?> =
        dao.observeLatest().map { it?.toDomain() }

    override suspend fun saveInsight(insight: CoachInsight) {
        dao.insert(insight.toEntity())
    }

    private fun CoachInsightEntity.toDomain() = CoachInsight(
        id = id,
        weekStartDate = weekStartDate,
        summary = summary,
        topCategory = topCategory,
        changeVsLastWeek = changeVsLastWeek,
        generatedAt = generatedAt
    )

    private fun CoachInsight.toEntity() = CoachInsightEntity(
        id = id,
        weekStartDate = weekStartDate,
        summary = summary,
        topCategory = topCategory,
        changeVsLastWeek = changeVsLastWeek,
        generatedAt = generatedAt
    )
}
