package net.cynreub.subly.ui.payment

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
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class PaymentMethodsViewModel @Inject constructor(
    private val paymentMethodRepository: PaymentMethodRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PaymentMethodsUiState())
    val uiState: StateFlow<PaymentMethodsUiState> = _uiState.asStateFlow()

    init {
        loadPaymentMethods()
    }

    private fun loadPaymentMethods() {
        viewModelScope.launch {
            paymentMethodRepository.getAllPaymentMethods()
                .catch { e ->
                    _uiState.value = PaymentMethodsUiState(
                        isLoading = false,
                        error = "Failed to load payment methods: ${e.message}"
                    )
                }
                .collectLatest { paymentMethods ->
                    // Get subscription count for each payment method
                    val paymentMethodsWithUsage = paymentMethods.map { paymentMethod ->
                        val count = try {
                            paymentMethodRepository.getSubscriptionCountForPaymentMethod(paymentMethod.id)
                        } catch (e: Exception) {
                            0 // Default to 0 if count fails
                        }
                        PaymentMethodWithUsage(paymentMethod, count)
                    }

                    _uiState.value = PaymentMethodsUiState(
                        paymentMethods = paymentMethodsWithUsage,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }

    fun showDeleteDialog(paymentMethodWithUsage: PaymentMethodWithUsage) {
        _uiState.value = _uiState.value.copy(
            showDeleteDialog = true,
            deletingPaymentMethod = paymentMethodWithUsage
        )
    }

    fun dismissDeleteDialog() {
        _uiState.value = _uiState.value.copy(
            showDeleteDialog = false,
            deletingPaymentMethod = null
        )
    }

    fun onDeleteConfirm() {
        val paymentMethodWithUsage = _uiState.value.deletingPaymentMethod ?: return

        if (paymentMethodWithUsage.subscriptionCount > 0) {
            _uiState.value = _uiState.value.copy(
                showDeleteDialog = false,
                deletingPaymentMethod = null,
                error = "Cannot delete: used by ${paymentMethodWithUsage.subscriptionCount} subscription(s)"
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isDeleting = true,
                showDeleteDialog = false
            )

            try {
                paymentMethodRepository.deletePaymentMethodById(paymentMethodWithUsage.paymentMethod.id)
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    deletingPaymentMethod = null,
                    error = null
                )
                // Data will automatically refresh via Flow
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    deletingPaymentMethod = null,
                    error = "Failed to delete payment method: ${e.message}"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        loadPaymentMethods()
    }
}
