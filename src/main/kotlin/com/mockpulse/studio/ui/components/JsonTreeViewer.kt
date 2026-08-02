package com.mockpulse.studio.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.mockpulse.studio.core.util.prettyJson
import com.mockpulse.studio.ui.theme.MockColors

/**
 * Renders a JSON string with syntax coloring. Falls back to plain text when the
 * input is not valid JSON.
 */
@Composable
fun JsonTreeViewer(
    json: String,
    modifier: Modifier = Modifier,
    maxLines: Int = Int.MAX_VALUE,
) {
    val parsed = runCatching { prettyJson(json) }.getOrNull() ?: json
    val annotated = highlightJson(parsed)
    Column(
        modifier = modifier
            .horizontalScroll(rememberScrollState())
            .padding(8.dp),
    ) {
        Text(
            text = annotated,
            style = MaterialTheme.typography.bodySmall,
            color = MockColors.TextPrimary,
            maxLines = maxLines,
        )
    }
}

/**
 * Lightweight JSON syntax highlighter operating on pretty-printed output.
 * Uses character classification rather than a tokenizer; good enough for
 * inspection use cases.
 */
private fun highlightJson(text: String): AnnotatedString = buildAnnotatedString {
    var i = 0
    val n = text.length
    while (i < n) {
        val c = text[i]
        when {
            c == '"' -> {
                val start = i
                i++
                var escaped = false
                while (i < n) {
                    val ch = text[i]
                    if (!escaped && ch == '"') { i++; break }
                    if (!escaped && ch == '\\') escaped = true else escaped = false
                    i++
                }
                appendStyled(text.substring(start, i), MockColors.Accent)
            }
            c == ':' -> {
                appendStyled(":", MockColors.TextDisabled)
                i++
            }
            c.isDigit() || c == '-' || c == '+' || c == '.' || c == 'e' || c == 'E' -> {
                val start = i
                while (i < n && (text[i].isDigit() || text[i] in "-+.eE")) i++
                appendStyled(text.substring(start, i), MockColors.Warning)
            }
            c == 't' || c == 'f' || c == 'n' -> {
                val start = i
                val word = text.substring(i).takeWhile { it.isLetter() }
                i += word.length
                appendStyled(word, MockColors.Info)
            }
            c == '{' || c == '}' -> {
                appendStyled(c.toString(), MockColors.TextSecondary)
                i++
            }
            c == '[' || c == ']' -> {
                appendStyled(c.toString(), MockColors.TextSecondary)
                i++
            }
            c == ',' -> {
                appendStyled(",", MockColors.TextDisabled)
                i++
            }
            else -> {
                appendStyled(c.toString(), MockColors.TextPrimary)
                i++
            }
        }
    }
}

private fun AnnotatedString.Builder.appendStyled(text: String, color: Color) {
    withStyle(androidx.compose.ui.text.SpanStyle(color = color)) {
        append(text)
    }
}
