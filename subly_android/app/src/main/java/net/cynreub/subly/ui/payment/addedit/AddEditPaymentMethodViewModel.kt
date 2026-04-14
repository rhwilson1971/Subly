package net.cynreub.subly.ui.payment.addedit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import net.cynreub.subly.domain.model.PaymentMethod
import net.cynreub.subly.domain.model.PaymentType
import net.cynreub.subly.domain.repository.PaymentMethodRepository
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AddEditPaymentMethodViewModel @Inject constructor(
    private val paymentMethodRepository: PaymentMethodRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val paymentMethodId: String? = savedStateHandle["paymentMethodId"]

    private val _uiState = MutableStateFlow(AddEditPaymentMethodUiState())
    val uiState: StateFlow<AddEditPaymentMethodUiState> = _uiState.asStateFlow()

    init {
        if (paymentMethodId != null) {
            loadPaymentMethod(paymentMethodId)
        }
    }

    private fun loadPaymentMethod(id: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)

            try {
                val uuid = UUID.fromString(id)
                val paymentMethod = paymentMethodRepository.getPaymentMethodById(uuid)
                    .catch { e ->
                        _uiState.value = AddEditPaymentMethodUiState(
                            isLoading = false,
                            error = "Failed to load payment method: ${e.message}"
                        )
                    }
                    .firstOrNull()

                if (paymentMethod != null) {
                    _uiState.value = AddEditPaymentMethodUiState(
                        nickname = paymentMethod.nickname,
                        selectedType = paymentMethod.type,
                        lastFourDigits = paymentMethod.lastFourDigits ?: "",
                        isLoading = false,
                        isEditMode = true,
                        paymentMethodId = paymentMethod.id
                    )
                } else {
                    _uiState.value = AddEditPaymentMethodUiState(
                        isLoading = false,
                        error = "Payment method not found"
                    )
                }
            } catch (e: IllegalArgumentException) {
                _uiState.value = AddEditPaymentMethodUiState(
                    isLoading = false,
                    error = "Invalid payment method ID"
                )
            }
        }
    }

    fun onNicknameChange(nickname: String) {
        _uiState.value = _uiState.value.copy(
            nickname = nickname,
            nicknameError = null
        )
    }

    fun onLastFourDigitsChange(digits: String) {
        // Only allow digits and limit to 4 characters
        val filteredDigits = digits.filter { it.isDigit() }.take(4)
        _uiState.value = _uiState.value.copy(
            lastFourDigits = filteredDigits,
            lastFourDigitsError = null
        )
    }

    fun showTypeDialog() {
        _uiState.value = _uiState.value.copy(showTypeDialog = true)
    }

    fun dismissTypeDialog() {
        _uiState.value = _uiState.value.copy(showTypeDialog = false)
    }

    fun onTypeSelected(type: PaymentType) {
        _uiState.value = _uiState.value.copy(
            selectedType = type,
            showTypeDialog = false
        )
    }

    fun onSaveClick() {
        if (!validateForm()) {
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true)

            try {
                val paymentMethod = PaymentMethod(
                    id = _uiState.value.paymentMethodId ?: UUID.randomUUID(),
                    nickname = _uiState.value.nickname.trim(),
                    type = _uiState.value.selectedType,
                    lastFourDigits = _uiState.value.lastFourDigits.takeIf { it.isNotBlank() },
                    icon = null // Icon not used in current implementation
                )

                if (_uiState.value.isEditMode) {
                    paymentMethodRepository.updatePaymentMethod(paymentMethod)
                } else {
                    paymentMethodRepository.insertPaymentMethod(paymentMethod)
                }

                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveSuccess = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    error = "Failed to save payment method: ${e.message}"
                )
            }
        }
    }

    private fun validateForm(): Boolean {
        val nickname = _uiState.value.nickname.trim()
        val lastFourDigits = _uiState.value.lastFourDigits

        var isValid = true

        // Validate nickname
        if (nickname.isBlank()) {
            _uiState.value = _uiState.value.copy(nicknameError = "Nickname is required")
            isValid = false
        }

        // Validate last four digits (optional, but if provided must be 4 digits)
        if (lastFourDigits.isNotBlank() && lastFourDigits.length != 4) {
            _uiState.value = _uiState.value.copy(lastFourDigitsError = "Must be exactly 4 digits")
            isValid = false
        }

        return isValid
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}
