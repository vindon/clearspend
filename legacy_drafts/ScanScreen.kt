// ─── presentation/screens/scan/ScanScreen.kt ─────────────────────────────────
package com.clearspend.presentation.screens.scan

import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.*
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.*
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.clearspend.domain.model.*
import com.clearspend.presentation.theme.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

// ── Screen Entry Point ────────────────────────────────────────────────────────

@Composable
fun ScanScreen(
    onNavigateBack: () -> Unit,
    onTransactionSaved: () -> Unit,
    viewModel: ScanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    ScanScreenContent(
        uiState = uiState,
        onCapture = { imageProxy -> viewModel.processImage(imageProxy) },
        onConfirmSave = { viewModel.saveTransaction() },
        onEditField = { field, value -> viewModel.editField(field, value) },
        onRetry = { viewModel.reset() },
        onBack = onNavigateBack
    )

    // Navigate back after successful save
    LaunchedEffect(uiState.savedSuccessfully) {
        if (uiState.savedSuccessfully) onTransactionSaved()
    }
}

// ── Scan States ────────────────────────────────────────────────────────────────

@Composable
private fun ScanScreenContent(
    uiState: ScanUiState,
    onCapture: (ImageProxy) -> Unit,
    onConfirmSave: () -> Unit,
    onEditField: (ScanField, String) -> Unit,
    onRetry: () -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        when (uiState.stage) {
            ScanStage.CAMERA -> {
                CameraViewfinder(
                    onCapture = onCapture,
                    onBack = onBack
                )
            }
            ScanStage.PROCESSING -> {
                ProcessingOverlay()
            }
            ScanStage.CONFIRM -> {
                uiState.parsedResult?.let { result ->
                    ConfirmSheet(
                        result = result,
                        editState = uiState.editState,
                        onEditField = onEditField,
                        onConfirm = onConfirmSave,
                        onRetry = onRetry
                    )
                }
            }
            ScanStage.ERROR -> {
                ErrorState(
                    message = uiState.errorMessage ?: "Something went wrong",
                    onRetry = onRetry,
                    onBack = onBack
                )
            }
        }
    }
}

// ── Camera Viewfinder ─────────────────────────────────────────────────────────

@Composable
private fun CameraViewfinder(
    onCapture: (ImageProxy) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }

    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    val previewView = remember { PreviewView(context) }

    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.surfaceProvider = previewView.surfaceProvider
            }

            val capture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
            imageCapture = capture

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, capture)
            } catch (e: Exception) {
                // Handle camera bind error
            }
        }, ContextCompat.getMainExecutor(context))
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera preview
        AndroidView(
            factory = { previewView },
            modifier = Modifier.fillMaxSize()
        )

        // Receipt frame overlay
        ReceiptFrameOverlay()

        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    Icons.Rounded.Close,
                    contentDescription = "Close",
                    tint = Color.White,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                        .padding(4.dp)
                )
            }
            Spacer(Modifier.weight(1f))
            Text(
                text = "Scan Receipt",
                color = Color.White,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                    .padding(horizontal = 12.dp, vertical = 4.dp)
            )
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.size(48.dp)) // Balance the close button
        }

        // Hint text
        Text(
            text = "Position receipt within the frame",
            color = Color.White.copy(alpha = 0.8f),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = (140).dp)
                .background(Color.Black.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 6.dp)
        )

        // Capture button
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(bottom = 40.dp)
        ) {
            CaptureButton {
                val capture = imageCapture ?: return@CaptureButton
                capture.takePicture(
                    cameraExecutor,
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            onCapture(image)
                        }
                        override fun onError(exception: ImageCaptureException) {
                            // Error handled in ViewModel
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun ReceiptFrameOverlay() {
    val animatedAlpha by rememberInfiniteTransition(label = "frame").animateFloat(
        initialValue = 0.6f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            tween(1200, easing = EaseInOutSine),
            RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        val frameW = size.width * 0.85f
        val frameH = size.height * 0.55f
        val left = (size.width - frameW) / 2
        val top = (size.height - frameH) / 2

        // Dark overlay outside frame
        drawRect(Color.Black.copy(alpha = 0.55f))

        // Clear the frame area
        drawRoundRect(
            color = Color.Transparent,
            topLeft = androidx.compose.ui.geometry.Offset(left, top),
            size = androidx.compose.ui.geometry.Size(frameW, frameH),
            cornerRadius = CornerRadius(12.dp.toPx()),
            blendMode = BlendMode.Clear
        )

        // Animated border
        drawRoundRect(
            color = Color(0xFFF59E0B).copy(alpha = animatedAlpha),
            topLeft = androidx.compose.ui.geometry.Offset(left, top),
            size = androidx.compose.ui.geometry.Size(frameW, frameH),
            cornerRadius = CornerRadius(12.dp.toPx()),
            style = Stroke(width = 2.dp.toPx())
        )

        // Corner accents
        val cornerLen = 24.dp.toPx()
        val corners = listOf(
            Triple(left, top, Pair(1f, 1f)),
            Triple(left + frameW, top, Pair(-1f, 1f)),
            Triple(left, top + frameH, Pair(1f, -1f)),
            Triple(left + frameW, top + frameH, Pair(-1f, -1f))
        )
        corners.forEach { (x, y, dir) ->
            drawLine(Color(0xFFF59E0B), start = androidx.compose.ui.geometry.Offset(x, y), end = androidx.compose.ui.geometry.Offset(x + cornerLen * dir.first, y), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
            drawLine(Color(0xFFF59E0B), start = androidx.compose.ui.geometry.Offset(x, y), end = androidx.compose.ui.geometry.Offset(x, y + cornerLen * dir.second), strokeWidth = 3.dp.toPx(), cap = StrokeCap.Round)
        }
    }
}

@Composable
private fun CaptureButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(CircleShape)
            .background(ClearSpendColors.Amber500)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            Icons.Rounded.CameraAlt,
            contentDescription = "Capture",
            tint = Color(0xFF1A1200),
            modifier = Modifier.size(32.dp)
        )
    }
}

// ── Processing Overlay ────────────────────────────────────────────────────────

@Composable
private fun ProcessingOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "processing")
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1000, easing = LinearEasing)),
        label = "rotation"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ClearSpendColors.Surface0),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(56.dp),
            color = ClearSpendColors.Amber500,
            strokeWidth = 3.dp
        )
        Spacer(Modifier.height(24.dp))
        Text(
            "Reading receipt...",
            style = MaterialTheme.typography.titleMedium,
            color = ClearSpendColors.TextPrimary
        )
        Text(
            "Extracting merchant & amount",
            style = MaterialTheme.typography.bodySmall,
            color = ClearSpendColors.TextMuted,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

// ── Confirm Sheet ─────────────────────────────────────────────────────────────

@Composable
private fun ConfirmSheet(
    result: ParsedReceiptState,
    editState: EditState,
    onEditField: (ScanField, String) -> Unit,
    onConfirm: () -> Unit,
    onRetry: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ClearSpendColors.Surface0)
    ) {
        // Success header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(ClearSpendColors.GreenSuccess.copy(alpha = 0.15f), ClearSpendColors.Surface0)
                    )
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(ClearSpendColors.GreenSuccess.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Rounded.CheckCircle, null, tint = ClearSpendColors.GreenSuccess, modifier = Modifier.size(32.dp))
                }
                Spacer(Modifier.height(12.dp))
                Text("Receipt Scanned", style = MaterialTheme.typography.titleLarge, color = ClearSpendColors.TextPrimary)
                Text("Review and confirm details", style = MaterialTheme.typography.bodySmall, color = ClearSpendColors.TextMuted)
            }
        }

        // Editable fields
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Merchant
            EditableField(
                label = "MERCHANT",
                value = editState.merchant,
                onValueChange = { onEditField(ScanField.MERCHANT, it) }
            )

            // Amount
            EditableField(
                label = "AMOUNT (₹)",
                value = editState.amount,
                onValueChange = { onEditField(ScanField.AMOUNT, it) },
                keyboardType = KeyboardType.Decimal
            )

            // Category
            CategorySelector(
                selected = editState.category,
                onSelect = { onEditField(ScanField.CATEGORY, it.name) }
            )

            // Note (optional)
            EditableField(
                label = "NOTE (optional)",
                value = editState.note,
                onValueChange = { onEditField(ScanField.NOTE, it) },
                placeholder = "e.g. business dinner"
            )
        }

        // Action buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onRetry,
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, ClearSpendColors.Border)
            ) {
                Icon(Icons.Rounded.Refresh, null, modifier = Modifier.size(16.dp), tint = ClearSpendColors.TextSecond)
                Spacer(Modifier.width(6.dp))
                Text("Retry", color = ClearSpendColors.TextSecond)
            }
            Button(
                onClick = onConfirm,
                modifier = Modifier.weight(2f),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = ClearSpendColors.Amber500, contentColor = Color(0xFF1A1200))
            ) {
                Icon(Icons.Rounded.Check, null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(6.dp))
                Text("Save Transaction", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun EditableField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = ClearSpendColors.TextMuted,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { if (placeholder.isNotEmpty()) Text(placeholder, color = ClearSpendColors.TextMuted) },
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            singleLine = true,
            shape = RoundedCornerShape(10.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = ClearSpendColors.Amber500,
                unfocusedBorderColor = ClearSpendColors.Border,
                focusedTextColor = ClearSpendColors.TextPrimary,
                unfocusedTextColor = ClearSpendColors.TextPrimary,
                cursorColor = ClearSpendColors.Amber500,
                unfocusedContainerColor = ClearSpendColors.Surface2,
                focusedContainerColor = ClearSpendColors.Surface2
            )
        )
    }
}

@Composable
private fun CategorySelector(
    selected: Category,
    onSelect: (Category) -> Unit
) {
    Column {
        Text(
            text = "CATEGORY",
            style = MaterialTheme.typography.labelSmall,
            color = ClearSpendColors.TextMuted,
            letterSpacing = 1.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        // Wrap in a scrollable row for compact display
        androidx.compose.foundation.lazy.LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(Category.entries.size) { i ->
                val cat = Category.entries[i]
                val isSelected = cat == selected
                val color = categoryColor(cat)
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isSelected) color.copy(alpha = 0.2f) else ClearSpendColors.Surface2,
                    border = BorderStroke(1.dp, if (isSelected) color else ClearSpendColors.Border),
                    modifier = Modifier.clickable { onSelect(cat) }
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(cat.emoji, fontSize = 18.sp)
                        Text(
                            cat.displayName.take(7),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) color else ClearSpendColors.TextMuted,
                            fontSize = 9.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ErrorState(message: String, onRetry: () -> Unit, onBack: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("😕", fontSize = 48.sp)
        Spacer(Modifier.height(16.dp))
        Text("Couldn't read receipt", style = MaterialTheme.typography.titleMedium, color = ClearSpendColors.TextPrimary)
        Text(message, style = MaterialTheme.typography.bodySmall, color = ClearSpendColors.TextMuted, modifier = Modifier.padding(top = 8.dp))
        Spacer(Modifier.height(24.dp))
        Button(onClick = onRetry, colors = ButtonDefaults.buttonColors(containerColor = ClearSpendColors.Amber500, contentColor = Color(0xFF1A1200)), shape = RoundedCornerShape(12.dp)) {
            Text("Try Again", fontWeight = FontWeight.Bold)
        }
        TextButton(onClick = onBack) {
            Text("Enter Manually", color = ClearSpendColors.TextSecond)
        }
    }
}

// ── State Types ───────────────────────────────────────────────────────────────

enum class ScanStage { CAMERA, PROCESSING, CONFIRM, ERROR }
enum class ScanField { MERCHANT, AMOUNT, CATEGORY, NOTE }

data class ParsedReceiptState(
    val merchant: String,
    val amount: Double,
    val category: Category,
    val rawText: String
)

data class EditState(
    val merchant: String = "",
    val amount: String = "",
    val category: Category = Category.OTHER,
    val note: String = ""
)

data class ScanUiState(
    val stage: ScanStage = ScanStage.CAMERA,
    val parsedResult: ParsedReceiptState? = null,
    val editState: EditState = EditState(),
    val errorMessage: String? = null,
    val savedSuccessfully: Boolean = false
)
