package net.cynreub.subly.ui.home

import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import net.cynreub.subly.domain.usecase.CategorySpend
import net.cynreub.subly.domain.usecase.GetCategorySpendUseCase
import net.cynreub.subly.domain.usecase.GetSubscriptionStatsUseCase
import net.cynreub.subly.domain.usecase.GetUpcomingSubscriptionsUseCase
import net.cynreub.subly.domain.usecase.SubscriptionStats
import net.cynreub.subly.util.MainCoroutineRule
import net.cynreub.subly.util.testSubscription
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class HomeViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private val getUpcomingSubscriptionsUseCase: GetUpcomingSubscriptionsUseCase = mockk()
    private val getSubscriptionStatsUseCase: GetSubscriptionStatsUseCase = mockk()
    private val getCategorySpendUseCase: GetCategorySpendUseCase = mockk()

    private val defaultStats = SubscriptionStats(
        totalMonthly = 0.0,
        totalYearly = 0.0,
        activeCount = 0,
        categoryBreakdown = emptyMap()
    )

    private fun createViewModel() = HomeViewModel(
        getUpcomingSubscriptionsUseCase,
        getSubscriptionStatsUseCase,
        getCategorySpendUseCase
    )

    @Before
    fun setUp() {
        every { getUpcomingSubscriptionsUseCase(days = 30) } returns flowOf(emptyList())
        every { getSubscriptionStatsUseCase() } returns flowOf(defaultStats)
        every { getCategorySpendUseCase() } returns flowOf(emptyList())
    }

    @Test
    fun `initial state has isLoading true`() {
        val upstreamFlow = MutableSharedFlow<List<net.cynreub.subly.domain.model.Subscription>>()
        every { getUpcomingSubscriptionsUseCase(days = 30) } returns upstreamFlow

        val viewModel = createViewModel()

        assertTrue(viewModel.uiState.value.isLoading)
    }

    @Test
    fun `loads dashboard data on init and clears isLoading`() = runTest {
        val subscriptions = listOf(testSubscription(name = "Netflix"))

        every { getUpcomingSubscriptionsUseCase(days = 30) } returns flowOf(subscriptions)
        every { getSubscriptionStatsUseCase() } returns flowOf(defaultStats)
        every { getCategorySpendUseCase() } returns flowOf(emptyList())

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(subscriptions, state.upcomingSubscriptions)
            assertEquals(defaultStats, state.stats)
            assertEquals(false, state.isLoading)
            assertNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `upcoming subscriptions is limited to 5 items`() = runTest {
        val subscriptions = (1..10).map { testSubscription(name = "Sub $it") }
        every { getUpcomingSubscriptionsUseCase(days = 30) } returns flowOf(subscriptions)

        val viewModel = createViewModel()

        assertEquals(5, viewModel.uiState.value.upcomingSubscriptions.size)
    }

    @Test
    fun `error state set when use case throws`() = runTest {
        every { getUpcomingSubscriptionsUseCase(days = 30) } returns flow {
            throw RuntimeException("Network error")
        }

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNotNull(state.error)
            assertTrue(state.error!!.contains("Network error"))
            assertEquals(false, state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `error state uses fallback message when exception message is null`() = runTest {
        every { getUpcomingSubscriptionsUseCase(days = 30) } returns flow {
            throw RuntimeException()
        }

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNotNull(state.error)
            assertEquals("Unknown error occurred", state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `refresh sets isLoading true then reloads data`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Consume initial loaded state

            viewModel.refresh()

            val loadingState = awaitItem()
            assertTrue(loadingState.isLoading)

            val loadedState = awaitItem()
            assertEquals(false, loadedState.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `categorySpend is populated from use case`() = runTest {
        val category = net.cynreub.subly.util.testCategory()
        val categorySpend = listOf(
            CategorySpend(category = category, monthlyAmount = 15.99, percentage = 1.0f)
        )
        every { getCategorySpendUseCase() } returns flowOf(categorySpend)

        val viewModel = createViewModel()

        assertEquals(categorySpend, viewModel.uiState.value.categorySpend)
    }
}
