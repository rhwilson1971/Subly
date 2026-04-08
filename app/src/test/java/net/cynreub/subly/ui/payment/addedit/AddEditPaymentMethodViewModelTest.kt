package net.cynreub.subly.ui.payment.addedit

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import net.cynreub.subly.domain.model.PaymentType
import net.cynreub.subly.domain.repository.PaymentMethodRepository
import net.cynreub.subly.util.MainCoroutineRule
import net.cynreub.subly.util.testPaymentMethod
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import java.util.UUID

class AddEditPaymentMethodViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private val paymentMethodRepository: PaymentMethodRepository = mockk()
    private val existingMethod = testPaymentMethod(
        nickname = "Visa Rewards",
        type = PaymentType.VISA,
        lastFourDigits = "4242"
    )

    private fun createViewModel(paymentMethodId: String? = null): AddEditPaymentMethodViewModel {
        val handle = if (paymentMethodId != null) {
            SavedStateHandle(mapOf("paymentMethodId" to paymentMethodId))
        } else {
            SavedStateHandle()
        }
        return AddEditPaymentMethodViewModel(paymentMethodRepository, handle)
    }

    // ── Add mode (no ID) ───────────────────────────────────────────────────────

    @Test
    fun `initial state in add mode is empty and not in edit mode`() = runTest {
        val viewModel = createViewModel()

        val state = viewModel.uiState.value
        assertEquals("", state.nickname)
        assertEquals("", state.lastFourDigits)
        assertFalse(state.isEditMode)
        assertFalse(state.isLoading)
        assertNull(state.error)
    }

    // ── Edit mode (with ID) ────────────────────────────────────────────────────

    @Test
    fun `edit mode loads payment method into form`() = runTest {
        every { paymentMethodRepository.getPaymentMethodById(existingMethod.id) } returns flowOf(existingMethod)

        val viewModel = createViewModel(paymentMethodId = existingMethod.id.toString())

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) state = awaitItem()

            assertEquals("Visa Rewards", state.nickname)
            assertEquals(PaymentType.VISA, state.selectedType)
            assertEquals("4242", state.lastFourDigits)
            assertTrue(state.isEditMode)
            assertEquals(existingMethod.id, state.paymentMethodId)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `edit mode sets error when payment method not found`() = runTest {
        val missingId = UUID.randomUUID()
        every { paymentMethodRepository.getPaymentMethodById(missingId) } returns flowOf(null)

        val viewModel = createViewModel(paymentMethodId = missingId.toString())

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) state = awaitItem()

            assertEquals("Payment method not found", state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `edit mode sets error when loading throws`() = runTest {
        every { paymentMethodRepository.getPaymentMethodById(existingMethod.id) } returns kotlinx.coroutines.flow.flow {
            throw RuntimeException("Load failed")
        }

        val viewModel = createViewModel(paymentMethodId = existingMethod.id.toString())

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) state = awaitItem()

            assertNotNull(state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Field changes ──────────────────────────────────────────────────────────

    @Test
    fun `onNicknameChange updates nickname and clears nicknameError`() = runTest {
        val viewModel = createViewModel()
        viewModel.onSaveClick() // trigger validation to produce an error
        viewModel.onNicknameChange("My Card")

        val state = viewModel.uiState.value
        assertEquals("My Card", state.nickname)
        assertNull(state.nicknameError)
    }

    @Test
    fun `onLastFourDigitsChange filters non-digit characters`() = runTest {
        val viewModel = createViewModel()
        viewModel.onLastFourDigitsChange("12ab")
        assertEquals("12", viewModel.uiState.value.lastFourDigits)
    }

    @Test
    fun `onLastFourDigitsChange limits to 4 digits`() = runTest {
        val viewModel = createViewModel()
        viewModel.onLastFourDigitsChange("123456")
        assertEquals("1234", viewModel.uiState.value.lastFourDigits)
    }

    @Test
    fun `onLastFourDigitsChange clears lastFourDigitsError`() = runTest {
        val viewModel = createViewModel()
        // Force an error state via save with partial digits
        viewModel.onNicknameChange("Test")
        viewModel.onLastFourDigitsChange("12") // 2 digits - will fail validation
        viewModel.onSaveClick()
        assertNotNull(viewModel.uiState.value.lastFourDigitsError)

        viewModel.onLastFourDigitsChange("1234")
        assertNull(viewModel.uiState.value.lastFourDigitsError)
    }

    // ── Type dialog ────────────────────────────────────────────────────────────

    @Test
    fun `showTypeDialog and dismissTypeDialog toggle flag`() = runTest {
        val viewModel = createViewModel()
        viewModel.showTypeDialog()
        assertTrue(viewModel.uiState.value.showTypeDialog)
        viewModel.dismissTypeDialog()
        assertFalse(viewModel.uiState.value.showTypeDialog)
    }

    @Test
    fun `onTypeSelected updates type and closes dialog`() = runTest {
        val viewModel = createViewModel()
        viewModel.showTypeDialog()
        viewModel.onTypeSelected(PaymentType.MASTERCARD)
        assertEquals(PaymentType.MASTERCARD, viewModel.uiState.value.selectedType)
        assertFalse(viewModel.uiState.value.showTypeDialog)
    }

    // ── Validation ─────────────────────────────────────────────────────────────

    @Test
    fun `onSaveClick with blank nickname sets nicknameError`() = runTest {
        val viewModel = createViewModel()
        viewModel.onNicknameChange("")
        viewModel.onSaveClick()
        assertEquals("Nickname is required", viewModel.uiState.value.nicknameError)
    }

    @Test
    fun `onSaveClick with partial lastFourDigits sets lastFourDigitsError`() = runTest {
        val viewModel = createViewModel()
        viewModel.onNicknameChange("My Card")
        viewModel.onLastFourDigitsChange("12") // not 4 digits
        viewModel.onSaveClick()
        assertEquals("Must be exactly 4 digits", viewModel.uiState.value.lastFourDigitsError)
    }

    @Test
    fun `onSaveClick with empty lastFourDigits is valid (optional field)`() = runTest {
        coEvery { paymentMethodRepository.insertPaymentMethod(any()) } returns Unit
        val viewModel = createViewModel()
        viewModel.onNicknameChange("My Card")
        // lastFourDigits is empty by default - should pass validation
        viewModel.onSaveClick()
        assertTrue(viewModel.uiState.value.saveSuccess)
    }

    // ── Save (insert) ──────────────────────────────────────────────────────────

    @Test
    fun `onSaveClick calls insertPaymentMethod and sets saveSuccess`() = runTest {
        coEvery { paymentMethodRepository.insertPaymentMethod(any()) } returns Unit
        val viewModel = createViewModel()

        viewModel.uiState.test {
            awaitItem()

            viewModel.onNicknameChange("New Card")
            awaitItem()

            viewModel.onSaveClick()

            // With UnconfinedTestDispatcher the coroutine completes eagerly — final state only.
            val done = awaitItem()
            assertFalse(done.isSaving)
            assertTrue(done.saveSuccess)

            coVerify { paymentMethodRepository.insertPaymentMethod(any()) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `onSaveClick sets error when insertPaymentMethod throws`() = runTest {
        coEvery { paymentMethodRepository.insertPaymentMethod(any()) } throws RuntimeException("Insert failed")
        val viewModel = createViewModel()

        viewModel.onNicknameChange("New Card")
        viewModel.onSaveClick()

        viewModel.uiState.test {
            val state = awaitItem()
            // Either we land in error state or saving = false
            if (state.isSaving) {
                val errorState = awaitItem()
                assertNotNull(errorState.error)
            } else {
                assertNotNull(state.error)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Save (update / edit mode) ──────────────────────────────────────────────

    @Test
    fun `onSaveClick in edit mode calls updatePaymentMethod`() = runTest {
        every { paymentMethodRepository.getPaymentMethodById(existingMethod.id) } returns flowOf(existingMethod)
        coEvery { paymentMethodRepository.updatePaymentMethod(any()) } returns Unit

        val viewModel = createViewModel(paymentMethodId = existingMethod.id.toString())

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) state = awaitItem()

            viewModel.onSaveClick()

            val done = awaitItem()
            assertTrue(done.saveSuccess)

            coVerify { paymentMethodRepository.updatePaymentMethod(any()) }
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Clear error ────────────────────────────────────────────────────────────

    @Test
    fun `clearError removes error from state`() = runTest {
        coEvery { paymentMethodRepository.insertPaymentMethod(any()) } throws RuntimeException("Error")
        val viewModel = createViewModel()
        viewModel.onNicknameChange("New Card")
        viewModel.onSaveClick()

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isSaving) state = awaitItem()
            // Now we should have error state

            viewModel.clearError()
            val cleared = awaitItem()
            assertNull(cleared.error)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
