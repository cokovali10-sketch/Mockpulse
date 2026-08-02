package com.mockpulse.studio.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mockpulse.studio.core.model.TrafficEvent
import com.mockpulse.studio.core.util.formatBytes
import com.mockpulse.studio.core.util.formatLatency
import com.mockpulse.studio.core.util.formatTimestamp
import com.mockpulse.studio.ui.components.GlassButton
import com.mockpulse.studio.ui.components.JsonTreeViewer
import com.mockpulse.studio.ui.components.StatusBadge
import com.mockpulse.studio.ui.theme.GlassPanel
import com.mockpulse.studio.ui.theme.MockColors
import com.mockpulse.studio.ui.theme.statusBackground
import com.mockpulse.studio.ui.theme.statusColor

/**
 * Live traffic inspector with filters: a virtualized table of captured
 * exchanges plus a frosted right-hand detail drawer for the selected row.
 */
@Composable
fun TrafficInspectorScreen(
    events: List<TrafficEvent>,
    totalEvents: Int,
    selected: TrafficEvent?,
    onSelect: (TrafficEvent?) -> Unit,
    onClear: () -> Unit,
    paused: Boolean,
    methodFilter: String?,
    statusFilter: Int?,
    filterText: String,
    onMethodFilter: (String?) -> Unit,
    onStatusFilter: (Int?) -> Unit,
    onFilterText: (String) -> Unit,
    onClearFilters: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxSize()) {
            // ---- Traffic table ----
            Column(modifier = Modifier.weight(1f).fillMaxHeight()) {
                TrafficHeader(
                    paused = paused,
                    total = totalEvents,
                    shown = events.size,
                    methodFilter = methodFilter,
                    statusFilter = statusFilter,
                    filterText = filterText,
                    onMethodFilter = onMethodFilter,
                    onStatusFilter = onStatusFilter,
                    onFilterText = onFilterText,
                    onClearFilters = onClearFilters,
                    onClear = onClear,
                )
                Spacer(modifier = Modifier.height(8.dp))
if (events.isEmpty()) {
                    GlassPanel(
                        modifier = Modifier.fillMaxSize(),
                        cornerRadius = 18.dp,
                        fillAlpha = 0.10f,
                    ) {
                        EmptyTraffic()
                    }
                } else {
                    GlassPanel(
                        modifier = Modifier.fillMaxSize(),
                        cornerRadius = 18.dp,
                        fillAlpha = 0.10f,
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(events, key = { it.id }) { event ->
                                TrafficRow(
                                    event = event,
                                    selected = event.id == selected?.id,
                                    onClick = { onSelect(if (event.id == selected?.id) null else event) },
                                )
                                HorizontalDivider(
                                    color = Color.White.copy(alpha = 0.06f),
                                    thickness = 1.dp,
                                )
                            }
                        }
                    }
                }
            }

            // ---- Detail drawer ----
            if (selected != null) {
                GlassPanel(
                    modifier = Modifier
                        .width(440.dp)
                        .fillMaxHeight()
                        .padding(start = 12.dp),
                    cornerRadius = 18.dp,
                    fillAlpha = 0.16f,
                ) {
                    TrafficDetailDrawer(
                        event = selected,
                        onClose = { onSelect(null) },
                    )
                }
            }
        }
    }
}

@Composable
private fun TrafficHeader(
    paused: Boolean,
    total: Int,
    shown: Int,
    methodFilter: String?,
    statusFilter: Int?,
    filterText: String,
    onMethodFilter: (String?) -> Unit,
    onStatusFilter: (Int?) -> Unit,
    onFilterText: (String) -> Unit,
    onClearFilters: () -> Unit,
    onClear: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Live Traffic",
                    style = MaterialTheme.typography.titleMedium,
                    color = if (paused) MockColors.Warning else MockColors.TextPrimary,
                    fontWeight = FontWeight.Bold,
                )
                if (paused) {
                    Spacer(modifier = Modifier.width(8.dp))
                    StatusBadge(text = "PAUSED", color = MockColors.Warning)
                }
            }
            Text(
                text = if (shown == total) "$total exchange(s)" else "$shown of $total",
                style = MaterialTheme.typography.labelSmall,
                color = MockColors.TextSecondary,
            )
        }
        // Search filter
        OutlinedTextField(
            value = filterText,
            onValueChange = onFilterText,
            placeholder = { Text("filter by path…", style = MaterialTheme.typography.bodySmall) },
            singleLine = true,
            modifier = Modifier.width(180.dp).height(48.dp),
            shape = RoundedCornerShape(12.dp),
        )
        // Method filter chips
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            FilterChip("GET", methodFilter == "GET") { onMethodFilter(if (methodFilter == "GET") null else "GET") }
            FilterChip("POST", methodFilter == "POST") { onMethodFilter(if (methodFilter == "POST") null else "POST") }
            FilterChip("5xx", statusFilter == 500) { onStatusFilter(if (statusFilter == 500) null else 500) }
            FilterChip("4xx", statusFilter == 400) { onStatusFilter(if (statusFilter == 400) null else 400) }
        }
        if (methodFilter != null || statusFilter != null || filterText.isNotEmpty()) {
            GlassButton(text = "✕", onClick = onClearFilters, tint = MockColors.Warning)
        }
        GlassButton(text = "Clear", onClick = onClear, tint = MockColors.TextSecondary)
    }
}

@Composable
private fun FilterChip(label: String, active: Boolean, onClick: () -> Unit) {
    val shape = RoundedCornerShape(9.dp)
    Box(
        modifier = Modifier
            .clip(shape)
            .background(
                if (active) MockColors.Accent.copy(alpha = 0.25f)
                else Color.White.copy(alpha = 0.05f),
            )
            .border(1.dp, if (active) MockColors.Accent else Color.White.copy(alpha = 0.14f), shape)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = if (active) FontWeight.Bold else FontWeight.Normal,
            color = if (active) MockColors.Accent else MockColors.TextSecondary,
        )
    }
}

@Composable
private fun TrafficRow(
    event: TrafficEvent,
    selected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (selected) MockColors.Accent.copy(alpha = 0.12f)
                else Color.White.copy(alpha = 0.02f),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = formatTimestamp(event.timestamp),
            style = MaterialTheme.typography.labelSmall,
            color = MockColors.TextDisabled,
            modifier = Modifier.width(82.dp),
        )
        StatusBadge(
            text = event.statusCode.toString(),
            color = statusColor(event.statusCode),
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = event.method,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = methodColor(event.method),
            modifier = Modifier.width(54.dp),
        )
        Text(
            text = event.path,
            style = MaterialTheme.typography.bodySmall,
            color = MockColors.TextPrimary,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = formatLatency(event.latencyMs),
            style = MaterialTheme.typography.labelSmall,
            color = MockColors.TextSecondary,
            modifier = Modifier.width(70.dp),
        )
        Text(
            text = formatBytes(event.sizeBytes),
            style = MaterialTheme.typography.labelSmall,
            color = MockColors.TextSecondary,
            modifier = Modifier.width(70.dp),
        )
    }
}

@Composable
private fun EmptyTraffic() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "No traffic yet",
                style = MaterialTheme.typography.titleSmall,
                color = MockColors.TextSecondary,
            )
            Text(
                text = "Send requests to the mock server to see live exchanges",
                style = MaterialTheme.typography.labelSmall,
                color = MockColors.TextDisabled,
            )
        }
    }
}

@Composable
private fun TrafficDetailDrawer(
    event: TrafficEvent,
    onClose: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Request Details",
                style = MaterialTheme.typography.titleMedium,
                color = MockColors.TextPrimary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = onClose) {
                Text("✕", color = MockColors.TextSecondary)
            }
        }

        HorizontalDivider(color = Color.White.copy(alpha = 0.10f), thickness = 1.dp)
        Spacer(modifier = Modifier.height(12.dp))

        DetailKVRow("URL", event.url)
        DetailKVRow("Method", event.method)
        DetailKVRow("Status", event.statusCode.toString())
        DetailKVRow("Latency", formatLatency(event.latencyMs))
        DetailKVRow("Remote", event.remoteAddr ?: "—")
        DetailKVRow("Mocked", if (event.mocked) "yes" else "no (proxied)")
        DetailKVRow("Size", formatBytes(event.sizeBytes))

        Spacer(modifier = Modifier.height(16.dp))
        DetailSectionTitle("Request Headers")
        if (event.requestHeaders.isEmpty()) {
            Muted("none")
        } else {
            JsonTreeViewer(prettyFromHeaders(event.requestHeaders))
        }

        Spacer(modifier = Modifier.height(12.dp))
        DetailSectionTitle("Request Body")
        if (event.requestBody.isBlank()) {
            Muted("empty")
        } else {
            JsonTreeViewer(event.requestBody)
        }

        Spacer(modifier = Modifier.height(12.dp))
        DetailSectionTitle("Response Headers")
        if (event.responseHeaders.isEmpty()) {
            Muted("none")
        } else {
            JsonTreeViewer(prettyFromHeaders(event.responseHeaders))
        }

        Spacer(modifier = Modifier.height(12.dp))
        DetailSectionTitle("Response Body")
        if (event.responseBody.isBlank()) {
            Muted("empty")
        } else {
            JsonTreeViewer(event.responseBody)
        }
    }
}

@Composable
private fun DetailKVRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MockColors.TextDisabled,
            modifier = Modifier.width(72.dp),
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MockColors.TextPrimary,
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun DetailSectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        color = MockColors.Info,
    )
    Spacer(modifier = Modifier.height(4.dp))
}

@Composable
private fun Muted(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall,
        color = MockColors.TextDisabled,
    )
}

private fun methodColor(method: String): Color = when (method) {
    "GET" -> MockColors.Info
    "POST" -> MockColors.Accent
    "PUT", "PATCH" -> MockColors.Warning
    "DELETE" -> MockColors.Error
    else -> MockColors.TextSecondary
}

/** Renders a header map as a JSON object so the tree viewer colors keys. */
private fun prettyFromHeaders(headers: Map<String, List<String>>): String {
    val sb = StringBuilder("{")
    headers.forEach { (k, v) ->
        sb.append("\"").append(k).append("\": \"")
            .append(v.joinToString(", ").replace("\"", "\\\""))
            .append("\", ")
    }
    if (sb.length > 1) sb.setLength(sb.length - 2)
    sb.append("}")
    return sb.toString()
}