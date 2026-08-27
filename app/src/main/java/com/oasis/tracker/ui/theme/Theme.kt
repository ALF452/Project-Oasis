package com.oasis.tracker.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val OasisColorScheme = darkColorScheme(
    primary = NeonBlue,
    onPrimary = CharcoalBackground,
    secondary = NeonBlueDim,
    onSecondary = CharcoalBackground,
    background = CharcoalBackground,
    onBackground = TextPrimary,
    surface = CharcoalSurface,
    onSurface = TextPrimary,
    surfaceVariant = CharcoalSurfaceRaised,
    onSurfaceVariant = TextSecondary,
    error = DangerMagenta,
    onError = CharcoalBackground,
    outline = NeonBlueDim
)

@Composable
fun OasisTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = OasisColorScheme,
        typography = OasisTypography,
        content = content
    )
}
