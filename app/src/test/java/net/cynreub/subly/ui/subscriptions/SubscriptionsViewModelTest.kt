package net.cynreub.subly.ui.subscriptions

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import net.cynreub.subly.domain.model.BillingFrequency
import net.cynreub.subly.domain.repository.CategoryRepository
import net.cynreub.subly.domain.repository.SubscriptionRepository
import net.cynreub.subly.util.MainCoroutineRule
import net.cynreub.subly.util.testCategory
import net.cynreub.subly.util.testSubscription
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.util.UUID

class SubscriptionsViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private val subscriptionRepository: SubscriptionRepository = mockk()
    private val categoryRepository: CategoryRepository = mockk()

    private val categoryStreaming = testCategory(
        id = UUID.randomUUID(),
        name = "STREAMING",
        displayName = "Streaming"
    )
    private val categoryMusic = testCategory(
        id = UUID.randomUUID(),
        name = "MUSIC",
        displayName = "Music"
    )

    private val subNetflix = testSubscription(
        name = "Netflix",
        categoryId = categoryStreaming.id,
        amount = 15.99,
        isActive = true,
        frequency = BillingFrequency.MONTHLY,
        nextBillingDate = LocalDate.of(2026, 4, 15)
    )
    private val subSpotify = testSubscription(
        name = "Spotify",
        categoryId = categoryMusic.id,
        amount = 9.99,
        isActive = false,
        frequency = BillingFrequency.MONTHLY,
        nextBillingDate = LocalDate.of(2026, 4, 10)
    )
    private val subHulu = testSubscription(
        name = "Hulu",
        categoryId = categoryStreaming.id,
        amount = 12.00,
        isActive = true,
        frequency = BillingFrequency.ANNUAL,
        nextBillingDate = LocalDate.of(2026, 5, 1)
    )

    @Before
    fun setUp() {
        every { subscriptionRepository.getAllSubscriptions() } returns flowOf(
            listOf(subNetflix, subSpotify, subHulu)
        )
        every { categoryRepository.getAllCategories() } returns flowOf(
            listOf(categoryStreaming, categoryMusic)
        )
    }

    private fun createViewModel(savedStateHandle: SavedStateHandle = SavedStateHandle()) =
        SubscriptionsViewModel(subscriptionRepository, categoryRepository, savedStateHandle)

    // ── Initial load ───────────────────────────────────────────────────────────

    @Test
    fun `loads all subscriptions on init`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(3, state.subscriptions.size)
            assertEquals(false, state.isLoading)
            assertNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `error state set when repository throws`() = runTest {
        every { subscriptionRepository.getAllSubscriptions() } returns kotlinx.coroutines.flow.flow {
            throw RuntimeException("DB failure")
        }

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNotNull(state.error)
            assertTrue(state.error!!.contains("DB failure"))
            assertEquals(false, state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Search ─────────────────────────────────────────────────────────────────

    @Test
    fun `onSearchQueryChange filters by name case-insensitively`() = runTest {
        val viewModel = createViewModel()

        // Consume initial load
        viewModel.uiState.test {
            awaitItem() // loaded state

            viewModel.onSearchQueryChange("net")
            val filtered = awaitItem()
            assertEquals(1, filtered.filteredSubscriptions.size)
            assertEquals("Netflix", filtered.filteredSubscriptions[0].name)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onSearchQueryChange with empty string shows all subscriptions`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.onSearchQueryChange("spotify")
            awaitItem()

            viewModel.onSearchQueryChange("")
            val state = awaitItem()
            assertEquals(3, state.filteredSubscriptions.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Status filter ──────────────────────────────────────────────────────────

    @Test
    fun `onFilterChange ACTIVE shows only active subscriptions`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.onFilterChange(SubscriptionFilter.ACTIVE)
            val state = awaitItem()
            assertTrue(state.filteredSubscriptions.all { it.isActive })
            assertEquals(2, state.filteredSubscriptions.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onFilterChange INACTIVE shows only inactive subscriptions`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.onFilterChange(SubscriptionFilter.INACTIVE)
            val state = awaitItem()
            assertTrue(state.filteredSubscriptions.all { !it.isActive })
            assertEquals(1, state.filteredSubscriptions.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Category filter ────────────────────────────────────────────────────────

    @Test
    fun `onCategoryFilterChange filters by category`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.onCategoryFilterChange(categoryStreaming.id)
            val state = awaitItem()
            assertTrue(state.filteredSubscriptions.all { it.categoryId == categoryStreaming.id })
            assertEquals(2, state.filteredSubscriptions.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onCategoryFilterChange with null shows all categories`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.onCategoryFilterChange(categoryStreaming.id)
            awaitItem()

            viewModel.onCategoryFilterChange(null)
            val state = awaitItem()
            assertEquals(3, state.filteredSubscriptions.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Sort orders ────────────────────────────────────────────────────────────

    @Test
    fun `onSortOrderChange NAME_ASC sorts alphabetically ascending`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.onSortOrderChange(SortOrder.NAME_ASC)
            val state = awaitItem()
            val names = state.filteredSubscriptions.map { it.name }
            assertEquals(listOf("Hulu", "Netflix", "Spotify"), names)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onSortOrderChange NAME_DESC sorts alphabetically descending`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.onSortOrderChange(SortOrder.NAME_DESC)
            val state = awaitItem()
            val names = state.filteredSubscriptions.map { it.name }
            assertEquals(listOf("Spotify", "Netflix", "Hulu"), names)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onSortOrderChange AMOUNT_ASC sorts by amount ascending`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.onSortOrderChange(SortOrder.AMOUNT_ASC)
            val state = awaitItem()
            val amounts = state.filteredSubscriptions.map { it.amount }
            assertEquals(listOf(9.99, 12.00, 15.99), amounts)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onSortOrderChange NEXT_BILLING_DATE sorts by next billing date`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            // NEXT_BILLING_DATE is the default sort order; change away first then back.
            awaitItem()

            viewModel.onSortOrderChange(SortOrder.NAME_ASC)
            awaitItem()

            viewModel.onSortOrderChange(SortOrder.NEXT_BILLING_DATE)
            val state = awaitItem()
            val dates = state.filteredSubscriptions.map { it.nextBillingDate }
            assertEquals(dates.sorted(), dates)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Dashboard filters ──────────────────────────────────────────────────────

    @Test
    fun `DashboardFilter Monthly only shows monthly subscriptions`() = runTest {
        val handle = SavedStateHandle(mapOf("dashboardFilter" to "monthly"))
        val viewModel = createViewModel(handle)

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.filteredSubscriptions.all { it.frequency == BillingFrequency.MONTHLY })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `DashboardFilter Yearly only shows annual subscriptions`() = runTest {
        val handle = SavedStateHandle(mapOf("dashboardFilter" to "yearly"))
        val viewModel = createViewModel(handle)

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.filteredSubscriptions.all { it.frequency == BillingFrequency.ANNUAL })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `DashboardFilter Active only shows active subscriptions and ignores status filter`() = runTest {
        val handle = SavedStateHandle(mapOf("dashboardFilter" to "active"))
        val viewModel = createViewModel(handle)

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.filteredSubscriptions.all { it.isActive })
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `DashboardFilter ByCategory filters by specified category`() = runTest {
        val arg = "cat|${categoryStreaming.id}|Streaming"
        val handle = SavedStateHandle(mapOf("dashboardFilter" to arg))
        val viewModel = createViewModel(handle)

        viewModel.uiState.test {
            val state = awaitItem()
            assertTrue(state.filteredSubscriptions.all { it.categoryId == categoryStreaming.id })
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Refresh ────────────────────────────────────────────────────────────────

    @Test
    fun `refresh sets isLoading true then completes`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem() // Initial loaded state

            viewModel.refresh()

            val loading = awaitItem()
            assertTrue(loading.isLoading)

            val loaded = awaitItem()
            assertEquals(false, loaded.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
