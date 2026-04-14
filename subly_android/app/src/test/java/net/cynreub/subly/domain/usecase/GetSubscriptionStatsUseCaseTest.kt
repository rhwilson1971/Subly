package net.cynreub.subly.domain.usecase

import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import net.cynreub.subly.domain.model.BillingFrequency
import net.cynreub.subly.domain.model.Category
import net.cynreub.subly.domain.repository.SubscriptionRepository
import net.cynreub.subly.util.MainCoroutineRule
import net.cynreub.subly.util.testSubscription
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class GetSubscriptionStatsUseCaseTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private val repository: SubscriptionRepository = mockk()
    private lateinit var useCase: GetSubscriptionStatsUseCase

    @Before
    fun setUp() {
        useCase = GetSubscriptionStatsUseCase(repository)
    }

    @Test
    fun `returns zero stats when there are no active subscriptions`() = runTest {
        every { repository.getActiveSubscriptions() } returns flowOf(emptyList())

        useCase().test {
            val stats = awaitItem()
            assertEquals(0.0, stats.totalMonthly, 0.001)
            assertEquals(0.0, stats.totalYearly, 0.001)
            assertEquals(0, stats.activeCount)
            assertEquals(emptyMap<Any, Any>(), stats.categoryBreakdown)
            awaitComplete()
        }
    }

    @Test
    fun `totalYearly is always 12 times totalMonthly`() = runTest {
        val sub = testSubscription(amount = 10.0, frequency = BillingFrequency.MONTHLY)
        every { repository.getActiveSubscriptions() } returns flowOf(listOf(sub))

        useCase().test {
            val stats = awaitItem()
            assertEquals(stats.totalMonthly * 12, stats.totalYearly, 0.001)
            awaitComplete()
        }
    }

    @Test
    fun `activeCount reflects number of subscriptions in the list`() = runTest {
        val subscriptions = listOf(
            testSubscription(name = "Netflix"),
            testSubscription(name = "Spotify"),
            testSubscription(name = "GitHub")
        )
        every { repository.getActiveSubscriptions() } returns flowOf(subscriptions)

        useCase().test {
            assertEquals(3, awaitItem().activeCount)
            awaitComplete()
        }
    }

    @Test
    fun `categoryBreakdown groups subscriptions by categoryId`() = runTest {
        val streamingId = Category.ID_STREAMING
        val softwareId = Category.ID_SOFTWARE

        val subscriptions = listOf(
            testSubscription(categoryId = streamingId, amount = 10.0, frequency = BillingFrequency.MONTHLY),
            testSubscription(categoryId = streamingId, amount = 5.0, frequency = BillingFrequency.MONTHLY),
            testSubscription(categoryId = softwareId, amount = 20.0, frequency = BillingFrequency.MONTHLY)
        )
        every { repository.getActiveSubscriptions() } returns flowOf(subscriptions)

        useCase().test {
            val stats = awaitItem()
            assertEquals(15.0, stats.categoryBreakdown[streamingId]!!, 0.001)
            assertEquals(20.0, stats.categoryBreakdown[softwareId]!!, 0.001)
            awaitComplete()
        }
    }

    @Test
    fun `totalMonthly converts all frequencies to monthly equivalent`() = runTest {
        val subscriptions = listOf(
            testSubscription(amount = 12.0, frequency = BillingFrequency.ANNUAL),     // 1.0/month
            testSubscription(amount = 3.0, frequency = BillingFrequency.QUARTERLY),   // 1.0/month
            testSubscription(amount = 1.0, frequency = BillingFrequency.MONTHLY)      // 1.0/month
        )
        every { repository.getActiveSubscriptions() } returns flowOf(subscriptions)

        useCase().test {
            assertEquals(3.0, awaitItem().totalMonthly, 0.001)
            awaitComplete()
        }
    }
}
