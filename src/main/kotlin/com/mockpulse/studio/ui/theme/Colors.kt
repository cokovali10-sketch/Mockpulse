package com.mockpulse.studio.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * MockPulse aurora palette. Neutral surfaces are translucent blue-slate so
 * glass panels pick up the neon backdrop; accent colors map to HTTP outcomes:
 *  2xx -> green, 3xx -> blue, 4xx -> amber, 5xx -> red.
 */
object MockColors {
    val Background = Color(0xFF0B1020)
    val Surface = Color(0xFF16213E)
    val SurfaceElevated = Color(0xFF1E2A4A)
    val SurfaceVariant = Color(0xFF263553)
    val Border = Color(0xFF3A4A6A)

    val TextPrimary = Color(0xFFEBF0FF)
    val TextSecondary = Color(0xFF9FB0CC)
    val TextDisabled = Color(0xFF5D6B87)

    val Accent = Color(0xFF14F1A0)
    val AccentDim = Color(0xFF10B981)
    val Info = Color(0xFF63A4FF)
    val Warning = Color(0xFFFFC24D)
    val Error = Color(0xFFFF5F6D)

    val SuccessBg = Color(0x2214F1A0)
    val InfoBg = Color(0x2263A4FF)
    val WarningBg = Color(0x22FFC24D)
    val ErrorBg = Color(0x22FF5F6D)

    val Link = Color(0xFF8BC0FF)
}

/**
 * Returns the accent color for an HTTP status code and its background tint.
 */
fun statusColor(status: Int): Color = when (status) {
    in 200..299 -> MockColors.Accent
    in 300..399 -> MockColors.Info
    in 400..499 -> MockColors.Warning
    in 500..599 -> MockColors.Error
    else -> MockColors.TextSecondary
}

fun statusBackground(status: Int): Color = when (status) {
    in 200..299 -> MockColors.SuccessBg
    in 300..399 -> MockColors.InfoBg
    in 400..499 -> MockColors.WarningBg
    in 500..599 -> MockColors.ErrorBg
    else -> MockColors.SurfaceVariant.copy(alpha = 0.5f)
}