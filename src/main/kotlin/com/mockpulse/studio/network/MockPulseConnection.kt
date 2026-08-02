package com.mockpulse.studio.network

import com.mockpulse.studio.core.model.HealthStatus
import com.mockpulse.studio.core.model.WsMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.retryWhen
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Owns the lifecycle of the connection to a MockPulse instance:
 * periodic health polling (REST) plus a persistent WebSocket session that
 * reconnects with backoff on failure.
 */
class MockPulseConnection(
    private val scope: CoroutineScope,
    initialHost: String = "localhost",
    initialPort: Int = 8080,
) {
    private val _host = MutableStateFlow(initialHost)
    private val _port = MutableStateFlow(initialPort)
    private val _connected = MutableStateFlow(false)
    private val _version = MutableStateFlow<String?>(null)
    private val _error = MutableStateFlow<String?>(null)

    private val _health = MutableStateFlow<HealthStatus?>(null)
    private val _wsEvents = MutableSharedFlow<com.mockpulse.studio.core.model.WsMessage>(
        extraBufferCapacity = 512,
    )

    val host: StateFlow<String> = _host.asStateFlow()
    val port: StateFlow<Int> = _port.asStateFlow()
    val connected: StateFlow<Boolean> = _connected.asStateFlow()
    val version: StateFlow<String?> = _version.asStateFlow()
    val error: StateFlow<String?> = _error.asStateFlow()
    val health: StateFlow<HealthStatus?> = _health.asStateFlow()

    /** Raw decoded WS messages for downstream view models. */
    val wsEvents: SharedFlow<WsMessage> = _wsEvents.asSharedFlow()

    private var api: MockPulseApiClient? = null
    private var wsJob: Job? = null
    private var pollJob: Job? = null
    private var backoff = 1_000L

    val client: MockPulseApiClient?
        get() = api

    fun currentBaseUrl(): String = "http://${_host.value}:${_port.value}"

    fun updateTarget(host: String, port: Int) {
        _host.value = host.trim()
        _port.value = port
        connect()
    }

    fun connect() {
        disconnect()
        val base = "http://${_host.value}:${_port.value}"
        val newApi = MockPulseApiClient(base)
        api = newApi

        pollJob = scope.launch {
            while (isActive) {
                when (val r = newApi.health()) {
                    is ApiResult.Success -> {
                        _health.value = r.value
                        _version.value = r.value.version
                        _connected.value = true
                        _error.value = null
                        backoff = 1_000L
                    }
                    is ApiResult.Failure -> {
                        _connected.value = false
                        _error.value = r.message
                    }
                }
                delay(2_000)
            }
        }

        launchWs(newApi)
    }

    private fun launchWs(api: MockPulseApiClient) {
        wsJob?.cancel()
        wsJob = scope.launch {
            api.wsEventFlow()
                .retryWhen { _, _ ->
                    delay(backoff)
                    backoff = (backoff * 2).coerceAtMost(15_000)
                    true
                }
                .catch { /* transient; retry loop handles reconnect */ }
                .collect { element -> dispatchRaw(element) }
        }
    }

    private fun dispatchRaw(element: kotlinx.serialization.json.JsonElement) {
        // decode into WsMessage and forward to shared flow
        val msg = runCatching {
            val json = kotlinx.serialization.json.Json { ignoreUnknownKeys = true }
            json.decodeFromJsonElement(WsMessage.serializer(), element)
        }.getOrNull() ?: return
        _wsEvents.tryEmit(msg)
    }

    fun disconnect() {
        wsJob?.cancel()
        wsJob = null
        pollJob?.cancel()
        pollJob = null
        api?.close()
        api = null
        _connected.value = false
        _version.value = null
        _health.value = null
        _error.value = null
    }

    fun dispose() {
        disconnect()
    }

    /** Re-sends a resolve command over the current connection. */
    suspend fun sendResolve(command: com.mockpulse.studio.core.model.ResolveCommand): Boolean {
        val a = api ?: return false
        return when (val r = a.sendResolve(command)) {
            is ApiResult.Success -> true
            is ApiResult.Failure -> false
        }
    }
}
