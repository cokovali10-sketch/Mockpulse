package com.mockpulse.studio.network

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class WsEventParserTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun `parses ready message`() {
        val raw = """{"type":"ready","data":{"version":"0.2.0","intercept":false,"client_id":"abc"}}"""
        val event = WsEventParser.parse(raw)
        assertIs<WsEventParser.WsEvent.Ready>(event)
        assertEquals("0.2.0", event.version)
        assertEquals(false, event.intercept)
    }

    @Test
    fun `parses traffic message`() {
        val raw = """
            {"type":"traffic","data":{
                "id":"abc123","timestamp":"2026-01-01T00:00:00Z",
                "method":"GET","path":"/accounts/1","url":"http://x/accounts/1",
                "status_code":200,"latency_ms":12,"remote_addr":"127.0.0.1:1",
                "request_headers":{"Accept":["application/json"]},
                "request_body":"{}","response_headers":{},"response_body":"{\"a\":1}","mocked":true
            }}
        """.trimIndent()
        val event = WsEventParser.parse(raw)
        assertIs<WsEventParser.WsEvent.Traffic>(event)
        assertEquals("GET", event.event.method)
        assertEquals(200, event.event.statusCode)
        assertEquals(12L, event.event.latencyMs)
        assertEquals(true, event.event.mocked)
    }

    @Test
    fun `parses breakpoint message`() {
        val raw = """
            {"type":"breakpoint","data":{
                "id":"bp-1","method":"POST","path":"/accounts","arrived_at":"2026-01-01T00:00:00Z",
                "headers":{"Content-Type":"application/json"},"body":"{\"n\":1}"
            }}
        """.trimIndent()
        val event = WsEventParser.parse(raw)
        assertIs<WsEventParser.WsEvent.Breakpoint>(event)
        assertEquals("bp-1", event.event.id)
        assertEquals("POST", event.event.method)
        assertEquals("/accounts", event.event.path)
    }

    @Test
    fun `parses resolved message with timeout flag`() {
        val raw = """{"type":"resolved","data":{"id":"bp-1","decision":"pass","timed_out":true}}"""
        val event = WsEventParser.parse(raw)
        assertIs<WsEventParser.WsEvent.Resolved>(event)
        assertEquals("bp-1", event.id)
        assertEquals("pass", event.decision)
        assertEquals(true, event.timedOut)
    }

    @Test
    fun `parses ping`() {
        assertIs<WsEventParser.WsEvent.Ping>(WsEventParser.parse("""{"type":"ping","data":{}}"""))
    }

    @Test
    fun `returns Unknown for malformed or unknown frames`() {
        assertIs<WsEventParser.WsEvent.Unknown>(WsEventParser.parse("not json"))
        assertIs<WsEventParser.WsEvent.Unknown>(WsEventParser.parse("""{"type":"whatever","data":{}}"""))
    }

    @Test
    fun `parse by type and element overload`() {
        // Simulate a JSON element produced by the connection's WsMessage path.
        val data = buildJsonObject {
            put("version", "1.0.0")
            put("intercept", false)
        }
        val result = WsEventParser.parse("ready", data)
        assertIs<WsEventParser.WsEvent.Ready>(result)
        assertEquals("1.0.0", result.version)
    }

    @Test
    fun `tolerates missing data`() {
        val data = json.parseToJsonElement("""{"type":"resolved","data":{"timed_out":false}}""")
        val result = WsEventParser.parse(data)
        assertIs<WsEventParser.WsEvent.Resolved>(result)
        assertEquals("", result.id)
        assertEquals(false, result.timedOut)
    }
}