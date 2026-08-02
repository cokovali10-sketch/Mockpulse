package com.mockpulse.studio.core.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

/**
 * WebSocket message envelope from the MockPulse control plane.
 * `data` is kept raw and decoded per type to stay forward-compatible.
 */
@Serializable
data class WsMessage(
    val type: String,
    val data: JsonObject? = null,
)

/**
 * A captured HTTP exchange (Go: traffic.Event).
 */
@Serializable
data class TrafficEvent(
    val id: String = "",
    val timestamp: String = "",
    val method: String = "",
    val path: String = "",
    val url: String = "",
    @SerialName("status_code") val statusCode: Int = 0,
    @SerialName("latency_ms") val latencyMs: Long = 0,
    @SerialName("remote_addr") val remoteAddr: String? = null,
    @SerialName("request_headers") val requestHeaders: Map<String, List<String>> = emptyMap(),
    @SerialName("request_body") val requestBody: String = "",
    @SerialName("response_headers") val responseHeaders: Map<String, List<String>> = emptyMap(),
    @SerialName("response_body") val responseBody: String = "",
    val mocked: Boolean = true,
) {
    val sizeBytes: Long get() = requestBody.encodeToByteArray().size.toLong() +
        responseBody.encodeToByteArray().size.toLong()
}

/**
 * A request paused at a breakpoint awaiting an operator decision
 * (Go: realtime.InterceptRequest).
 */
@Serializable
data class BreakpointEvent(
    val id: String = "",
    val method: String = "",
    val path: String = "",
    val query: Map<String, String> = emptyMap(),
    val params: Map<String, String> = emptyMap(),
    val headers: Map<String, String> = emptyMap(),
    val body: String = "",
    @SerialName("arrived_at") val arrivedAt: String = "",
)

/**
 * A breakpoint resolution sent back to the server (Go: resolve command).
 */
@Serializable
data class ResolveCommand(
    val id: String,
    val decision: String, // drop | pass | modify
    val response: ModifiedResponse? = null,
)

@Serializable
data class ModifiedResponse(
    @SerialName("status") val status: Int? = null,
    @SerialName("headers") val headers: Map<String, String> = emptyMap(),
    @SerialName("body") val body: JsonObject? = null,
)

/**
 * Chaos engine configuration (Go: chaos.Config). Mirrors the JSON field names.
 */
@Serializable
data class ChaosConfig(
    @SerialName("latency_fixed_ms") val latencyFixedMs: Int = 0,
    @SerialName("latency_min_ms") val latencyMinMs: Int = 0,
    @SerialName("latency_max_ms") val latencyMaxMs: Int = 0,
    @SerialName("error_rate") val errorRate: Double = 0.0,
    @SerialName("error_codes") val errorCodes: List<Int> = emptyList(),
    @SerialName("drop_rate") val dropRate: Double = 0.0,
)

/**
 * Health/stats snapshot (Go: /__mockpulse/health).
 */
@Serializable
data class HealthStatus(
    val status: String = "",
    val version: String = "",
    val spec: SpecInfo? = null,
    val routes: Int = 0,
    @SerialName("uptime_sec") val uptimeSec: Long = 0,
    val subscribers: Long = 0,
    @SerialName("events_total") val eventsTotal: Long = 0,
    @SerialName("events_dropped") val eventsDropped: Long = 0,
)

@Serializable
data class SpecInfo(
    val title: String = "",
    val version: String = "",
    val openapi: String = "",
)

/**
 * A registered mock route (Go: /__mockpulse/routes entry).
 */
@Serializable
data class RouteInfo(
    val method: String = "",
    val path: String = "",
    val summary: String = "",
    @SerialName("operation_id") val operationId: String = "",
    val tags: List<String> = emptyList(),
    val deprecated: Boolean = false,
    @SerialName("status_codes") val statusCodes: List<Int> = emptyList(),
    @SerialName("success_status") val successStatus: Int = 200,
)

@Serializable
data class RouteList(
    val title: String = "",
    val version: String = "",
    val count: Int = 0,
    val routes: List<RouteInfo> = emptyList(),
)

/**
 * A recorded in-memory mock exchange (Go: /__mockpulse/recordings).
 */
@Serializable
data class Recordings(
    val count: Int = 0,
    val keys: List<String> = emptyList(),
)
