package net.cynreub.subly.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.cynreub.subly.domain.model.User
import net.cynreub.subly.domain.repository.UserProfileRepository
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class ProfileSetupUiState(
    val fullName: String = "",
    val email: String = "",
    val dateOfBirth: LocalDate? = null,
    val phoneNumber: String = "",
    val fullNameError: String? = null,
    val showDatePicker: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val navigateNext: Boolean = false
)

@HiltViewModel
class ProfileSetupViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileSetupUiState())
    val uiState: StateFlow<ProfileSetupUiState> = _uiState.asStateFlow()

    init {
        val firebaseUser = auth.currentUser
        _uiState.value = _uiState.value.copy(
            email = firebaseUser?.email ?: "",
            fullName = firebaseUser?.displayName ?: ""
        )
    }

    fun onFullNameChange(value: String) {
        _uiState.value = _uiState.value.copy(fullName = value, fullNameError = null)
    }

    fun onPhoneNumberChange(value: String) {
        _uiState.value = _uiState.value.copy(phoneNumber = value)
    }

    fun showDatePicker() {
        _uiState.value = _uiState.value.copy(showDatePicker = true)
    }

    fun dismissDatePicker() {
        _uiState.value = _uiState.value.copy(showDatePicker = false)
    }

    fun onDateOfBirthSelected(date: LocalDate) {
        _uiState.value = _uiState.value.copy(dateOfBirth = date, showDatePicker = false)
    }

    fun clearDateOfBirth() {
        _uiState.value = _uiState.value.copy(dateOfBirth = null)
    }

    fun saveProfile() {
        val state = _uiState.value

        if (state.fullName.isBlank()) {
            _uiState.value = state.copy(fullNameError = "Full name is required")
            return
        }

        val uid = auth.currentUser?.uid ?: run {
            _uiState.value = state.copy(error = "Not signed in. Please log in again.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)

            val user = User(
                uid = uid,
                email = state.email.ifBlank { null },
                displayName = state.fullName,
                fullName = state.fullName,
                dateOfBirth = state.dateOfBirth?.format(DateTimeFormatter.ISO_LOCAL_DATE),
                phoneNumber = state.phoneNumber.ifBlank { null }
            )

            userProfileRepository.saveProfile(user).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isSaving = false, navigateNext = true)
                },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = e.message ?: "Failed to save profile"
                    )
                }
            )
        }
    }

    fun onNavigateNextHandled() {
        _uiState.value = _uiState.value.copy(navigateNext = false)
    }
}
