package net.cynreub.subly.ui.payment

import net.cynreub.subly.domain.model.PaymentMethod

data class PaymentMethodsUiState(
    val paymentMethods: List<PaymentMethodWithUsage> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    val showDeleteDialog: Boolean = false,
    val deletingPaymentMethod: PaymentMethodWithUsage? = null,
    val isDeleting: Boolean = false
)

data class PaymentMethodWithUsage(
    val paymentMethod: PaymentMethod,
    val subscriptionCount: Int
)
