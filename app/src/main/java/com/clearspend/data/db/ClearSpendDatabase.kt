package com.clearspend.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        TransactionEntity::class,
        BudgetEntity::class,
        CoachInsightEntity::class,
        CreditCardEntity::class,
        CardAgreementEntity::class,
        AuditLogEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class ClearSpendDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
    abstract fun budgetDao(): BudgetDao
    abstract fun coachInsightDao(): CoachInsightDao
    abstract fun creditCardDao(): CreditCardDao
    abstract fun cardAgreementDao(): CardAgreementDao
    abstract fun auditLogDao(): AuditLogDao
}
