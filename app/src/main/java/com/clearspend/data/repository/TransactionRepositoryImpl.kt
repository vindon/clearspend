package com.clearspend.data.repository

import com.clearspend.data.db.TransactionDao
import com.clearspend.data.db.TransactionEntity
import com.clearspend.domain.model.Category
import com.clearspend.domain.model.ReviewStatus
import com.clearspend.domain.model.Transaction
import com.clearspend.domain.repository.TransactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val dao: TransactionDao
) : TransactionRepository {

    override fun observeAll(): Flow<List<Transaction>> =
        dao.observeAll().map { entities -> entities.map { it.toDomain() } }

    override fun observeByMonth(month: Int, year: String): Flow<List<Transaction>> =
        dao.observeByMonth(month, year).map { entities -> entities.map { it.toDomain() } }

    override fun observePendingReview(): Flow<List<Transaction>> =
        dao.observePendingReview().map { entities -> entities.map { it.toDomain() } }

    override suspend fun getById(id: String): Transaction? =
        dao.getById(id)?.toDomain()

    override suspend fun getForDateRange(from: LocalDate, to: LocalDate): List<Transaction> =
        dao.getForRange(from, to).map { it.toDomain() }

    override suspend fun sumByMonthAndCategory(month: Int, year: String, category: Category?): Double? =
        dao.sumByMonthAndCategory(month, year, category?.name)

    override suspend fun save(transaction: Transaction) {
        dao.upsert(transaction.toEntity())
    }

    override suspend fun saveAll(transactions: List<Transaction>) {
        dao.upsertAll(transactions.map { it.toEntity() })
    }

    override suspend fun updateReviewStatus(id: String, status: ReviewStatus) {
        dao.updateReviewStatus(id, status)
    }

    override suspend fun delete(id: String) {
        dao.deleteById(id)
    }

    override suspend fun countDuplicates(amount: Double, merchant: String, minTime: Long, maxTime: Long): Int =
        dao.countDuplicates(amount, merchant, minTime, maxTime)

    override suspend fun wipeAllData() {
        dao.deleteAll()
    }

    private fun TransactionEntity.toDomain() = Transaction(
        id = id,
        merchant = merchant,
        amount = amount,
        originalAmount = originalAmount,
        originalCurrency = originalCurrency,
        category = category,
        date = date,
        note = note,
        source = source,
        isRecurring = isRecurring,
        receiptImagePath = receiptImagePath,
        cardId = cardId,
        confidenceScore = confidenceScore,
        reviewStatus = reviewStatus,
        createdAt = createdAt
    )

    private fun Transaction.toEntity() = TransactionEntity(
        id = id,
        merchant = merchant,
        amount = amount,
        originalAmount = originalAmount,
        originalCurrency = originalCurrency,
        category = category,
        date = date,
        note = note,
        source = source,
        isRecurring = isRecurring,
        receiptImagePath = receiptImagePath,
        cardId = cardId,
        confidenceScore = confidenceScore,
        reviewStatus = reviewStatus,
        createdAt = createdAt
    )
}
