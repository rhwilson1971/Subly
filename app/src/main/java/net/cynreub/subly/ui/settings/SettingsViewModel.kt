package net.cynreub.subly.ui.settings

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import net.cynreub.subly.data.preferences.PreferencesManager
import net.cynreub.subly.data.preferences.StorageProviderPreference
import net.cynreub.subly.data.preferences.ThemePreference
import net.cynreub.subly.data.remote.dropbox.DropboxAuthManager
import net.cynreub.subly.data.remote.gdrive.GoogleDriveAuthManager
import net.cynreub.subly.data.remote.onedrive.MsalAuthManager
import net.cynreub.subly.notification.NotificationScheduler
import net.cynreub.subly.notification.PermissionHandler
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val notificationScheduler: NotificationScheduler,
    private val permissionHandler: PermissionHandler,
    private val googleDriveAuthManager: GoogleDriveAuthManager,
    private val dropboxAuthManager: DropboxAuthManager,
    private val msalAuthManager: MsalAuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            // Kotlin combine() supports up to 5 typed flows; use nested combines for 6.
            val coreFlow = combine(
                preferencesManager.notificationPreferences,
                preferencesManager.themePreference,
                preferencesManager.storageProviderPreference
            ) { notifications, theme, storageProvider ->
                Triple(notifications, theme, storageProvider)
            }
            val cloudFlow = combine(
                preferencesManager.googleDriveAccountEmail,
                preferencesManager.dropboxCredential,
                preferencesManager.oneDriveAccountEmail
            ) { driveEmail, dropboxCred, oneDriveEmail ->
                Triple(driveEmail, dropboxCred, oneDriveEmail)
            }
            combine(coreFlow, cloudFlow) { (notifications, theme, storageProvider), (driveEmail, dropboxCred, oneDriveEmail) ->
                SettingsUiState(
                    notificationsEnabled = notifications.notificationsEnabled,
                    morningNotificationTime = notifications.morningNotificationTime,
                    eveningNotificationTime = notifications.eveningNotificationTime,
                    defaultReminderDays = notifications.defaultReminderDays,
                    morningReminderEnabled = notifications.morningReminderEnabled,
                    eveningReminderEnabled = notifications.eveningReminderEnabled,
                    hasNotificationPermission = permissionHandler.isNotificationPermissionGranted(),
                    isLoading = false,
                    selectedTheme = theme,
                    selectedStorageProvider = storageProvider,
                    googleDriveAccountEmail = driveEmail,
                    isDropboxConnected = dropboxCred != null,
                    oneDriveAccountEmail = oneDriveEmail
                )
            }
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load settings: ${e.message}"
                    )
                }
                .collect { _uiState.value = it }
        }
    }

    fun onNotificationsEnabledChange(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled && !_uiState.value.hasNotificationPermission) {
                _uiState.value = _uiState.value.copy(showPermissionDialog = true)
            } else {
                preferencesManager.updateNotificationsEnabled(enabled)
                if (enabled) scheduleNotifications() else notificationScheduler.cancelReminders()
            }
        }
    }

    fun onMorningTimeChange(time: String) {
        viewModelScope.launch {
            preferencesManager.updateMorningTime(time)
            if (_uiState.value.notificationsEnabled) scheduleNotifications()
        }
    }

    fun onEveningTimeChange(time: String) {
        viewModelScope.launch {
            preferencesManager.updateEveningTime(time)
            if (_uiState.value.notificationsEnabled) scheduleNotifications()
        }
    }

    fun onDefaultReminderDaysChange(days: Int) {
        viewModelScope.launch { preferencesManager.updateDefaultReminderDays(days) }
    }

    fun onMorningReminderEnabledChange(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateMorningReminderEnabled(enabled)
            if (_uiState.value.notificationsEnabled) scheduleNotifications()
        }
    }

    fun onEveningReminderEnabledChange(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.updateEveningReminderEnabled(enabled)
            if (_uiState.value.notificationsEnabled) scheduleNotifications()
        }
    }

    fun showMorningTimePicker() { _uiState.value = _uiState.value.copy(showMorningTimePicker = true) }
    fun dismissMorningTimePicker() { _uiState.value = _uiState.value.copy(showMorningTimePicker = false) }
    fun showEveningTimePicker() { _uiState.value = _uiState.value.copy(showEveningTimePicker = true) }
    fun dismissEveningTimePicker() { _uiState.value = _uiState.value.copy(showEveningTimePicker = false) }
    fun showPermissionDialog() { _uiState.value = _uiState.value.copy(showPermissionDialog = true) }
    fun dismissPermissionDialog() { _uiState.value = _uiState.value.copy(showPermissionDialog = false) }

    fun onPermissionGranted() {
        _uiState.value = _uiState.value.copy(
            hasNotificationPermission = true,
            showPermissionDialog = false
        )
        viewModelScope.launch {
            preferencesManager.updateNotificationsEnabled(true)
            scheduleNotifications()
        }
    }

    fun onPermissionDenied() {
        _uiState.value = _uiState.value.copy(
            hasNotificationPermission = false,
            showPermissionDialog = false,
            notificationsEnabled = false
        )
        viewModelScope.launch { preferencesManager.updateNotificationsEnabled(false) }
    }

    fun onThemeChange(theme: ThemePreference) {
        viewModelScope.launch { preferencesManager.updateTheme(theme) }
    }

    fun onStorageProviderChange(provider: StorageProviderPreference) {
        viewModelScope.launch { preferencesManager.updateStorageProvider(provider) }
    }

    // --- Google Drive ---

    fun getGoogleDriveSignInIntent(): Intent =
        googleDriveAuthManager.buildSignInClient().signInIntent

    fun onGoogleDriveConnected(account: GoogleSignInAccount) {
        viewModelScope.launch {
            preferencesManager.updateGoogleDriveAccountEmail(account.email)
            preferencesManager.updateStorageProvider(StorageProviderPreference.GOOGLE_DRIVE)
        }
    }

    fun disconnectGoogleDrive() {
        viewModelScope.launch {
            googleDriveAuthManager.buildSignInClient().signOut()
            preferencesManager.updateGoogleDriveAccountEmail(null)
            preferencesManager.updateStorageProvider(StorageProviderPreference.FIREBASE)
        }
    }

    // --- Dropbox ---

    /** Returns the browser URL to open to start the Dropbox OAuth flow. */
    fun getDropboxAuthUrl(): String = dropboxAuthManager.buildAuthUrl()

    fun disconnectDropbox() {
        viewModelScope.launch {
            preferencesManager.updateDropboxCredential(null)
            preferencesManager.updateStorageProvider(StorageProviderPreference.FIREBASE)
        }
    }

    // --- OneDrive ---

    fun disconnectOneDrive() {
        viewModelScope.launch {
            msalAuthManager.signOut()
            preferencesManager.updateOneDriveAccountEmail(null)
            preferencesManager.updateStorageProvider(StorageProviderPreference.FIREBASE)
        }
    }

    fun refreshPermissionStatus() {
        _uiState.value = _uiState.value.copy(
            hasNotificationPermission = permissionHandler.isNotificationPermissionGranted()
        )
    }

    private fun scheduleNotifications() {
        val state = _uiState.value
        notificationScheduler.scheduleDailyReminders(
            morningTime = state.morningNotificationTime,
            eveningTime = state.eveningNotificationTime,
            morningEnabled = state.morningReminderEnabled,
            eveningEnabled = state.eveningReminderEnabled
        )
    }
}
