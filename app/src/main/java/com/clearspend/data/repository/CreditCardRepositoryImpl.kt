package com.clearspend.data.repository

import com.clearspend.data.db.CardAgreementDao
import com.clearspend.data.db.CardAgreementEntity
import com.clearspend.data.db.CreditCardDao
import com.clearspend.data.db.CreditCardEntity
import com.clearspend.domain.model.CreditCard
import com.clearspend.domain.model.CreditCardAgreement
import com.clearspend.domain.repository.CreditCardRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CreditCardRepositoryImpl @Inject constructor(
    private val cardDao: CreditCardDao,
    private val agreementDao: CardAgreementDao
) : CreditCardRepository {

    override fun observeAllCards(): Flow<List<CreditCard>> =
        cardDao.observeAll().map { cards ->
            cards.map { cardEntity ->
                val agreement = agreementDao.getByCardId(cardEntity.id)?.toDomain()
                cardEntity.toDomain(agreement)
            }
        }

    override suspend fun getCardById(id: String): CreditCard? {
        val cardEntity = cardDao.getById(id) ?: return null
        val agreement = agreementDao.getByCardId(id)?.toDomain()
        return cardEntity.toDomain(agreement)
    }

    override suspend fun saveCard(card: CreditCard) {
        cardDao.upsert(card.toEntity())
        card.agreement?.let { agreementDao.upsert(it.toEntity()) }
    }

    override suspend fun saveAgreement(agreement: CreditCardAgreement) {
        agreementDao.upsert(agreement.toEntity())
    }

    override suspend fun getAgreementForCard(cardId: String): CreditCardAgreement? =
        agreementDao.getByCardId(cardId)?.toDomain()

    override suspend fun deleteCard(id: String) {
        agreementDao.deleteByCardId(id)
        cardDao.deleteById(id)
    }

    private fun CreditCardEntity.toDomain(agreement: CreditCardAgreement?) = CreditCard(
        id = id,
        issuer = issuer,
        productName = productName,
        last4Digits = last4Digits,
        cardNetwork = cardNetwork,
        creditLimit = creditLimit,
        billingCycleDay = billingCycleDay,
        agreement = agreement
    )

    private fun CreditCard.toEntity() = CreditCardEntity(
        id = id,
        issuer = issuer,
        productName = productName,
        last4Digits = last4Digits,
        cardNetwork = cardNetwork,
        creditLimit = creditLimit,
        billingCycleDay = billingCycleDay
    )

    private fun CardAgreementEntity.toDomain() = CreditCardAgreement(
        id = id,
        cardId = cardId,
        issuerName = issuerName,
        productName = productName,
        agreementDate = agreementDate,
        purchaseAprMin = purchaseAprMin,
        purchaseAprMax = purchaseAprMax,
        aprType = aprType,
        cashAdvanceApr = cashAdvanceApr,
        penaltyApr = penaltyApr,
        annualFee = annualFee,
        annualFeeWaiverSpend = annualFeeWaiverSpend,
        latePaymentFee = latePaymentFee,
        foreignTxFeePercent = foreignTxFeePercent,
        gracePeriodDays = gracePeriodDays,
        balanceTransferFeePercent = balanceTransferFeePercent,
        cashAdvanceFeePercent = cashAdvanceFeePercent,
        icsConfidenceScore = icsConfidenceScore,
        routingDecision = routingDecision,
        lastAuditedAt = lastAuditedAt
    )

    private fun CreditCardAgreement.toEntity() = CardAgreementEntity(
        id = id,
        cardId = cardId,
        issuerName = issuerName,
        productName = productName,
        agreementDate = agreementDate,
        purchaseAprMin = purchaseAprMin,
        purchaseAprMax = purchaseAprMax,
        aprType = aprType,
        cashAdvanceApr = cashAdvanceApr,
        penaltyApr = penaltyApr,
        annualFee = annualFee,
        annualFeeWaiverSpend = annualFeeWaiverSpend,
        latePaymentFee = latePaymentFee,
        foreignTxFeePercent = foreignTxFeePercent,
        gracePeriodDays = gracePeriodDays,
        balanceTransferFeePercent = balanceTransferFeePercent,
        cashAdvanceFeePercent = cashAdvanceFeePercent,
        icsConfidenceScore = icsConfidenceScore,
        routingDecision = routingDecision,
        lastAuditedAt = lastAuditedAt
    )
}
