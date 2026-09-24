package com.clearspend.data.repository

import com.clearspend.data.db.BudgetDao
import com.clearspend.data.db.BudgetEntity
import com.clearspend.domain.model.Budget
import com.clearspend.domain.model.Category
import com.clearspend.domain.repository.BudgetRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BudgetRepositoryImpl @Inject constructor(
    private val dao: BudgetDao
) : BudgetRepository {

    override fun observeByMonth(month: Int, year: Int): Flow<List<Budget>> =
        dao.observeByMonth(month, year).map { entities -> entities.map { it.toDomain() } }

    override suspend fun getTotalBudget(month: Int, year: Int): Budget? =
        dao.getTotalBudget(month, year)?.toDomain()

    override suspend fun getCategoryBudget(month: Int, year: Int, category: Category): Budget? =
        dao.getCategoryBudget(month, year, category.name)?.toDomain()

    override suspend fun setTotalBudget(amount: Double, month: Int, year: Int) {
        val existing = dao.getTotalBudget(month, year)
        val budget = BudgetEntity(
            id = existing?.id ?: UUID.randomUUID().toString(),
            category = null,
            limitAmount = amount,
            month = month,
            year = year,
            rolloverBonus = existing?.rolloverBonus ?: 0.0
        )
        dao.upsert(budget)
    }

    override suspend fun setCategoryBudget(category: Category, amount: Double, month: Int, year: Int) {
        val existing = dao.getCategoryBudget(month, year, category.name)
        val budget = BudgetEntity(
            id = existing?.id ?: UUID.randomUUID().toString(),
            category = category.name,
            limitAmount = amount,
            month = month,
            year = year,
            rolloverBonus = existing?.rolloverBonus ?: 0.0
        )
        dao.upsert(budget)
    }

    override suspend fun save(budget: Budget) {
        dao.upsert(budget.toEntity())
    }

    override suspend fun delete(id: String) {
        dao.deleteById(id)
    }

    private fun BudgetEntity.toDomain() = Budget(
        id = id,
        category = category?.let { runCatching { Category.valueOf(it) }.getOrNull() },
        limitAmount = limitAmount,
        month = month,
        year = year,
        rolloverBonus = rolloverBonus
    )

    private fun Budget.toEntity() = BudgetEntity(
        id = id,
        category = category?.name,
        limitAmount = limitAmount,
        month = month,
        year = year,
        rolloverBonus = rolloverBonus
    )
}
