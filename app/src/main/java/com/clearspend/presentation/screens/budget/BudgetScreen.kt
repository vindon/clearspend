package com.clearspend.presentation.screens.budget

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.clearspend.domain.model.BudgetProgress
import com.clearspend.domain.model.BudgetStatus
import com.clearspend.domain.model.Category
import com.clearspend.presentation.common.ClearSpendTopBar
import com.clearspend.presentation.theme.ClearSpendColors
import com.clearspend.presentation.theme.categoryColor
import java.util.Locale

@Composable
fun BudgetScreen(
    onNavigateBack: () -> Unit,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var showTotalDialog by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<Category?>(null) }

    Scaffold(
        containerColor = ClearSpendColors.Surface0,
        topBar = {
            ClearSpendTopBar(title = "Budget Envelopes", onBack = onNavigateBack)
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
            // Total Budget Card
            TotalBudgetCard(
                spent = uiState.totalSpent,
                budget = uiState.totalBudget,
                onEdit = { showTotalDialog = true }
            )

            Text(
                text = "CATEGORY ENVELOPES",
                style = MaterialTheme.typography.labelSmall,
                color = ClearSpendColors.TextMuted,
                letterSpacing = 1.5.sp
            )

            uiState.categoryProgress.forEach { progress ->
                CategoryBudgetCard(
                    progress = progress,
                    onEdit = { editingCategory = progress.category }
                )
            }
        }
    }

    if (showTotalDialog) {
        BudgetSetDialog(
            title = "Set Monthly Total Budget",
            initialValue = uiState.totalBudget?.toInt()?.toString() ?: "",
            onConfirm = {
                it.toDoubleOrNull()?.let { amt -> viewModel.setTotalBudget(amt) }
                showTotalDialog = false
            },
            onDismiss = { showTotalDialog = false }
        )
    }

    editingCategory?.let { cat ->
        val currentLimit = uiState.categoryProgress.find { it.category == cat }?.limit ?: 0.0
        BudgetSetDialog(
            title = "Set ${cat.displayName} Limit",
            initialValue = if (currentLimit > 0) currentLimit.toInt().toString() else "",
            onConfirm = {
                it.toDoubleOrNull()?.let { amt -> viewModel.setCategoryBudget(cat, amt) }
                editingCategory = null
            },
            onDismiss = { editingCategory = null }
        )
    }
}

@Composable
private fun TotalBudgetCard(
    spent: Double,
    budget: Double?,
    onEdit: () -> Unit
) {
    Surface(
        color = ClearSpendColors.Surface1,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ClearSpendColors.Amber500.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Monthly Total Budget", style = MaterialTheme.typography.titleMedium, color = ClearSpendColors.TextPrimary)
                TextButton(onClick = onEdit) {
                    Text(if (budget == null) "Set Limit" else "Edit", color = ClearSpendColors.Amber500)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "₹${String.format(Locale.getDefault(), "%,.0f", spent)}",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = ClearSpendColors.TextPrimary
                )
                if (budget != null) {
                    Text(
                        text = " / ₹${String.format(Locale.getDefault(), "%,.0f", budget)}",
                        style = MaterialTheme.typography.titleMedium,
                        color = ClearSpendColors.TextMuted,
                        modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                    )
                }
            }

            if (budget != null && budget > 0) {
                val ratio = (spent / budget).toFloat().coerceIn(0f, 1f)
                val statusColor = when {
                    spent > budget -> ClearSpendColors.RedDanger
                    ratio >= 0.8f -> ClearSpendColors.YellowWarn
                    else -> ClearSpendColors.GreenSuccess
                }

                Spacer(modifier = Modifier.height(14.dp))
                LinearProgressIndicator(
                    progress = { ratio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = statusColor,
                    trackColor = ClearSpendColors.Border
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (spent > budget) "Over budget by ₹${(spent - budget).toInt()}" else "₹${(budget - spent).toInt()} remaining this month",
                    style = MaterialTheme.typography.bodySmall,
                    color = statusColor
                )
            }
        }
    }
}

@Composable
private fun CategoryBudgetCard(
    progress: BudgetProgress,
    onEdit: () -> Unit
) {
    val category = progress.category ?: return
    val percent = progress.percentUsed.toInt()

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
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(category.emoji, fontSize = 20.sp)
                    Text(category.displayName, style = MaterialTheme.typography.titleMedium, color = ClearSpendColors.TextPrimary)
                }
                TextButton(onClick = onEdit) {
                    Text(if (progress.limit > 0) "Edit" else "Set Limit", color = ClearSpendColors.Amber500, style = MaterialTheme.typography.labelLarge)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text = "Spent: ₹${progress.spent.toInt()}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClearSpendColors.TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = if (progress.limit > 0) "Limit: ₹${progress.limit.toInt()}" else "No limit set",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClearSpendColors.TextMuted
                )
            }

            if (progress.limit > 0) {
                val ratio = (progress.spent / progress.limit).toFloat().coerceIn(0f, 1f)
                val barColor = when (progress.status) {
                    BudgetStatus.OVER -> ClearSpendColors.RedDanger
                    BudgetStatus.WARNING -> ClearSpendColors.YellowWarn
                    BudgetStatus.OK -> categoryColor(category)
                }

                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { ratio },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = barColor,
                    trackColor = ClearSpendColors.Border
                )

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "$percent% used · ₹${progress.remaining.toInt().coerceAtLeast(0)} left",
                    style = MaterialTheme.typography.bodySmall,
                    color = ClearSpendColors.TextMuted
                )
            }
        }
    }
}

@Composable
private fun BudgetSetDialog(
    title: String,
    initialValue: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(initialValue) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ClearSpendColors.Surface2,
        title = { Text(title, style = MaterialTheme.typography.titleLarge, color = ClearSpendColors.TextPrimary) },
        text = {
            OutlinedTextField(
                value = text,
                onValueChange = { text = it },
                label = { Text("Monthly Limit (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ClearSpendColors.Amber500,
                    unfocusedBorderColor = ClearSpendColors.Border,
                    focusedTextColor = ClearSpendColors.TextPrimary,
                    unfocusedTextColor = ClearSpendColors.TextPrimary
                )
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(text) },
                colors = ButtonDefaults.buttonColors(containerColor = ClearSpendColors.Amber500, contentColor = Color.Black)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = ClearSpendColors.TextMuted)
            }
        }
    )
}
