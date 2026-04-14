package net.cynreub.subly.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import net.cynreub.subly.MainActivity
import net.cynreub.subly.R
import net.cynreub.subly.domain.model.Subscription
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_ID = "subscription_reminders"

        // Intent extras
        const val EXTRA_SUBSCRIPTION_ID = "subscription_id"
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                context.getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = context.getString(R.string.notification_channel_description)
                enableVibration(true)
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun sendNotificationForSubscription(subscription: Subscription) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_SUBSCRIPTION_ID, subscription.id.toString())
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            subscription.id.hashCode(), // Unique request code per subscription
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val dateFormatter = DateTimeFormatter.ofPattern("MMM dd")
        val formattedDate = subscription.nextBillingDate.format(dateFormatter)

        val daysUntil = ChronoUnit.DAYS.between(
            java.time.LocalDate.now(),
            subscription.nextBillingDate
        ).toInt()

        val title = when {
            daysUntil == 0 -> "Payment Due Today: ${subscription.name}"
            daysUntil == 1 -> "Payment Due Tomorrow: ${subscription.name}"
            else -> "Upcoming Payment: ${subscription.name}"
        }

        val message = when {
            daysUntil == 0 -> "${subscription.currency} ${String.format("%.2f", subscription.amount)} is due today"
            daysUntil == 1 -> "${subscription.currency} ${String.format("%.2f", subscription.amount)} is due tomorrow"
            else -> "${subscription.currency} ${String.format("%.2f", subscription.amount)} due on $formattedDate ($daysUntil days)"
        }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(
            subscription.id.hashCode(),
            notification
        )
    }

    fun sendGroupNotification(subscriptions: List<Subscription>) {
        if (subscriptions.isEmpty()) return

        val totalAmount = subscriptions.sumOf { it.amount }

        val title = if (subscriptions.size == 1) {
            "1 Upcoming Payment"
        } else {
            "${subscriptions.size} Upcoming Payments"
        }

        val message = "Total: USD ${String.format("%.2f", totalAmount)}"

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setGroup("subscription_reminders")
            .setGroupSummary(true)
            .build()

        NotificationManagerCompat.from(context).notify(
            Int.MAX_VALUE, // Summary notification ID
            notification
        )
    }
}
