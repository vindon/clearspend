package com.clearspend.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.clearspend.domain.model.Category

// ── Brand Colors (Obsidian & Gold Luxury Fintech Palette) ────────────────────────

object ClearSpendColors {
    // Primary Amber/Gold
    val Amber500     = Color(0xFFF59E0B)
    val Amber400     = Color(0xFFFBBF24)
    val Amber600     = Color(0xFFD97706)
    val AmberLight   = Color(0xFFFEF3C7)

    // Dark Obsidian Surfaces
    val Surface0     = Color(0xFF0C0C0E)  // Main app background
    val Surface1     = Color(0xFF141416)  // Base cards
    val Surface2     = Color(0xFF1C1C1F)  // Elevated components
    val Surface3     = Color(0xFF242428)  // Dialogs & sheets

    // Hairline Borders
    val Border       = Color(0xFF2A2A2E)
    val BorderLight  = Color(0xFF3A3A3F)

    // Semantic Accents
    val GreenSuccess = Color(0xFF22C55E)
    val RedDanger    = Color(0xFFEF4444)
    val YellowWarn   = Color(0xFFEAB308)
    val IndigoCards  = Color(0xFF6366F1)
    val PurpleInfo   = Color(0xFFA855F7)

    // Text Tokens
    val TextPrimary  = Color(0xFFF0EEE8)
    val TextSecond   = Color(0xFFA0A0A8)
    val TextMuted    = Color(0xFF6E6C72)

    // Category Specific Colors
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

val ClearSpendTypography = Typography(
    displayLarge = TextStyle(
        fontWeight = FontWeight.Black,
        fontSize = 48.sp,
        letterSpacing = (-2).sp
    ),
    displayMedium = TextStyle(
        fontWeight = FontWeight.ExtraBold,
        fontSize = 34.sp,
        letterSpacing = (-1.2).sp
    ),
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
    titleLarge = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 17.sp
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp
    ),
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

@Composable
fun ClearSpendTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        typography = ClearSpendTypography,
        content = content
    )
}

fun categoryColor(category: Category): Color = when (category) {
    Category.FOOD          -> ClearSpendColors.CatFood
    Category.TRANSPORT     -> ClearSpendColors.CatTransport
    Category.SHOPPING      -> ClearSpendColors.CatShopping
    Category.HEALTH        -> ClearSpendColors.CatHealth
    Category.BILLS         -> ClearSpendColors.CatBills
    Category.ENTERTAINMENT -> ClearSpendColors.CatEntertain
    Category.GROCERIES     -> ClearSpendColors.CatGroceries
    Category.TRAVEL        -> ClearSpendColors.CatTravel
    Category.EMI           -> ClearSpendColors.CatEmi
    Category.OTHER         -> ClearSpendColors.CatOther
}
