package com.mockpulse.studio.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.mockpulse.studio.ui.components.ConnectionDialog
import com.mockpulse.studio.ui.components.GlassButton
import com.mockpulse.studio.ui.components.MetricCard
import com.mockpulse.studio.ui.components.SidebarEntry
import com.mockpulse.studio.ui.components.StatusBadge
import com.mockpulse.studio.ui.screens.ChaosDashboardScreen
import com.mockpulse.studio.ui.screens.DynamicRoutesScreen
import com.mockpulse.studio.ui.screens.StateStorageScreen
import com.mockpulse.studio.ui.screens.TrafficInspectorScreen
import com.mockpulse.studio.ui.theme.AuroraBackground
import com.mockpulse.studio.ui.theme.GlassPanel
import com.mockpulse.studio.ui.theme.MockColors
import com.mockpulse.studio.viewmodel.AppViewModel
import com.mockpulse.studio.viewmodel.ChaosViewModel
import com.mockpulse.studio.viewmodel.RoutesViewModel
import com.mockpulse.studio.viewmodel.StateViewModel
import kotlinx.coroutines.launch

enum class Screen { TRAFFIC, CHAOS, ROUTES, STATE }

/**
 * Top-level layout: aurora backdrop, glass header bar, sidebar navigation,
 * content pane and status bar. Owns per-screen view models and dialogs.
 */
@Composable
fun MockPulseApp(viewModel: AppViewModel, scope: kotlinx.coroutines.CoroutineScope) {
    var current by remember { mutableStateOf(Screen.TRAFFIC) }
    var selectedEvent by remember { mutableStateOf<com.mockpulse.studio.core.model.TrafficEvent?>(null) }
    var activeBreakpoint by remember {
        mutableStateOf<com.mockpulse.studio.core.model.BreakpointEvent?>(null)
    }
    var showConnection by remember { mutableStateOf(false) }

    val traffic by viewModel.traffic.collectAsState()
    val filteredTraffic by viewModel.filteredTraffic.collectAsState()
    val methodFilter by viewModel.methodFilter.collectAsState()
    val statusFilter by viewModel.statusFilter.collectAsState()
    val filterText by viewModel.filterText.collectAsState()
    val connected by viewModel.connected.collectAsState()
    val host by viewModel.host.collectAsState()
    val port by viewModel.port.collectAsState()
    val health by viewModel.health.collectAsState()
    val paused by viewModel.paused.collectAsState()

    val chaosVm = remember { ChaosViewModel(scope) { viewModel.client } }
    val routesVm = remember { RoutesViewModel(scope) { viewModel.client } }
    val stateVm = remember { StateViewModel(scope) { viewModel.client } }

    LaunchedEffect(Unit) {
        chaosVm.refresh()
    }

    LaunchedEffect(Unit) {
        viewModel.breakpoints.collect { bp -> activeBreakpoint = bp.event }
    }

    AuroraBackground(modifier = Modifier.fillMaxSize()) {
        Column(modifier = Modifier.fillMaxSize()) {
            HeaderBar(
                connected = connected,
                host = host,
                port = port,
                version = viewModel.version.value,
                paused = paused,
                onTogglePause = { viewModel.togglePause() },
                onOpenConnection = { showConnection = true },
            )
            Row(modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp)) {
                Column(
                    modifier = Modifier
                        .width(170.dp)
                        .fillMaxHeight()
                        .padding(end = 12.dp),
                ) {
                    GlassPanel(
                        modifier = Modifier.fillMaxSize(),
                        cornerRadius = 18.dp,
                        fillAlpha = 0.14f,
                    ) {
                        Column(modifier = Modifier.fillMaxSize().padding(10.dp)) {
                            SidebarEntry(
                                label = "Traffic",
                                selected = current == Screen.TRAFFIC,
                                onClick = { current = Screen.TRAFFIC },
                                badge = if (traffic.size > 0) traffic.size.toString() else null,
                            )
                            SidebarEntry(
                                label = "Chaos",
                                selected = current == Screen.CHAOS,
                                onClick = { current = Screen.CHAOS },
                            )
                            SidebarEntry(
                                label = "Routes",
                                selected = current == Screen.ROUTES,
                                onClick = { current = Screen.ROUTES },
                            )
                            SidebarEntry(
                                label = "State",
                                selected = current == Screen.STATE,
                                onClick = { current = Screen.STATE },
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "realtime inspector\n& chaos control",
                                style = MaterialTheme.typography.labelSmall,
                                color = MockColors.TextDisabled,
                            )
                        }
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                ) {
                    when (current) {
                        Screen.TRAFFIC -> TrafficInspectorScreen(
                            events = filteredTraffic,
                            totalEvents = traffic.size,
                            selected = selectedEvent,
                            onSelect = { selectedEvent = it },
                            onClear = { viewModel.clearTraffic() },
                            paused = paused,
                            methodFilter = methodFilter,
                            statusFilter = statusFilter,
                            filterText = filterText,
                            onMethodFilter = { viewModel.setMethodFilter(it) },
                            onStatusFilter = { viewModel.setStatusFilter(it) },
                            onFilterText = { viewModel.setFilterText(it) },
                            onClearFilters = { viewModel.clearFilters() },
                        )
                        Screen.CHAOS -> ChaosDashboardScreen(viewModel = chaosVm)
                        Screen.ROUTES -> DynamicRoutesScreen(viewModel = routesVm)
                        Screen.STATE -> StateStorageScreen(viewModel = stateVm)
                    }
                }
            }
            BottomBar(viewModel = viewModel, trafficCount = traffic.size)
        }

        activeBreakpoint?.let { bp ->
            com.mockpulse.studio.ui.components.BreakpointInterceptorDialog(
                event = bp,
                onResolve = { cmd ->
                    scope.launch {
                        viewModel.sendResolve(cmd)
                        activeBreakpoint = null
                    }
                },
                onDismiss = { activeBreakpoint = null },
            )
        }

        if (showConnection) {
            ConnectionDialog(
                host = host,
                port = port,
                onConnect = { h, p ->
                    viewModel.connect(h, p)
                    showConnection = false
                },
                onDismiss = { showConnection = false },
            )
        }
    }
}

@Composable
private fun HeaderBar(
    connected: Boolean,
    host: String,
    port: Int,
    version: String?,
    paused: Boolean,
    onTogglePause: () -> Unit,
    onOpenConnection: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // logo dot
            Box(
                Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(MockColors.Accent, MockColors.Info),
                        ),
                    ),
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "MOCKPULSE",
                style = MaterialTheme.typography.titleLarge,
                color = MockColors.TextPrimary,
                fontWeight = FontWeight.Bold,
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        StatusBadge(
            text = if (connected) "ONLINE" else "OFFLINE",
            color = if (connected) MockColors.Accent else MockColors.Error,
        )
        if (version != null) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = version,
                style = MaterialTheme.typography.labelSmall,
                color = MockColors.TextDisabled,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "$host:$port",
            style = MaterialTheme.typography.bodySmall,
            color = MockColors.TextSecondary,
        )
        Spacer(modifier = Modifier.width(10.dp))
        GlassButton(
            text = if (paused) "RESUME" else "PAUSE",
            onClick = onTogglePause,
            tint = if (paused) MockColors.Warning else MockColors.Accent,
        )
        Spacer(modifier = Modifier.width(8.dp))
        GlassButton(
            text = "CONNECT",
            onClick = onOpenConnection,
            tint = MockColors.Info,
        )
    }
}

@Composable
private fun BottomBar(
    viewModel: AppViewModel,
    trafficCount: Int,
) {
    val health by viewModel.health.collectAsState()
    val connected by viewModel.connected.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        MetricCard(
            label = "Total",
            value = (health?.eventsTotal ?: 0L).toString(),
            valueColor = MockColors.TextPrimary,
            accent = MockColors.Info,
        )
        MetricCard(
            label = "RPS",
            value = (health?.eventsTotal ?: 0L).toString(),
            valueColor = MockColors.Accent,
            accent = MockColors.Accent,
        )
        MetricCard(
            label = "Dropped",
            value = (health?.eventsDropped ?: 0L).toString(),
            valueColor = if ((health?.eventsDropped ?: 0L) > 0) MockColors.Warning else MockColors.TextPrimary,
            accent = MockColors.Warning,
        )
        MetricCard(
            label = "Routes",
            value = (health?.routes ?: 0).toString(),
            valueColor = MockColors.Info,
            accent = MockColors.Info,
        )
        MetricCard(
            label = "Uptime",
            value = formatUptime(health?.uptimeSec ?: 0L),
            valueColor = MockColors.TextSecondary,
            accent = MockColors.TextDisabled,
        )
        MetricCard(
            label = "Events",
            value = trafficCount.toString(),
            valueColor = MockColors.TextPrimary,
            accent = MockColors.Accent,
        )
        Spacer(modifier = Modifier.weight(1f))
        StatusBadge(
            text = if (connected) "WS connected" else "WS down",
            color = if (connected) MockColors.Accent else MockColors.Error,
        )
    }
}

private fun formatUptime(sec: Long): String {
    if (sec <= 0) return "—"
    val h = sec / 3600
    val m = (sec % 3600) / 60
    val s = sec % 60
    return if (h > 0) "${h}h ${m}m" else if (m > 0) "${m}m ${s}s" else "${s}s"
}