@file:OptIn(ExperimentalSerializationApi::class)

package com.mockpulse.studio.core.util

import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")

/** Formats an RFC3339 timestamp into a compact clock time "HH:mm:ss.SSS". */
fun formatTimestamp(iso: String): String {
    if (iso.isBlank()) return "—"
    return try {
        Instant.parse(iso).atZone(ZoneId.systemDefault()).format(CLOCK)
    } catch (_: Exception) {
        iso
    }
}

/** Formats a byte count into a human-readable size. */
fun formatBytes(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format(Locale.ROOT, "%.1f KB", kb)
    return String.format(Locale.ROOT, "%.1f MB", kb / 1024.0)
}

/** Formats an elapsed-millis value. */
fun formatLatency(ms: Long): String =
    if (ms < 1000) "$ms ms" else String.format(Locale.ROOT, "%.2f s", ms / 1000.0)

/**
 * Pretty-prints JSON text with 2-space indentation using kotlinx.serialization.
 * Returns the raw input on failure (e.g. non-JSON body).
 */
fun prettyJson(raw: String): String {
    if (raw.isBlank()) return raw
    return try {
        prettyJsonElement(Json.parseToJsonElement(raw))
    } catch (_: Exception) {
        raw
    }
}

/** Renders a JsonElement with 2-space indentation. */
fun prettyJsonElement(element: kotlinx.serialization.json.JsonElement): String {
    val json = Json { prettyPrint = true; prettyPrintIndent = "  " }
    return json.encodeToString(kotlinx.serialization.json.JsonElement.serializer(), element)
}

/** Best-effort parse: returns parsed JsonElement or null. */
fun parseJsonElement(raw: String): kotlinx.serialization.json.JsonElement? =
    try {
        Json.parseToJsonElement(raw)
    } catch (_: Exception) {
        null
    }

fun isJson(raw: String): Boolean {
    if (raw.isBlank()) return false
    return try {
        Json.parseToJsonElement(raw)
        true
    } catch (_: Exception) {
        false
    }
}

/** Formats a request/response header map for display. */
fun formatHeaders(headers: Map<String, List<String>>): String =
    headers.entries.joinToString("\n") { (k, v) -> "$k: ${v.joinToString(", ")}" }

fun formatFlatHeaders(headers: Map<String, String>): String =
    headers.entries.joinToString("\n") { (k, v) -> "$k: $v" }

/** Builds a JSON object from a flat string map (for modified responses). */
fun jsonObjectFromMap(map: Map<String, String>): JsonObject =
    buildJsonObject {
        map.forEach { (k, v) -> put(k, JsonPrimitive(v)) }
    }
