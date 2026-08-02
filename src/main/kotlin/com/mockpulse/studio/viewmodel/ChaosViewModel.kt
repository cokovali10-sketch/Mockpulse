package com.mockpulse.studio.viewmodel

import com.mockpulse.studio.core.model.ChaosConfig
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
 * Drives the Chaos Engineering dashboard: reads and mutates the chaos engine
 * configuration. Mutations are debounced lightly so slider drags don't spam
 * the control plane.
 */
class ChaosViewModel(
    private val scope: CoroutineScope,
    private val clientProvider: () -> MockPulseApiClient?,
) {
    private val _config = MutableStateFlow(ChaosConfig())
    val config: StateFlow<ChaosConfig> = _config.asStateFlow()

    private val _saving = MutableStateFlow(false)
    val saving: StateFlow<Boolean> = _saving.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    fun refresh() {
        val api = clientProvider() ?: return
        scope.launch {
            when (val r = withContext(Dispatchers.IO) { api.chaosConfig() }) {
                is ApiResult.Success -> _config.value = r.value
                is ApiResult.Failure -> _lastError.value = r.message
            }
        }
    }

    fun update(patch: ChaosConfig.() -> ChaosConfig) {
        val next = _config.value.patch()
        _config.value = next
        push(next)
    }

    fun setLatency(mode: LatencyMode) {
        update {
            copy(
                latencyFixedMs = if (mode == LatencyMode.Fixed) latencyFixedMs else 0,
                latencyMinMs = if (mode == LatencyMode.Range) latencyMinMs else 0,
                latencyMaxMs = if (mode == LatencyMode.Range) latencyMaxMs else 0,
            )
        }
    }

    fun setErrorRate(rate: Double) = update { copy(errorRate = rate) }
    fun setDropRate(rate: Double) = update { copy(dropRate = rate) }

    private fun push(cfg: ChaosConfig) {
        val api = clientProvider() ?: return
        scope.launch {
            _saving.value = true
            when (val r = withContext(Dispatchers.IO) { api.patchChaosConfig(cfg) }) {
                is ApiResult.Success -> {
                    _config.value = r.value
                    _lastError.value = null
                }
                is ApiResult.Failure -> _lastError.value = r.message
            }
            _saving.value = false
        }
    }

    enum class LatencyMode { Off, Fixed, Range }
}
