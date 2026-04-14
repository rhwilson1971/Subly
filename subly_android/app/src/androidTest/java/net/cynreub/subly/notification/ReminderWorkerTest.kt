package net.cynreub.subly.notification

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.work.ListenableWorker
import androidx.work.ListenableWorker.Result
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import net.cynreub.subly.data.preferences.NotificationPreferences
import net.cynreub.subly.data.preferences.PreferencesManager
import net.cynreub.subly.domain.model.BillingFrequency
import net.cynreub.subly.domain.model.Category
import net.cynreub.subly.domain.model.Subscription
import net.cynreub.subly.domain.repository.SubscriptionRepository
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate
import java.util.UUID

/**
 * Instrumented tests for [ReminderWorker].
 *
 * Uses [WorkManagerTestInitHelper] for isolated WorkManager execution and
 * MockK to stub injected dependencies without Hilt. The worker is built via
 * [TestListenableWorkerBuilder] with an anonymous [WorkerFactory] that
 * directly instantiates [ReminderWorker], exercising the real constructor.
 */
@RunWith(AndroidJUnit4::class)
class ReminderWorkerTest {

    private lateinit var context: Context
    private lateinit var subscriptionRepository: SubscriptionRepository
    private lateinit var preferencesManager: PreferencesManager
    private lateinit var notificationHelper: NotificationHelper

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        WorkManagerTestInitHelper.initializeTestWorkManager(context)

        subscriptionRepository = mockk(relaxed = true)
        preferencesManager = mockk(relaxed = true)
        notificationHelper = mockk(relaxed = true)
    }

    private fun buildWorker(): ReminderWorker =
        TestListenableWorkerBuilder<ReminderWorker>(context)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ): ListenableWorker? {
                    return if (workerClassName == ReminderWorker::class.java.name)
                        ReminderWorker(
                            appContext,
                            workerParameters,
                            subscriptionRepository,
                            preferencesManager,
                            notificationHelper
                        )
                    else null
                }
            })
            .build()

    // Stubs a subscription due within its reminder window (today + 1 day, reminder = 2 days).
    private fun dueSubscription() = Subscription(
        id = UUID.randomUUID(),
        name = "Netflix",
        categoryId = Category.ID_STREAMING,
        amount = 15.99,
        currency = "USD",
        frequency = BillingFrequency.MONTHLY,
        startDate = LocalDate.now().minusMonths(1),
        nextBillingDate = LocalDate.now().plusDays(1),
        paymentMethodId = null,
        notes = null,
        isActive = true,
        reminderDaysBefore = 2
    )

    @Test
    fun returns_success_when_subscriptions_are_due() = runTest {
        val prefs = NotificationPreferences(notificationsEnabled = true)
        every { preferencesManager.notificationPreferences } returns flowOf(prefs)
        every { subscriptionRepository.getUpcomingSubscriptions(any()) } returns flowOf(listOf(dueSubscription()))

        val result = buildWorker().doWork()

        assertEquals(Result.success(), result)
    }

    @Test
    fun returns_success_as_no_op_when_no_subscriptions_are_due() = runTest {
        val prefs = NotificationPreferences(notificationsEnabled = true)
        every { preferencesManager.notificationPreferences } returns flowOf(prefs)
        every { subscriptionRepository.getUpcomingSubscriptions(any()) } returns flowOf(emptyList())

        val result = buildWorker().doWork()

        assertEquals(Result.success(), result)
    }

    @Test
    fun returns_success_immediately_when_notifications_are_disabled() = runTest {
        val prefs = NotificationPreferences(notificationsEnabled = false)
        every { preferencesManager.notificationPreferences } returns flowOf(prefs)

        val result = buildWorker().doWork()

        assertEquals(Result.success(), result)
    }
}
