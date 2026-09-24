package com.clearspend.presentation.screens.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clearspend.data.service.GeminiReceiptParser
import com.clearspend.domain.model.*
import com.clearspend.domain.repository.AuditRepository
import com.clearspend.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val geminiParser: GeminiReceiptParser,
    private val transactionRepo: TransactionRepository,
    private val auditRepo: AuditRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    fun processOcrText(rawText: String) {
        _uiState.update { it.copy(stage = ScanStage.PROCESSING) }

        viewModelScope.launch {
            val result = geminiParser.parseReceiptText(rawText)
            when (result) {
                is ScanResult.Success -> {
                    _uiState.update {
                        it.copy(
                            stage = ScanStage.CONFIRM,
                            parsedResult = result,
                            editState = ScanEditState(
                                merchant = result.merchant,
                                amount = result.amount.toString(),
                                category = result.category
                            )
                        )
                    }
                }
                is ScanResult.Partial -> {
                    _uiState.update {
                        it.copy(
                            stage = ScanStage.CONFIRM,
                            parsedResult = result,
                            editState = ScanEditState(
                                merchant = result.possibleMerchant ?: "",
                                amount = result.possibleAmount?.toString() ?: "",
                                category = result.suggestedCategory
                            )
                        )
                    }
                }
                is ScanResult.Failure -> {
                    _uiState.update {
                        it.copy(
                            stage = ScanStage.ERROR,
                            errorMessage = result.reason
                        )
                    }
                }
            }
        }
    }

    fun updateMerchant(merchant: String) {
        _uiState.update { it.copy(editState = it.editState.copy(merchant = merchant)) }
    }

    fun updateAmount(amount: String) {
        _uiState.update { it.copy(editState = it.editState.copy(amount = amount)) }
    }

    fun updateCategory(category: Category) {
        _uiState.update { it.copy(editState = it.editState.copy(category = category)) }
    }

    fun saveTransaction() {
        val editState = _uiState.value.editState
        val amount = editState.amount.toDoubleOrNull() ?: return
        val merchant = editState.merchant.ifBlank { "Receipt Purchase" }

        viewModelScope.launch {
            val transaction = Transaction(
                merchant = merchant,
                amount = amount,
                category = editState.category,
                date = LocalDate.now(),
                source = TransactionSource.RECEIPT_SCAN,
                confidenceScore = 1.0f,
                reviewStatus = ReviewStatus.APPROVED
            )

            transactionRepo.save(transaction)
            auditRepo.logEvent(
                eventType = AuditEventType.RECEIPT_SCANNED,
                details = "Scanned ₹$amount at $merchant",
                confidenceScore = 1.0f
            )

            _uiState.update { it.copy(savedSuccessfully = true) }
        }
    }

    fun reset() {
        _uiState.value = ScanUiState()
    }
}
