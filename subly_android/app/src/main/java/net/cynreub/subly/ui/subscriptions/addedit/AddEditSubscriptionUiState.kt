package net.cynreub.subly.ui.subscriptions.addedit

import net.cynreub.subly.domain.model.BillingFrequency
import net.cynreub.subly.domain.model.Category
import net.cynreub.subly.domain.model.PaymentMethod
import java.time.LocalDate
import java.util.UUID

data class AddEditSubscriptionUiState(
    // Form Fields
    val name: String = "",
    val selectedCategoryId: UUID? = null,
    val amount: String = "",
    val currency: String = "USD",
    val selectedFrequency: BillingFrequency = BillingFrequency.MONTHLY,
    val startDate: LocalDate = LocalDate.now(),
    val selectedPaymentMethod: PaymentMethod? = null,
    val notes: String = "",
    val reminderDaysBefore: String = "2",
    val isActive: Boolean = true,

    // Available Options (loaded from repositories)
    val availablePaymentMethods: List<PaymentMethod> = emptyList(),
    val availableCategories: List<Category> = emptyList(),

    // UI State
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,

    // Validation Errors (shown on submit only)
    val nameError: String? = null,
    val amountError: String? = null,
    val reminderDaysError: String? = null,

    // Dialog States
    val showCategoryDialog: Boolean = false,
    val showCurrencyDialog: Boolean = false,
    val showFrequencyDialog: Boolean = false,
    val showPaymentMethodDialog: Boolean = false,
    val showDatePicker: Boolean = false,

    // Edit Mode
    val isEditMode: Boolean = false,
    val subscriptionId: UUID? = null
) {
    val selectedCategory: Category?
        get() = availableCategories.find { it.id == selectedCategoryId }
}
