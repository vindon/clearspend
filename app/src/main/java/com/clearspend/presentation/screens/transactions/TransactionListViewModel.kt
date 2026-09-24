package com.clearspend.presentation.screens.transactions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clearspend.domain.model.Category
import com.clearspend.domain.model.ReviewStatus
import com.clearspend.domain.model.Transaction
import com.clearspend.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class TransactionListUiState(
    val transactions: List<Transaction> = emptyList(),
    val selectedCategory: Category? = null,
    val searchQuery: String = "",
    val onlyPendingReview: Boolean = false
)

@HiltViewModel
class TransactionListViewModel @Inject constructor(
    private val transactionRepo: TransactionRepository
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<Category?>(null)
    private val _searchQuery = MutableStateFlow("")
    private val _onlyPendingReview = MutableStateFlow(false)

    val uiState: StateFlow<TransactionListUiState> = combine(
        transactionRepo.observeAll(),
        _selectedCategory,
        _searchQuery,
        _onlyPendingReview
    ) { allTx, category, search, onlyPending ->
        val filtered = allTx.filter { tx ->
            val matchesCat = category == null || tx.category == category
            val matchesSearch = search.isBlank() || tx.merchant.contains(search, ignoreCase = true)
            val matchesReview = !onlyPending || tx.reviewStatus == ReviewStatus.PENDING_REVIEW
            matchesCat && matchesSearch && matchesReview
        }

        TransactionListUiState(
            transactions = filtered,
            selectedCategory = category,
            searchQuery = search,
            onlyPendingReview = onlyPending
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TransactionListUiState()
    )

    fun setCategory(category: Category?) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun togglePendingReviewOnly() {
        _onlyPendingReview.value = !_onlyPendingReview.value
    }

    fun confirmTransaction(id: String) {
        viewModelScope.launch {
            transactionRepo.updateReviewStatus(id, ReviewStatus.APPROVED)
        }
    }

    fun deleteTransaction(id: String) {
        viewModelScope.launch {
            transactionRepo.delete(id)
        }
    }
}
