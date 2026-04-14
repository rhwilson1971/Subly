package net.cynreub.subly.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
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
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun onPageChanged(page: Int) {
        _uiState.value = _uiState.value.copy(currentPage = page)
    }

    fun completeOnboarding() {
        val uid = auth.currentUser?.uid ?: run {
            _uiState.value = _uiState.value.copy(error = "Not signed in. Please log in again.")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCompleting = true, error = null)
            userProfileRepository.markOnboardingCompleted(uid).fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(isCompleting = false, navigateToHome = true)
                },
                onFailure = { e ->
                    // Non-fatal — let the user proceed even if the write fails
                    _uiState.value = _uiState.value.copy(isCompleting = false, navigateToHome = true)
                    e.printStackTrace()
                }
            )
        }
    }

    fun onNavigateToHomeHandled() {
        _uiState.value = _uiState.value.copy(navigateToHome = false)
    }
}
