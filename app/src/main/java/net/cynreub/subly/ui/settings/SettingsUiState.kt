package net.cynreub.subly.ui.settings

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
    val error: String? = null
)
