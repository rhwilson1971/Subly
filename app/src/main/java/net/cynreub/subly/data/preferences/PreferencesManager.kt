package net.cynreub.subly.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import net.cynreub.subly.data.preferences.StorageProviderPreference
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
        val THEME = stringPreferencesKey("theme_preference")
        val STORAGE_PROVIDER = stringPreferencesKey("storage_provider")
        val GOOGLE_DRIVE_ACCOUNT_EMAIL = stringPreferencesKey("google_drive_account_email")
        val DROPBOX_CREDENTIAL = stringPreferencesKey("dropbox_credential")
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

    val themePreference: Flow<ThemePreference> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            when (preferences[PreferencesKeys.THEME]) {
                "LIGHT" -> ThemePreference.LIGHT
                "DARK" -> ThemePreference.DARK
                else -> ThemePreference.SYSTEM
            }
        }

    suspend fun updateTheme(theme: ThemePreference) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.THEME] = theme.name
        }
    }

    val storageProviderPreference: Flow<StorageProviderPreference> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            when (preferences[PreferencesKeys.STORAGE_PROVIDER]) {
                "LOCAL" -> StorageProviderPreference.LOCAL
                "GOOGLE_DRIVE" -> StorageProviderPreference.GOOGLE_DRIVE
                "DROPBOX" -> StorageProviderPreference.DROPBOX
                else -> StorageProviderPreference.FIREBASE
            }
        }

    suspend fun updateStorageProvider(provider: StorageProviderPreference) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.STORAGE_PROVIDER] = provider.name
        }
    }

    val googleDriveAccountEmail: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> preferences[PreferencesKeys.GOOGLE_DRIVE_ACCOUNT_EMAIL] }

    suspend fun updateGoogleDriveAccountEmail(email: String?) {
        context.dataStore.edit { preferences ->
            if (email != null) {
                preferences[PreferencesKeys.GOOGLE_DRIVE_ACCOUNT_EMAIL] = email
            } else {
                preferences.remove(PreferencesKeys.GOOGLE_DRIVE_ACCOUNT_EMAIL)
            }
        }
    }

    val dropboxCredential: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { preferences -> preferences[PreferencesKeys.DROPBOX_CREDENTIAL] }

    suspend fun updateDropboxCredential(credential: String?) {
        context.dataStore.edit { preferences ->
            if (credential != null) {
                preferences[PreferencesKeys.DROPBOX_CREDENTIAL] = credential
            } else {
                preferences.remove(PreferencesKeys.DROPBOX_CREDENTIAL)
            }
        }
    }
}
