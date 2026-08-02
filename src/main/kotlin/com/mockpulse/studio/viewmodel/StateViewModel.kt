package com.mockpulse.studio.viewmodel

import com.mockpulse.studio.core.model.Recordings
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
 * Manages the in-memory recording store of the backend.
 */
class StateViewModel(
    private val scope: CoroutineScope,
    private val clientProvider: () -> MockPulseApiClient?,
) {
    private val _recordings = MutableStateFlow(Recordings())
    val recordings: StateFlow<Recordings> = _recordings.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    private val _lastError = MutableStateFlow<String?>(null)
    val lastError: StateFlow<String?> = _lastError.asStateFlow()

    fun refresh() {
        val api = clientProvider() ?: return
        scope.launch {
            _loading.value = true
            when (val r = withContext(Dispatchers.IO) { api.recordings() }) {
                is ApiResult.Success -> {
                    _recordings.value = r.value
                    _lastError.value = null
                }
                is ApiResult.Failure -> _lastError.value = r.message
            }
            _loading.value = false
        }
    }

    fun clear() {
        val api = clientProvider() ?: return
        scope.launch {
            when (val r = withContext(Dispatchers.IO) { api.clearRecordings() }) {
                is ApiResult.Success -> {
                    _recordings.value = r.value
                    _lastError.value = null
                }
                is ApiResult.Failure -> _lastError.value = r.message
            }
        }
    }
}
