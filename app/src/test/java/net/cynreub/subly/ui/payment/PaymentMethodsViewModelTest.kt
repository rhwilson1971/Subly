package net.cynreub.subly.ui.payment

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import net.cynreub.subly.domain.repository.PaymentMethodRepository
import net.cynreub.subly.util.MainCoroutineRule
import net.cynreub.subly.util.testPaymentMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PaymentMethodsViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private val paymentMethodRepository: PaymentMethodRepository = mockk()

    private val visa = testPaymentMethod(nickname = "Visa Rewards")
    private val amex = testPaymentMethod(nickname = "Amex Platinum")

    private val visaWithUsage = PaymentMethodWithUsage(visa, subscriptionCount = 0)
    private val amexWithUsage = PaymentMethodWithUsage(amex, subscriptionCount = 2)

    @Before
    fun setUp() {
        every { paymentMethodRepository.getAllPaymentMethods() } returns flowOf(listOf(visa, amex))
        coEvery { paymentMethodRepository.getSubscriptionCountForPaymentMethod(visa.id) } returns 0
        coEvery { paymentMethodRepository.getSubscriptionCountForPaymentMethod(amex.id) } returns 2
    }

    private fun createViewModel() = PaymentMethodsViewModel(paymentMethodRepository)

    // ── Load ───────────────────────────────────────────────────────────────────

    @Test
    fun `loads payment methods with usage counts on init`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertEquals(2, state.paymentMethods.size)
            assertFalse(state.isLoading)
            assertNull(state.error)

            val visaResult = state.paymentMethods.find { it.paymentMethod.id == visa.id }
            val amexResult = state.paymentMethods.find { it.paymentMethod.id == amex.id }
            assertEquals(0, visaResult?.subscriptionCount)
            assertEquals(2, amexResult?.subscriptionCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `error state set when repository flow throws`() = runTest {
        every { paymentMethodRepository.getAllPaymentMethods() } returns flow {
            throw RuntimeException("Load failed")
        }

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            assertNotNull(state.error)
            assertTrue(state.error!!.contains("Load failed"))
            assertFalse(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `usage count defaults to 0 when getSubscriptionCountForPaymentMethod throws`() = runTest {
        coEvery { paymentMethodRepository.getSubscriptionCountForPaymentMethod(visa.id) } throws Exception("Count error")

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            val visaResult = state.paymentMethods.find { it.paymentMethod.id == visa.id }
            assertEquals(0, visaResult?.subscriptionCount)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Delete dialog ──────────────────────────────────────────────────────────

    @Test
    fun `showDeleteDialog sets flag and stores payment method`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.showDeleteDialog(visaWithUsage)
            val state = awaitItem()
            assertTrue(state.showDeleteDialog)
            assertEquals(visaWithUsage, state.deletingPaymentMethod)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `dismissDeleteDialog clears flag and candidate`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.showDeleteDialog(visaWithUsage)
            awaitItem()

            viewModel.dismissDeleteDialog()
            val state = awaitItem()
            assertFalse(state.showDeleteDialog)
            assertNull(state.deletingPaymentMethod)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Delete confirm ─────────────────────────────────────────────────────────

    @Test
    fun `onDeleteConfirm blocked when subscriptionCount greater than zero`() = runTest {
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.showDeleteDialog(amexWithUsage) // amex has 2 subscriptions
            awaitItem()

            viewModel.onDeleteConfirm()

            val state = awaitItem()
            assertFalse(state.showDeleteDialog)
            assertNull(state.deletingPaymentMethod)
            assertNotNull(state.error)
            assertTrue(state.error!!.contains("Cannot delete"))
            assertTrue(state.error!!.contains("2"))

            coVerify(exactly = 0) { paymentMethodRepository.deletePaymentMethodById(any()) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onDeleteConfirm succeeds when subscriptionCount is zero`() = runTest {
        coEvery { paymentMethodRepository.deletePaymentMethodById(visa.id) } returns Unit
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.showDeleteDialog(visaWithUsage) // visa has 0 subscriptions
            awaitItem()

            viewModel.onDeleteConfirm()

            // With UnconfinedTestDispatcher the coroutine completes eagerly — final state only.
            val done = awaitItem()
            assertFalse(done.isDeleting)
            assertNull(done.deletingPaymentMethod)
            assertNull(done.error)

            coVerify { paymentMethodRepository.deletePaymentMethodById(visa.id) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onDeleteConfirm sets error when deletePaymentMethodById throws`() = runTest {
        coEvery { paymentMethodRepository.deletePaymentMethodById(visa.id) } throws RuntimeException("Delete failed")
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.showDeleteDialog(visaWithUsage)
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
    fun `onDeleteConfirm does nothing when no candidate`() = runTest {
        val viewModel = createViewModel()

        viewModel.onDeleteConfirm() // no-op

        coVerify(exactly = 0) { paymentMethodRepository.deletePaymentMethodById(any()) }
    }

    // ── Clear error ────────────────────────────────────────────────────────────

    @Test
    fun `clearError removes error from state`() = runTest {
        coEvery { paymentMethodRepository.deletePaymentMethodById(amex.id) } returns Unit
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            // Trigger an error by trying to delete in-use method
            viewModel.showDeleteDialog(amexWithUsage)
            awaitItem()
            viewModel.onDeleteConfirm()
            val errorState = awaitItem()
            assertNotNull(errorState.error)

            viewModel.clearError()
            val cleared = awaitItem()
            assertNull(cleared.error)
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
            assertFalse(loaded.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
