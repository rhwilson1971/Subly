package net.cynreub.subly.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Patterns
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.cynreub.subly.data.preferences.PreferencesManager
import net.cynreub.subly.data.preferences.StorageProviderPreference
import net.cynreub.subly.domain.model.User
import net.cynreub.subly.domain.repository.AuthRepository
import net.cynreub.subly.domain.repository.UserProfileRepository
import net.cynreub.subly.ui.auth.PasswordValidator
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import javax.inject.Inject

data class ProfileSetupUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordErrors: List<String> = emptyList(),
    val fullName: String = "",
    val dateOfBirth: LocalDate? = null,
    val phoneNumber: String = "",
    val showDatePicker: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val navigateNext: Boolean = false
)

@HiltViewModel
class ProfileSetupViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userProfileRepository: UserProfileRepository,
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileSetupUiState())
    val uiState: StateFlow<ProfileSetupUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, emailError = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(
            password = value,
            passwordErrors = PasswordValidator.errors(value)
        )
    }

    fun onFullNameChange(value: String) {
        _uiState.value = _uiState.value.copy(fullName = value)
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

    fun signUp() {
        val state = _uiState.value

        val emailError = if (!Patterns.EMAIL_ADDRESS.matcher(state.email).matches()) {
            "Enter a valid email address"
        } else null

        val passwordErrors = PasswordValidator.errors(state.password)

        if (emailError != null || passwordErrors.isNotEmpty()) {
            _uiState.value = state.copy(emailError = emailError, passwordErrors = passwordErrors)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, error = null)

            authRepository.registerWithEmail(state.email, state.password).fold(
                onSuccess = { authUser -> saveProfileAndFinish(authUser.uid, state) },
                onFailure = { e ->
                    _uiState.value = _uiState.value.copy(
                        isSaving = false,
                        error = e.message ?: "Failed to create account"
                    )
                }
            )
        }
    }

    private suspend fun saveProfileAndFinish(uid: String, state: ProfileSetupUiState) {
        val user = User(
            uid = uid,
            email = state.email,
            displayName = state.fullName.ifBlank { null },
            fullName = state.fullName.ifBlank { null },
            dateOfBirth = state.dateOfBirth?.format(DateTimeFormatter.ISO_LOCAL_DATE),
            phoneNumber = state.phoneNumber.ifBlank { null }
        )

        // Best-effort — an account was already created; don't block the user on this write.
        userProfileRepository.saveProfile(user).onFailure { it.printStackTrace() }

        preferencesManager.updateStorageProvider(StorageProviderPreference.FIREBASE)
        preferencesManager.updateHasCompletedOnboarding(true)

        _uiState.value = _uiState.value.copy(isSaving = false, navigateNext = true)
    }

    fun onNavigateNextHandled() {
        _uiState.value = _uiState.value.copy(navigateNext = false)
    }
}
