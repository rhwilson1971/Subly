package net.cynreub.subly.ui.subscriptions.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import net.cynreub.subly.domain.repository.PaymentMethodRepository
import net.cynreub.subly.domain.repository.SubscriptionRepository
import net.cynreub.subly.domain.usecase.UpdateNextBillingDateUseCase
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SubscriptionDetailViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
    private val paymentMethodRepository: PaymentMethodRepository,
    private val updateNextBillingDateUseCase: UpdateNextBillingDateUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val subscriptionId: String =
        savedStateHandle["subscriptionId"] ?: throw IllegalArgumentException("subscriptionId required")

    private val _uiState = MutableStateFlow(SubscriptionDetailUiState())
    val uiState: StateFlow<SubscriptionDetailUiState> = _uiState.asStateFlow()

    init {
        loadSubscriptionDetail()
    }

    private fun loadSubscriptionDetail() {
        viewModelScope.launch {
            try {
                val uuid = UUID.fromString(subscriptionId)

                subscriptionRepository.getSubscriptionById(uuid)
                    .catch { e ->
                        _uiState.value = SubscriptionDetailUiState(
                            isLoading = false,
                            error = "Failed to load subscription: ${e.message}"
                        )
                    }
                    .collectLatest { subscription ->
                        if (subscription != null) {
                            // Load payment method if available
                            subscription.paymentMethodId?.let { paymentMethodId ->
                                paymentMethodRepository.getPaymentMethodById(paymentMethodId)
                                    .catch { /* Ignore payment method load errors */ }
                                    .collectLatest { paymentMethod ->
                                        _uiState.value = _uiState.value.copy(
                                            subscription = subscription,
                                            paymentMethod = paymentMethod,
                                            isLoading = false,
                                            error = null
                                        )
                                    }
                            } ?: run {
                                // No payment method
                                _uiState.value = SubscriptionDetailUiState(
                                    subscription = subscription,
                                    paymentMethod = null,
                                    isLoading = false,
                                    error = null
                                )
                            }
                        } else {
                            _uiState.value = SubscriptionDetailUiState(
                                isLoading = false,
                                error = "Subscription not found"
                            )
                        }
                    }
            } catch (e: IllegalArgumentException) {
                _uiState.value = SubscriptionDetailUiState(
                    isLoading = false,
                    error = "Invalid subscription ID"
                )
            }
        }
    }

    fun showDeleteDialog() {
        _uiState.value = _uiState.value.copy(showDeleteDialog = true)
    }

    fun dismissDeleteDialog() {
        _uiState.value = _uiState.value.copy(showDeleteDialog = false)
    }

    fun onDeleteConfirm() {
        val subscription = _uiState.value.subscription ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDeleting = true,
                showDeleteDialog = false
            )

            try {
                subscriptionRepository.deleteSubscription(subscription)
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    deleteSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    error = "Failed to delete subscription: ${e.message}"
                )
            }
        }
    }

    fun onMarkAsPaid() {
        val subscription = _uiState.value.subscription ?: return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isMarkingAsPaid = true)

            try {
                // Use the existing UpdateNextBillingDateUseCase to advance the date
                updateNextBillingDateUseCase(subscription)

                _uiState.value = _uiState.value.copy(
                    isMarkingAsPaid = false,
                    error = null
                )
                // Data will automatically refresh via Flow
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isMarkingAsPaid = false,
                    error = "Failed to mark as paid: ${e.message}"
                )
            }
        }
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        loadSubscriptionDetail()
    }
}
