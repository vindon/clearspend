package com.clearspend.presentation.screens.cards

import com.clearspend.domain.model.CreditCard

data class CardUiState(
    val cards: List<CreditCard> = emptyList(),
    val selectedCard: CreditCard? = null,
    val isAuditing: Boolean = false,
    val auditSummary: String? = null
)
