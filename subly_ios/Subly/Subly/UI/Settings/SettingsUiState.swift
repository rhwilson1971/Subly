import Foundation

struct SettingsUiState {
    var notificationsEnabled: Bool = true
    var morningReminderEnabled: Bool = true
    var eveningReminderEnabled: Bool = false
    var morningReminderTime: String = "09:00"
    var eveningReminderTime: String = "18:00"
    var reminderDaysBefore: Int = 2

    var selectedTheme: ThemePreference = .system
    var selectedStorageProvider: StorageProviderPreference = .firebase

    var showMorningTimePicker: Bool = false
    var showEveningTimePicker: Bool = false
    var showSignOutConfirmation: Bool = false

    var error: String? = nil
}
