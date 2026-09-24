// ─── presentation/theme/Theme.kt ─────────────────────────────────────────────
package com.clearspend.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Brand Colors ──────────────────────────────────────────────────────────────
// ClearSpend palette: deep amber primary, dark surfaces, sharp accent greens

object ClearSpendColors {
    // Primary — warm amber/gold (money, clarity, optimism)
    val Amber500     = Color(0xFFF59E0B)
    val Amber400     = Color(0xFFFBBF24)
    val Amber600     = Color(0xFFD97706)
    val AmberLight   = Color(0xFFFEF3C7)

    // Surfaces — rich near-blacks (not pure black — easier on eyes)
    val Surface0     = Color(0xFF0C0C0E)  // App background
    val Surface1     = Color(0xFF141416)  // Cards
    val Surface2     = Color(0xFF1C1C1F)  // Elevated cards
    val Surface3     = Color(0xFF242428)  // Dialogs

    // Borders & dividers
    val Border       = Color(0xFF2A2A2E)
    val BorderLight  = Color(0xFF3A3A3F)

    // Semantic
    val GreenSuccess = Color(0xFF22C55E)
    val RedDanger    = Color(0xFFEF4444)
    val YellowWarn   = Color(0xFFEAB308)
    val PurpleInfo   = Color(0xFFA855F7)

    // Text
    val TextPrimary  = Color(0xFFF0EEE8)
    val TextSecond   = Color(0xFFA0A0A8)
    val TextMuted    = Color(0xFF6E6C72)

    // Category colors (matches domain model)
    val CatFood      = Color(0xFFF97316)
    val CatTransport = Color(0xFF3B82F6)
    val CatShopping  = Color(0xFFEC4899)
    val CatHealth    = Color(0xFF22C55E)
    val CatBills     = Color(0xFFEAB308)
    val CatEntertain = Color(0xFFA855F7)
    val CatGroceries = Color(0xFF14B8A6)
    val CatTravel    = Color(0xFF06B6D4)
    val CatEmi       = Color(0xFFEF4444)
    val CatOther     = Color(0xFF6B7280)
}

// ── Typography ─────────────────────────────────────────────────────────────────
// Using system font fallback; in production replace with DM Sans from res/font/

val ClearSpendTypography = Typography(
    // Hero numbers (spend totals)
    displayLarge = TextStyle(
        fontWeight = FontWeight.Black,
        fontSize = 48.sp,
        letterSpacing = (-2).sp
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.ExtraBold,
        fontSize = 36.sp,
        letterSpacing = (-1.5).sp
    ),
    // Section headers
    headlineLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        letterSpacing = (-0.5).sp
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        letterSpacing = (-0.3).sp
    ),
    // Card titles
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp
    ),
    // Body text
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        color = ClearSpendColors.TextSecond
    ),
    // Labels and tags
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp,
        letterSpacing = 0.1.sp
    ),
    labelSmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 10.sp,
        letterSpacing = 1.sp
    )
)

// ── Dark Color Scheme (primary theme) ────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary          = ClearSpendColors.Amber500,
    onPrimary        = Color(0xFF1A1200),
    primaryContainer = ClearSpendColors.Amber600.copy(alpha = 0.2f),
    secondary        = ClearSpendColors.GreenSuccess,
    onSecondary      = Color.Black,
    background       = ClearSpendColors.Surface0,
    onBackground     = ClearSpendColors.TextPrimary,
    surface          = ClearSpendColors.Surface1,
    onSurface        = ClearSpendColors.TextPrimary,
    surfaceVariant   = ClearSpendColors.Surface2,
    onSurfaceVariant = ClearSpendColors.TextSecond,
    outline          = ClearSpendColors.Border,
    error            = ClearSpendColors.RedDanger,
    onError          = Color.White
)

// ── Light Color Scheme (optional, for V2) ────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary          = ClearSpendColors.Amber600,
    onPrimary        = Color.White,
    background       = Color(0xFFFAF8F5),
    onBackground     = Color(0xFF1A1A1A),
    surface          = Color.White,
    onSurface        = Color(0xFF1A1A1A),
)

// ── Theme composable ──────────────────────────────────────────────────────────
@Composable
fun ClearSpendTheme(
    darkTheme: Boolean = true,  // Default dark — matches target audience preference
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = ClearSpendTypography,
        content = content
    )
}

// ── Helper extensions ─────────────────────────────────────────────────────────
val MaterialTheme.clearColors: ClearSpendColors get() = ClearSpendColors

fun categoryColor(category: com.clearspend.domain.model.Category): Color = when (category) {
    com.clearspend.domain.model.Category.FOOD         -> ClearSpendColors.CatFood
    com.clearspend.domain.model.Category.TRANSPORT    -> ClearSpendColors.CatTransport
    com.clearspend.domain.model.Category.SHOPPING     -> ClearSpendColors.CatShopping
    com.clearspend.domain.model.Category.HEALTH       -> ClearSpendColors.CatHealth
    com.clearspend.domain.model.Category.BILLS        -> ClearSpendColors.CatBills
    com.clearspend.domain.model.Category.ENTERTAINMENT-> ClearSpendColors.CatEntertain
    com.clearspend.domain.model.Category.GROCERIES    -> ClearSpendColors.CatGroceries
    com.clearspend.domain.model.Category.TRAVEL       -> ClearSpendColors.CatTravel
    com.clearspend.domain.model.Category.EMI          -> ClearSpendColors.CatEmi
    com.clearspend.domain.model.Category.OTHER        -> ClearSpendColors.CatOther
}
