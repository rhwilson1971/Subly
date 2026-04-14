package net.cynreub.subly.data.sync

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Shared singleton that records the last successful sync timestamp and any sync error.
 * Written by [DelegatingSyncProvider] after each delegated operation; read by
 * [net.cynreub.subly.ui.settings.storage.StorageProviderViewModel].
 */
@Singleton
class SyncStateTracker @Inject constructor() {

    private val _lastSyncAt = MutableStateFlow<Long?>(null)
    val lastSyncAt: StateFlow<Long?> = _lastSyncAt.asStateFlow()

    private val _lastSyncError = MutableStateFlow<String?>(null)
    val lastSyncError: StateFlow<String?> = _lastSyncError.asStateFlow()

    fun onSyncSuccess() {
        _lastSyncAt.value = System.currentTimeMillis()
        _lastSyncError.value = null
    }

    fun onSyncError(message: String) {
        _lastSyncError.value = message
    }

    fun clearError() {
        _lastSyncError.value = null
    }
}
