package com.mockpulse.studio

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPosition
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.mockpulse.studio.ui.MockPulseApp
import com.mockpulse.studio.ui.theme.MockPulseTheme
import com.mockpulse.studio.viewmodel.AppViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel

/**
 * Entry point: owns the app-wide coroutine scope and the connection view model,
 * then renders the desktop window.
 */
fun main() = application {
    val appScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    val viewModel = AppViewModel(appScope)

    val windowState = rememberWindowState(
        position = WindowPosition.Aligned(androidx.compose.ui.Alignment.Center),
        width = 1180.dp,
        height = 760.dp,
    )

    Window(
        onCloseRequest = {
            viewModel.dispose()
            appScope.cancel()
            exitApplication()
        },
        state = windowState,
        title = "MockPulse",
    ) {
        MockPulseTheme {
            MockPulseApp(viewModel = viewModel, scope = appScope)
        }
    }
}
