package net.cynreub.subly.ui.subscriptions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import net.cynreub.subly.domain.model.Subscription
import net.cynreub.subly.domain.repository.SubscriptionRepository
import javax.inject.Inject

@HiltViewModel
class SubscriptionsViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SubscriptionsUiState())
    val uiState: StateFlow<SubscriptionsUiState> = _uiState.asStateFlow()

    init {
        loadSubscriptions()
    }

    private fun loadSubscriptions() {
        viewModelScope.launch {
            subscriptionRepository.getAllSubscriptions()
                .catch { e ->
                    _uiState.value = SubscriptionsUiState(
                        isLoading = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
                .map { subscriptions ->
                    _uiState.value.copy(
                        subscriptions = subscriptions,
                        filteredSubscriptions = applyFiltersAndSort(subscriptions),
                        isLoading = false,
                        error = null
                    )
                }
                .collect { state ->
                    _uiState.value = state
                }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        updateFilteredSubscriptions()
    }

    fun onFilterChange(filter: SubscriptionFilter) {
        _uiState.value = _uiState.value.copy(selectedFilter = filter)
        updateFilteredSubscriptions()
    }

    fun onSortOrderChange(sortOrder: SortOrder) {
        _uiState.value = _uiState.value.copy(sortOrder = sortOrder)
        updateFilteredSubscriptions()
    }

    private fun updateFilteredSubscriptions() {
        val filtered = applyFiltersAndSort(_uiState.value.subscriptions)
        _uiState.value = _uiState.value.copy(filteredSubscriptions = filtered)
    }

    private fun applyFiltersAndSort(subscriptions: List<Subscription>): List<Subscription> {
        var result = subscriptions

        // Apply search filter
        if (_uiState.value.searchQuery.isNotEmpty()) {
            result = result.filter { subscription ->
                subscription.name.contains(_uiState.value.searchQuery, ignoreCase = true) ||
                subscription.type.name.contains(_uiState.value.searchQuery, ignoreCase = true)
            }
        }

        // Apply status filter
        result = when (_uiState.value.selectedFilter) {
            SubscriptionFilter.ALL -> result
            SubscriptionFilter.ACTIVE -> result.filter { it.isActive }
            SubscriptionFilter.INACTIVE -> result.filter { !it.isActive }
            SubscriptionFilter.BY_TYPE -> result
        }

        // Apply sorting
        result = when (_uiState.value.sortOrder) {
            SortOrder.NAME_ASC -> result.sortedBy { it.name.lowercase() }
            SortOrder.NAME_DESC -> result.sortedByDescending { it.name.lowercase() }
            SortOrder.AMOUNT_ASC -> result.sortedBy { it.amount }
            SortOrder.AMOUNT_DESC -> result.sortedByDescending { it.amount }
            SortOrder.NEXT_BILLING_DATE -> result.sortedBy { it.nextBillingDate }
        }

        return result
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        loadSubscriptions()
    }
}
