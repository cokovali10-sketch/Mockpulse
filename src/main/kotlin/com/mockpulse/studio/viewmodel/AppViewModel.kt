package com.mockpulse.studio.viewmodel

import com.mockpulse.studio.core.model.HealthStatus
import com.mockpulse.studio.core.model.TrafficEvent
import com.mockpulse.studio.network.MockPulseApiClient
import com.mockpulse.studio.network.MockPulseConnection
import com.mockpulse.studio.network.WsEventParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/**
 * Root view model that owns the connection and exposes domain-level streams to
 * the screens. Screens never talk to Ktor directly.
 */
class AppViewModel(private val scope: CoroutineScope) {

    private val _connection = MockPulseConnection(scope)

    val host: StateFlow<String> = _connection.host
    val port: StateFlow<Int> = _connection.port
    val connected: StateFlow<Boolean> = _connection.connected
    val version: StateFlow<String?> = _connection.version
    val error: StateFlow<String?> = _connection.error
    val health: StateFlow<HealthStatus?> = _connection.health

    private val _traffic = MutableStateFlow<List<TrafficEvent>>(emptyList())
    val traffic: StateFlow<List<TrafficEvent>> = _traffic.asStateFlow()

    private val _breakpoints = MutableSharedFlow<WsEventParser.WsEvent.Breakpoint>(
        extraBufferCapacity = 32,
    )
    val breakpoints: SharedFlow<WsEventParser.WsEvent.Breakpoint> = _breakpoints.asSharedFlow()

    private val _resolved = MutableSharedFlow<WsEventParser.WsEvent.Resolved>(
        extraBufferCapacity = 32,
    )
    val resolved: SharedFlow<WsEventParser.WsEvent.Resolved> = _resolved.asSharedFlow()

    private val _paused = MutableStateFlow(false)
    val paused: StateFlow<Boolean> = _paused.asStateFlow()

    private val _methodFilter = MutableStateFlow<String?>(null)
    val methodFilter: StateFlow<String?> = _methodFilter.asStateFlow()

    private val _statusFilter = MutableStateFlow<Int?>(null)
    val statusFilter: StateFlow<Int?> = _statusFilter.asStateFlow()

    private val _filterText = MutableStateFlow("")
    val filterText: StateFlow<String> = _filterText.asStateFlow()

    /** Filtered traffic derived from the raw list plus active filters. */
    val filteredTraffic: StateFlow<List<TrafficEvent>> = kotlinx.coroutines.flow.combine(
        _traffic, _methodFilter, _statusFilter, _filterText,
    ) { list, method, status, text ->
        list.filter { ev ->
            (method == null || ev.method.equals(method, ignoreCase = true)) &&
                (status == null || ev.statusCode == status) &&
                (text.isBlank() || ev.path.contains(text) || ev.url.contains(text))
        }
    }.stateIn(
        scope = scope,
        started = kotlinx.coroutines.flow.SharingStarted.Eagerly,
        initialValue = emptyList(),
    )

    val client: MockPulseApiClient? get() = _connection.client

    init {
        _connection.connect()
        scope.launch {
            _connection.wsEvents.collect { raw ->
                val event = WsEventParser.parse(raw.type, raw.data ?: return@collect)
                when (event) {
                    is WsEventParser.WsEvent.Traffic -> {
                        if (!_paused.value) {
                            _traffic.value = listOf(event.event) + _traffic.value
                        }
                    }
                    is WsEventParser.WsEvent.Breakpoint -> _breakpoints.tryEmit(event)
                    is WsEventParser.WsEvent.Resolved -> _resolved.tryEmit(event)
                    else -> {}
                }
            }
        }
    }

    fun connect(host: String, port: Int) = _connection.updateTarget(host, port)

    fun togglePause() {
        _paused.value = !_paused.value
    }

    fun clearTraffic() {
        _traffic.value = emptyList()
    }

    fun setMethodFilter(method: String?) {
        _methodFilter.value = method
    }

    fun setStatusFilter(status: Int?) {
        _statusFilter.value = status
    }

    fun setFilterText(text: String) {
        _filterText.value = text
    }

    fun clearFilters() {
        _methodFilter.value = null
        _statusFilter.value = null
        _filterText.value = ""
    }

    suspend fun sendResolve(command: com.mockpulse.studio.core.model.ResolveCommand): Boolean =
        _connection.sendResolve(command)

    fun dispose() {
        _connection.dispose()
    }
}
