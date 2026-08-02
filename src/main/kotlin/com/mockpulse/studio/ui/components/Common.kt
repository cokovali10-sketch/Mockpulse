package com.mockpulse.studio.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mockpulse.studio.ui.theme.GlassPanel
import com.mockpulse.studio.ui.theme.MockColors

/**
 * A compact frosted card shown in the status bar: label over a big value.
 */
@Composable
fun MetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    valueColor: Color = MaterialTheme.colorScheme.onSurface,
    accent: Color? = null,
) {
    GlassPanel(
        modifier = modifier.height(54.dp),
        cornerRadius = 14.dp,
        fillAlpha = 0.20f,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (accent != null) {
                Box(
                    Modifier
                        .width(3.dp)
                        .height(26.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(accent),
                )
            }
            Column(verticalArrangement = Arrangement.Center) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MockColors.TextSecondary,
                    maxLines = 1,
                )
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleMedium,
                    color = valueColor,
                    maxLines = 1,
                )
            }
        }
    }
}

/**
 * Section header row with an optional action button on the right.
 */
@Composable
fun SectionHeader(
    title: String,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MockColors.TextPrimary,
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MockColors.TextSecondary,
                )
            }
        }
        if (action != null) action()
    }
    Spacer(modifier = Modifier.height(8.dp))
}

/**
 * A glass sidebar entry that highlights with a neon glow when selected.
 */
@Composable
fun SidebarEntry(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    badge: String? = null,
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (selected) {
                    Modifier
                        .shadow(elevation = 6.dp, shape = shape)
                        .clip(shape)
                        .background(MockColors.Accent.copy(alpha = 0.16f))
                        .border(
                            width = 1.dp,
                            color = MockColors.Accent.copy(alpha = 0.45f),
                            shape = shape,
                        )
                } else {
                    Modifier
                        .clip(shape)
                        .background(Color.White.copy(alpha = 0.04f))
                }
            )
            .clickable(onClick = onClick)
            .height(40.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = if (selected) MockColors.Accent else MockColors.TextSecondary,
            )
        }
        if (badge != null) {
            StatusBadge(
                text = badge,
                color = if (selected) MockColors.Accent else MockColors.TextDisabled,
                modifier = Modifier.padding(end = 10.dp),
            )
        }
    }
}

/**
 * A frosted action button with a translucent neon fill.
 */
@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    tint: Color = MockColors.Accent,
    enabled: Boolean = true,
) {
    Button(
        onClick = { if (enabled) onClick() },
        enabled = enabled,
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (enabled) tint.copy(alpha = 0.22f) else Color.White.copy(alpha = 0.06f),
            contentColor = if (enabled) tint else MockColors.TextDisabled,
            disabledContainerColor = Color.White.copy(alpha = 0.06f),
            disabledContentColor = MockColors.TextDisabled,
        ),
        border = BorderStroke(
            1.dp,
            if (enabled) tint.copy(alpha = 0.45f) else Color.White.copy(alpha = 0.12f),
        ),
        modifier = modifier,
    ) {
        Text(text = text, fontWeight = FontWeight.Bold)
    }
}