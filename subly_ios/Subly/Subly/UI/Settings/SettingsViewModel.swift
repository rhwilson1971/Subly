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
            selectedStorageProvider: prefs.storageProvider,
            isSignedIn: authRepository.currentUser != nil,
            accountEmail: authRepository.currentUser?.email
        )
    }

    // MARK: - Notifications

    func onNotificationsEnabledChange(_ enabled: Bool) {
        prefs.notificationsEnabled = enabled
        uiState.notificationsEnabled = enabled
    }

    func onMorningReminderEnabledChange(_ enabled: Bool) {
        prefs.morningReminderEnabled = enabled
        uiState.morningReminderEnabled = enabled
    }

    func onEveningReminderEnabledChange(_ enabled: Bool) {
        prefs.eveningReminderEnabled = enabled
        uiState.eveningReminderEnabled = enabled
    }

    func onMorningTimeChange(_ time: String) {
        prefs.morningReminderTime = time
        uiState.morningReminderTime = time
        uiState.showMorningTimePicker = false
    }

    func onEveningTimeChange(_ time: String) {
        prefs.eveningReminderTime = time
        uiState.eveningReminderTime = time
        uiState.showEveningTimePicker = false
    }

    func onReminderDaysChange(_ days: Int) {
        let clamped = max(1, min(30, days))
        prefs.reminderDaysBefore = clamped
        uiState.reminderDaysBefore = clamped
    }

    // MARK: - Appearance

    func onThemeChange(_ theme: ThemePreference) {
        prefs.theme = theme
        uiState.selectedTheme = theme
    }

    // MARK: - Storage

    func onStorageProviderChange(_ provider: StorageProviderPreference) {
        if provider == .firebase && authRepository.currentUser == nil {
            uiState.requiresSignUp = true
            return
        }
        prefs.storageProvider = provider
        uiState.selectedStorageProvider = provider
    }

    func dismissSignUpPrompt() {
        uiState.requiresSignUp = false
    }

    // MARK: - Account

    func refreshAccountState() {
        uiState.isSignedIn = authRepository.currentUser != nil
        uiState.accountEmail = authRepository.currentUser?.email
    }

    func signOut() async {
        await authRepository.signOut()
        prefs.storageProvider = .local
        uiState.selectedStorageProvider = .local
        refreshAccountState()
    }
}
