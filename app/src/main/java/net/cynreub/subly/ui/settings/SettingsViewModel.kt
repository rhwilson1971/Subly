package net.cynreub.subly.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import net.cynreub.subly.data.preferences.PreferencesManager
import net.cynreub.subly.notification.NotificationScheduler
import net.cynreub.subly.notification.PermissionHandler
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val notificationScheduler: NotificationScheduler,
    private val permissionHandler: PermissionHandler
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            preferencesManager.notificationPreferences
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load settings: ${e.message}"
                    )
                }
                .collect { preferences ->
                    _uiState.value = SettingsUiState(
                        notificationsEnabled = preferences.notificationsEnabled,
                        morningNotificationTime = preferences.morningNotificationTime,
                        eveningNotificationTime = preferences.eveningNotificationTime,
                        defaultReminderDays = preferences.defaultReminderDays,
                        hasNotificationPermission = permissionHandler.isNotificationPermissionGranted(),
                        isLoading = false
                    )
                }
        }
    }

    fun onNotificationsEnabledChange(enabled: Boolean) {
        viewModelScope.launch {
            if (enabled && !_uiState.value.hasNotificationPermission) {
                // Show permission dialog before enabling
                _uiState.value = _uiState.value.copy(showPermissionDialog = true)
            } else {
                preferencesManager.updateNotificationsEnabled(enabled)

                if (enabled) {
                    scheduleNotifications()
                } else {
                    notificationScheduler.cancelReminders()
                }
            }
        }
    }

    fun onMorningTimeChange(time: String) {
        viewModelScope.launch {
            preferencesManager.updateMorningTime(time)
            if (_uiState.value.notificationsEnabled) {
                scheduleNotifications()
            }
        }
    }

    fun onEveningTimeChange(time: String) {
        viewModelScope.launch {
            preferencesManager.updateEveningTime(time)
            if (_uiState.value.notificationsEnabled) {
                scheduleNotifications()
            }
        }
    }

    fun onDefaultReminderDaysChange(days: Int) {
        viewModelScope.launch {
            preferencesManager.updateDefaultReminderDays(days)
        }
    }

    fun showMorningTimePicker() {
        _uiState.value = _uiState.value.copy(showMorningTimePicker = true)
    }

    fun dismissMorningTimePicker() {
        _uiState.value = _uiState.value.copy(showMorningTimePicker = false)
    }

    fun showEveningTimePicker() {
        _uiState.value = _uiState.value.copy(showEveningTimePicker = true)
    }

    fun dismissEveningTimePicker() {
        _uiState.value = _uiState.value.copy(showEveningTimePicker = false)
    }

    fun showPermissionDialog() {
        _uiState.value = _uiState.value.copy(showPermissionDialog = true)
    }

    fun dismissPermissionDialog() {
        _uiState.value = _uiState.value.copy(showPermissionDialog = false)
    }

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
        viewModelScope.launch {
            preferencesManager.updateNotificationsEnabled(false)
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
            eveningTime = state.eveningNotificationTime
        )
    }
}
