package net.cynreub.subly.domain.usecase

import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import net.cynreub.subly.domain.model.BillingFrequency
import net.cynreub.subly.domain.repository.SubscriptionRepository
import net.cynreub.subly.util.MainCoroutineRule
import net.cynreub.subly.util.testSubscription
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CalculateMonthlyTotalUseCaseTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private val repository: SubscriptionRepository = mockk()
    private lateinit var useCase: CalculateMonthlyTotalUseCase

    @Before
    fun setUp() {
        useCase = CalculateMonthlyTotalUseCase(repository)
    }

    @Test
    fun `returns 0 when there are no active subscriptions`() = runTest {
        every { repository.getActiveSubscriptions() } returns flowOf(emptyList())

        useCase().test {
            assertEquals(0.0, awaitItem(), 0.001)
            awaitComplete()
        }
    }

    @Test
    fun `MONTHLY frequency counts full amount`() = runTest {
        val sub = testSubscription(amount = 10.0, frequency = BillingFrequency.MONTHLY)
        every { repository.getActiveSubscriptions() } returns flowOf(listOf(sub))

        useCase().test {
            assertEquals(10.0, awaitItem(), 0.001)
            awaitComplete()
        }
    }

    @Test
    fun `WEEKLY frequency multiplies amount by 4`() = runTest {
        val sub = testSubscription(amount = 5.0, frequency = BillingFrequency.WEEKLY)
        every { repository.getActiveSubscriptions() } returns flowOf(listOf(sub))

        useCase().test {
            assertEquals(20.0, awaitItem(), 0.001)
            awaitComplete()
        }
    }

    @Test
    fun `QUARTERLY frequency divides amount by 3`() = runTest {
        val sub = testSubscription(amount = 30.0, frequency = BillingFrequency.QUARTERLY)
        every { repository.getActiveSubscriptions() } returns flowOf(listOf(sub))

        useCase().test {
            assertEquals(10.0, awaitItem(), 0.001)
            awaitComplete()
        }
    }

    @Test
    fun `SEMI_ANNUAL frequency divides amount by 6`() = runTest {
        val sub = testSubscription(amount = 60.0, frequency = BillingFrequency.SEMI_ANNUAL)
        every { repository.getActiveSubscriptions() } returns flowOf(listOf(sub))

        useCase().test {
            assertEquals(10.0, awaitItem(), 0.001)
            awaitComplete()
        }
    }

    @Test
    fun `ANNUAL frequency divides amount by 12`() = runTest {
        val sub = testSubscription(amount = 120.0, frequency = BillingFrequency.ANNUAL)
        every { repository.getActiveSubscriptions() } returns flowOf(listOf(sub))

        useCase().test {
            assertEquals(10.0, awaitItem(), 0.001)
            awaitComplete()
        }
    }

    @Test
    fun `CUSTOM frequency counts full amount`() = runTest {
        val sub = testSubscription(amount = 25.0, frequency = BillingFrequency.CUSTOM)
        every { repository.getActiveSubscriptions() } returns flowOf(listOf(sub))

        useCase().test {
            assertEquals(25.0, awaitItem(), 0.001)
            awaitComplete()
        }
    }

    @Test
    fun `sums multiple subscriptions with mixed frequencies`() = runTest {
        val subscriptions = listOf(
            testSubscription(amount = 10.0, frequency = BillingFrequency.MONTHLY),  // 10.0
            testSubscription(amount = 5.0, frequency = BillingFrequency.WEEKLY),    // 20.0
            testSubscription(amount = 120.0, frequency = BillingFrequency.ANNUAL)   // 10.0
        )
        every { repository.getActiveSubscriptions() } returns flowOf(subscriptions)

        useCase().test {
            assertEquals(40.0, awaitItem(), 0.001)
            awaitComplete()
        }
    }

    @Test
    fun `emits updated total when repository emits new list`() = runTest {
        val first = listOf(testSubscription(amount = 10.0, frequency = BillingFrequency.MONTHLY))
        val second = listOf(
            testSubscription(amount = 10.0, frequency = BillingFrequency.MONTHLY),
            testSubscription(amount = 20.0, frequency = BillingFrequency.MONTHLY)
        )
        every { repository.getActiveSubscriptions() } returns flowOf(first, second)

        useCase().test {
            assertEquals(10.0, awaitItem(), 0.001)
            assertEquals(30.0, awaitItem(), 0.001)
            awaitComplete()
        }
    }
}
