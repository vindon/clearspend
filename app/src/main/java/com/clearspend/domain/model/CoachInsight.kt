package com.clearspend.domain.model

import java.time.LocalDate
import java.util.UUID

data class CoachInsight(
    val id: String = UUID.randomUUID().toString(),
    val weekStartDate: LocalDate,
    val summary: String,
    val topCategory: Category,
    val changeVsLastWeek: Double,
    val generatedAt: Long = System.currentTimeMillis()
)
