package net.cynreub.subly.ui.subscriptions.detail

import net.cynreub.subly.domain.model.PaymentMethod
import net.cynreub.subly.domain.model.Subscription

data class SubscriptionDetailUiState(
    // Core Data
    val subscription: Subscription? = null,
    val paymentMethod: PaymentMethod? = null,

    // UI State
    val isLoading: Boolean = true,
    val error: String? = null,

    // Action States
    val isDeleting: Boolean = false,
    val isMarkingAsPaid: Boolean = false,
    val showDeleteDialog: Boolean = false,

    // Success State (for navigation)
    val deleteSuccess: Boolean = false
)
