package com.clearspend.presentation.screens.scan

import com.clearspend.domain.model.Category
import com.clearspend.domain.model.ScanResult

data class ScanUiState(
    val stage: ScanStage = ScanStage.CAMERA,
    val parsedResult: ScanResult? = null,
    val editState: ScanEditState = ScanEditState(),
    val errorMessage: String? = null,
    val savedSuccessfully: Boolean = false
)

enum class ScanStage {
    CAMERA,
    PROCESSING,
    CONFIRM,
    ERROR
}

data class ScanEditState(
    val merchant: String = "",
    val amount: String = "",
    val category: Category = Category.OTHER
)
