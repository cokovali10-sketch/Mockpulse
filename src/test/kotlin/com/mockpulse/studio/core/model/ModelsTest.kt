package com.mockpulse.studio.core.model

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ModelsTest {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    @Test
    fun `TrafficEvent serializes with Go field names`() {
        val event = TrafficEvent(
            id = "id",
            statusCode = 200,
            latencyMs = 5,
            remoteAddr = "1.2.3.4:5",
            mocked = false,
        )
        val encoded = json.encodeToString(TrafficEvent.serializer(), event)
        assertTrue(encoded.contains("\"status_code\":200"))
        assertTrue(encoded.contains("\"latency_ms\":5"))
        assertTrue(encoded.contains("\"remote_addr\":\"1.2.3.4:5\""))
        assertTrue(encoded.contains("\"mocked\":false"))
        assertTrue(encoded.contains("\"id\":\"id\""))
    }

    @Test
    fun `TrafficEvent sizeBytes reflects body lengths`() {
        val event = TrafficEvent(requestBody = "abcd", responseBody = "efg")
        assertEquals(7L, event.sizeBytes)
    }

    @Test
    fun `TrafficEvent decodes ignoring unknown fields`() {
        val raw = """{"id":"x","status_code":500,"banana":123}"""
        val decoded = json.decodeFromString(TrafficEvent.serializer(), raw)
        assertEquals("x", decoded.id)
        assertEquals(500, decoded.statusCode)
    }

    @Test
    fun `ChaosConfig round-trips`() {
        val cfg = ChaosConfig(
            latencyFixedMs = 100,
            errorRate = 0.5,
            errorCodes = listOf(500, 503),
            dropRate = 0.25,
        )
        val encoded = json.encodeToString(ChaosConfig.serializer(), cfg)
        assertTrue(encoded.contains("\"latency_fixed_ms\":100"))
        assertTrue(encoded.contains("\"error_rate\":0.5"))
        assertTrue(encoded.contains("\"drop_rate\":0.25"))
        val decoded = json.decodeFromString(ChaosConfig.serializer(), encoded)
        assertEquals(cfg, decoded)
    }

    @Test
    fun `HealthStatus maps spec and counters`() {
        val raw = """
            {"status":"ok","version":"0.2.0","routes":3,"uptime_sec":12,
             "subscribers":1,"events_total":9,"events_dropped":1,
             "spec":{"title":"T","version":"v","openapi":"3.1.0"}}
        """.trimIndent()
        val h = json.decodeFromString(HealthStatus.serializer(), raw)
        assertEquals("0.2.0", h.version)
        assertEquals(3, h.routes)
        assertEquals(9L, h.eventsTotal)
        assertEquals(1L, h.subscribers)
        assertEquals("T", h.spec?.title)
    }

    @Test
    fun `ResolveCommand encodes decision and modified response`() {
        val cmd = ResolveCommand(
            id = "bp-1",
            decision = "modify",
            response = ModifiedResponse(
                status = 201,
                body = buildJsonObject {
                    put("ok", true)
                },
            ),
        )
        val encoded = json.encodeToString(ResolveCommand.serializer(), cmd)
        assertTrue(encoded.contains("\"decision\":\"modify\""))
        assertTrue(encoded.contains("\"status\":201"))
    }

    @Test
    fun `RouteList pressure Map`() {
        val raw = """
            {"title":"Users JSON API","version":"2.0.0","count":1,
             "routes":[{"method":"GET","path":"/accounts","summary":"","status_codes":[200]}]}
        """.trimIndent()
        val list = json.decodeFromString(RouteList.serializer(), raw)
        assertEquals(1, list.count)
        assertEquals(1, list.routes.size)
        assertEquals("GET", list.routes[0].method)
        assertFalse(list.routes[0].deprecated)
    }

    @Test
    fun `BreakpointEvent decodes headers and query`() {
        val raw = """
            {"id":"bp","method":"POST","path":"/x","arrived_at":"2026-01-01T00:00:00Z",
             "query":{"a":"1"},"headers":{"Content-Type":"application/json"},"body":"{}"}
        """.trimIndent()
        val bp = json.decodeFromString(BreakpointEvent.serializer(), raw)
        assertEquals("1", bp.query["a"])
        assertEquals("application/json", bp.headers["Content-Type"])
        assertEquals("{}", bp.body)
    }
}