package net.cynreub.subly.ui.subscriptions.detail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import net.cynreub.subly.domain.repository.CategoryRepository
import net.cynreub.subly.domain.repository.PaymentMethodRepository
import net.cynreub.subly.domain.repository.SubscriptionRepository
import net.cynreub.subly.domain.usecase.UpdateNextBillingDateUseCase
import net.cynreub.subly.util.MainCoroutineRule
import net.cynreub.subly.util.testCategory
import net.cynreub.subly.util.testPaymentMethod
import net.cynreub.subly.util.testSubscription
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.UUID

class SubscriptionDetailViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private val subscriptionRepository: SubscriptionRepository = mockk()
    private val paymentMethodRepository: PaymentMethodRepository = mockk()
    private val categoryRepository: CategoryRepository = mockk()
    private val updateNextBillingDateUseCase: UpdateNextBillingDateUseCase = mockk()

    private val category = testCategory()
    private val paymentMethod = testPaymentMethod()
    private val subscription = testSubscription(
        paymentMethodId = paymentMethod.id,
        categoryId = category.id
    )

    private fun createViewModel(subscriptionId: String = subscription.id.toString()) =
        SubscriptionDetailViewModel(
            subscriptionRepository,
            paymentMethodRepository,
            categoryRepository,
            updateNextBillingDateUseCase,
            SavedStateHandle(mapOf("subscriptionId" to subscriptionId))
        )

    private fun stubHappyPath() {
        every { subscriptionRepository.getSubscriptionById(subscription.id) } returns flowOf(subscription)
        every { paymentMethodRepository.getPaymentMethodById(paymentMethod.id) } returns flowOf(paymentMethod)
        every { categoryRepository.getCategoryById(category.id) } returns flowOf(category)
    }

    // ── Load subscription detail ───────────────────────────────────────────────

    @Test
    fun `loads subscription, category and payment method`() = runTest {
        stubHappyPath()
        val viewModel = createViewModel()

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) state = awaitItem()

            assertEquals(subscription, state.subscription)
            assertEquals(category, state.category)
            assertEquals(paymentMethod, state.paymentMethod)
            assertNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `loads subscription without payment method when paymentMethodId is null`() = runTest {
        val subNoPayment = subscription.copy(paymentMethodId = null)
        every { subscriptionRepository.getSubscriptionById(subscription.id) } returns flowOf(subNoPayment)
        every { categoryRepository.getCategoryById(category.id) } returns flowOf(category)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) state = awaitItem()

            assertEquals(subNoPayment, state.subscription)
            assertNull(state.paymentMethod)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `error state when subscription not found`() = runTest {
        every { subscriptionRepository.getSubscriptionById(subscription.id) } returns flowOf(null)

        val viewModel = createViewModel()

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) state = awaitItem()

            assertEquals("Subscription not found", state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `error state when repository flow throws`() = runTest {
        every { subscriptionRepository.getSubscriptionById(subscription.id) } returns flow {
            throw RuntimeException("Load failed")
        }

        val viewModel = createViewModel()

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) state = awaitItem()

            assertNotNull(state.error)
            assertTrue(state.error!!.contains("Load failed"))
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Delete dialog ──────────────────────────────────────────────────────────

    @Test
    fun `showDeleteDialog and dismissDeleteDialog toggle flag`() = runTest {
        stubHappyPath()
        val viewModel = createViewModel()

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) state = awaitItem()

            viewModel.showDeleteDialog()
            assertTrue(awaitItem().showDeleteDialog)

            viewModel.dismissDeleteDialog()
            assertFalse(awaitItem().showDeleteDialog)

            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Delete confirm ─────────────────────────────────────────────────────────

    @Test
    fun `onDeleteConfirm calls deleteSubscription and sets deleteSuccess`() = runTest {
        stubHappyPath()
        coEvery { subscriptionRepository.deleteSubscription(any()) } returns Unit

        val viewModel = createViewModel()

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) state = awaitItem()

            viewModel.showDeleteDialog()
            awaitItem()

            viewModel.onDeleteConfirm()

            // With UnconfinedTestDispatcher the coroutine runs eagerly; the final state
            // has isDeleting = false and deleteSuccess = true.
            val doneState = awaitItem()
            assertFalse(doneState.isDeleting)
            assertTrue(doneState.deleteSuccess)

            coVerify { subscriptionRepository.deleteSubscription(subscription) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onDeleteConfirm sets error when deleteSubscription throws`() = runTest {
        stubHappyPath()
        coEvery { subscriptionRepository.deleteSubscription(any()) } throws RuntimeException("Delete failed")

        val viewModel = createViewModel()

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) state = awaitItem()

            viewModel.showDeleteDialog()
            awaitItem()

            viewModel.onDeleteConfirm()

            val errorState = awaitItem()
            assertNotNull(errorState.error)
            assertTrue(errorState.error!!.contains("Delete failed"))
            assertFalse(errorState.isDeleting)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onDeleteConfirm does nothing when subscription is null`() = runTest {
        every { subscriptionRepository.getSubscriptionById(subscription.id) } returns flowOf(null)

        val viewModel = createViewModel()

        // subscription will be null after "not found" state
        viewModel.onDeleteConfirm() // should no-op

        coVerify(exactly = 0) { subscriptionRepository.deleteSubscription(any()) }
    }

    // ── Mark as paid ───────────────────────────────────────────────────────────

    @Test
    fun `onMarkAsPaid calls updateNextBillingDateUseCase`() = runTest {
        stubHappyPath()
        coEvery { updateNextBillingDateUseCase(any()) } returns Unit

        val viewModel = createViewModel()

        // Wait for the ViewModel to finish loading, then call onMarkAsPaid outside turbine.
        // Success leaves state identical (isMarkingAsPaid=false, error=null) so StateFlow
        // won't emit; verify the interaction directly instead.
        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) state = awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.onMarkAsPaid()

        coVerify { updateNextBillingDateUseCase(subscription) }
        val finalState = viewModel.uiState.value
        assertFalse(finalState.isMarkingAsPaid)
        assertNull(finalState.error)
    }

    @Test
    fun `onMarkAsPaid sets error when use case throws`() = runTest {
        stubHappyPath()
        coEvery { updateNextBillingDateUseCase(any()) } throws RuntimeException("Update failed")

        val viewModel = createViewModel()

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) state = awaitItem()

            viewModel.onMarkAsPaid()

            val errorState = awaitItem()
            assertNotNull(errorState.error)
            assertTrue(errorState.error!!.contains("Update failed"))
            assertFalse(errorState.isMarkingAsPaid)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Toggle active ──────────────────────────────────────────────────────────

    @Test
    fun `onToggleActive calls updateSubscription with flipped isActive`() = runTest {
        stubHappyPath()
        coEvery { subscriptionRepository.updateSubscription(any()) } returns Unit

        val viewModel = createViewModel()

        // Wait for load to complete, then verify the interaction outside turbine.
        // Success returns state to identical values so StateFlow won't emit.
        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) state = awaitItem()
            cancelAndIgnoreRemainingEvents()
        }

        viewModel.onToggleActive()

        coVerify {
            subscriptionRepository.updateSubscription(
                subscription.copy(isActive = !subscription.isActive)
            )
        }
        assertFalse(viewModel.uiState.value.isTogglingActive)
    }

    @Test
    fun `onToggleActive sets error when updateSubscription throws`() = runTest {
        stubHappyPath()
        coEvery { subscriptionRepository.updateSubscription(any()) } throws RuntimeException("Toggle failed")

        val viewModel = createViewModel()

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) state = awaitItem()

            viewModel.onToggleActive()

            val errorState = awaitItem()
            assertNotNull(errorState.error)
            assertTrue(errorState.error!!.contains("Toggle failed"))
            assertFalse(errorState.isTogglingActive)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Refresh ────────────────────────────────────────────────────────────────

    @Test
    fun `refresh sets isLoading true then completes`() = runTest {
        stubHappyPath()
        val viewModel = createViewModel()

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) state = awaitItem()

            viewModel.refresh()

            val loading = awaitItem()
            assertTrue(loading.isLoading)

            val loaded = awaitItem()
            assertFalse(loaded.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
