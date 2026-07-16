package net.cynreub.subly.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.cynreub.subly.data.preferences.PreferencesManager
import net.cynreub.subly.domain.repository.UserProfileRepository
import javax.inject.Inject

data class OnboardingUiState(
    val currentPage: Int = 0,
    val isCompleting: Boolean = false,
    val error: String? = null,
    val navigateToHome: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val preferencesManager: PreferencesManager,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onPageChanged(page: Int) {
        _uiState.value = _uiState.value.copy(currentPage = page)
    }

    /** Works whether or not the user has an account — onboarding is no longer auth-gated. */
    fun completeOnboarding() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCompleting = true, error = null)

            preferencesManager.updateHasCompletedOnboarding(true)

            auth.currentUser?.uid?.let { uid ->
                userProfileRepository.markOnboardingCompleted(uid).onFailure { it.printStackTrace() }
            }

            _uiState.value = _uiState.value.copy(isCompleting = false, navigateToHome = true)
        }
    }

    fun onNavigateToHomeHandled() {
        _uiState.value = _uiState.value.copy(navigateToHome = false)
    }
}
