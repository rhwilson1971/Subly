package net.cynreub.subly.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import net.cynreub.subly.data.preferences.PreferencesManager
import net.cynreub.subly.data.preferences.StorageProviderPreference
import net.cynreub.subly.domain.model.User
import net.cynreub.subly.domain.repository.AuthRepository
import net.cynreub.subly.ui.navigation.NavDestination
import javax.inject.Inject

data class StartupState(
    val isReady: Boolean = false,
    val startDestination: String = NavDestination.Onboarding.route
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _startupState = MutableStateFlow(StartupState())
    val startupState: StateFlow<StartupState> = _startupState.asStateFlow()

    private val _currentUser = MutableStateFlow(authRepository.currentUser)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    init {
        resolveStartDestination()
    }

    /**
     * Sign-up is optional: the app never forces Login/Register. First run shows
     * Onboarding once (tracked locally, independent of auth state); every run after
     * that goes straight to Home, whether or not the user ever created an account.
     *
     * Deliberately does not consult the signed-in user's Firestore `onboardingCompleted`
     * flag here — that would require a network round-trip on every cold start. This means
     * a returning user who reinstalls will see Onboarding again once, even though their
     * cloud profile says otherwise; accepted tradeoff (see Jira SUB-57 plan).
     */
    private fun resolveStartDestination() {
        viewModelScope.launch {
            val hasCompletedOnboarding = preferencesManager.hasCompletedOnboarding.first()
            val destination = if (hasCompletedOnboarding) {
                NavDestination.Home.route
            } else {
                NavDestination.Onboarding.route
            }
            _startupState.value = StartupState(isReady = true, startDestination = destination)
        }
    }

    fun signOut() {
        viewModelScope.launch {
            authRepository.signOut()
            _currentUser.value = null
            // No account left to sync with — drop back to local storage.
            preferencesManager.updateStorageProvider(StorageProviderPreference.LOCAL)
        }
    }
}
