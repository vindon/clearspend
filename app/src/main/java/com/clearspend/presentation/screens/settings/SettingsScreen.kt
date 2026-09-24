package com.clearspend.presentation.screens.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.clearspend.presentation.common.ClearSpendTopBar
import com.clearspend.presentation.theme.ClearSpendColors

@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showWipeConfirm by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = ClearSpendColors.Surface0,
        topBar = {
            ClearSpendTopBar(title = "Settings & Governance", onBack = onNavigateBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // QA & Developer Tools
            Text("DEVELOPER & QA TOOLS", style = MaterialTheme.typography.labelSmall, color = ClearSpendColors.TextMuted, letterSpacing = 1.sp)

            Surface(
                color = ClearSpendColors.Surface1,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, ClearSpendColors.Border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("1-Click Demo Data", style = MaterialTheme.typography.titleMedium, color = ClearSpendColors.TextPrimary)
                            Text(
                                "Populates realistic expenses (Swiggy, Uber, Amazon), budgets, credit cards, and coach insights for instant testing.",
                                style = MaterialTheme.typography.bodySmall,
                                color = ClearSpendColors.TextMuted
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = { viewModel.seedDemoData() },
                            colors = ButtonDefaults.buttonColors(containerColor = ClearSpendColors.Amber500, contentColor = MaterialTheme.colorScheme.onPrimary),
                            enabled = !uiState.isSeeding
                        ) {
                            Text(if (uiState.isSeeding) "Seeding..." else "Seed Data")
                        }
                    }

                    if (uiState.seedSuccess) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("✓ Demo transactions & cards successfully added!", color = ClearSpendColors.GreenSuccess, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Privacy & Governance (DPDP / GDPR)
            Text("DATA PRIVACY & COMPLIANCE", style = MaterialTheme.typography.labelSmall, color = ClearSpendColors.TextMuted, letterSpacing = 1.sp)

            Surface(
                color = ClearSpendColors.Surface1,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, ClearSpendColors.Border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    SettingItem(
                        icon = Icons.Rounded.Lock,
                        title = "Local Encrypted Database",
                        subtitle = "All records stored locally in SQLCipher-backed database with AES-256 encryption."
                    )
                    SettingItem(
                        icon = Icons.Rounded.Shield,
                        title = "On-Device Processing",
                        subtitle = "Bank SMS alerts are parsed 100% on-device. Zero personal finance data is sold or exfiltrated."
                    )
                    SettingItem(
                        icon = Icons.Rounded.DeleteForever,
                        title = "Right to Forget",
                        subtitle = "Wipe all local records, database entries, and logs with one tap."
                    ) {
                        Button(
                            onClick = { showWipeConfirm = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ClearSpendColors.RedDanger)
                        ) {
                            Text("Erase All Data")
                        }
                    }

                    if (uiState.wipeSuccess) {
                        Text("✓ All local financial records completely wiped.", color = ClearSpendColors.RedDanger, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }

            // Architecture & Build Info
            Text("ABOUT CLEARSPEND", style = MaterialTheme.typography.labelSmall, color = ClearSpendColors.TextMuted, letterSpacing = 1.sp)

            Surface(
                color = ClearSpendColors.Surface1,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, ClearSpendColors.Border),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("ClearSpend v1.0.0", style = MaterialTheme.typography.titleMedium, color = ClearSpendColors.TextPrimary)
                    Text("Architecture: Jetpack Compose · Kotlin 2.2 · Room Offline-First · Gemini AI", style = MaterialTheme.typography.bodySmall, color = ClearSpendColors.TextMuted)
                }
            }
        }
    }

    if (showWipeConfirm) {
        AlertDialog(
            onDismissRequest = { showWipeConfirm = false },
            containerColor = ClearSpendColors.Surface2,
            title = { Text("Erase All Data?", color = ClearSpendColors.TextPrimary) },
            text = {
                Text(
                    "This action will permanently delete all transactions, budgets, cards, and coach insights from your device. This cannot be undone.",
                    color = ClearSpendColors.TextSecond
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.wipeAllData()
                        showWipeConfirm = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ClearSpendColors.RedDanger)
                ) {
                    Text("Erase Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showWipeConfirm = false }) {
                    Text("Cancel", color = ClearSpendColors.TextMuted)
                }
            }
        )
    }
}

@Composable
private fun SettingItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    action: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, contentDescription = null, tint = ClearSpendColors.Amber500)
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.titleMedium, color = ClearSpendColors.TextPrimary)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = ClearSpendColors.TextMuted)
        }
        action?.invoke()
    }
}
