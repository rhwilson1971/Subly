package net.cynreub.subly.ui.settings

import net.cynreub.subly.data.preferences.StorageProviderPreference
import net.cynreub.subly.data.preferences.ThemePreference

data class SettingsUiState(
    val notificationsEnabled: Boolean = true,
    val morningNotificationTime: String = "09:00",
    val eveningNotificationTime: String = "18:00",
    val defaultReminderDays: Int = 2,
    val hasNotificationPermission: Boolean = false,
    val showPermissionDialog: Boolean = false,
    val showMorningTimePicker: Boolean = false,
    val showEveningTimePicker: Boolean = false,
    val isLoading: Boolean = true,
    val error: String? = null,
    val selectedTheme: ThemePreference = ThemePreference.SYSTEM,
    val selectedStorageProvider: StorageProviderPreference = StorageProviderPreference.FIREBASE,
    val googleDriveAccountEmail: String? = null,
    val isDropboxConnected: Boolean = false,
    val oneDriveAccountEmail: String? = null
)
