package com.oasis.tracker.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val HudFont = FontFamily.Monospace

val OasisTypography = Typography(
    headlineLarge = TextStyle(
        fontFamily = HudFont,
        fontWeight = FontWeight.Bold,
        fontSize = 30.sp,
        letterSpacing = 1.5.sp,
        color = NeonBlue
    ),
    headlineMedium = TextStyle(
        fontFamily = HudFont,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        letterSpacing = 1.2.sp,
        color = NeonBlue
    ),
    titleLarge = TextStyle(
        fontFamily = HudFont,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        letterSpacing = 1.sp,
        color = TextPrimary
    ),
    titleMedium = TextStyle(
        fontFamily = HudFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        letterSpacing = 0.8.sp,
        color = TextPrimary
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 16.sp,
        color = TextPrimary
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontSize = 14.sp,
        color = TextSecondary
    ),
    labelLarge = TextStyle(
        fontFamily = HudFont,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp,
        letterSpacing = 1.sp,
        color = NeonBlue
    )
)
