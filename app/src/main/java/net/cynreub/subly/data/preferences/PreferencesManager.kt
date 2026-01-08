package net.cynreub.subly.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "notification_preferences")

@Singleton
class PreferencesManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object PreferencesKeys {
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val MORNING_TIME = stringPreferencesKey("morning_notification_time")
        val EVENING_TIME = stringPreferencesKey("evening_notification_time")
        val DEFAULT_REMINDER_DAYS = intPreferencesKey("default_reminder_days")
    }

    val notificationPreferences: Flow<NotificationPreferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            NotificationPreferences(
                notificationsEnabled = preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] ?: true,
                morningNotificationTime = preferences[PreferencesKeys.MORNING_TIME] ?: "09:00",
                eveningNotificationTime = preferences[PreferencesKeys.EVENING_TIME] ?: "18:00",
                defaultReminderDays = preferences[PreferencesKeys.DEFAULT_REMINDER_DAYS] ?: 2
            )
        }

    suspend fun updateNotificationsEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS_ENABLED] = enabled
        }
    }

    suspend fun updateMorningTime(time: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.MORNING_TIME] = time
        }
    }

    suspend fun updateEveningTime(time: String) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.EVENING_TIME] = time
        }
    }

    suspend fun updateDefaultReminderDays(days: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DEFAULT_REMINDER_DAYS] = days
        }
    }
}
