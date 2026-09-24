package com.clearspend.presentation.screens.cards

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clearspend.data.service.CardAgreementParser
import com.clearspend.domain.model.CreditCard
import com.clearspend.domain.repository.CreditCardRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CardViewModel @Inject constructor(
    private val cardRepo: CreditCardRepository,
    private val cardParser: CardAgreementParser
) : ViewModel() {

    private val _selectedCardId = MutableStateFlow<String?>(null)

    val uiState: StateFlow<CardUiState> = combine(
        cardRepo.observeAllCards(),
        _selectedCardId
    ) { cards, selectedId ->
        val selected = cards.find { it.id == selectedId } ?: cards.firstOrNull()
        CardUiState(
            cards = cards,
            selectedCard = selected
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CardUiState()
    )

    init {
        // Automatically seed pre-indexed cards if database is empty on first visit
        viewModelScope.launch {
            val existing = cardRepo.observeAllCards().first()
            if (existing.isEmpty()) {
                val preIndexed = cardParser.getPreIndexedCardAgreements()
                preIndexed.forEach { (card, agreement) ->
                    cardRepo.saveCard(card)
                    cardRepo.saveAgreement(agreement)
                }
            }
        }
    }

    fun selectCard(card: CreditCard) {
        _selectedCardId.value = card.id
    }

    fun auditAgreement(agreementText: String) {
        val currentCard = uiState.value.selectedCard ?: return
        val analyzed = cardParser.analyzeAgreementText(currentCard.id, agreementText)
        viewModelScope.launch {
            cardRepo.saveAgreement(analyzed)
        }
    }
}
