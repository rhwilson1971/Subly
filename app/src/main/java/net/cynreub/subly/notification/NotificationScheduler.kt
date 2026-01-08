package net.cynreub.subly.notification

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.Duration
import java.time.LocalTime
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val MORNING_WORK_NAME = "morning_reminder_work"
        private const val EVENING_WORK_NAME = "evening_reminder_work"
    }

    fun scheduleDailyReminders(morningTime: String, eveningTime: String) {
        scheduleMorningReminder(morningTime)
        scheduleEveningReminder(eveningTime)
    }

    private fun scheduleMorningReminder(time: String) {
        val initialDelay = calculateInitialDelay(time)

        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
            24, TimeUnit.HOURS,
            15, TimeUnit.MINUTES // Flex interval
        )
            .setInitialDelay(initialDelay.toMillis(), TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            MORNING_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    private fun scheduleEveningReminder(time: String) {
        val initialDelay = calculateInitialDelay(time)

        val workRequest = PeriodicWorkRequestBuilder<ReminderWorker>(
            24, TimeUnit.HOURS,
            15, TimeUnit.MINUTES
        )
            .setInitialDelay(initialDelay.toMillis(), TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiresBatteryNotLow(true)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            EVENING_WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            workRequest
        )
    }

    private fun calculateInitialDelay(timeString: String): Duration {
        val (hour, minute) = timeString.split(":").map { it.toInt() }
        val targetTime = LocalTime.of(hour, minute)
        val now = LocalTime.now()

        val duration = if (now.isBefore(targetTime)) {
            Duration.between(now, targetTime)
        } else {
            // Schedule for tomorrow
            Duration.between(now, targetTime).plusHours(24)
        }

        return duration
    }

    fun cancelReminders() {
        WorkManager.getInstance(context).cancelUniqueWork(MORNING_WORK_NAME)
        WorkManager.getInstance(context).cancelUniqueWork(EVENING_WORK_NAME)
    }
}
