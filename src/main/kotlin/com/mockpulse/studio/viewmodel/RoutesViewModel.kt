package com.mockpulse.studio.viewmodel

import com.mockpulse.studio.core.model.RouteInfo
import com.mockpulse.studio.core.model.RouteList
import com.mockpulse.studio.network.ApiResult
import com.mockpulse.studio.network.MockPulseApiClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Loads the registered mock route catalog from the control plane.
 */
class RoutesViewModel(
    private val scope: CoroutineScope,
    private val clientProvider: () -> MockPulseApiClient?,
) {
    private val _routes = MutableStateFlow<List<RouteInfo>>(emptyList())
    val routes: StateFlow<List<RouteInfo>> = _routes.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    fun refresh() {
        val api = clientProvider() ?: return
        scope.launch {
            _loading.value = true
            when (val r = withContext(Dispatchers.IO) { api.routes() }) {
                is ApiResult.Success -> {
                    _routes.value = r.value.routes
                    _lastError.value = null
                }
                is ApiResult.Failure -> _lastError.value = r.message
            }
            _loading.value = false
        }
    }
}
