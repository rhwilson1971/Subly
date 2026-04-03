package net.cynreub.subly.domain.usecase

import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import net.cynreub.subly.domain.repository.SubscriptionRepository
import net.cynreub.subly.util.MainCoroutineRule
import net.cynreub.subly.util.testSubscription
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

class GetUpcomingSubscriptionsUseCaseTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private val repository: SubscriptionRepository = mockk()
    private lateinit var useCase: GetUpcomingSubscriptionsUseCase

    @Before
    fun setUp() {
        useCase = GetUpcomingSubscriptionsUseCase(repository)
    }

    @Test
    fun `default days parameter uses 30-day lookahead`() = runTest {
        val dateSlot = slot<LocalDate>()
        every { repository.getUpcomingSubscriptions(capture(dateSlot)) } returns flowOf(emptyList())

        useCase().test {
            awaitItem()
            awaitComplete()
        }

        val expectedCutoff = LocalDate.now().plusDays(30)
        assertEquals(expectedCutoff, dateSlot.captured)
    }

    @Test
    fun `custom days parameter passes correct cutoff date to repository`() = runTest {
        val dateSlot = slot<LocalDate>()
        every { repository.getUpcomingSubscriptions(capture(dateSlot)) } returns flowOf(emptyList())

        useCase(days = 7).test {
            awaitItem()
            awaitComplete()
        }

        val expectedCutoff = LocalDate.now().plusDays(7)
        assertEquals(expectedCutoff, dateSlot.captured)
    }

    @Test
    fun `passes through repository Flow result unchanged`() = runTest {
        val subscriptions = listOf(
            testSubscription(name = "Netflix"),
            testSubscription(name = "Spotify")
        )
        every { repository.getUpcomingSubscriptions(any()) } returns flowOf(subscriptions)

        useCase(days = 14).test {
            assertEquals(subscriptions, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun `returns empty list when no upcoming subscriptions`() = runTest {
        every { repository.getUpcomingSubscriptions(any()) } returns flowOf(emptyList())

        useCase(days = 30).test {
            assertEquals(emptyList<Any>(), awaitItem())
            awaitComplete()
        }
    }
}
