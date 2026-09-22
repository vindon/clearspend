// ─── data/db/Entities.kt ─────────────────────────────────────────────────────
package com.clearspend.data.db

import androidx.room.*
import com.clearspend.domain.model.Category
import com.clearspend.domain.model.TransactionSource
import java.time.LocalDate

// ── Type Converters ──────────────────────────────────────────────────────────

class Converters {
    @TypeConverter fun fromLocalDate(date: LocalDate?): String? = date?.toString()
    @TypeConverter fun toLocalDate(value: String?): LocalDate? =
        value?.let { LocalDate.parse(it) }

    @TypeConverter fun fromCategory(cat: Category): String = cat.name
    @TypeConverter fun toCategory(value: String): Category =
        Category.valueOf(value)

    @TypeConverter fun fromSource(source: TransactionSource): String = source.name
    @TypeConverter fun toSource(value: String): TransactionSource =
        TransactionSource.valueOf(value)
}

// ── Transaction Entity ───────────────────────────────────────────────────────

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey val id: String,
    val merchant: String,
    val amount: Double,
    val originalAmount: Double?,
    val originalCurrency: String?,
    val category: Category,
    val date: LocalDate,
    val note: String?,
    val source: TransactionSource,
    val isRecurring: Boolean,
    val receiptImagePath: String?,
    val createdAt: Long = System.currentTimeMillis()
)

// ── Budget Entity ─────────────────────────────────────────────────────────────

@Entity(
    tableName = "budgets",
    indices = [Index(value = ["category", "month", "year"], unique = true)]
)
data class BudgetEntity(
    @PrimaryKey val id: String,
    val category: String?,    // null stored as null = total budget
    val limitAmount: Double,
    val month: Int,
    val year: Int,
    val rolloverBonus: Double = 0.0
)

// ── Coach Insight Entity ──────────────────────────────────────────────────────

@Entity(tableName = "coach_insights")
data class CoachInsightEntity(
    @PrimaryKey val id: String,
    val weekStartDate: LocalDate,
    val summary: String,
    val topCategory: Category,
    val changeVsLastWeek: Double,
    val generatedAt: Long
)

// ─── data/db/TransactionDao.kt ───────────────────────────────────────────────

@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions ORDER BY date DESC, createdAt DESC")
    fun observeAll(): kotlinx.coroutines.flow.Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions 
        WHERE strftime('%m', date) = printf('%02d', :month)
        AND strftime('%Y', date) = :year
        ORDER BY date DESC
    """)
    fun observeByMonth(month: Int, year: String): kotlinx.coroutines.flow.Flow<List<TransactionEntity>>

    @Query("""
        SELECT * FROM transactions 
        WHERE date BETWEEN :from AND :to
        ORDER BY date DESC
    """)
    fun observeByRange(from: LocalDate, to: LocalDate): kotlinx.coroutines.flow.Flow<List<TransactionEntity>>

    @Query("""
        SELECT SUM(amount) FROM transactions
        WHERE strftime('%m', date) = printf('%02d', :month)
        AND strftime('%Y', date) = :year
        AND (:category IS NULL OR category = :category)
    """)
    suspend fun sumByMonthAndCategory(month: Int, year: String, category: String?): Double?

    @Query("""
        SELECT category, SUM(amount) as total 
        FROM transactions
        WHERE strftime('%m', date) = printf('%02d', :month)
        AND strftime('%Y', date) = :year
        GROUP BY category
        ORDER BY total DESC
    """)
    suspend fun categoryTotalsForMonth(month: Int, year: String): List<CategoryTotal>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getById(id: String): TransactionEntity?

    @Query("""
        SELECT * FROM transactions
        WHERE date BETWEEN :weekStart AND :weekEnd
        ORDER BY date DESC
    """)
    suspend fun getForWeek(weekStart: LocalDate, weekEnd: LocalDate): List<TransactionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(transaction: TransactionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(transactions: List<TransactionEntity>)

    @Delete
    suspend fun delete(transaction: TransactionEntity)

    @Query("DELETE FROM transactions WHERE id = :id")
    suspend fun deleteById(id: String)

    // Dedup check: same amount + same merchant within 5 minutes (SMS import)
    @Query("""
        SELECT COUNT(*) FROM transactions
        WHERE ABS(amount - :amount) < 0.01
        AND merchant = :merchant
        AND createdAt BETWEEN :minTime AND :maxTime
    """)
    suspend fun countDuplicates(amount: Double, merchant: String, minTime: Long, maxTime: Long): Int
}

// Projection for category aggregation
data class CategoryTotal(
    val category: String,
    val total: Double
)

// ─── data/db/BudgetDao.kt ────────────────────────────────────────────────────

@Dao
interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year")
    fun observeByMonth(month: Int, year: Int): kotlinx.coroutines.flow.Flow<List<BudgetEntity>>

    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year AND category IS NULL")
    suspend fun getTotalBudget(month: Int, year: Int): BudgetEntity?

    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year AND category = :category")
    suspend fun getCategoryBudget(month: Int, year: Int, category: String): BudgetEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(budget: BudgetEntity)

    @Delete
    suspend fun delete(budget: BudgetEntity)
}

// ─── data/db/CoachInsightDao.kt ──────────────────────────────────────────────

@Dao
interface CoachInsightDao {
    @Query("SELECT * FROM coach_insights ORDER BY weekStartDate DESC LIMIT 4")
    fun observeRecent(): kotlinx.coroutines.flow.Flow<List<CoachInsightEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(insight: CoachInsightEntity)
}

// ─── data/db/ClearSpendDatabase.kt ──────────────────────────────────────────

@Database(
    entities = [TransactionEntity::class, BudgetEntity::class, CoachInsightEntity::class],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class ClearSpendDatabase : androidx.room.RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun coachInsightDao(): CoachInsightDao
}
