package com.clearspend.presentation.screens.transactions

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.clearspend.domain.model.Category
import com.clearspend.presentation.common.ClearSpendTopBar
import com.clearspend.presentation.screens.home.TransactionRow
import com.clearspend.presentation.theme.ClearSpendColors

@Composable
fun TransactionListScreen(
    onNavigateBack: () -> Unit,
    viewModel: TransactionListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = ClearSpendColors.Surface0,
        topBar = {
            ClearSpendTopBar(title = "All Expenses", onBack = onNavigateBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = { viewModel.setSearchQuery(it) },
                placeholder = { Text("Search merchants...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null, tint = ClearSpendColors.TextMuted) },
                trailingIcon = {
                    if (uiState.searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setSearchQuery("") }) {
                            Icon(Icons.Rounded.Clear, contentDescription = "Clear", tint = ClearSpendColors.TextMuted)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ClearSpendColors.Amber500,
                    unfocusedBorderColor = ClearSpendColors.Border,
                    focusedTextColor = ClearSpendColors.TextPrimary,
                    unfocusedTextColor = ClearSpendColors.TextPrimary
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )

            // Category Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = uiState.selectedCategory == null && !uiState.onlyPendingReview,
                    onClick = {
                        viewModel.setCategory(null)
                        if (uiState.onlyPendingReview) viewModel.togglePendingReviewOnly()
                    },
                    label = { Text("All") }
                )

                FilterChip(
                    selected = uiState.onlyPendingReview,
                    onClick = { viewModel.togglePendingReviewOnly() },
                    label = { Text("⚡ Needs Review") }
                )

                Category.entries.forEach { cat ->
                    FilterChip(
                        selected = uiState.selectedCategory == cat,
                        onClick = {
                            viewModel.setCategory(if (uiState.selectedCategory == cat) null else cat)
                        },
                        label = { Text("${cat.emoji} ${cat.displayName}") }
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Transaction List
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (uiState.transactions.isEmpty()) {
                    item {
                        Surface(
                            color = ClearSpendColors.Surface1,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, ClearSpendColors.Border),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 32.dp)
                        ) {
                            Text(
                                text = "No matching expenses found.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ClearSpendColors.TextMuted,
                                modifier = Modifier.padding(24.dp)
                            )
                        }
                    }
                } else {
                    items(uiState.transactions, key = { it.id }) { tx ->
                        TransactionRow(
                            transaction = tx,
                            onConfirm = { viewModel.confirmTransaction(tx.id) }
                        )
                    }
                }
            }
        }
    }
}
