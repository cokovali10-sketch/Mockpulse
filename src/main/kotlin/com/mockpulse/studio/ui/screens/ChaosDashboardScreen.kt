package com.mockpulse.studio.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mockpulse.studio.core.model.ChaosConfig
import com.mockpulse.studio.ui.theme.GlassPanel
import com.mockpulse.studio.ui.theme.MockColors
import com.mockpulse.studio.viewmodel.ChaosViewModel

/**
 * Chaos Engineering dashboard: sliders and toggles that mutate the backend's
 * chaos engine configuration live.
 */
@Composable
fun ChaosDashboardScreen(
    viewModel: ChaosViewModel,
    modifier: Modifier = Modifier,
) {
    val config = viewModel.config.value
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            text = "Chaos Engineering",
            style = MaterialTheme.typography.titleMedium,
            color = MockColors.TextPrimary,
        )
        Text(
            text = "Inject latency, errors and drops into the mock traffic. Changes apply live.",
            style = MaterialTheme.typography.bodySmall,
            color = MockColors.TextSecondary,
        )

        // ---- Latency ----
        SectionCard("Latency") {
            LatencyRow(
                label = "Fixed delay (ms)",
                value = config.latencyFixedMs,
                enabled = config.latencyMinMs <= 0 && config.latencyMaxMs <= 0,
                onValue = { viewModel.update { copy(latencyFixedMs = it) } },
                range = 0f..2000f,
            )
            LatencyRow(
                label = "Min (ms)",
                value = config.latencyMinMs,
                enabled = config.latencyFixedMs <= 0,
                onValue = {
                    viewModel.update {
                        copy(
                            latencyMinMs = it,
                            latencyMaxMs = if (latencyMaxMs < it) it else latencyMaxMs,
                        )
                    }
                },
                range = 0f..2000f,
            )
            LatencyRow(
                label = "Max (ms)",
                value = config.latencyMaxMs,
                enabled = config.latencyFixedMs <= 0,
                onValue = {
                    viewModel.update {
                        copy(
                            latencyMaxMs = it,
                            latencyMinMs = if (latencyMinMs > it) it else latencyMinMs,
                        )
                    }
                },
                range = 0f..2000f,
            )
        }

        // ---- Error rate ----
        SectionCard("Errors") {
            PercentSlider(
                label = "Error rate",
                value = config.errorRate,
                onValue = { viewModel.setErrorRate(it) },
            )
        }

        // ---- Drop rate ----
        SectionCard("Drops") {
            PercentSlider(
                label = "Drop rate",
                value = config.dropRate,
                onValue = { viewModel.setDropRate(it) },
            )
        }

        // ---- Reset ----
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            Button(
                onClick = { viewModel.update { ChaosConfig() } },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MockColors.Error,
                    contentColor = Color(0xFF2B0A0A),
                ),
            ) {
                Text("Reset Chaos")
            }
        }

        if (viewModel.lastError.value != null) {
            Text(
                text = "Error: ${viewModel.lastError.value}",
                style = MaterialTheme.typography.bodySmall,
                color = MockColors.Error,
            )
        }
        if (viewModel.saving.value) {
            Text(
                text = "Saving…",
                style = MaterialTheme.typography.labelSmall,
                color = MockColors.TextDisabled,
            )
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable ColumnScope.() -> Unit) {
    GlassPanel(
        modifier = Modifier.fillMaxWidth(),
        cornerRadius = 18.dp,
        fillAlpha = 0.14f,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelLarge,
                color = MockColors.Info,
            )
            content()
        }
    }
}

@Composable
private fun LatencyRow(
    label: String,
    value: Int,
    enabled: Boolean,
    onValue: (Int) -> Unit,
    range: ClosedFloatingPointRange<Float>,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = if (enabled) MockColors.TextPrimary else MockColors.TextDisabled,
            modifier = Modifier.width(140.dp),
        )
        Slider(
            value = value.toFloat().coerceIn(range),
            onValueChange = { onValue(it.toInt()) },
            enabled = enabled,
            valueRange = range,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = value.toString(),
            style = MaterialTheme.typography.labelLarge,
            color = if (enabled) MockColors.Accent else MockColors.TextDisabled,
            modifier = Modifier.width(48.dp),
        )
    }
}

@Composable
private fun PercentSlider(
    label: String,
    value: Double,
    onValue: (Double) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MockColors.TextPrimary,
            modifier = Modifier.width(140.dp),
        )
        Slider(
            value = value.toFloat().coerceIn(0f, 1f),
            onValueChange = { onValue(it.toDouble()) },
            valueRange = 0f..1f,
            modifier = Modifier.weight(1f),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = "${(value * 100).toInt()}%",
            style = MaterialTheme.typography.labelLarge,
            color = MockColors.Warning,
            modifier = Modifier.width(48.dp),
        )
    }
}
