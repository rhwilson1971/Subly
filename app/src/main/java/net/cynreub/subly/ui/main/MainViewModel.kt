package net.cynreub.subly.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import net.cynreub.subly.domain.repository.UserProfileRepository
import net.cynreub.subly.ui.navigation.NavDestination
import javax.inject.Inject

data class StartupState(
    val isReady: Boolean = false,
    val startDestination: String = NavDestination.Login.route
)

@HiltViewModel
class MainViewModel @Inject constructor(
    private val userProfileRepository: UserProfileRepository,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _startupState = MutableStateFlow(StartupState())
    val startupState: StateFlow<StartupState> = _startupState.asStateFlow()

    init {
        resolveStartDestination()
    }

    private fun resolveStartDestination() {
        viewModelScope.launch {
            val currentUser = auth.currentUser

            if (currentUser == null) {
                _startupState.value = StartupState(isReady = true, startDestination = NavDestination.Login.route)
                return@launch
            }

            // User is logged in — check whether they completed onboarding
            val profile = userProfileRepository.getProfile(currentUser.uid).getOrNull()
            val destination = if (profile != null && profile.onboardingCompleted) {
                NavDestination.Home.route
            } else {
                NavDestination.Onboarding.route
            }

            _startupState.value = StartupState(isReady = true, startDestination = destination)
        }
    }
}
