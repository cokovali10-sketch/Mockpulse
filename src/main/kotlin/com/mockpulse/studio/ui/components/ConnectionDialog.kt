package com.mockpulse.studio.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.mockpulse.studio.ui.theme.MockColors

/**
 * Dialog to configure the mock server address and reconnect.
 */
@Composable
fun ConnectionDialog(
    host: String,
    port: Int,
    onConnect: (String, Int) -> Unit,
    onDismiss: () -> Unit,
) {
    var hostText by remember { mutableStateOf(host) }
    var portText by remember { mutableStateOf(port.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = androidx.compose.ui.graphics.Color(0xFF16213E),
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Connect to MockPulse",
                style = MaterialTheme.typography.titleMedium,
                color = MockColors.TextPrimary,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = hostText,
                    onValueChange = { hostText = it },
                    label = { Text("Host") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = portText,
                    onValueChange = { portText = it.filter(Char::isDigit) },
                    label = { Text("Port") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                )
                Text(
                    text = "Expected at ws://<host>:<port>/__mockpulse/ws",
                    style = MaterialTheme.typography.labelSmall,
                    color = MockColors.TextDisabled,
                )
            }
        },
        confirmButton = {
            androidx.compose.material3.Button(
                onClick = {
                    val p = portText.toIntOrNull() ?: port
                    onConnect(hostText.trim().ifEmpty { "localhost" }, p)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MockColors.Accent.copy(alpha = 0.25f),
                    contentColor = MockColors.Accent,
                ),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MockColors.Accent.copy(alpha = 0.5f)),
            ) {
                Text("Connect", fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
            }
        },
        dismissButton = {
            androidx.compose.material3.TextButton(onClick = onDismiss) {
                Text("Cancel", color = MockColors.TextSecondary)
            }
        },
    )
}