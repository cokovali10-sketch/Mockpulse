package com.mockpulse.studio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mockpulse.studio.core.model.BreakpointEvent
import com.mockpulse.studio.core.model.ModifiedResponse
import com.mockpulse.studio.core.model.ResolveCommand
import com.mockpulse.studio.core.util.formatTimestamp
import com.mockpulse.studio.ui.theme.MockColors
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

/**
 * Modal shown when a request is paused at a breakpoint. The operator chooses
 * drop, pass, or modify (editing the response body before resolution).
 */
@Composable
fun BreakpointInterceptorDialog(
    event: BreakpointEvent,
    onResolve: (ResolveCommand) -> Unit,
    onDismiss: () -> Unit,
) {
    var decision by remember { mutableStateOf("pass") }
    var statusText by remember { mutableStateOf("200") }
    var bodyText by remember { mutableStateOf("{}") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MockColors.SurfaceElevated,
        title = {
            Text(
                text = "Breakpoint",
                style = MaterialTheme.typography.titleMedium,
                color = MockColors.Warning,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 480.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                // Request summary
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(
                        text = event.method,
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MockColors.Info,
                    )
                    Text(
                        text = event.path,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MockColors.TextPrimary,
                    )
                    Text(
                        text = formatTimestamp(event.arrivedAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MockColors.TextDisabled,
                    )
                }

                if (event.headers.isNotEmpty()) {
                    Text(
                        text = event.headers.entries.joinToString("\n") { "${it.key}: ${it.value}" },
                        style = MaterialTheme.typography.bodySmall,
                        color = MockColors.TextSecondary,
                        modifier = Modifier
                            .horizontalScroll(rememberScrollState()),
                    )
                }

                if (event.body.isNotBlank()) {
                    Text(
                        text = "Request Body",
                        style = MaterialTheme.typography.labelLarge,
                        color = MockColors.Info,
                    )
                    JsonTreeViewer(event.body)
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Decision",
                    style = MaterialTheme.typography.labelLarge,
                    color = MockColors.TextPrimary,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DecisionChip("pass", decision == "pass") { decision = "pass" }
                    DecisionChip("drop", decision == "drop") { decision = "drop" }
                    DecisionChip("modify", decision == "modify") { decision = "modify" }
                }

                if (decision == "modify") {
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = statusText,
                        onValueChange = { statusText = it },
                        label = { Text("Status") },
                        singleLine = true,
                        modifier = Modifier.width(120.dp),
                    )
                    OutlinedTextField(
                        value = bodyText,
                        onValueChange = { bodyText = it },
                        label = { Text("Response Body (JSON)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp),
                    )
                    Text(
                        text = "The request will be answered with this payload.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MockColors.TextDisabled,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onResolve(
                        ResolveCommand(
                            id = event.id,
                            decision = decision,
                            response = if (decision == "modify") {
                                ModifiedResponse(
                                    status = statusText.toIntOrNull(),
                                    body = runCatching {
                                        kotlinx.serialization.json.Json.parseToJsonElement(bodyText).jsonObject
                                    }.getOrNull(),
                                )
                            } else null,
                        ),
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MockColors.Warning,
                    contentColor = Color(0xFF2A1E00),
                ),
            ) {
                Text(decision.uppercase())
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Dismiss", color = MockColors.TextSecondary)
            }
        },
    )
}

@Composable
private fun DecisionChip(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (selected) MockColors.SurfaceVariant else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            color = if (selected) MockColors.Warning else MockColors.TextSecondary,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        )
    }
}
