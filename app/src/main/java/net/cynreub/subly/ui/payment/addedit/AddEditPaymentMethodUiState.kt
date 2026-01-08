package net.cynreub.subly.ui.payment.addedit

import net.cynreub.subly.domain.model.PaymentType
import java.util.UUID

data class AddEditPaymentMethodUiState(
    // Form Fields
    val nickname: String = "",
    val selectedType: PaymentType = PaymentType.VISA,
    val lastFourDigits: String = "",

    // UI State
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,

    // Validation Errors
    val nicknameError: String? = null,
    val lastFourDigitsError: String? = null,

    // Dialog States
    val showTypeDialog: Boolean = false,

    // Edit Mode
    val isEditMode: Boolean = false,
    val paymentMethodId: UUID? = null,

    // Success State
    val saveSuccess: Boolean = false
)
