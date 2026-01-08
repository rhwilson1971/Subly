package net.cynreub.subly.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import net.cynreub.subly.domain.usecase.GetSubscriptionStatsUseCase
import net.cynreub.subly.domain.usecase.GetUpcomingSubscriptionsUseCase
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getUpcomingSubscriptionsUseCase: GetUpcomingSubscriptionsUseCase,
    private val getSubscriptionStatsUseCase: GetSubscriptionStatsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            combine(
                getUpcomingSubscriptionsUseCase(days = 30),
                getSubscriptionStatsUseCase()
            ) { upcomingSubscriptions, stats ->
                HomeUiState(
                    upcomingSubscriptions = upcomingSubscriptions.take(5),
                    stats = stats,
                    isLoading = false,
                    error = null
                )
            }
                .catch { e ->
                    _uiState.value = HomeUiState(
                        isLoading = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        loadDashboardData()
    }
}
