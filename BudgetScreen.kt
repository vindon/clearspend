// ─── presentation/screens/budget/BudgetScreen.kt ────────────────────────────
package com.clearspend.presentation.screens.budget

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.clearspend.domain.model.*
import com.clearspend.presentation.theme.*

@Composable
fun BudgetScreen(
    onNavigateBack: () -> Unit,
    viewModel: BudgetViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = ClearSpendColors.Surface0,
        topBar = {
            ClearSpendTopBar(title = "Budget", onBack = onNavigateBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Total budget card
            TotalBudgetCard(
                spent = uiState.totalSpent,
                budget = uiState.totalBudget,
                onSetBudget = { viewModel.setTotalBudget(it) }
            )

            // Per-category budgets
            Text(
                "BY CATEGORY",
                style = MaterialTheme.typography.labelSmall,
                color = ClearSpendColors.TextMuted,
                letterSpacing = 1.5.sp,
                modifier = Modifier.padding(top = 8.dp)
            )

            uiState.categoryProgress.forEach { progress ->
                CategoryBudgetCard(
                    progress = progress,
                    onSetLimit = { viewModel.setCategoryBudget(progress.category!!, it) }
                )
            }
        }
    }
}

@Composable
private fun TotalBudgetCard(spent: Double, budget: Double?, onSetBudget: (Double) -> Unit) {
    var showDialog by remember { mutableStateOf(false) }
    val percentUsed = if (budget != null && budget > 0) (spent / budget * 100).toInt() else null
    val statusColor = when {
        percentUsed == null -> ClearSpendColors.TextMuted
        percentUsed >= 100 -> ClearSpendColors.RedDanger
        percentUsed >= 80 -> ClearSpendColors.YellowWarn
        else -> ClearSpendColors.GreenSuccess
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = ClearSpendColors.Surface1,
        border = BorderStroke(1.dp, ClearSpendColors.Amber500.copy(alpha = 0.3f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Monthly Total", style = MaterialTheme.typography.titleMedium, color = ClearSpendColors.TextPrimary)
                TextButton(onClick = { showDialog = true }) {
                    Text(if (budget == null) "Set Budget" else "Edit", color = ClearSpendColors.Amber500, style = MaterialTheme.typography.labelLarge)
                }
            }
            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("₹${spent.formatAmount()}", style = MaterialTheme.typography.displayMedium, color = ClearSpendColors.TextPrimary, fontWeight = FontWeight.Black)
                if (budget != null) {
                    Text("/ ₹${budget.formatAmount()}", style = MaterialTheme.typography.titleMedium, color = ClearSpendColors.TextMuted, modifier = Modifier.padding(bottom = 6.dp))
                }
            }
            if (budget != null) {
                Spacer(Modifier.height(12.dp))
                LinearProgressIndicator(
                    progress = { ((spent / budget).toFloat()).coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                    color = statusColor,
                    trackColor = ClearSpendColors.Surface3
                )
                percentUsed?.let {
                    Text(
                        "${it}% used",
                        style = MaterialTheme.typography.bodySmall,
                        color = statusColor,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }
    }

    if (showDialog) {
        BudgetSetDialog(
            current = budget,
            title = "Monthly Budget",
            onSave = { onSetBudget(it); showDialog = false },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
private fun CategoryBudgetCard(progress: BudgetProgress, onSetLimit: (Double) -> Unit) {
    val cat = progress.category ?: return
    val color = categoryColor(cat)
    var showDialog by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = ClearSpendColors.Surface1,
        border = BorderStroke(1.dp, ClearSpendColors.Border),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier.size(36.dp).clip(CircleShape).background(color.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) { Text(cat.emoji, fontSize = 18.sp) }
                    Column {
                        Text(cat.displayName, style = MaterialTheme.typography.titleMedium, color = ClearSpendColors.TextPrimary)
                        Text("₹${progress.spent.formatAmount()} spent", style = MaterialTheme.typography.bodySmall, color = ClearSpendColors.TextMuted)
                    }
                }
                TextButton(onClick = { showDialog = true }) {
                    Text(if (progress.limit > 0) "Edit" else "Set", color = color, style = MaterialTheme.typography.labelLarge)
                }
            }

            if (progress.limit > 0) {
                Spacer(Modifier.height(10.dp))
                val pct = progress.percentUsed / 100f
                val barColor = when (progress.status) {
                    BudgetStatus.OVER -> ClearSpendColors.RedDanger
                    BudgetStatus.WARNING -> ClearSpendColors.YellowWarn
                    BudgetStatus.OK -> color
                }
                LinearProgressIndicator(
                    progress = { pct.coerceIn(0f, 1f) },
                    modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
                    color = barColor,
                    trackColor = ClearSpendColors.Surface3
                )
                Text(
                    "₹${progress.remaining.formatAmount()} left of ₹${progress.limit.formatAmount()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = ClearSpendColors.TextMuted,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }

    if (showDialog) {
        BudgetSetDialog(
            current = progress.limit.takeIf { it > 0 },
            title = "${cat.emoji} ${cat.displayName}",
            onSave = { onSetLimit(it); showDialog = false },
            onDismiss = { showDialog = false }
        )
    }
}

@Composable
private fun BudgetSetDialog(current: Double?, title: String, onSave: (Double) -> Unit, onDismiss: () -> Unit) {
    var value by remember { mutableStateOf(current?.toInt()?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ClearSpendColors.Surface2,
        title = { Text(title, color = ClearSpendColors.TextPrimary) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                label = { Text("Amount (₹)") },
                singleLine = true,
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
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
                onClick = { value.toDoubleOrNull()?.let(onSave) },
                enabled = value.toDoubleOrNull() != null,
                colors = ButtonDefaults.buttonColors(containerColor = ClearSpendColors.Amber500, contentColor = androidx.compose.ui.graphics.Color(0xFF1A1200))
            ) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel", color = ClearSpendColors.TextSecond) } }
    )
}

// ── Shared Top Bar ────────────────────────────────────────────────────────────

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClearSpendTopBar(title: String, onBack: (() -> Unit)? = null) {
    TopAppBar(
        title = { Text(title, style = MaterialTheme.typography.titleLarge, color = ClearSpendColors.TextPrimary) },
        navigationIcon = {
            onBack?.let {
                IconButton(onClick = it) {
                    Icon(Icons.Rounded.ArrowBackIosNew, contentDescription = "Back", tint = ClearSpendColors.TextPrimary)
                }
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = ClearSpendColors.Surface0)
    )
}

// ── Budget UI State ───────────────────────────────────────────────────────────

data class BudgetUiState(
    val totalSpent: Double = 0.0,
    val totalBudget: Double? = null,
    val categoryProgress: List<BudgetProgress> = emptyList()
)

// ─── presentation/screens/home/HomeViewModel.kt ──────────────────────────────

/*
 * HomeViewModel — connects Room DB flows to UI state.
 * All data access is read-only here; mutations happen via dedicated use cases.
 */
// @HiltViewModel
// class HomeViewModel @Inject constructor(
//     private val transactionRepo: TransactionRepository,
//     private val budgetRepo: BudgetRepository,
//     private val coachRepo: CoachRepository
// ) : ViewModel() {
//
//     private val now = LocalDate.now()
//
//     val uiState: StateFlow<HomeUiState> = combine(
//         transactionRepo.observeByMonth(now.monthValue, now.year.toString()),
//         budgetRepo.observeByMonth(now.monthValue, now.year),
//         coachRepo.observeLatestInsight()
//     ) { transactions, budgets, insight ->
//         val totalSpent = transactions.sumOf { it.amount }
//         val totalBudget = budgets.find { it.category == null }?.limitAmount
//
//         // Weekly bar chart data
//         val today = now
//         val weekStart = today.minusDays(6)
//         val weeklyData = (0..6).map { offset ->
//             val day = weekStart.plusDays(offset.toLong())
//             val dayTotal = transactions
//                 .filter { it.date == day }
//                 .sumOf { it.amount }
//             DaySpend(
//                 dayLabel = day.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).take(2).uppercase(),
//                 amount = dayTotal,
//                 isToday = day == today
//             )
//         }
//
//         // Top categories
//         val catTotals = transactions
//             .groupBy { it.category }
//             .mapValues { it.value.sumOf { t -> t.amount } }
//             .entries.sortedByDescending { it.value }
//             .take(4)
//             .map { CategorySpend(it.key, it.value, (it.value / totalSpent.coerceAtLeast(1.0) * 100).toFloat()) }
//
//         // Quick insights
//         val insights = buildList {
//             catTotals.firstOrNull()?.let { add(SpendInsight(it.category.emoji, "Top: ${it.category.displayName}", categoryColor(it.category))) }
//             if (totalBudget != null) {
//                 val pct = (totalSpent / totalBudget * 100).toInt()
//                 add(SpendInsight("📊", "$pct% of budget", if (pct >= 80) ClearSpendColors.YellowWarn else ClearSpendColors.GreenSuccess))
//             }
//         }
//
//         HomeUiState(
//             isLoading = false,
//             totalSpent = totalSpent,
//             totalBudget = totalBudget,
//             recentTransactions = transactions.take(10),
//             weeklyData = weeklyData,
//             topCategories = catTotals,
//             insights = insights,
//             coachInsight = insight
//         )
//     }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())
//
//     fun selectTransaction(transaction: Transaction) { /* open detail */ }
// }

// ─── Navigation ──────────────────────────────────────────────────────────────

/*
 * ClearSpendNavGraph.kt — single-activity navigation
 *
 * Destinations:
 *   home          → HomeScreen
 *   scan          → ScanScreen (full-screen camera, no bottom bar)
 *   transactions  → TransactionListScreen
 *   budget        → BudgetScreen
 *   coach         → CoachScreen (Pro)
 *   settings      → SettingsScreen
 *   onboarding    → OnboardingScreen (shown once on first install)
 *
 * Bottom nav tabs: Home | Transactions | Budget | Settings
 * Scan is launched via FAB, not bottom nav.
 *
 * @Composable
 * fun ClearSpendNavGraph(navController: NavHostController) {
 *     NavHost(navController, startDestination = "home") {
 *         composable("home") {
 *             HomeScreen(
 *                 onNavigateToTransactions = { navController.navigate("transactions") },
 *                 onNavigateToScan = { navController.navigate("scan") },
 *                 onNavigateToBudget = { navController.navigate("budget") },
 *                 onNavigateToCoach = { navController.navigate("coach") }
 *             )
 *         }
 *         composable("scan") {
 *             ScanScreen(
 *                 onNavigateBack = { navController.popBackStack() },
 *                 onTransactionSaved = { navController.popBackStack() }
 *             )
 *         }
 *         composable("transactions") {
 *             TransactionListScreen(onNavigateBack = { navController.popBackStack() })
 *         }
 *         composable("budget") {
 *             BudgetScreen(onNavigateBack = { navController.popBackStack() })
 *         }
 *         composable("coach") {
 *             CoachScreen(onNavigateBack = { navController.popBackStack() })
 *         }
 *         composable("settings") {
 *             SettingsScreen(onNavigateBack = { navController.popBackStack() })
 *         }
 *     }
 * }
 */
