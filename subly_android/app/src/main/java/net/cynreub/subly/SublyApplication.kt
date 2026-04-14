package net.cynreub.subly

import android.app.Application
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import net.cynreub.subly.data.preferences.PreferencesManager
import net.cynreub.subly.notification.NotificationScheduler
import javax.inject.Inject

@HiltAndroidApp
class SublyApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: androidx.hilt.work.HiltWorkerFactory

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    @Inject
    lateinit var preferencesManager: PreferencesManager

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        initializeNotifications()
    }

    private fun initializeNotifications() {
        applicationScope.launch {
            try {
                val preferences = preferencesManager.notificationPreferences.first()
                if (preferences.notificationsEnabled) {
                    notificationScheduler.scheduleDailyReminders(
                        morningTime = preferences.morningNotificationTime,
                        eveningTime = preferences.eveningNotificationTime
                    )
                }
            } catch (e: Exception) {
                // Log error but don't crash app
                e.printStackTrace()
            }
        }
    }
}
