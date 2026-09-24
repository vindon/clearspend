package com.clearspend.presentation.screens.scan

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.clearspend.domain.model.Category
import com.clearspend.presentation.theme.ClearSpendColors
import com.clearspend.presentation.theme.categoryColor

@Composable
fun ScanScreen(
    onNavigateBack: () -> Unit,
    onTransactionSaved: () -> Unit,
    viewModel: ScanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.savedSuccessfully) {
        if (uiState.savedSuccessfully) {
            onTransactionSaved()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ClearSpendColors.Surface0)
    ) {
        when (uiState.stage) {
            ScanStage.CAMERA -> {
                CameraViewfinder(
                    onBack = onNavigateBack,
                    onSimulateCapture = {
                        // Simulates capturing a receipt text block for instant testing
                        val sampleReceipt = """
                            STARBUCKS COFFEE INDIA
                            STORE #49102, INDIRANAGAR, BANGALORE
                            DATE: 22/09/2026 14:32
                            1x CAFFE LATTE GRANDE   ₹280.00
                            1x BLUEBERRY MUFFIN     ₹160.00
                            SUBTOTAL                ₹440.00
                            CGST 2.5%               ₹11.00
                            SGST 2.5%               ₹11.00
                            TOTAL AMOUNT PAID       ₹462.00
                            PAID VIA VISA CONTACTLESS
                            THANK YOU FOR VISITING!
                        """.trimIndent()
                        viewModel.processOcrText(sampleReceipt)
                    }
                )
            }
            ScanStage.PROCESSING -> {
                ProcessingOverlay()
            }
            ScanStage.CONFIRM -> {
                ConfirmSheet(
                    editState = uiState.editState,
                    onMerchantChange = { viewModel.updateMerchant(it) },
                    onAmountChange = { viewModel.updateAmount(it) },
                    onCategoryChange = { viewModel.updateCategory(it) },
                    onConfirm = { viewModel.saveTransaction() },
                    onRetry = { viewModel.reset() }
                )
            }
            ScanStage.ERROR -> {
                ErrorView(
                    message = uiState.errorMessage ?: "Failed to parse receipt",
                    onRetry = { viewModel.reset() }
                )
            }
        }
    }
}

@Composable
private fun CameraViewfinder(
    onBack: () -> Unit,
    onSimulateCapture: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Darkened Camera background simulation
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF111114)),
            contentAlignment = Alignment.Center
        ) {
            // Animated Receipt Frame
            ReceiptFrame()
        }

        // Top Controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White)
            }

            Surface(
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = "Center receipt in frame",
                    color = Color.White,
                    style = MaterialTheme.typography.labelSmall,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Spacer(modifier = Modifier.size(40.dp))
        }

        // Bottom Shutter Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                onClick = onSimulateCapture,
                shape = CircleShape,
                color = ClearSpendColors.Amber500,
                modifier = Modifier.size(76.dp),
                border = BorderStroke(4.dp, Color.White.copy(alpha = 0.8f))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Rounded.CameraAlt, contentDescription = "Capture", tint = Color.Black, modifier = Modifier.size(32.dp))
                }
            }
        }
    }
}

@Composable
private fun ReceiptFrame() {
    val infiniteTransition = rememberInfiniteTransition(label = "laser")
    val laserY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laserAnimation"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .height(380.dp)
            .border(2.dp, ClearSpendColors.Amber500.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        // Laser scanning line
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(laserY)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            ClearSpendColors.Amber500.copy(alpha = 0.08f),
                            ClearSpendColors.Amber500.copy(alpha = 0.25f)
                        )
                    )
                )
        )
    }
}

@Composable
private fun ProcessingOverlay() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.85f)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = ClearSpendColors.Amber500, strokeWidth = 3.dp)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Analyzing Receipt...", style = MaterialTheme.typography.titleMedium, color = Color.White)
            Text("Extracting merchant, totals, and category on-device", style = MaterialTheme.typography.bodySmall, color = ClearSpendColors.TextMuted)
        }
    }
}

@Composable
private fun ConfirmSheet(
    editState: ScanEditState,
    onMerchantChange: (String) -> Unit,
    onAmountChange: (String) -> Unit,
    onCategoryChange: (Category) -> Unit,
    onConfirm: () -> Unit,
    onRetry: () -> Unit
) {
    Surface(
        color = ClearSpendColors.Surface1,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Confirm Transaction", style = MaterialTheme.typography.headlineMedium, color = ClearSpendColors.TextPrimary)
                IconButton(onClick = onRetry) {
                    Icon(Icons.Rounded.Refresh, contentDescription = "Retry", tint = ClearSpendColors.TextMuted)
                }
            }

            Text(
                "Review extracted data. You can edit any field before saving.",
                style = MaterialTheme.typography.bodySmall,
                color = ClearSpendColors.TextMuted
            )

            // Merchant Field
            OutlinedTextField(
                value = editState.merchant,
                onValueChange = onMerchantChange,
                label = { Text("Merchant / Store") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ClearSpendColors.Amber500,
                    unfocusedBorderColor = ClearSpendColors.Border,
                    focusedTextColor = ClearSpendColors.TextPrimary,
                    unfocusedTextColor = ClearSpendColors.TextPrimary
                )
            )

            // Amount Field
            OutlinedTextField(
                value = editState.amount,
                onValueChange = onAmountChange,
                label = { Text("Amount (₹)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = ClearSpendColors.Amber500,
                    unfocusedBorderColor = ClearSpendColors.Border,
                    focusedTextColor = ClearSpendColors.TextPrimary,
                    unfocusedTextColor = ClearSpendColors.TextPrimary
                )
            )

            // Category Selector
            Text("Category", style = MaterialTheme.typography.titleMedium, color = ClearSpendColors.TextPrimary)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Category.entries.forEach { cat ->
                    val isSelected = editState.category == cat
                    FilterChip(
                        selected = isSelected,
                        onClick = { onCategoryChange(cat) },
                        label = { Text("${cat.emoji} ${cat.displayName}") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = categoryColor(cat).copy(alpha = 0.2f),
                            selectedLabelColor = categoryColor(cat)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = ClearSpendColors.Amber500,
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Transaction", style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Composable
private fun ErrorView(message: String, onRetry: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = ClearSpendColors.RedDanger, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text("Scan Unsuccessful", style = MaterialTheme.typography.titleLarge, color = ClearSpendColors.TextPrimary)
            Text(message, style = MaterialTheme.typography.bodySmall, color = ClearSpendColors.TextMuted)
            Spacer(modifier = Modifier.height(20.dp))
            Button(onClick = onRetry) {
                Text("Try Again")
            }
        }
    }
}
