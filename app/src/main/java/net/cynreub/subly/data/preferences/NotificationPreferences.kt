package net.cynreub.subly.data.preferences

data class NotificationPreferences(
    val notificationsEnabled: Boolean = true,
    val morningNotificationTime: String = "09:00", // HH:mm format
    val eveningNotificationTime: String = "18:00",
    val defaultReminderDays: Int = 2
)
