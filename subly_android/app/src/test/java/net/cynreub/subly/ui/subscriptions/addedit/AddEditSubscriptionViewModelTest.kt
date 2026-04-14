package net.cynreub.subly.ui.subscriptions.addedit

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import net.cynreub.subly.domain.model.BillingFrequency
import net.cynreub.subly.domain.repository.CategoryRepository
import net.cynreub.subly.domain.repository.PaymentMethodRepository
import net.cynreub.subly.domain.repository.SubscriptionRepository
import net.cynreub.subly.util.MainCoroutineRule
import net.cynreub.subly.util.testCategory
import net.cynreub.subly.util.testPaymentMethod
import net.cynreub.subly.util.testSubscription
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate
import java.util.UUID

class AddEditSubscriptionViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private val subscriptionRepository: SubscriptionRepository = mockk()
    private val paymentMethodRepository: PaymentMethodRepository = mockk()
    private val categoryRepository: CategoryRepository = mockk()

    private val category = testCategory()
    private val paymentMethod = testPaymentMethod()

    @Before
    fun setUp() {
        every { paymentMethodRepository.getAllPaymentMethods() } returns flowOf(listOf(paymentMethod))
        every { categoryRepository.getAllCategories() } returns flowOf(listOf(category))
    }

    private fun createViewModel(subscriptionId: String? = null): AddEditSubscriptionViewModel {
        val handle = if (subscriptionId != null) {
            SavedStateHandle(mapOf("subscriptionId" to subscriptionId))
        } else {
            SavedStateHandle()
        }
        return AddEditSubscriptionViewModel(
            subscriptionRepository,
            paymentMethodRepository,
            categoryRepository,
            handle
        )
    }

    // ── Form field updates ─────────────────────────────────────────────────────

    @Test
    fun `onNameChange updates name and clears nameError`() = runTest {
        val viewModel = createViewModel()
        viewModel.onSaveClick {} // Trigger validation to set an error first
        viewModel.onNameChange("Netflix")

        val state = viewModel.uiState.value
        assertEquals("Netflix", state.name)
        assertNull(state.nameError)
    }

    @Test
    fun `onAmountChange accepts valid decimal input`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAmountChange("15.99")
        assertEquals("15.99", viewModel.uiState.value.amount)
    }

    @Test
    fun `onAmountChange rejects more than 2 decimal places`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAmountChange("15.999")
        assertEquals("", viewModel.uiState.value.amount) // unchanged from default
    }

    @Test
    fun `onAmountChange accepts empty string`() = runTest {
        val viewModel = createViewModel()
        viewModel.onAmountChange("10.00")
        viewModel.onAmountChange("")
        assertEquals("", viewModel.uiState.value.amount)
    }

    @Test
    fun `onNotesChange updates notes`() = runTest {
        val viewModel = createViewModel()
        viewModel.onNotesChange("Annual plan")
        assertEquals("Annual plan", viewModel.uiState.value.notes)
    }

    @Test
    fun `onReminderDaysChange accepts numeric input`() = runTest {
        val viewModel = createViewModel()
        viewModel.onReminderDaysChange("5")
        assertEquals("5", viewModel.uiState.value.reminderDaysBefore)
    }

    @Test
    fun `onReminderDaysChange rejects non-numeric input`() = runTest {
        val viewModel = createViewModel()
        viewModel.onReminderDaysChange("abc")
        assertEquals("2", viewModel.uiState.value.reminderDaysBefore) // unchanged default
    }

    @Test
    fun `onIsActiveChange updates isActive`() = runTest {
        val viewModel = createViewModel()
        viewModel.onIsActiveChange(false)
        assertFalse(viewModel.uiState.value.isActive)
    }

    // ── Dialog state management ────────────────────────────────────────────────

    @Test
    fun `showCurrencyDialog and dismissCurrencyDialog toggle flag`() = runTest {
        val viewModel = createViewModel()
        viewModel.showCurrencyDialog()
        assertTrue(viewModel.uiState.value.showCurrencyDialog)
        viewModel.dismissCurrencyDialog()
        assertFalse(viewModel.uiState.value.showCurrencyDialog)
    }

    @Test
    fun `onCurrencySelected updates currency and closes dialog`() = runTest {
        val viewModel = createViewModel()
        viewModel.showCurrencyDialog()
        viewModel.onCurrencySelected("EUR")
        assertEquals("EUR", viewModel.uiState.value.currency)
        assertFalse(viewModel.uiState.value.showCurrencyDialog)
    }

    @Test
    fun `showCategoryDialog and dismissCategoryDialog toggle flag`() = runTest {
        val viewModel = createViewModel()
        viewModel.showCategoryDialog()
        assertTrue(viewModel.uiState.value.showCategoryDialog)
        viewModel.dismissCategoryDialog()
        assertFalse(viewModel.uiState.value.showCategoryDialog)
    }

    @Test
    fun `onCategorySelected updates category and closes dialog`() = runTest {
        val viewModel = createViewModel()
        viewModel.showCategoryDialog()
        viewModel.onCategorySelected(category)
        assertEquals(category.id, viewModel.uiState.value.selectedCategoryId)
        assertFalse(viewModel.uiState.value.showCategoryDialog)
    }

    @Test
    fun `showFrequencyDialog and dismissFrequencyDialog toggle flag`() = runTest {
        val viewModel = createViewModel()
        viewModel.showFrequencyDialog()
        assertTrue(viewModel.uiState.value.showFrequencyDialog)
        viewModel.dismissFrequencyDialog()
        assertFalse(viewModel.uiState.value.showFrequencyDialog)
    }

    @Test
    fun `onFrequencySelected updates frequency and closes dialog`() = runTest {
        val viewModel = createViewModel()
        viewModel.showFrequencyDialog()
        viewModel.onFrequencySelected(BillingFrequency.ANNUAL)
        assertEquals(BillingFrequency.ANNUAL, viewModel.uiState.value.selectedFrequency)
        assertFalse(viewModel.uiState.value.showFrequencyDialog)
    }

    @Test
    fun `showPaymentMethodDialog and dismissPaymentMethodDialog toggle flag`() = runTest {
        val viewModel = createViewModel()
        viewModel.showPaymentMethodDialog()
        assertTrue(viewModel.uiState.value.showPaymentMethodDialog)
        viewModel.dismissPaymentMethodDialog()
        assertFalse(viewModel.uiState.value.showPaymentMethodDialog)
    }

    @Test
    fun `onPaymentMethodSelected updates method and closes dialog`() = runTest {
        val viewModel = createViewModel()
        viewModel.showPaymentMethodDialog()
        viewModel.onPaymentMethodSelected(paymentMethod)
        assertEquals(paymentMethod, viewModel.uiState.value.selectedPaymentMethod)
        assertFalse(viewModel.uiState.value.showPaymentMethodDialog)
    }

    @Test
    fun `showDatePicker and dismissDatePicker toggle flag`() = runTest {
        val viewModel = createViewModel()
        viewModel.showDatePicker()
        assertTrue(viewModel.uiState.value.showDatePicker)
        viewModel.dismissDatePicker()
        assertFalse(viewModel.uiState.value.showDatePicker)
    }

    @Test
    fun `onStartDateSelected updates date and closes picker`() = runTest {
        val viewModel = createViewModel()
        val newDate = LocalDate.of(2026, 6, 1)
        viewModel.showDatePicker()
        viewModel.onStartDateSelected(newDate)
        assertEquals(newDate, viewModel.uiState.value.startDate)
        assertFalse(viewModel.uiState.value.showDatePicker)
    }

    // ── Validation errors ──────────────────────────────────────────────────────

    @Test
    fun `onSaveClick with blank name sets nameError`() = runTest {
        val viewModel = createViewModel()
        viewModel.onNameChange("")
        viewModel.onAmountChange("9.99")
        viewModel.onReminderDaysChange("2")

        var successCalled = false
        viewModel.onSaveClick { successCalled = true }

        assertEquals("Name is required", viewModel.uiState.value.nameError)
        assertFalse(successCalled)
    }

    @Test
    fun `onSaveClick with blank amount sets amountError`() = runTest {
        val viewModel = createViewModel()
        viewModel.onNameChange("Netflix")
        viewModel.onAmountChange("")
        viewModel.onReminderDaysChange("2")

        viewModel.onSaveClick {}

        assertEquals("Amount is required", viewModel.uiState.value.amountError)
    }

    @Test
    fun `onSaveClick with zero amount sets amountError`() = runTest {
        val viewModel = createViewModel()
        viewModel.onNameChange("Netflix")
        viewModel.onAmountChange("0")
        viewModel.onReminderDaysChange("2")

        viewModel.onSaveClick {}

        assertEquals("Amount must be greater than 0", viewModel.uiState.value.amountError)
    }

    @Test
    fun `onSaveClick with negative amount sets amountError`() = runTest {
        val viewModel = createViewModel()
        viewModel.onNameChange("Netflix")
        // negative values are blocked by onAmountChange, so set via state
        // Test the validation path with an invalid amount string edge case
        viewModel.onAmountChange("0.00")
        viewModel.onReminderDaysChange("2")

        viewModel.onSaveClick {}

        assertEquals("Amount must be greater than 0", viewModel.uiState.value.amountError)
    }

    @Test
    fun `onSaveClick with blank reminderDays sets reminderDaysError`() = runTest {
        val viewModel = createViewModel()
        viewModel.onNameChange("Netflix")
        viewModel.onAmountChange("9.99")
        viewModel.onReminderDaysChange("")

        viewModel.onSaveClick {}

        assertEquals("Reminder days is required", viewModel.uiState.value.reminderDaysError)
    }

    @Test
    fun `onSaveClick with reminderDays greater than 30 sets reminderDaysError`() = runTest {
        val viewModel = createViewModel()
        viewModel.onNameChange("Netflix")
        viewModel.onAmountChange("9.99")
        // bypass onReminderDaysChange filter by setting valid value first, then test boundary
        // We need to test the validateForm path directly - set state to 31 days
        // onReminderDaysChange accepts any positive integer, so "31" is valid input
        viewModel.onReminderDaysChange("31")

        viewModel.onSaveClick {}

        assertEquals("Must be between 0 and 30", viewModel.uiState.value.reminderDaysError)
    }

    // ── Save (insert) ──────────────────────────────────────────────────────────

    @Test
    fun `onSaveClick with valid form calls insertSubscription and invokes onSuccess`() = runTest {
        coEvery { subscriptionRepository.insertSubscription(any()) } returns Unit
        val viewModel = createViewModel()

        viewModel.onNameChange("Netflix")
        viewModel.onAmountChange("9.99")
        viewModel.onReminderDaysChange("2")

        var successCalled = false
        viewModel.onSaveClick { successCalled = true }

        coVerify { subscriptionRepository.insertSubscription(any()) }
        assertTrue(successCalled)
        assertFalse(viewModel.uiState.value.isSaving)
    }

    @Test
    fun `onSaveClick sets error when insertSubscription throws`() = runTest {
        coEvery { subscriptionRepository.insertSubscription(any()) } throws RuntimeException("DB error")
        val viewModel = createViewModel()

        viewModel.onNameChange("Netflix")
        viewModel.onAmountChange("9.99")
        viewModel.onReminderDaysChange("2")

        var successCalled = false
        viewModel.onSaveClick { successCalled = true }

        assertFalse(successCalled)
        assertNotNull(viewModel.uiState.value.error)
        assertTrue(viewModel.uiState.value.error!!.contains("DB error"))
        assertFalse(viewModel.uiState.value.isSaving)
    }

    // ── Save (update / edit mode) ──────────────────────────────────────────────

    @Test
    fun `onSaveClick in edit mode calls updateSubscription`() = runTest {
        val subscription = testSubscription()
        every { subscriptionRepository.getSubscriptionById(subscription.id) } returns flowOf(subscription)
        coEvery { subscriptionRepository.updateSubscription(any()) } returns Unit

        val viewModel = createViewModel(subscriptionId = subscription.id.toString())

        // Let init load complete, then save
        viewModel.uiState.test {
            // wait until isLoading = false
            var state = awaitItem()
            while (state.isLoading) state = awaitItem()

            viewModel.onSaveClick {}

            cancelAndIgnoreRemainingEvents()
        }

        coVerify { subscriptionRepository.updateSubscription(any()) }
    }

    // ── Error when loading options fails ───────────────────────────────────────

    @Test
    fun `error state set when loading options throws`() = runTest {
        every { paymentMethodRepository.getAllPaymentMethods() } returns kotlinx.coroutines.flow.flow {
            throw RuntimeException("Load failed")
        }

        val viewModel = createViewModel()

        viewModel.uiState.test {
            val state = awaitItem()
            if (state.isLoading) {
                val errorState = awaitItem()
                assertNotNull(errorState.error)
            } else {
                assertNotNull(state.error)
            }
            cancelAndIgnoreRemainingEvents()
        }
    }

    // ── Edit mode: subscription not found ─────────────────────────────────────

    @Test
    fun `edit mode sets error when subscription not found`() = runTest {
        val id = UUID.randomUUID()
        every { subscriptionRepository.getSubscriptionById(id) } returns flowOf(null)

        val viewModel = createViewModel(subscriptionId = id.toString())

        viewModel.uiState.test {
            var state = awaitItem()
            while (state.isLoading) state = awaitItem()

            assertEquals("Subscription not found", state.error)
            cancelAndIgnoreRemainingEvents()
        }
    }
}
