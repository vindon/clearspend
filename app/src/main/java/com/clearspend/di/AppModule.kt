package com.clearspend.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.room.Room
import com.clearspend.data.db.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ClearSpendDatabase {
        return Room.databaseBuilder(
            context,
            ClearSpendDatabase::class.java,
            "clearspend.db"
        )
            .fallbackToDestructiveMigration()
            .build()
    }

    @Provides fun provideTransactionDao(db: ClearSpendDatabase): TransactionDao = db.transactionDao()
    @Provides fun provideBudgetDao(db: ClearSpendDatabase): BudgetDao = db.budgetDao()
    @Provides fun provideCoachInsightDao(db: ClearSpendDatabase): CoachInsightDao = db.coachInsightDao()
    @Provides fun provideCreditCardDao(db: ClearSpendDatabase): CreditCardDao = db.creditCardDao()
    @Provides fun provideCardAgreementDao(db: ClearSpendDatabase): CardAgreementDao = db.cardAgreementDao()
    @Provides fun provideAuditLogDao(db: ClearSpendDatabase): AuditLogDao = db.auditLogDao()

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): DataStore<Preferences> {
        return PreferenceDataStoreFactory.create {
            context.preferencesDataStoreFile("clearspend_prefs")
        }
    }
}
