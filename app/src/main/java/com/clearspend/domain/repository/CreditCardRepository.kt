package com.clearspend.domain.repository

import com.clearspend.domain.model.CreditCard
import com.clearspend.domain.model.CreditCardAgreement
import kotlinx.coroutines.flow.Flow

interface CreditCardRepository {
    fun observeAllCards(): Flow<List<CreditCard>>
    suspend fun getCardById(id: String): CreditCard?
    suspend fun saveCard(card: CreditCard)
    suspend fun saveAgreement(agreement: CreditCardAgreement)
    suspend fun getAgreementForCard(cardId: String): CreditCardAgreement?
    suspend fun deleteCard(id: String)
}
