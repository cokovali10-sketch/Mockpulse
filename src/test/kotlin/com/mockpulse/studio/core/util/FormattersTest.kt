package com.mockpulse.studio.core.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FormattersTest {

    @Test
    fun `formats byte counts`() {
        assertEquals("0 B", formatBytes(0))
        assertEquals("511 B", formatBytes(511))
        assertEquals("1.0 KB", formatBytes(1024))
        assertEquals("1.5 KB", formatBytes(1536))
        assertEquals("1.0 MB", formatBytes(1024 * 1024))
    }

    @Test
    fun `formats latency`() {
        assertEquals("12 ms", formatLatency(12))
        assertEquals("999 ms", formatLatency(999))
        assertEquals("1.00 s", formatLatency(1000))
        assertEquals("2.50 s", formatLatency(2500))
    }

    @Test
    fun `formats timestamp`() {
        // RFC3339 input renders as HH:mm:ss.SSS local time (non-blank).
        val out = formatTimestamp("2026-01-01T12:34:56Z")
        assertTrue(out.matches(Regex("""\d{2}:\d{2}:\d{2}.\d{3}""")), "got: $out")
        assertEquals("—", formatTimestamp(""))
        assertEquals("garbage", formatTimestamp("garbage"))
    }

    @Test
    fun `pretty prins JSON`() {
        val pretty = prettyJson("""{"a":1,"b":[true,null]}""")
        assertTrue(pretty.contains("\n"), "expected multi-line pretty JSON")
        assertTrue(pretty.contains("\"a\": 1"))
    }

    @Test
    fun `isJson honours blank and invalid input`() {
        assertFalse(isJson(""))
        assertFalse(isJson("not json"))
        assertTrue(isJson("""{"x":1}"""))
    }

    @Test
    fun `pretty returns raw input on invalid json`() {
        val raw = "just text"
        assertEquals(raw, prettyJson(raw))
    }

    @Test
    fun `formats headers`() {
        val headers = mapOf("Accept" to listOf("application/json"), "X-Foo" to listOf("a", "b"))
        val out = formatHeaders(headers)
        assertTrue(out.contains("Accept: application/json"))
        assertTrue(out.contains("X-Foo: a, b"))
    }

    @Test
    fun `formats flat headers`() {
        val out = formatFlatHeaders(mapOf("K" to "V"))
        assertEquals("K: V", out)
    }

    @Test
    fun `builds json object from map`() {
        val obj = jsonObjectFromMap(mapOf("a" to "1", "b" to "2"))
        assertEquals("1", obj["a"]?.toString()?.trim('"'))
        assertEquals("2", obj["b"]?.toString()?.trim('"'))
    }
}