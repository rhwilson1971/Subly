package net.cynreub.subly.ui.settings.storage

import net.cynreub.subly.data.preferences.StorageProviderPreference

data class StorageProviderUiState(
    val selectedProvider: StorageProviderPreference = StorageProviderPreference.FIREBASE,
    val googleDriveAccountEmail: String? = null,
    val isDropboxConnected: Boolean = false,
    val oneDriveAccountEmail: String? = null,
    val lastSyncAt: Long? = null,
    val lastSyncError: String? = null,
    val isSyncing: Boolean = false,
    val isMigrating: Boolean = false,
    /** Current step during migration (0-indexed), null when not migrating. */
    val migrationStep: Int? = null,
    /** Total steps in the current migration, null when not migrating. */
    val migrationTotal: Int? = null,
    /** Provider the user has tapped but migration/confirmation is pending. */
    val pendingProvider: StorageProviderPreference? = null,
    val isLoading: Boolean = true
)
