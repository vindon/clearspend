package com.clearspend.presentation.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.clearspend.data.seed.DemoDataSeeder
import com.clearspend.domain.model.AuditEventType
import com.clearspend.domain.repository.AuditRepository
import com.clearspend.domain.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsUiState(
    val isSeeding: Boolean = false,
    val seedSuccess: Boolean = false,
    val wipeSuccess: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val seeder: DemoDataSeeder,
    private val transactionRepo: TransactionRepository,
    private val auditRepo: AuditRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun seedDemoData() {
        _uiState.value = _uiState.value.copy(isSeeding = true)
        viewModelScope.launch {
            seeder.seedDemoData()
            auditRepo.logEvent(AuditEventType.DATA_WIPED, "Demo dataset populated")
            _uiState.value = _uiState.value.copy(isSeeding = false, seedSuccess = true)
        }
    }

    fun wipeAllData() {
        viewModelScope.launch {
            transactionRepo.wipeAllData()
            auditRepo.clearLogs()
            _uiState.value = _uiState.value.copy(wipeSuccess = true)
        }
    }
}
