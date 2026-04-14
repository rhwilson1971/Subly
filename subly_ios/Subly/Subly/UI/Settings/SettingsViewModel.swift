import Foundation
import Observation

@Observable
final class SettingsViewModel {
    var uiState: SettingsUiState

    private let prefs: PreferencesManager
    private let authRepository: AuthRepository

    init(prefs: PreferencesManager = .shared, authRepository: AuthRepository) {
        self.prefs = prefs
        self.authRepository = authRepository
        self.uiState = SettingsUiState(
            notificationsEnabled: prefs.notificationsEnabled,
            morningReminderEnabled: prefs.morningReminderEnabled,
            eveningReminderEnabled: prefs.eveningReminderEnabled,
            morningReminderTime: prefs.morningReminderTime,
            eveningReminderTime: prefs.eveningReminderTime,
            reminderDaysBefore: prefs.reminderDaysBefore,
            selectedTheme: prefs.theme,
            selectedStorageProvider: prefs.storageProvider
        )
    }

    // MARK: - Notifications

    func onNotificationsEnabledChange(_ enabled: Bool) {
        prefs.notificationsEnabled = enabled
        uiState.notificationsEnabled = enabled
        if enabled {
            rescheduleNotifications()
        } else {
            NotificationScheduler.shared.cancelReminders()
        }
    }

    func onMorningReminderEnabledChange(_ enabled: Bool) {
        prefs.morningReminderEnabled = enabled
        uiState.morningReminderEnabled = enabled
        rescheduleNotifications()
    }

    func onEveningReminderEnabledChange(_ enabled: Bool) {
        prefs.eveningReminderEnabled = enabled
        uiState.eveningReminderEnabled = enabled
        rescheduleNotifications()
    }

    func onMorningTimeChange(_ time: String) {
        prefs.morningReminderTime = time
        uiState.morningReminderTime = time
        uiState.showMorningTimePicker = false
        rescheduleNotifications()
    }

    func onEveningTimeChange(_ time: String) {
        prefs.eveningReminderTime = time
        uiState.eveningReminderTime = time
        uiState.showEveningTimePicker = false
        rescheduleNotifications()
    }

    func onReminderDaysChange(_ days: Int) {
        let clamped = max(1, min(30, days))
        prefs.reminderDaysBefore = clamped
        uiState.reminderDaysBefore = clamped
    }

    private func rescheduleNotifications() {
        guard prefs.notificationsEnabled else { return }
        NotificationScheduler.shared.scheduleDailyReminders(
            morningTime: prefs.morningReminderTime,
            eveningTime: prefs.eveningReminderTime,
            morningEnabled: prefs.morningReminderEnabled,
            eveningEnabled: prefs.eveningReminderEnabled
        )
    }

    // MARK: - Appearance

    func onThemeChange(_ theme: ThemePreference) {
        prefs.theme = theme
        uiState.selectedTheme = theme
    }

    // MARK: - Storage

    func onStorageProviderChange(_ provider: StorageProviderPreference) {
        prefs.storageProvider = provider
        uiState.selectedStorageProvider = provider
    }

    // MARK: - Account

    func signOut() async {
        await authRepository.signOut()
    }
}
