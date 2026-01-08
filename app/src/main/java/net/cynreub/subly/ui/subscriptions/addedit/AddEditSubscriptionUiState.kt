package net.cynreub.subly.ui.subscriptions.addedit

import net.cynreub.subly.domain.model.BillingFrequency
import net.cynreub.subly.domain.model.PaymentMethod
import net.cynreub.subly.domain.model.SubscriptionType
import java.time.LocalDate
import java.util.UUID

data class AddEditSubscriptionUiState(
    // Form Fields
    val name: String = "",
    val selectedType: SubscriptionType = SubscriptionType.STREAMING,
    val amount: String = "", // String to handle user input, convert to Double on save
    val selectedFrequency: BillingFrequency = BillingFrequency.MONTHLY,
    val startDate: LocalDate = LocalDate.now(),
    val selectedPaymentMethod: PaymentMethod? = null,
    val notes: String = "",
    val reminderDaysBefore: String = "2", // String to handle user input
    val isActive: Boolean = true,

    // Available Options (loaded from repository)
    val availablePaymentMethods: List<PaymentMethod> = emptyList(),

    // UI State
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,

    // Validation Errors (shown on submit only)
    val nameError: String? = null,
    val amountError: String? = null,
    val reminderDaysError: String? = null,

    // Dialog States
    val showTypeDialog: Boolean = false,
    val showFrequencyDialog: Boolean = false,
    val showPaymentMethodDialog: Boolean = false,
    val showDatePicker: Boolean = false,

    // Edit Mode
    val isEditMode: Boolean = false,
    val subscriptionId: UUID? = null
)
