package net.cynreub.subly.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import net.cynreub.subly.data.remote.firestore.FirestoreSyncOrchestrator
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val syncOrchestrator: FirestoreSyncOrchestrator
) : ViewModel() {

    /** Call whenever the user is confirmed logged in (cold start or after sign-in). */
    fun onUserLoggedIn(uid: String) {
        viewModelScope.launch {
            try {
                syncOrchestrator.ensureCategoriesSeeded(uid)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
