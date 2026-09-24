// ─── presentation/screens/onboarding/OnboardingScreen.kt ────────────────────
package com.clearspend.presentation.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.*
import androidx.compose.foundation.shape.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import com.clearspend.presentation.theme.*

/**
 * 3-page onboarding flow shown once on first install.
 * Page 1: Value prop + currency selection
 * Page 2: Set monthly budget (optional, skippable)
 * Page 3: SMS permission request with trust-building copy
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: (currency: String, monthlyBudget: Double?, smsPermissionGranted: Boolean) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()

    var selectedCurrency by remember { mutableStateOf("INR") }
    var monthlyBudget by remember { mutableStateOf("") }
    var smsGranted by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ClearSpendColors.Amber500.copy(alpha = 0.08f),
                        ClearSpendColors.Surface0,
                        ClearSpendColors.Surface0
                    )
                )
            )
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Page indicator
            PageIndicator(
                current = pagerState.currentPage,
                total = 3,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 56.dp, bottom = 16.dp)
            )

            // Pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = false
            ) { page ->
                when (page) {
                    0 -> WelcomePage(
                        selectedCurrency = selectedCurrency,
                        onCurrencySelect = { selectedCurrency = it }
                    )
                    1 -> BudgetPage(
                        budget = monthlyBudget,
                        currency = selectedCurrency,
                        onBudgetChange = { monthlyBudget = it }
                    )
                    2 -> SmsPermissionPage(
                        onPermissionResult = { smsGranted = it }
                    )
                }
            }

            // Navigation buttons
            OnboardingFooter(
                currentPage = pagerState.currentPage,
                isLastPage = pagerState.currentPage == 2,
                onNext = {
                    scope.launch {
                        if (pagerState.currentPage < 2) {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        } else {
                            onComplete(
                                selectedCurrency,
                                monthlyBudget.toDoubleOrNull(),
                                smsGranted
                            )
                        }
                    }
                },
                onSkip = {
                    scope.launch {
                        if (pagerState.currentPage == 1) {
                            pagerState.animateScrollToPage(2)
                        }
                    }
                },
                canSkip = pagerState.currentPage == 1
            )
        }
    }
}

@Composable
private fun WelcomePage(selectedCurrency: String, onCurrencySelect: (String) -> Unit) {
    val currencies = listOf(
        Triple("INR", "₹", "India"),
        Triple("USD", "$", "US"),
        Triple("EUR", "€", "Europe"),
        Triple("GBP", "£", "UK"),
        Triple("SGD", "S$", "Singapore"),
        Triple("AED", "د.إ", "UAE")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("🧾", fontSize = 72.sp)
        Spacer(Modifier.height(24.dp))
        Text(
            "Know exactly where your money goes",
            style = MaterialTheme.typography.headlineLarge,
            color = ClearSpendColors.TextPrimary,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Black,
            lineHeight = 32.sp
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "Snap a receipt. Done. ClearSpend handles the rest.",
            style = MaterialTheme.typography.bodyLarge,
            color = ClearSpendColors.TextSecond,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        Spacer(Modifier.height(40.dp))
        Text(
            "YOUR CURRENCY",
            style = MaterialTheme.typography.labelSmall,
            color = ClearSpendColors.TextMuted,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(12.dp))
        // Currency grid
        androidx.compose.foundation.lazy.LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(currencies.size) { i ->
                val (code, symbol, country) = currencies[i]
                val isSelected = code == selectedCurrency
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) ClearSpendColors.Amber500.copy(alpha = 0.2f) else ClearSpendColors.Surface2,
                    border = BorderStroke(1.dp, if (isSelected) ClearSpendColors.Amber500 else ClearSpendColors.Border),
                    modifier = Modifier.clickable { onCurrencySelect(code) }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(symbol, style = MaterialTheme.typography.titleLarge, color = if (isSelected) ClearSpendColors.Amber500 else ClearSpendColors.TextPrimary, fontWeight = FontWeight.Bold)
                        Text(code, style = MaterialTheme.typography.labelSmall, color = if (isSelected) ClearSpendColors.Amber500 else ClearSpendColors.TextMuted, fontSize = 10.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetPage(budget: String, currency: String, onBudgetChange: (String) -> Unit) {
    val symbol = when (currency) { "USD" -> "$"; "EUR" -> "€"; "GBP" -> "£"; else -> "₹" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("📊", fontSize = 72.sp)
        Spacer(Modifier.height(24.dp))
        Text(
            "Set a monthly budget",
            style = MaterialTheme.typography.headlineLarge,
            color = ClearSpendColors.TextPrimary,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Black
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "We'll show you how much you have left as you spend. You can always change this later.",
            style = MaterialTheme.typography.bodyLarge,
            color = ClearSpendColors.TextSecond,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        Spacer(Modifier.height(40.dp))
        OutlinedTextField(
            value = budget,
            onValueChange = onBudgetChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Monthly budget ($symbol)") },
            placeholder = { Text(if (currency == "INR") "e.g. 30000" else "e.g. 1500") },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number),
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ClearSpendColors.Amber500,
                unfocusedBorderColor = ClearSpendColors.Border,
                focusedTextColor = ClearSpendColors.TextPrimary,
                unfocusedTextColor = ClearSpendColors.TextPrimary,
                unfocusedContainerColor = ClearSpendColors.Surface2,
                focusedContainerColor = ClearSpendColors.Surface2
            )
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "You can skip this and set it later",
            style = MaterialTheme.typography.bodySmall,
            color = ClearSpendColors.TextMuted
        )
    }
}

@Composable
private fun SmsPermissionPage(onPermissionResult: (Boolean) -> Unit) {
    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { granted ->
        onPermissionResult(granted)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("📱", fontSize = 72.sp)
        Spacer(Modifier.height(24.dp))
        Text(
            "Auto-detect bank transactions",
            style = MaterialTheme.typography.headlineLarge,
            color = ClearSpendColors.TextPrimary,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Black
        )
        Spacer(Modifier.height(16.dp))

        // Trust-building bullets
        val bullets = listOf(
            "✓  We only read SMS from your bank — never personal messages",
            "✓  No bank login or credentials ever required",
            "✓  All transaction data stays on your device",
            "✓  You can disable this anytime in settings"
        )
        bullets.forEach { bullet ->
            Text(
                text = bullet,
                style = MaterialTheme.typography.bodyMedium,
                color = ClearSpendColors.TextSecond,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }
        Spacer(Modifier.height(32.dp))
        Button(
            onClick = { launcher.launch(android.Manifest.permission.RECEIVE_SMS) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = ClearSpendColors.Amber500, contentColor = Color(0xFF1A1200)),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Allow Bank SMS", fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
        }
    }
}

@Composable
private fun PageIndicator(current: Int, total: Int, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        repeat(total) { index ->
            val width by animateDpAsState(
                targetValue = if (index == current) 24.dp else 6.dp,
                animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
                label = "indicator"
            )
            Box(
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .height(6.dp)
                    .width(width)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        if (index == current) ClearSpendColors.Amber500
                        else ClearSpendColors.Surface3
                    )
            )
        }
    }
}

@Composable
private fun OnboardingFooter(
    currentPage: Int,
    isLastPage: Boolean,
    onNext: () -> Unit,
    onSkip: () -> Unit,
    canSkip: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (canSkip) {
            OutlinedButton(
                onClick = onSkip,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, ClearSpendColors.Border)
            ) {
                Text("Skip", color = ClearSpendColors.TextSecond)
            }
        }
        Button(
            onClick = onNext,
            modifier = Modifier.weight(if (canSkip) 2f else 1f),
            colors = ButtonDefaults.buttonColors(
                containerColor = ClearSpendColors.Amber500,
                contentColor = Color(0xFF1A1200)
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text(
                text = when (currentPage) {
                    0 -> "Get Started"
                    1 -> "Set Budget"
                    else -> "Let's Go →"
                },
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}

// This import is needed at the top of the file:
private val kotlinx_coroutines_launch = "kotlinx.coroutines"
private val scope_ref = "rememberCoroutineScope"
// → import kotlinx.coroutines.launch and rememberCoroutineScope in actual project
