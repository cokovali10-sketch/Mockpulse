package com.mockpulse.studio.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColors = darkColorScheme(
    primary = MockColors.Accent,
    onPrimary = Color(0xFF06281B),
    primaryContainer = MockColors.SurfaceVariant,
    onPrimaryContainer = MockColors.TextPrimary,
    secondary = MockColors.Info,
    onSecondary = Color(0xFF0B1F3A),
    secondaryContainer = MockColors.SurfaceVariant,
    onSecondaryContainer = MockColors.TextPrimary,
    tertiary = MockColors.Warning,
    onTertiary = Color(0xFF2A1E00),
    error = MockColors.Error,
    onError = Color(0xFF2B0A0A),
    background = MockColors.Background,
    onBackground = MockColors.TextPrimary,
    surface = MockColors.Surface,
    onSurface = MockColors.TextPrimary,
    surfaceVariant = MockColors.SurfaceVariant,
    onSurfaceVariant = MockColors.TextSecondary,
    outline = MockColors.Border,
    outlineVariant = MockColors.SurfaceVariant,
)

@Composable
fun MockPulseTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColors,
        typography = MockTypography,
        content = content,
    )
}
