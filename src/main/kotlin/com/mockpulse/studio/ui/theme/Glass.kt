package com.mockpulse.studio.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

/**
 * MockPulse aurora backdrop: a deep gradient canvas with ambient neon colour
 * blobs placed behind the glass panels. Glass cards read as frosted light
 * passing through rather than flat colour.
 */
@Composable
fun AuroraBackground(
    modifier: Modifier = Modifier,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(modifier = modifier.background(MockColors.Background)) {
        // Base diagonal gradient
        Box(
            Modifier
                .matchParentSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0B1020),
                            Color(0xFF1E1B4B),
                            Color(0xFF0B1220),
                        ),
                    ),
                ),
        )
        // Neon light sources
        NeonBlob(
            color = MockColors.Accent,
            size = DpSize(480.dp, 480.dp),
            modifier = Modifier.offset(x = (-120).dp, y = (-80).dp),
        )
        NeonBlob(
            color = MockColors.Info,
            size = DpSize(520.dp, 520.dp),
            modifier = Modifier.offset(x = 520.dp, y = 120.dp),
        )
        NeonBlob(
            color = Color(0xFFA855F7),
            size = DpSize(500.dp, 500.dp),
            modifier = Modifier.offset(x = (-60).dp, y = 420.dp),
        )
        content()
    }
}

/**
 * A blurred radial-gradient disc used as an ambient light source.
 */
@Composable
fun NeonBlob(
    color: Color,
    size: DpSize,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size.width, size.height)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(color.copy(alpha = 0.40f), Color.Transparent),
                ),
            )
            .blur(60.dp),
    )
}

/**
 * A frosted-glass surface: translucent fill, soft highlight border and rounded
 * corners. Use inside [AuroraBackground] so the blobs show through.
 */
@Composable
fun GlassPanel(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    fillAlpha: Float = 0.22f,
    shadowElevation: Dp = 0.dp,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .shadow(elevation = shadowElevation, shape = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius))
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius))
            .background(MockColors.Surface.copy(alpha = fillAlpha))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.14f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius),
            ),
    ) {
        content()
    }
}

/**
 * A frosted pill for chips and badges with a stronger fill for legibility.
 */
@Composable
fun GlassPill(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 10.dp,
    fillAlpha: Float = 0.30f,
    content: @Composable BoxScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .clip(androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius))
            .background(MockColors.SurfaceVariant.copy(alpha = fillAlpha))
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.16f),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(cornerRadius),
            ),
    ) {
        content()
    }
}