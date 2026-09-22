// ─── presentation/screens/home/HomeScreen.kt ─────────────────────────────────
package com.clearspend.presentation.screens.home

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.clearspend.domain.model.*
import com.clearspend.presentation.theme.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

// ── Screen Entry Point ────────────────────────────────────────────────────────

@Composable
fun HomeScreen(
    onNavigateToTransactions: () -> Unit,
    onNavigateToScan: () -> Unit,
    onNavigateToBudget: () -> Unit,
    onNavigateToCoach: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    HomeScreenContent(
        uiState = uiState,
        onScanTap = onNavigateToScan,
        onBudgetTap = onNavigateToBudget,
        onAllTransactionsTap = onNavigateToTransactions,
        onCoachCardTap = onNavigateToCoach,
        onTransactionTap = { viewModel.selectTransaction(it) }
    )
}

// ── Main Content ──────────────────────────────────────────────────────────────

@Composable
private fun HomeScreenContent(
    uiState: HomeUiState,
    onScanTap: () -> Unit,
    onBudgetTap: () -> Unit,
    onAllTransactionsTap: () -> Unit,
    onCoachCardTap: () -> Unit,
    onTransactionTap: (Transaction) -> Unit
) {
    val today = LocalDate.now()
    val monthName = today.month.getDisplayName(TextStyle.FULL, Locale.getDefault())

    Scaffold(
        containerColor = ClearSpendColors.Surface0,
        floatingActionButton = {
            ScanFab(onClick = onScanTap)
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 100.dp)
        ) {
            // ── Top Header ──────────────────────────────────────────────
            item {
                HomeHeader(
                    monthName = monthName,
                    totalSpent = uiState.totalSpent,
                    totalBudget = uiState.totalBudget,
                    isLoading = uiState.isLoading
                )
            }

            // ── Quick Insight Chips ─────────────────────────────────────
            if (uiState.insights.isNotEmpty()) {
                item {
                    InsightChipsRow(insights = uiState.insights)
                }
            }

            // ── Weekly Bar Chart ────────────────────────────────────────
            item {
                WeeklySpendChart(
                    weeklyData = uiState.weeklyData,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }

            // ── Top Categories ──────────────────────────────────────────
            if (uiState.topCategories.isNotEmpty()) {
                item {
                    SectionHeader(
                        title = "Top Categories",
                        actionLabel = "See Budget",
                        onAction = onBudgetTap
                    )
                }
                item {
                    CategoryDonutRow(categories = uiState.topCategories)
                }
            }

            // ── AI Coach Teaser (Pro) ───────────────────────────────────
            uiState.coachInsight?.let { insight ->
                item {
                    CoachInsightCard(
                        insight = insight,
                        onClick = onCoachCardTap
                    )
                }
            }

            // ── Recent Transactions ─────────────────────────────────────
            item {
                SectionHeader(
                    title = "Recent",
                    actionLabel = "All",
                    onAction = onAllTransactionsTap
                )
            }

            if (uiState.recentTransactions.isEmpty() && !uiState.isLoading) {
                item { EmptyTransactionsState(onScanTap = onScanTap) }
            } else {
                items(
                    items = uiState.recentTransactions,
                    key = { it.id }
                ) { transaction ->
                    TransactionRow(
                        transaction = transaction,
                        onClick = { onTransactionTap(transaction) }
                    )
                }
            }
        }
    }
}

// ── Header Component ──────────────────────────────────────────────────────────

@Composable
private fun HomeHeader(
    monthName: String,
    totalSpent: Double,
    totalBudget: Double?,
    isLoading: Boolean
) {
    val progress = if (totalBudget != null && totalBudget > 0) {
        (totalSpent / totalBudget).toFloat().coerceIn(0f, 1f)
    } else null

    val progressColor = when {
        progress == null -> ClearSpendColors.Amber500
        progress >= 1f   -> ClearSpendColors.RedDanger
        progress >= 0.8f -> ClearSpendColors.YellowWarn
        else             -> ClearSpendColors.GreenSuccess
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ClearSpendColors.Amber500.copy(alpha = 0.12f),
                        ClearSpendColors.Surface0
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 32.dp, bottom = 24.dp)
        ) {
            // Label
            Text(
                text = monthName.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = ClearSpendColors.TextMuted,
                letterSpacing = 2.sp
            )

            Spacer(Modifier.height(8.dp))

            // Big spend number
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .height(52.dp)
                        .width(160.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .shimmerEffect()
                )
            } else {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "₹",
                        style = MaterialTheme.typography.headlineLarge,
                        color = ClearSpendColors.TextSecond,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = totalSpent.formatAmount(),
                        style = MaterialTheme.typography.displayMedium,
                        color = ClearSpendColors.TextPrimary,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            // Budget remaining
            totalBudget?.let { budget ->
                val remaining = budget - totalSpent
                Text(
                    text = if (remaining >= 0)
                        "₹${remaining.formatAmount()} remaining of ₹${budget.formatAmount()}"
                    else
                        "₹${(-remaining).formatAmount()} over budget",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (remaining >= 0) ClearSpendColors.TextSecond else ClearSpendColors.RedDanger,
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(Modifier.height(16.dp))

                // Budget progress bar
                progress?.let { p ->
                    Column {
                        LinearProgressIndicator(
                            progress = { p },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = progressColor,
                            trackColor = ClearSpendColors.Surface3
                        )
                    }
                }
            }
        }
    }
}

// ── Insight Chips ─────────────────────────────────────────────────────────────

@Composable
private fun InsightChipsRow(insights: List<SpendInsight>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.padding(vertical = 8.dp)
    ) {
        items(insights) { insight ->
            InsightChip(insight = insight)
        }
    }
}

@Composable
private fun InsightChip(insight: SpendInsight) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = insight.color.copy(alpha = 0.12f),
        border = BorderStroke(1.dp, insight.color.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(text = insight.emoji, fontSize = 14.sp)
            Text(
                text = insight.label,
                style = MaterialTheme.typography.labelLarge,
                color = insight.color
            )
        }
    }
}

// ── Weekly Chart Placeholder ──────────────────────────────────────────────────
// Real Vico chart wired in WeeklySpendChart.kt — this is the Compose scaffold

@Composable
private fun WeeklySpendChart(
    weeklyData: List<DaySpend>,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(16.dp),
        color = ClearSpendColors.Surface1,
        border = BorderStroke(1.dp, ClearSpendColors.Border)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "This Week",
                style = MaterialTheme.typography.labelSmall,
                color = ClearSpendColors.TextMuted,
                letterSpacing = 1.5.sp
            )
            Spacer(Modifier.height(12.dp))

            if (weeklyData.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "Scan your first receipt to see trends",
                        style = MaterialTheme.typography.bodySmall,
                        color = ClearSpendColors.TextMuted
                    )
                }
            } else {
                // Bar chart using simple Compose Canvas (Vico replaces this in prod)
                SimpleBarChart(
                    data = weeklyData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(80.dp)
                )
            }
        }
    }
}

@Composable
private fun SimpleBarChart(
    data: List<DaySpend>,
    modifier: Modifier = Modifier
) {
    val maxVal = data.maxOfOrNull { it.amount } ?: 1.0
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { day ->
            val heightFrac = (day.amount / maxVal).toFloat().coerceIn(0.05f, 1f)
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Box(
                    modifier = Modifier
                        .width(20.dp)
                        .fillMaxHeight(heightFrac)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                        .background(
                            if (day.isToday) ClearSpendColors.Amber500
                            else ClearSpendColors.Surface3
                        )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = day.dayLabel,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (day.isToday) ClearSpendColors.Amber500 else ClearSpendColors.TextMuted,
                    fontSize = 9.sp
                )
            }
        }
    }
}

// ── Category Donut Row ────────────────────────────────────────────────────────

@Composable
private fun CategoryDonutRow(categories: List<CategorySpend>) {
    LazyRow(
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        items(categories) { cat ->
            CategoryCard(categorySpend = cat)
        }
    }
}

@Composable
private fun CategoryCard(categorySpend: CategorySpend) {
    val color = categoryColor(categorySpend.category)
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, color.copy(alpha = 0.25f))
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = categorySpend.category.emoji, fontSize = 20.sp)
            Spacer(Modifier.height(4.dp))
            Text(
                text = "₹${categorySpend.amount.formatAmount()}",
                style = MaterialTheme.typography.labelLarge,
                color = color,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = categorySpend.category.displayName.take(10),
                style = MaterialTheme.typography.labelSmall,
                color = ClearSpendColors.TextMuted,
                fontSize = 9.sp
            )
        }
    }
}

// ── AI Coach Teaser ───────────────────────────────────────────────────────────

@Composable
private fun CoachInsightCard(
    insight: CoachInsight,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = ClearSpendColors.Surface2,
        border = BorderStroke(1.dp, ClearSpendColors.Amber500.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ClearSpendColors.Amber500.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text("✨", fontSize = 18.sp)
            }
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "Weekly Insight",
                        style = MaterialTheme.typography.labelLarge,
                        color = ClearSpendColors.Amber500
                    )
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = ClearSpendColors.Amber500.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "PRO",
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = ClearSpendColors.Amber500,
                            fontSize = 8.sp
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    text = insight.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ClearSpendColors.TextSecond,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
            }
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = ClearSpendColors.TextMuted,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// ── Transaction Row ───────────────────────────────────────────────────────────

@Composable
fun TransactionRow(
    transaction: Transaction,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val catColor = categoryColor(transaction.category)
    val dateFormatter = DateTimeFormatter.ofPattern("dd MMM")

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Category icon circle
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(catColor.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = transaction.category.emoji, fontSize = 20.sp)
        }

        // Merchant + category
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = transaction.merchant,
                style = MaterialTheme.typography.titleMedium,
                color = ClearSpendColors.TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = transaction.category.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = ClearSpendColors.TextMuted
                )
                if (transaction.source == TransactionSource.SMS_IMPORT) {
                    Text("·", color = ClearSpendColors.TextMuted, fontSize = 10.sp)
                    Text(
                        text = "SMS",
                        style = MaterialTheme.typography.labelSmall,
                        color = ClearSpendColors.TextMuted,
                        fontSize = 9.sp
                    )
                }
            }
        }

        // Amount + date
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "₹${transaction.amount.formatAmount()}",
                style = MaterialTheme.typography.titleMedium,
                color = ClearSpendColors.TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = transaction.date.format(dateFormatter),
                style = MaterialTheme.typography.bodySmall,
                color = ClearSpendColors.TextMuted
            )
        }
    }
}

// ── Section Header ────────────────────────────────────────────────────────────

@Composable
private fun SectionHeader(
    title: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 16.dp, top = 20.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = ClearSpendColors.TextMuted,
            letterSpacing = 1.5.sp
        )
        if (actionLabel != null && onAction != null) {
            TextButton(onClick = onAction) {
                Text(
                    text = actionLabel,
                    style = MaterialTheme.typography.labelLarge,
                    color = ClearSpendColors.Amber500
                )
            }
        }
    }
}

// ── FAB ───────────────────────────────────────────────────────────────────────

@Composable
private fun ScanFab(onClick: () -> Unit) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        containerColor = ClearSpendColors.Amber500,
        contentColor = Color(0xFF1A1200),
        shape = RoundedCornerShape(16.dp),
        icon = {
            Icon(
                imageVector = Icons.Rounded.CameraAlt,
                contentDescription = "Scan Receipt"
            )
        },
        text = {
            Text(
                text = "Scan Receipt",
                fontWeight = FontWeight.Bold
            )
        }
    )
}

// ── Empty State ───────────────────────────────────────────────────────────────

@Composable
private fun EmptyTransactionsState(onScanTap: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("🧾", fontSize = 48.sp)
        Spacer(Modifier.height(16.dp))
        Text(
            text = "No transactions yet",
            style = MaterialTheme.typography.titleMedium,
            color = ClearSpendColors.TextSecond
        )
        Text(
            text = "Scan your first receipt to start tracking",
            style = MaterialTheme.typography.bodySmall,
            color = ClearSpendColors.TextMuted,
            modifier = Modifier.padding(top = 4.dp)
        )
        Spacer(Modifier.height(20.dp))
        Button(
            onClick = onScanTap,
            colors = ButtonDefaults.buttonColors(
                containerColor = ClearSpendColors.Amber500,
                contentColor = Color(0xFF1A1200)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Rounded.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Scan Receipt", fontWeight = FontWeight.Bold)
        }
    }
}

// ── Shimmer effect ────────────────────────────────────────────────────────────

@Composable
fun Modifier.shimmerEffect(): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "shimmer")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.7f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer_alpha"
    )
    return this.background(ClearSpendColors.Surface2.copy(alpha = alpha))
}

// ── Formatting helpers ─────────────────────────────────────────────────────────

fun Double.formatAmount(): String {
    return when {
        this >= 100_000 -> "${(this / 100_000).toInt()}L"          // 1,00,000 → 1L
        this >= 1_000   -> String.format("%,.0f", this)             // 1,234
        else            -> String.format("%.0f", this)
    }
}

// ── UI State types ─────────────────────────────────────────────────────────────

data class HomeUiState(
    val isLoading: Boolean = true,
    val totalSpent: Double = 0.0,
    val totalBudget: Double? = null,
    val recentTransactions: List<Transaction> = emptyList(),
    val weeklyData: List<DaySpend> = emptyList(),
    val topCategories: List<CategorySpend> = emptyList(),
    val insights: List<SpendInsight> = emptyList(),
    val coachInsight: CoachInsight? = null
)

data class DaySpend(val dayLabel: String, val amount: Double, val isToday: Boolean)
data class CategorySpend(val category: Category, val amount: Double, val percentOfTotal: Float)
data class SpendInsight(val emoji: String, val label: String, val color: Color)
