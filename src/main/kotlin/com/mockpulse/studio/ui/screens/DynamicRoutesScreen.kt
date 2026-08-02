package com.mockpulse.studio.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mockpulse.studio.core.model.RouteInfo
import com.mockpulse.studio.ui.components.GlassButton
import com.mockpulse.studio.ui.components.StatusBadge
import com.mockpulse.studio.ui.theme.GlassPanel
import com.mockpulse.studio.ui.theme.MockColors
import com.mockpulse.studio.ui.theme.statusBackground
import com.mockpulse.studio.viewmodel.RoutesViewModel

/**
 * Catalog of registered mock routes loaded from the control plane.
 */
@Composable
fun DynamicRoutesScreen(
    viewModel: RoutesViewModel,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) { viewModel.refresh() }

    Column(
        modifier = modifier.fillMaxSize(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Mock Routes",
                    style = MaterialTheme.typography.titleMedium,
                    color = MockColors.TextPrimary,
                )
                Text(
                    text = "${viewModel.routes.value.size} routes registered in the active spec",
                    style = MaterialTheme.typography.labelSmall,
                    color = MockColors.TextSecondary,
                )
            }
            Button(
                onClick = { viewModel.refresh() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MockColors.Accent.copy(alpha = 0.22f),
                    contentColor = MockColors.Accent,
                ),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MockColors.Accent.copy(alpha = 0.5f)),
            ) {
                Text("Refresh")
            }
        }
        HorizontalDivider(color = Color.White.copy(alpha = 0.10f), thickness = 1.dp)

        GlassPanel(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            cornerRadius = 18.dp,
            fillAlpha = 0.12f,
        ) {
            if (viewModel.loading.value) {
                Text(
                    text = "Loading routes…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MockColors.TextSecondary,
                    modifier = Modifier.padding(16.dp),
                )
            } else if (viewModel.routes.value.isEmpty()) {
                Text(
                    text = "No routes registered.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MockColors.TextDisabled,
                    modifier = Modifier.padding(16.dp),
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(viewModel.routes.value, key = { "${it.method} ${it.path}" }) { route ->
                        RouteRow(route)
                        HorizontalDivider(
                            color = Color.White.copy(alpha = 0.06f),
                            thickness = 1.dp,
                        )
                    }
                }
            }
        }

        if (viewModel.lastError.value != null) {
            Text(
                text = "Error: ${viewModel.lastError.value}",
                style = MaterialTheme.typography.bodySmall,
                color = MockColors.Error,
                modifier = Modifier.padding(16.dp),
            )
        }
    }
}

@Composable
private fun RouteRow(route: RouteInfo) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(6.dp))
            .clickable { }
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = route.method,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = methodColor(route.method),
            modifier = Modifier.width(64.dp),
        )
        Text(
            text = route.path,
            style = MaterialTheme.typography.bodyMedium,
            color = MockColors.TextPrimary,
            modifier = Modifier.weight(1f),
        )
        if (route.deprecated) {
            StatusBadge(
                text = "deprecated",
                color = MockColors.Warning,
                modifier = Modifier.padding(end = 8.dp),
            )
        }
        if (route.statusCodes.isNotEmpty()) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                route.statusCodes.take(4).forEach { code ->
                    StatusBadge(
                        text = code.toString(),
                        color = statusBackground(code),
                    )
                }
                if (route.statusCodes.size > 4) {
                    StatusBadge(
                        text = "+${route.statusCodes.size - 4}",
                        color = MockColors.SurfaceVariant,
                    )
                }
            }
        }
    }
}

private fun methodColor(method: String): Color = when (method) {
    "GET" -> MockColors.Info
    "POST" -> MockColors.Accent
    "PUT", "PATCH" -> MockColors.Warning
    "DELETE" -> MockColors.Error
    else -> MockColors.TextSecondary
}
