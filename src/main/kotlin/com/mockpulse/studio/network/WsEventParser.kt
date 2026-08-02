package com.mockpulse.studio.network

import com.mockpulse.studio.core.model.BreakpointEvent
import com.mockpulse.studio.core.model.TrafficEvent
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.booleanOrNull

/**
 * Decodes raw WebSocket frames into typed domain events. Unknown message types
 * are ignored so the client stays forward-compatible with newer servers.
 */
object WsEventParser {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    sealed interface WsEvent {
        data class Ready(val version: String?, val intercept: Boolean?) : WsEvent
        data class Traffic(val event: TrafficEvent) : WsEvent
        data class Breakpoint(val event: BreakpointEvent) : WsEvent
        data class Resolved(val id: String, val decision: String?, val timedOut: Boolean?) : WsEvent
        data object Ping : WsEvent
        data object Unknown : WsEvent
    }

    /** Parses one frame. Returns Unknown for malformed or irrelevant frames. */
    fun parse(raw: String): WsEvent {
        val element = runCatching { json.parseToJsonElement(raw) }.getOrNull() ?: return WsEvent.Unknown
        return parse(element)
    }

    /** Parses an already-decoded envelope (type + optional data object). */
    fun parse(type: String, data: JsonElement?): WsEvent {
        if (type.isEmpty()) return WsEvent.Unknown
        val obj = (data ?: JsonNull)
        return when (type) {
            "ready" -> WsEvent.Ready(
                version = (obj.jsonObject["version"] as? JsonPrimitive)?.content,
                intercept = (obj.jsonObject["intercept"] as? JsonPrimitive)?.booleanOrNull,
            )
            "traffic" -> WsEvent.Traffic(runCatching { json.decodeFromJsonElement(TrafficEvent.serializer(), obj) }
                .getOrElse { TrafficEvent() })
            "breakpoint" -> WsEvent.Breakpoint(
                runCatching { json.decodeFromJsonElement(BreakpointEvent.serializer(), obj) }
                    .getOrElse { BreakpointEvent() },
            )
            "resolved" -> WsEvent.Resolved(
                id = (obj.jsonObject["id"] as? JsonPrimitive)?.content ?: "",
                decision = (obj.jsonObject["decision"] as? JsonPrimitive)?.content,
                timedOut = (obj.jsonObject["timed_out"] as? JsonPrimitive)?.booleanOrNull,
            )
            "ping" -> WsEvent.Ping
            else -> WsEvent.Unknown
        }
    }

    fun parse(element: JsonElement): WsEvent {
        val obj = element.jsonObject
        val type = obj["type"]?.let { (it as? kotlinx.serialization.json.JsonPrimitive)?.content } ?: ""
        val data = obj["data"] ?: JsonNull

        return when (type) {
            "ready" -> WsEvent.Ready(
                version = (data.jsonObject["version"] as? JsonPrimitive)?.content,
                intercept = (data.jsonObject["intercept"] as? JsonPrimitive)?.booleanOrNull,
            )
            "traffic" -> WsEvent.Traffic(runCatching { json.decodeFromJsonElement(TrafficEvent.serializer(), data) }
                .getOrElse { TrafficEvent() })
            "breakpoint" -> WsEvent.Breakpoint(
                runCatching { json.decodeFromJsonElement(BreakpointEvent.serializer(), data) }
                    .getOrElse { BreakpointEvent() },
            )
            "resolved" -> WsEvent.Resolved(
                id = (data.jsonObject["id"] as? JsonPrimitive)?.content ?: "",
                decision = (data.jsonObject["decision"] as? JsonPrimitive)?.content,
                timedOut = (data.jsonObject["timed_out"] as? JsonPrimitive)?.booleanOrNull,
            )
            "ping" -> WsEvent.Ping
            else -> WsEvent.Unknown
        }
    }
}
