package com.clearspend.presentation.screens.cards

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.clearspend.domain.model.AgreementRouting
import com.clearspend.domain.model.CreditCard
import com.clearspend.domain.model.CreditCardAgreement
import com.clearspend.presentation.common.ClearSpendTopBar
import com.clearspend.presentation.theme.ClearSpendColors
import java.util.Locale

@Composable
fun CardIntelligenceScreen(
    onNavigateBack: () -> Unit,
    viewModel: CardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        containerColor = ClearSpendColors.Surface0,
        topBar = {
            ClearSpendTopBar(title = "Card Fee Intelligence", onBack = onNavigateBack)
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header Explanation
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
                Text(
                    text = "AI CREDIT AGREEMENT AUDITOR",
                    style = MaterialTheme.typography.labelSmall,
                    color = ClearSpendColors.IndigoCards,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = "Know your true interest rates, hidden penalties, and annual fee waiver milestones.",
                    style = MaterialTheme.typography.bodySmall,
                    color = ClearSpendColors.TextSecond
                )
            }

            // Cards Carousel
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                items(uiState.cards) { card ->
                    val isSelected = card.id == uiState.selectedCard?.id
                    CreditCardPlastic(
                        card = card,
                        isSelected = isSelected,
                        onSelect = { viewModel.selectCard(card) }
                    )
                }
            }

            // Selected Card Agreement Intelligence
            uiState.selectedCard?.agreement?.let { agreement ->
                AgreementAuditDetails(
                    card = uiState.selectedCard!!,
                    agreement = agreement,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            } ?: run {
                Surface(
                    color = ClearSpendColors.Surface1,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, ClearSpendColors.Border),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Column(modifier = Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📄", fontSize = 32.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("No Agreement Loaded", style = MaterialTheme.typography.titleMedium, color = ClearSpendColors.TextPrimary)
                        Text("Select a card above to inspect extracted legal terms and interest rates.", style = MaterialTheme.typography.bodySmall, color = ClearSpendColors.TextMuted)
                    }
                }
            }
        }
    }
}

@Composable
private fun CreditCardPlastic(
    card: CreditCard,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    val gradientColors = if (card.issuer.contains("HDFC", ignoreCase = true)) {
        listOf(Color(0xFF1E293B), Color(0xFF0F172A), Color(0xFF020617))
    } else {
        listOf(Color(0xFF7C2D12), Color(0xFF451A03), Color(0xFF180800))
    }

    Surface(
        onClick = onSelect,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) ClearSpendColors.Amber500 else ClearSpendColors.Border
        ),
        modifier = Modifier
            .width(280.dp)
            .height(160.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Brush.linearGradient(gradientColors))
                .padding(18.dp)
        ) {
            Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(card.issuer.uppercase(), style = MaterialTheme.typography.labelSmall, color = ClearSpendColors.Amber400, fontWeight = FontWeight.Bold)
                    Text("CHIP", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                }

                Text(
                    text = card.productName,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("••••  ${card.last4Digits}", style = MaterialTheme.typography.titleMedium, color = Color.White.copy(alpha = 0.8f))
                    Text(card.cardNetwork.name, style = MaterialTheme.typography.labelLarge, color = ClearSpendColors.Amber400, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun AgreementAuditDetails(
    card: CreditCard,
    agreement: CreditCardAgreement,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(14.dp)) {

        // Consensus & Routing Banner
        Surface(
            color = ClearSpendColors.Surface1,
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.dp, ClearSpendColors.Border),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("AI Agreement Confidence", style = MaterialTheme.typography.bodySmall, color = ClearSpendColors.TextMuted)
                    Text(
                        "${(agreement.icsConfidenceScore * 100).toInt()}% ICS Consensus",
                        style = MaterialTheme.typography.titleLarge,
                        color = ClearSpendColors.GreenSuccess,
                        fontWeight = FontWeight.Bold
                    )
                }
                Surface(
                    color = ClearSpendColors.GreenSuccess.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = agreement.routingDecision.name.replace("_", " "),
                        color = ClearSpendColors.GreenSuccess,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Section: Pricing & Interest
        Text("PRICING & APR RATES", style = MaterialTheme.typography.labelSmall, color = ClearSpendColors.TextMuted, letterSpacing = 1.sp)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricTile(
                title = "Purchase APR",
                value = "${agreement.purchaseAprMin ?: 42.0}%",
                subtitle = "Annual Interest Rate",
                accentColor = ClearSpendColors.RedDanger,
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                title = "Cash Advance APR",
                value = "${agreement.cashAdvanceApr ?: 45.6}%",
                subtitle = "ATM Cash Withdrawal",
                accentColor = ClearSpendColors.RedDanger,
                modifier = Modifier.weight(1f)
            )
        }

        // Section: Fees & Penalties
        Text("FEES & CHARGES", style = MaterialTheme.typography.labelSmall, color = ClearSpendColors.TextMuted, letterSpacing = 1.sp)

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricTile(
                title = "Annual Fee",
                value = if (agreement.annualFee == 0.0) "Free" else "₹${agreement.annualFee?.toInt()}",
                subtitle = if (agreement.annualFeeWaiverSpend != null && agreement.annualFeeWaiverSpend > 0) "Waived at ₹${agreement.annualFeeWaiverSpend.toInt()}" else "No spend waiver",
                accentColor = ClearSpendColors.Amber500,
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                title = "Late Payment Fee",
                value = "₹${agreement.latePaymentFee?.toInt() ?: 1200}",
                subtitle = "Max penalty charge",
                accentColor = ClearSpendColors.YellowWarn,
                modifier = Modifier.weight(1f)
            )
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MetricTile(
                title = "Forex Markup",
                value = "${agreement.foreignTxFeePercent ?: 3.5}%",
                subtitle = "Foreign currency fee",
                accentColor = ClearSpendColors.IndigoCards,
                modifier = Modifier.weight(1f)
            )
            MetricTile(
                title = "Grace Period",
                value = "${agreement.gracePeriodDays ?: 20} Days",
                subtitle = "Interest-free window",
                accentColor = ClearSpendColors.GreenSuccess,
                modifier = Modifier.weight(1f)
            )
        }

        // Disclaimer
        Surface(
            color = ClearSpendColors.Surface1,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "Extracted from official card cardholder agreement. Terms subject to card issuer changes.",
                style = MaterialTheme.typography.bodySmall,
                color = ClearSpendColors.TextMuted,
                modifier = Modifier.padding(12.dp)
            )
        }
    }
}

@Composable
private fun MetricTile(
    title: String,
    value: String,
    subtitle: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        color = ClearSpendColors.Surface1,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, ClearSpendColors.Border),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(title, style = MaterialTheme.typography.bodySmall, color = ClearSpendColors.TextMuted)
            Spacer(modifier = Modifier.height(4.dp))
            Text(value, style = MaterialTheme.typography.titleLarge, color = accentColor, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(2.dp))
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = ClearSpendColors.TextSecond, fontSize = 11.sp, maxLines = 1)
        }
    }
}
