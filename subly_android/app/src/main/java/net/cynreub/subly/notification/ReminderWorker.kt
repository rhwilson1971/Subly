package net.cynreub.subly.notification

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import net.cynreub.subly.data.preferences.PreferencesManager
import net.cynreub.subly.domain.model.Subscription
import net.cynreub.subly.domain.repository.SubscriptionRepository
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@HiltWorker
class ReminderWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted workerParams: WorkerParameters,
    private val subscriptionRepository: SubscriptionRepository,
    private val preferencesManager: PreferencesManager,
    private val notificationHelper: NotificationHelper
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            // Check if notifications are enabled
            val preferences = preferencesManager.notificationPreferences.first()
            if (!preferences.notificationsEnabled) {
                return Result.success()
            }

            // Get subscriptions due within the next 7 days
            val targetDate = LocalDate.now().plusDays(7)
            val upcomingSubscriptions = subscriptionRepository.getUpcomingSubscriptions(targetDate).first()

            // Filter active subscriptions that need reminders
            val subscriptionsToNotify = upcomingSubscriptions.filter { subscription ->
                subscription.isActive && shouldNotify(subscription)
            }

            // Send individual notifications
            subscriptionsToNotify.forEach { subscription ->
                notificationHelper.sendNotificationForSubscription(subscription)
            }

            // Send group notification if multiple subscriptions
            if (subscriptionsToNotify.size > 1) {
                notificationHelper.sendGroupNotification(subscriptionsToNotify)
            }

            Result.success()
        } catch (e: Exception) {
            // Log error and retry
            Result.retry()
        }
    }

    private fun shouldNotify(subscription: Subscription): Boolean {
        val daysUntilDue = ChronoUnit.DAYS.between(
            LocalDate.now(),
            subscription.nextBillingDate
        ).toInt()

        // Notify if within the reminder window (0 to reminderDaysBefore days)
        return daysUntilDue >= 0 && daysUntilDue <= subscription.reminderDaysBefore
    }
}
