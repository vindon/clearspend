package com.clearspend.domain.repository

import com.clearspend.domain.model.Category
import com.clearspend.domain.model.ReviewStatus
import com.clearspend.domain.model.Transaction
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface TransactionRepository {
    fun observeAll(): Flow<List<Transaction>>
    fun observeByMonth(month: Int, year: String): Flow<List<Transaction>>
    fun observePendingReview(): Flow<List<Transaction>>
    suspend fun getById(id: String): Transaction?
    suspend fun getForDateRange(from: LocalDate, to: LocalDate): List<Transaction>
    suspend fun sumByMonthAndCategory(month: Int, year: String, category: Category?): Double?
    suspend fun save(transaction: Transaction)
    suspend fun saveAll(transactions: List<Transaction>)
    suspend fun updateReviewStatus(id: String, status: ReviewStatus)
    suspend fun delete(id: String)
    suspend fun countDuplicates(amount: Double, merchant: String, minTime: Long, maxTime: Long): Int
    suspend fun wipeAllData()
}
