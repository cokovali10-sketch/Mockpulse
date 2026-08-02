package com.mockpulse.studio.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import com.mockpulse.studio.ui.components.StatusBadge
import com.mockpulse.studio.ui.theme.GlassPanel
import com.mockpulse.studio.ui.theme.MockColors
import com.mockpulse.studio.viewmodel.StateViewModel

/**
 * Inspects the backend's in-memory state: recorded mock exchanges stored as
 * key/value entries.
 */
@Composable
fun StateStorageScreen(
    viewModel: StateViewModel,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(Unit) { viewModel.refresh() }

    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "State & Recordings",
                    style = MaterialTheme.typography.titleMedium,
                    color = MockColors.TextPrimary,
                )
                Text(
                    text = "${viewModel.recordings.value.count} recorded exchanges",
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
                modifier = Modifier.padding(end = 8.dp),
            ) {
                Text("Refresh")
            }
            Button(
                onClick = { viewModel.clear() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MockColors.Error.copy(alpha = 0.22f),
                    contentColor = MockColors.Error,
                ),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MockColors.Error.copy(alpha = 0.5f)),
            ) {
                Text("Clear All")
            }
        }
        HorizontalDivider(color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.10f), thickness = 1.dp)

        GlassPanel(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            cornerRadius = 18.dp,
            fillAlpha = 0.12f,
        ) {
            if (viewModel.loading.value) {
                Text(
                    text = "Loading…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MockColors.TextSecondary,
                    modifier = Modifier.padding(16.dp),
                )
            } else if (viewModel.recordings.value.keys.isEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "No recorded exchanges",
                        style = MaterialTheme.typography.titleSmall,
                        color = MockColors.TextSecondary,
                    )
                    Text(
                        text = "Mocked responses are recorded automatically for later replay",
                        style = MaterialTheme.typography.labelSmall,
                        color = MockColors.TextDisabled,
                    )
                }
            } else {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    items(viewModel.recordings.value.keys) { key ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            StatusBadge(
                                text = "KV",
                                color = MockColors.Info,
                            )
                            Text(
                                text = key,
                                style = MaterialTheme.typography.bodySmall,
                                color = MockColors.TextPrimary,
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 10.dp),
                            )
                        }
                        HorizontalDivider(
                            color = androidx.compose.ui.graphics.Color.White.copy(alpha = 0.06f),
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
