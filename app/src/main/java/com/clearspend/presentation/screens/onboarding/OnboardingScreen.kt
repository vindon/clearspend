package com.clearspend.presentation.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.clearspend.presentation.theme.ClearSpendColors
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onComplete: (monthlyBudget: Double?) -> Unit
) {
    val pagerState = rememberPagerState(pageCount = { 3 })
    val scope = rememberCoroutineScope()
    var monthlyBudget by remember { mutableStateOf("50000") }

    Scaffold(containerColor = ClearSpendColors.Surface0) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            // Page Indicator Dots
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp, bottom = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                repeat(3) { index ->
                    val isSelected = pagerState.currentPage == index
                    Box(
                        modifier = Modifier
                            .padding(4.dp)
                            .height(6.dp)
                            .width(if (isSelected) 24.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) ClearSpendColors.Amber500 else ClearSpendColors.Border)
                    )
                }
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier.weight(1f),
                userScrollEnabled = false
            ) { page ->
                when (page) {
                    0 -> WelcomePage(
                        onNext = { scope.launch { pagerState.animateScrollToPage(1) } }
                    )
                    1 -> BudgetPage(
                        budget = monthlyBudget,
                        onBudgetChange = { monthlyBudget = it },
                        onNext = { scope.launch { pagerState.animateScrollToPage(2) } },
                        onSkip = { scope.launch { pagerState.animateScrollToPage(2) } }
                    )
                    2 -> PrivacyPermissionPage(
                        onFinish = { onComplete(monthlyBudget.toDoubleOrNull()) }
                    )
                }
            }
        }
    }
}

@Composable
private fun WelcomePage(onNext: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(48.dp))
            Text("💰", fontSize = 64.sp)
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Welcome to ClearSpend",
                style = MaterialTheme.typography.displayMedium,
                color = ClearSpendColors.TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Intelligent, 100% offline-first expense tracking, receipt scanner, and credit card fee auditor.",
                style = MaterialTheme.typography.bodyLarge,
                color = ClearSpendColors.TextSecond,
                textAlign = TextAlign.Center
            )
        }

        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ClearSpendColors.Amber500, contentColor = Color.Black),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Get Started", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun BudgetPage(
    budget: String,
    onBudgetChange: (String) -> Unit,
    onNext: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(48.dp))
            Text("🎯", fontSize = 56.sp)
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "Set Monthly Budget",
                style = MaterialTheme.typography.headlineLarge,
                color = ClearSpendColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "ClearSpend will track your safe-to-spend pace and alert you before you exceed your target envelope.",
                style = MaterialTheme.typography.bodyMedium,
                color = ClearSpendColors.TextSecond,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = budget,
                onValueChange = onBudgetChange,
                label = { Text("Monthly Budget (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ClearSpendColors.Amber500,
                    unfocusedBorderColor = ClearSpendColors.Border,
                    focusedTextColor = ClearSpendColors.TextPrimary,
                    unfocusedTextColor = ClearSpendColors.TextPrimary
                )
            )
        }

        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ClearSpendColors.Amber500, contentColor = Color.Black),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Continue", style = MaterialTheme.typography.titleMedium)
            }
            Spacer(modifier = Modifier.height(10.dp))
            TextButton(onClick = onSkip) {
                Text("Skip for now", color = ClearSpendColors.TextMuted)
            }
        }
    }
}

@Composable
private fun PrivacyPermissionPage(onFinish: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(32.dp))
            Icon(Icons.Rounded.Shield, contentDescription = null, tint = ClearSpendColors.GreenSuccess, modifier = Modifier.size(56.dp))
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Institutional Privacy",
                style = MaterialTheme.typography.headlineLarge,
                color = ClearSpendColors.TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Your bank SMS alerts are processed 100% on your device with zero cloud exfiltration.",
                style = MaterialTheme.typography.bodyMedium,
                color = ClearSpendColors.TextSecond,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))

            Surface(
                color = ClearSpendColors.Surface1,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    PrivacyBullet("100% On-Device Parsing — zero SMS uploaded")
                    PrivacyBullet("Encrypted Local SQLite Database with SQLCipher")
                    PrivacyBullet("1-Click Data Wipe at any time in Settings")
                }
            }
        }

        Button(
            onClick = onFinish,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = ClearSpendColors.Amber500, contentColor = Color.Black),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Enter ClearSpend", style = MaterialTheme.typography.titleMedium)
        }
    }
}

@Composable
private fun PrivacyBullet(text: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = ClearSpendColors.GreenSuccess, modifier = Modifier.size(18.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = ClearSpendColors.TextPrimary)
    }
}
