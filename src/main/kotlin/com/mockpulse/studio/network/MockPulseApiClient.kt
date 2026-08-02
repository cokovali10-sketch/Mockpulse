package com.mockpulse.studio.network

import com.mockpulse.studio.core.model.ChaosConfig
import com.mockpulse.studio.core.model.HealthStatus
import com.mockpulse.studio.core.model.Recordings
import com.mockpulse.studio.core.model.ResolveCommand
import com.mockpulse.studio.core.model.RouteList
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement

/**
 * Typed result of a control-plane REST operation.
 */
sealed interface ApiResult<out T> {
    data class Success<T>(val value: T) : ApiResult<T>
    data class Failure(val status: Int, val message: String) : ApiResult<Nothing>
}

/**
 * Ktor-based client for the MockPulse control plane.
 *
 * Responsibilities:
 *  - REST calls against the /__mockpulse control plane (health, routes, config, recordings)
 *  - WebSocket session against /__mockpulse/ws producing a Flow<JsonElement>
 *
 * All I/O runs on Dispatchers.IO; the returned flows are cold and suspend-free
 * so view models can collect them on the UI scope.
 */
class MockPulseApiClient(private val baseUrl: String) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        encodeDefaults = true
    }

    private val client: HttpClient = HttpClient(CIO) {
        expectSuccess = false
        install(ContentNegotiation) { json(json) }
        install(HttpTimeout) {
            connectTimeoutMillis = 5_000
            requestTimeoutMillis = 15_000
        }
        install(WebSockets) {
            pingIntervalMillis = 20_000
            maxFrameSize = 4L * 1024 * 1024
        }
    }

    /** Base URL of the control plane, e.g. "http://localhost:8080". */
    val controlBase: String = baseUrl.trimEnd('/')

    private fun url(path: String): String = controlBase + path

    suspend fun health(): ApiResult<HealthStatus> =
        getSafe(url("/__mockpulse/health")) { it.body<HealthStatus>() }

    suspend fun routes(): ApiResult<RouteList> =
        getSafe(url("/__mockpulse/routes")) { it.body<RouteList>() }

    suspend fun chaosConfig(): ApiResult<ChaosConfig> =
        getSafe(url("/__mockpulse/config")) { it.body<ChaosConfig>() }

    suspend fun patchChaosConfig(cfg: ChaosConfig): ApiResult<ChaosConfig> =
        withContext(Dispatchers.IO) {
            runCatching {
                client.patch(url("/__mockpulse/config")) {
                    contentType(ContentType.Application.Json)
                    setBody(json.encodeToString(ChaosConfig.serializer(), cfg))
                }
            }.fold(
                onSuccess = { resp ->
                    if (resp.status.value in 200..299) {
                        ApiResult.Success(runCatching { json.decodeFromString(ChaosConfig.serializer(), resp.bodyAsText()) }
                            .getOrElse { cfg })
                    } else {
                        ApiResult.Failure(resp.status.value, resp.bodyAsText().take(300))
                    }
                },
                onFailure = { ApiResult.Failure(-1, it.message ?: "network error") },
            )
        }

    suspend fun recordings(): ApiResult<Recordings> =
        getSafe(url("/__mockpulse/recordings")) { it.body<Recordings>() }

    suspend fun clearRecordings(): ApiResult<Recordings> =
        withContext(Dispatchers.IO) {
            runCatching { client.delete(url("/__mockpulse/recordings")) }.fold(
                onSuccess = { resp ->
                    if (resp.status.value in 200..299) {
                        ApiResult.Success(runCatching { json.decodeFromString(Recordings.serializer(), resp.bodyAsText()) }
                            .getOrElse { Recordings(0, emptyList()) })
                    } else {
                        ApiResult.Failure(resp.status.value, resp.bodyAsText().take(300))
                    }
                },
                onFailure = { ApiResult.Failure(-1, it.message ?: "network error") },
            )
        }

    /**
     * Opens a WebSocket session to /__mockpulse/ws and emits every incoming
     * text frame decoded as a JsonElement. The flow completes when the socket
     * closes or the collection scope is cancelled.
     */
    fun wsEventFlow(): Flow<JsonElement> = flow {
        client.webSocket(host = host(), port = port(), path = "/__mockpulse/ws") {
            for (frame in incoming) {
                val text = frame.data.decodeToString()
                val element = runCatching { json.parseToJsonElement(text) }.getOrNull()
                if (element != null) emit(element)
            }
        }
    }.flowOn(Dispatchers.IO)

    /**
     * Sends a breakpoint resolution over a one-shot WebSocket connection.
     * Returns the outcome; failures are surfaced so the UI can retry.
     */
    suspend fun sendResolve(command: ResolveCommand): ApiResult<Unit> =
        withContext(Dispatchers.IO) {
            runCatching {
                client.webSocket(host = host(), port = port(), path = "/__mockpulse/ws") {
                    val payload = json.encodeToString(ResolveCommand.serializer(), command)
                    send(
                        io.ktor.websocket.Frame.Text(
                            """{"type":"resolve","data":$payload}""",
                        ),
                    )
                }
            }.fold(
                onSuccess = { ApiResult.Success(Unit) },
                onFailure = { ApiResult.Failure(-1, it.message ?: "websocket error") },
            )
        }

    private fun host(): String {
        val after = controlBase.substringAfter("://", "")
        return after.substringBefore(":")
    }

    private fun port(): Int =
        controlBase.substringAfter("://", "").substringAfter(":", "80").toIntOrNull() ?: 80

    private suspend fun <T> getSafe(
        path: String,
        map: suspend (io.ktor.client.statement.HttpResponse) -> T,
    ): ApiResult<T> = withContext(Dispatchers.IO) {
        runCatching { client.get(path) }.fold(
            onSuccess = { resp ->
                if (resp.status.value in 200..299) {
                    runCatching { ApiResult.Success(map(resp)) }
                        .getOrElse { ApiResult.Failure(-1, it.message ?: "decode error") }
                } else {
                    ApiResult.Failure(resp.status.value, resp.bodyAsText().take(300))
                }
            },
            onFailure = { ApiResult.Failure(-1, it.message ?: "network error") },
        )
    }

    /** Closes the underlying HttpClient and releases all resources. */
    fun close() {
        client.close()
    }
}
