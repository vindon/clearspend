package com.clearspend.data.db

import androidx.room.*
import com.clearspend.domain.model.ReviewStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY date DESC, createdAt DESC")
    fun observeAll(): Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions 
        WHERE strftime('%m', date) = printf('%02d', :month)
        AND strftime('%Y', date) = :year
        ORDER BY date DESC
    """)
    fun observeByMonth(month: Int, year: String): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE reviewStatus = 'PENDING_REVIEW' ORDER BY date DESC")
    fun observePendingReview(): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE date BETWEEN :from AND :to ORDER BY date DESC")
    suspend fun getForRange(from: LocalDate, to: LocalDate): List<TransactionEntity>

    @Query("""
        SELECT SUM(amount) FROM transactions
        WHERE strftime('%m', date) = printf('%02d', :month)
        AND strftime('%Y', date) = :year
        AND (:category IS NULL OR category = :category)
    """)
    suspend fun sumByMonthAndCategory(month: Int, year: String, category: String?): Double?

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: String): TransactionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(transactions: List<TransactionEntity>)

    @Query("UPDATE transactions SET reviewStatus = :status WHERE id = :id")
    suspend fun updateReviewStatus(id: String, status: ReviewStatus)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("""
        SELECT COUNT(*) FROM transactions
        WHERE ABS(amount - :amount) < 0.01
        AND merchant = :merchant
        AND createdAt BETWEEN :minTime AND :maxTime
    """)
    suspend fun countDuplicates(amount: Double, merchant: String, minTime: Long, maxTime: Long): Int

    @Query("DELETE FROM transactions")
    suspend fun deleteAll()
}

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year")
    fun observeByMonth(month: Int, year: Int): Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year AND category IS NULL")
    suspend fun getTotalBudget(month: Int, year: Int): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year AND category = :category")
    suspend fun getCategoryBudget(month: Int, year: Int, category: String): BudgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(budget: BudgetEntity)

    @Delete
    suspend fun delete(budget: BudgetEntity)

    @Query("DELETE FROM budgets WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM budgets")
    suspend fun deleteAll()
}

@Dao
interface CoachInsightDao {
    @Query("SELECT * FROM coach_insights ORDER BY weekStartDate DESC LIMIT 10")
    fun observeRecent(): Flow<List<CoachInsightEntity>>

    @Query("SELECT * FROM coach_insights ORDER BY weekStartDate DESC LIMIT 1")
    fun observeLatest(): Flow<CoachInsightEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(insight: CoachInsightEntity)

    @Query("DELETE FROM coach_insights")
    suspend fun deleteAll()
}

@Dao
interface CreditCardDao {
    @Query("SELECT * FROM credit_cards ORDER BY issuer ASC, productName ASC")
    fun observeAll(): Flow<List<CreditCardEntity>>

    @Query("SELECT * FROM credit_cards WHERE id = :id")
    suspend fun getById(id: String): CreditCardEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(card: CreditCardEntity)

    @Query("DELETE FROM credit_cards WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("DELETE FROM credit_cards")
    suspend fun deleteAll()
}

@Dao
interface CardAgreementDao {
    @Query("SELECT * FROM card_agreements WHERE cardId = :cardId")
    suspend fun getByCardId(cardId: String): CardAgreementEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(agreement: CardAgreementEntity)

    @Query("DELETE FROM card_agreements WHERE cardId = :cardId")
    suspend fun deleteByCardId(cardId: String)

    @Query("DELETE FROM card_agreements")
    suspend fun deleteAll()
}

@Dao
interface AuditLogDao {
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<AuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(log: AuditLogEntity)

    @Query("DELETE FROM audit_logs")
    suspend fun deleteAll()
}
