package net.cynreub.subly.ui.subscriptions.addedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import net.cynreub.subly.domain.model.BillingFrequency
import net.cynreub.subly.domain.model.Category
import net.cynreub.subly.domain.model.PaymentMethod
import net.cynreub.subly.domain.model.Subscription
import net.cynreub.subly.domain.repository.CategoryRepository
import net.cynreub.subly.domain.repository.PaymentMethodRepository
import net.cynreub.subly.domain.repository.SubscriptionRepository
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddEditSubscriptionViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val categoryRepository: CategoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val subscriptionId: String? = savedStateHandle["subscriptionId"]

    private val _uiState = MutableStateFlow(AddEditSubscriptionUiState())
    val uiState: StateFlow<AddEditSubscriptionUiState> = _uiState.asStateFlow()

    init {
        loadInitialData()
    }

    private fun loadInitialData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                combine(
                    paymentMethodRepository.getAllPaymentMethods(),
                    categoryRepository.getAllCategories()
                ) { paymentMethods, categories ->
                    paymentMethods to categories
                }
                    .catch { e ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Failed to load options: ${e.message}"
                        )
                    }
                    .collect { (paymentMethods, categories) ->
                        val defaultCategoryId = categories.firstOrNull()?.id
                            ?: Category.DEFAULT_ID

                        _uiState.value = _uiState.value.copy(
                            availablePaymentMethods = paymentMethods,
                            availableCategories = categories,
                            selectedCategoryId = _uiState.value.selectedCategoryId ?: defaultCategoryId
                        )

                        if (subscriptionId != null) {
                            loadSubscription(UUID.fromString(subscriptionId))
                        } else {
                            _uiState.value = _uiState.value.copy(isLoading = false)
                        }
                    }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to initialize form: ${e.message}"
                )
            }
        }
    }

    private fun loadSubscription(id: UUID) {
        viewModelScope.launch {
            subscriptionRepository.getSubscriptionById(id)
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = "Failed to load subscription: ${e.message}"
                    )
                }
                .collect { subscription ->
                    if (subscription != null) {
                        populateFormFromSubscription(subscription)
                    } else {
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Subscription not found"
                        )
                    }
                }
        }
    }

    private fun populateFormFromSubscription(subscription: Subscription) {
        val paymentMethod = _uiState.value.availablePaymentMethods
            .find { it.id == subscription.paymentMethodId }

        _uiState.value = _uiState.value.copy(
            name = subscription.name,
            selectedCategoryId = subscription.categoryId,
            amount = subscription.amount.toString(),
            currency = subscription.currency,
            selectedFrequency = subscription.frequency,
            startDate = subscription.startDate,
            selectedPaymentMethod = paymentMethod,
            notes = subscription.notes ?: "",
            reminderDaysBefore = subscription.reminderDaysBefore.toString(),
            isActive = subscription.isActive,
            isEditMode = true,
            subscriptionId = subscription.id,
            isLoading = false
        )
    }

    // Form Field Updates
    fun onNameChange(name: String) {
        _uiState.value = _uiState.value.copy(name = name, nameError = null)
    }

    fun onAmountChange(amount: String) {
        if (amount.isEmpty() || amount.matches(Regex("^\\d*\\.?\\d{0,2}$"))) {
            _uiState.value = _uiState.value.copy(amount = amount, amountError = null)
        }
    }

    fun onNotesChange(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    fun onReminderDaysChange(days: String) {
        if (days.isEmpty() || days.matches(Regex("^\\d+$"))) {
            _uiState.value = _uiState.value.copy(reminderDaysBefore = days, reminderDaysError = null)
        }
    }

    fun onIsActiveChange(isActive: Boolean) {
        _uiState.value = _uiState.value.copy(isActive = isActive)
    }

    // Currency dialog
    fun showCurrencyDialog() {
        _uiState.value = _uiState.value.copy(showCurrencyDialog = true)
    }

    fun dismissCurrencyDialog() {
        _uiState.value = _uiState.value.copy(showCurrencyDialog = false)
    }

    fun onCurrencySelected(currency: String) {
        _uiState.value = _uiState.value.copy(currency = currency, showCurrencyDialog = false)
    }

    // Category dialog
    fun showCategoryDialog() {
        _uiState.value = _uiState.value.copy(showCategoryDialog = true)
    }

    fun dismissCategoryDialog() {
        _uiState.value = _uiState.value.copy(showCategoryDialog = false)
    }

    fun onCategorySelected(category: Category) {
        _uiState.value = _uiState.value.copy(
            selectedCategoryId = category.id,
            showCategoryDialog = false
        )
    }

    // Frequency dialog
    fun showFrequencyDialog() {
        _uiState.value = _uiState.value.copy(showFrequencyDialog = true)
    }

    fun dismissFrequencyDialog() {
        _uiState.value = _uiState.value.copy(showFrequencyDialog = false)
    }

    fun onFrequencySelected(frequency: BillingFrequency) {
        _uiState.value = _uiState.value.copy(
            selectedFrequency = frequency,
            showFrequencyDialog = false
        )
    }

    // Payment method dialog
    fun showPaymentMethodDialog() {
        _uiState.value = _uiState.value.copy(showPaymentMethodDialog = true)
    }

    fun dismissPaymentMethodDialog() {
        _uiState.value = _uiState.value.copy(showPaymentMethodDialog = false)
    }

    fun onPaymentMethodSelected(paymentMethod: PaymentMethod?) {
        _uiState.value = _uiState.value.copy(
            selectedPaymentMethod = paymentMethod,
            showPaymentMethodDialog = false
        )
    }

    // Date picker
    fun showDatePicker() {
        _uiState.value = _uiState.value.copy(showDatePicker = true)
    }

    fun dismissDatePicker() {
        _uiState.value = _uiState.value.copy(showDatePicker = false)
    }

    fun onStartDateSelected(date: LocalDate) {
        _uiState.value = _uiState.value.copy(startDate = date, showDatePicker = false)
    }

    // Validation and Save
    fun onSaveClick(onSuccess: () -> Unit) {
        _uiState.value = _uiState.value.copy(
            nameError = null, amountError = null, reminderDaysError = null, error = null
        )

        val validationErrors = validateForm()
        if (validationErrors.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                nameError = validationErrors["name"],
                amountError = validationErrors["amount"],
                reminderDaysError = validationErrors["reminderDays"]
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)
            try {
                val subscription = createSubscriptionFromForm()
                if (_uiState.value.isEditMode) {
                    subscriptionRepository.updateSubscription(subscription)
                } else {
                    subscriptionRepository.insertSubscription(subscription)
                }
                _uiState.value = _uiState.value.copy(isSaving = false)
                onSuccess()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = "Failed to save subscription: ${e.message}"
                )
            }
        }
    }

    private fun validateForm(): Map<String, String> {
        val errors = mutableMapOf<String, String>()

        if (_uiState.value.name.isBlank()) errors["name"] = "Name is required"

        val amountValue = _uiState.value.amount.toDoubleOrNull()
        when {
            _uiState.value.amount.isBlank() -> errors["amount"] = "Amount is required"
            amountValue == null             -> errors["amount"] = "Invalid amount"
            amountValue <= 0               -> errors["amount"] = "Amount must be greater than 0"
        }

        val reminderDays = _uiState.value.reminderDaysBefore.toIntOrNull()
        when {
            _uiState.value.reminderDaysBefore.isBlank() -> errors["reminderDays"] = "Reminder days is required"
            reminderDays == null                         -> errors["reminderDays"] = "Invalid number"
            reminderDays < 0 || reminderDays > 30        -> errors["reminderDays"] = "Must be between 0 and 30"
        }

        return errors
    }

    private fun createSubscriptionFromForm(): Subscription {
        val state = _uiState.value
        val categoryId = state.selectedCategoryId ?: Category.DEFAULT_ID
        val nextBillingDate = calculateNextBillingDate(state.startDate, state.selectedFrequency)

        return Subscription(
            id = state.subscriptionId ?: UUID.randomUUID(),
            name = state.name.trim(),
            categoryId = categoryId,
            amount = state.amount.toDouble(),
            currency = state.currency,
            frequency = state.selectedFrequency,
            startDate = state.startDate,
            nextBillingDate = nextBillingDate,
            paymentMethodId = state.selectedPaymentMethod?.id,
            notes = state.notes.trim().ifBlank { null },
            isActive = state.isActive,
            reminderDaysBefore = state.reminderDaysBefore.toInt()
        )
    }

    private fun calculateNextBillingDate(startDate: LocalDate, frequency: BillingFrequency): LocalDate =
        when (frequency) {
            BillingFrequency.WEEKLY      -> startDate.plusWeeks(1)
            BillingFrequency.MONTHLY     -> startDate.plusMonths(1)
            BillingFrequency.QUARTERLY   -> startDate.plusMonths(3)
            BillingFrequency.SEMI_ANNUAL -> startDate.plusMonths(6)
            BillingFrequency.ANNUAL      -> startDate.plusYears(1)
            BillingFrequency.CUSTOM      -> startDate.plusMonths(1)
        }
}
