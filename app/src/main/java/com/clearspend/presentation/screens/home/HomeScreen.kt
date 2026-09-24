package com.clearspend.presentation.screens.home

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.clearspend.domain.model.ReviewStatus
import com.clearspend.domain.model.Transaction
import com.clearspend.presentation.theme.ClearSpendColors
import com.clearspend.presentation.theme.categoryColor
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun HomeScreen(
    onNavigateToTransactions: () -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToCoach: () -> Unit,
    onNavigateToCards: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = ClearSpendColors.Surface0,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToScan,
                containerColor = ClearSpendColors.Amber500,
                contentColor = Color.Black,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(Icons.Rounded.DocumentScanner, contentDescription = "Scan Receipt")
            }
        }
    ) { padding ->
        val monthName = LocalDate.now().month.getDisplayName(TextStyle.FULL, Locale.getDefault())

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 96.dp)
        ) {
            // ── Top Header & Hero Spend ──────────────────────────────────────────
            item {
                HomeHeader(
                    monthName = monthName,
                    totalSpent = uiState.totalSpent,
                    totalBudget = uiState.totalBudget,
                    onBudgetTap = onNavigateToBudget
                )
            }

            // ── Pending Review Alert (HITL) ──────────────────────────────────────
            if (uiState.pendingReviewCount > 0) {
                item {
                    PendingReviewCard(
                        count = uiState.pendingReviewCount,
                        onReviewTap = onNavigateToTransactions
                    )
                }
            }

            // ── Quick Insight Chips ──────────────────────────────────────────────
            if (uiState.insights.isNotEmpty()) {
                item {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(uiState.insights) { insight ->
                            Surface(
                                shape = RoundedCornerShape(20.dp),
                                color = ClearSpendColors.Surface1,
                                border = BorderStroke(1.dp, insight.color.copy(alpha = 0.3f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(insight.emoji, fontSize = 14.sp)
                                    Text(
                                        insight.text,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = insight.color
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // ── Weekly 7-Day Spend Chart ─────────────────────────────────────────
            item {
                WeeklySpendChart(
                    weeklyData = uiState.weeklyData,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                )
            }

            // ── AI Financial Coach Card ──────────────────────────────────────────
            item {
                CoachWidget(
                    coachSummary = uiState.coachInsight?.summary,
                    onCoachTap = onNavigateToCoach,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // ── Top Categories Breakdown ─────────────────────────────────────────
            if (uiState.topCategories.isNotEmpty()) {
                item {
                    SectionTitle(
                        title = "Top Spending",
                        action = "Budget",
                        onAction = onNavigateToBudget
                    )
                    TopCategoriesRow(categories = uiState.topCategories)
                }
            }

            // ── Recent Transactions ──────────────────────────────────────────────
            item {
                SectionTitle(
                    title = "Recent Transactions",
                    action = "See All",
                    onAction = onNavigateToTransactions
                )
            }

            if (uiState.recentTransactions.isEmpty()) {
                item {
                    EmptyTransactionsPlaceholder(onScanTap = onNavigateToScan)
                }
            } else {
                items(uiState.recentTransactions, key = { it.id }) { tx ->
                    TransactionRow(
                        transaction = tx,
                        onConfirm = { viewModel.confirmTransaction(tx) },
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

// ── Supporting Components ────────────────────────────────────────────────────────

@Composable
private fun HomeHeader(
    monthName: String,
    totalSpent: Double,
    totalBudget: Double?,
    onBudgetTap: () -> Unit
) {
    Surface(
        color = ClearSpendColors.Surface1,
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .statusBarsPadding()
                .padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "CLEARSPEND",
                        style = MaterialTheme.typography.labelSmall,
                        color = ClearSpendColors.Amber500
                    )
                    Text(
                        text = "$monthName Overview",
                        style = MaterialTheme.typography.titleMedium,
                        color = ClearSpendColors.TextSecond
                    )
                }
                TextButton(onClick = onBudgetTap) {
                    Text(
                        text = if (totalBudget == null) "Set Budget" else "₹${totalBudget.toInt()} Limit",
                        color = ClearSpendColors.Amber500,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "₹${String.format(Locale.getDefault(), "%,.0f", totalSpent)}",
                style = MaterialTheme.typography.displayLarge,
                color = ClearSpendColors.TextPrimary
            )

            if (totalBudget != null && totalBudget > 0) {
                val progress = (totalSpent / totalBudget).toFloat().coerceIn(0f, 1f)
                val isOver = totalSpent > totalBudget
                val barColor = when {
                    isOver -> ClearSpendColors.RedDanger
                    progress >= 0.8f -> ClearSpendColors.YellowWarn
                    else -> ClearSpendColors.GreenSuccess
                }

                Spacer(modifier = Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = barColor,
                    trackColor = ClearSpendColors.Border
                )

                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = if (isOver) "Exceeded by ₹${(totalSpent - totalBudget).toInt()}" else "₹${(totalBudget - totalSpent).toInt()} remaining",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isOver) ClearSpendColors.RedDanger else ClearSpendColors.TextSecond
                    )
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = ClearSpendColors.TextSecond
                    )
                }
            }
        }
    }
}

@Composable
private fun PendingReviewCard(count: Int, onReviewTap: () -> Unit) {
    Surface(
        color = ClearSpendColors.Amber500.copy(alpha = 0.12f),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, ClearSpendColors.Amber500.copy(alpha = 0.35f)),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onReviewTap() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Rounded.Shield, contentDescription = null, tint = ClearSpendColors.Amber500)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Review Needed ($count)",
                    style = MaterialTheme.typography.titleMedium,
                    color = ClearSpendColors.Amber500
                )
                Text(
                    text = "Some SMS or receipt scans need your 1-tap confirmation.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ClearSpendColors.TextPrimary
                )
            }
            Icon(Icons.Rounded.ChevronRight, contentDescription = null, tint = ClearSpendColors.Amber500)
        }
    }
}

@Composable
private fun WeeklySpendChart(weeklyData: List<DaySpend>, modifier: Modifier = Modifier) {
    Surface(
        color = ClearSpendColors.Surface1,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ClearSpendColors.Border),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "7-Day Activity",
                style = MaterialTheme.typography.titleMedium,
                color = ClearSpendColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(16.dp))

            val maxAmount = weeklyData.maxOfOrNull { it.amount }?.coerceAtLeast(100.0) ?: 1000.0

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                weeklyData.forEach { item ->
                    val heightRatio = (item.amount / maxAmount).toFloat().coerceIn(0.08f, 1f)
                    val barColor = if (item.isToday) ClearSpendColors.Amber500 else ClearSpendColors.BorderLight

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(20.dp)
                                    .fillMaxHeight(heightRatio)
                                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                    .background(barColor)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = item.dayLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (item.isToday) ClearSpendColors.Amber500 else ClearSpendColors.TextMuted
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CoachWidget(
    coachSummary: String?,
    onCoachTap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ClearSpendColors.Surface2,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ClearSpendColors.Amber500.copy(alpha = 0.25f)),
        modifier = modifier
            .fillMaxWidth()
            .clickable { onCoachTap() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("💡", fontSize = 16.sp)
                    Text("AI Financial Coach", style = MaterialTheme.typography.titleMedium, color = ClearSpendColors.Amber500)
                }
                Surface(
                    color = ClearSpendColors.Amber500.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text("Weekly Summary", style = MaterialTheme.typography.labelSmall, color = ClearSpendColors.Amber400, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = coachSummary ?: "Keep recording your daily expenses. Your weekly coach report will generate every Sunday with personalized savings insights!",
                style = MaterialTheme.typography.bodyMedium,
                color = ClearSpendColors.TextPrimary
            )
        }
    }
}

@Composable
private fun TopCategoriesRow(categories: List<CategorySpend>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(categories) { item ->
            Surface(
                color = ClearSpendColors.Surface1,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, ClearSpendColors.Border),
                modifier = Modifier.width(130.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(item.category.emoji, fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(item.category.displayName, style = MaterialTheme.typography.labelLarge, color = ClearSpendColors.TextPrimary, maxLines = 1)
                    Text("₹${item.amount.toInt()}", style = MaterialTheme.typography.titleMedium, color = categoryColor(item.category), fontWeight = FontWeight.Bold)
                    Text("${item.percentage.toInt()}% of spend", style = MaterialTheme.typography.bodySmall, color = ClearSpendColors.TextMuted)
                }
            }
        }
    }
}

@Composable
fun TransactionRow(
    transaction: Transaction,
    onConfirm: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ClearSpendColors.Surface1,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            1.dp,
            if (transaction.reviewStatus == ReviewStatus.PENDING_REVIEW) ClearSpendColors.Amber500.copy(alpha = 0.5f) else ClearSpendColors.Border
        ),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(categoryColor(transaction.category).copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(transaction.category.emoji, fontSize = 18.sp)
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.merchant,
                    style = MaterialTheme.typography.titleMedium,
                    color = ClearSpendColors.TextPrimary,
                    maxLines = 1
                )
                Text(
                    text = "${transaction.date.format(DateTimeFormatter.ofPattern("MMM dd"))} · ${transaction.category.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ClearSpendColors.TextMuted
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "₹${String.format(Locale.getDefault(), "%,.0f", transaction.amount)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ClearSpendColors.TextPrimary
                )

                if (transaction.reviewStatus == ReviewStatus.PENDING_REVIEW && onConfirm != null) {
                    Text(
                        text = "Confirm",
                        color = ClearSpendColors.Amber500,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier
                            .clickable { onConfirm() }
                            .padding(top = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(title: String, action: String? = null, onAction: (() -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(title, style = MaterialTheme.typography.titleLarge, color = ClearSpendColors.TextPrimary)
        if (action != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(action, color = ClearSpendColors.Amber500, style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun EmptyTransactionsPlaceholder(onScanTap: () -> Unit) {
    Surface(
        color = ClearSpendColors.Surface1,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, ClearSpendColors.Border),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("🧾", fontSize = 36.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text("No transactions logged yet", style = MaterialTheme.typography.titleMedium, color = ClearSpendColors.TextPrimary)
            Text(
                "Scan a paper receipt or let bank SMS alerts track your spending automatically.",
                style = MaterialTheme.typography.bodySmall,
                color = ClearSpendColors.TextMuted,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            Button(
                onClick = onScanTap,
                colors = ButtonDefaults.buttonColors(containerColor = ClearSpendColors.Amber500, contentColor = Color.Black)
            ) {
                Text("Scan Your First Receipt")
            }
        }
    }
}
