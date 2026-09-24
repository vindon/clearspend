package com.clearspend.data.seed

import com.clearspend.data.service.CardAgreementParser
import com.clearspend.domain.model.*
import com.clearspend.domain.repository.BudgetRepository
import com.clearspend.domain.repository.CoachRepository
import com.clearspend.domain.repository.CreditCardRepository
import com.clearspend.domain.repository.TransactionRepository
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DemoDataSeeder @Inject constructor(
    private val transactionRepo: TransactionRepository,
    private val budgetRepo: BudgetRepository,
    private val coachRepo: CoachRepository,
    private val cardRepo: CreditCardRepository,
    private val cardParser: CardAgreementParser
) {

    suspend fun seedDemoData() {
        val today = LocalDate.now()
        val currentMonth = today.monthValue
        val currentYear = today.year

        // 1. Seed Monthly Total & Category Budgets
        budgetRepo.setTotalBudget(65_000.0, currentMonth, currentYear)
        budgetRepo.setCategoryBudget(Category.FOOD, 18_000.0, currentMonth, currentYear)
        budgetRepo.setCategoryBudget(Category.GROCERIES, 12_000.0, currentMonth, currentYear)
        budgetRepo.setCategoryBudget(Category.SHOPPING, 10_000.0, currentMonth, currentYear)
        budgetRepo.setCategoryBudget(Category.TRANSPORT, 6_000.0, currentMonth, currentYear)
        budgetRepo.setCategoryBudget(Category.BILLS, 8_000.0, currentMonth, currentYear)

        // 2. Seed Pre-indexed Credit Cards & Agreement Intelligence
        val pairs = cardParser.getPreIndexedCardAgreements()
        pairs.forEach { (card, agreement) ->
            cardRepo.saveCard(card)
            cardRepo.saveAgreement(agreement)
        }

        // 3. Seed Realistic Transactions across 7 days
        val sampleTransactions = listOf(
            Transaction(
                merchant = "Swiggy",
                amount = 485.0,
                category = Category.FOOD,
                date = today,
                source = TransactionSource.SMS_IMPORT,
                confidenceScore = 0.96f,
                reviewStatus = ReviewStatus.APPROVED
            ),
            Transaction(
                merchant = "Uber India",
                amount = 260.0,
                category = Category.TRANSPORT,
                date = today,
                source = TransactionSource.SMS_IMPORT,
                confidenceScore = 0.94f,
                reviewStatus = ReviewStatus.APPROVED
            ),
            Transaction(
                merchant = "Blinkit",
                amount = 640.0,
                category = Category.GROCERIES,
                date = today.minusDays(1),
                source = TransactionSource.SMS_IMPORT,
                confidenceScore = 0.95f,
                reviewStatus = ReviewStatus.APPROVED
            ),
            Transaction(
                merchant = "Starbucks Coffee",
                amount = 390.0,
                category = Category.FOOD,
                date = today.minusDays(1),
                source = TransactionSource.RECEIPT_SCAN,
                confidenceScore = 0.92f,
                reviewStatus = ReviewStatus.APPROVED
            ),
            Transaction(
                merchant = "Amazon India",
                amount = 2499.0,
                category = Category.SHOPPING,
                date = today.minusDays(2),
                source = TransactionSource.SMS_IMPORT,
                confidenceScore = 0.98f,
                reviewStatus = ReviewStatus.APPROVED
            ),
            Transaction(
                merchant = "Apollo Pharmacy",
                amount = 750.0,
                category = Category.HEALTH,
                date = today.minusDays(3),
                source = TransactionSource.RECEIPT_SCAN,
                confidenceScore = 0.89f,
                reviewStatus = ReviewStatus.APPROVED
            ),
            Transaction(
                merchant = "Bescom Electricity",
                amount = 1850.0,
                category = Category.BILLS,
                date = today.minusDays(4),
                source = TransactionSource.SMS_IMPORT,
                confidenceScore = 0.97f,
                reviewStatus = ReviewStatus.APPROVED
            ),
            Transaction(
                merchant = "D-Mart Supermarket",
                amount = 4320.0,
                category = Category.GROCERIES,
                date = today.minusDays(5),
                source = TransactionSource.RECEIPT_SCAN,
                confidenceScore = 0.91f,
                reviewStatus = ReviewStatus.APPROVED
            ),
            Transaction(
                merchant = "Local Vendor",
                amount = 350.0,
                category = Category.OTHER,
                date = today.minusDays(1),
                source = TransactionSource.SMS_IMPORT,
                confidenceScore = 0.72f,
                reviewStatus = ReviewStatus.PENDING_REVIEW  // HITL flagged for test
            )
        )

        transactionRepo.saveAll(sampleTransactions)

        // 4. Seed Coach Insight
        coachRepo.saveInsight(
            CoachInsight(
                weekStartDate = today.minusDays(6),
                summary = "Your dining out spend is trending 15% lower than last week. Keeping grocery prep consistent this weekend will keep you well below your target envelope.",
                topCategory = Category.GROCERIES,
                changeVsLastWeek = -15.2
            )
        )
    }
}
