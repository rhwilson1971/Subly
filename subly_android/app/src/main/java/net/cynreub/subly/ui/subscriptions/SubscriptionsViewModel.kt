package net.cynreub.subly.ui.subscriptions

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import net.cynreub.subly.domain.model.BillingFrequency
import net.cynreub.subly.domain.model.Subscription
import net.cynreub.subly.domain.repository.CategoryRepository
import net.cynreub.subly.domain.repository.SubscriptionRepository
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class SubscriptionsViewModel @Inject constructor(
    private val subscriptionRepository: SubscriptionRepository,
    private val categoryRepository: CategoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialDashboardFilter: DashboardFilter? =
        savedStateHandle.get<String>("dashboardFilter")?.let { DashboardFilter.fromRouteArg(it) }

    private val _uiState = MutableStateFlow(
        SubscriptionsUiState(activeDashboardFilter = initialDashboardFilter)
    )
    val uiState: StateFlow<SubscriptionsUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            combine(
                subscriptionRepository.getAllSubscriptions(),
                categoryRepository.getAllCategories()
            ) { subscriptions, categories ->
                val current = _uiState.value
                current.copy(
                    subscriptions = subscriptions,
                    availableCategories = categories,
                    filteredSubscriptions = applyFiltersAndSort(
                        subscriptions,
                        current.copy(availableCategories = categories)
                    ),
                    isLoading = false,
                    error = null
                )
            }
                .catch { e ->
                    _uiState.value = SubscriptionsUiState(
                        isLoading = false,
                        error = e.message ?: "Unknown error occurred"
                    )
                }
                .collect { state -> _uiState.value = state }
        }
    }

    fun onSearchQueryChange(query: String) {
        val updated = _uiState.value.copy(searchQuery = query)
        _uiState.value = updated.copy(
            filteredSubscriptions = applyFiltersAndSort(updated.subscriptions, updated)
        )
    }

    fun onFilterChange(filter: SubscriptionFilter) {
        val updated = _uiState.value.copy(selectedFilter = filter)
        _uiState.value = updated.copy(
            filteredSubscriptions = applyFiltersAndSort(updated.subscriptions, updated)
        )
    }

    fun onCategoryFilterChange(categoryId: UUID?) {
        val updated = _uiState.value.copy(selectedCategoryId = categoryId)
        _uiState.value = updated.copy(
            filteredSubscriptions = applyFiltersAndSort(updated.subscriptions, updated)
        )
    }

    fun onSortOrderChange(sortOrder: SortOrder) {
        val updated = _uiState.value.copy(sortOrder = sortOrder)
        _uiState.value = updated.copy(
            filteredSubscriptions = applyFiltersAndSort(updated.subscriptions, updated)
        )
    }

    fun refresh() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        loadData()
    }

    private fun applyFiltersAndSort(
        subscriptions: List<Subscription>,
        state: SubscriptionsUiState
    ): List<Subscription> {
        var result = subscriptions

        // Apply locked dashboard filter first
        result = when (val df = state.activeDashboardFilter) {
            is DashboardFilter.Monthly    -> result.filter { it.frequency == BillingFrequency.MONTHLY }
            is DashboardFilter.Yearly     -> result.filter { it.frequency == BillingFrequency.ANNUAL }
            is DashboardFilter.Active     -> result.filter { it.isActive }
            is DashboardFilter.ByCategory -> result.filter { it.categoryId == df.categoryId }
            null -> result
        }

        // Apply search by name
        if (state.searchQuery.isNotEmpty()) {
            result = result.filter { it.name.contains(state.searchQuery, ignoreCase = true) }
        }

        // Apply status filter (skip when dashboard already locks to Active)
        if (state.activeDashboardFilter !is DashboardFilter.Active) {
            result = when (state.selectedFilter) {
                SubscriptionFilter.ALL      -> result
                SubscriptionFilter.ACTIVE   -> result.filter { it.isActive }
                SubscriptionFilter.INACTIVE -> result.filter { !it.isActive }
            }
        }

        // Apply category filter (skip when dashboard already locked to a category)
        if (state.activeDashboardFilter !is DashboardFilter.ByCategory && state.selectedCategoryId != null) {
            result = result.filter { it.categoryId == state.selectedCategoryId }
        }

        // Apply sort
        result = when (state.sortOrder) {
            SortOrder.NAME_ASC          -> result.sortedBy { it.name.lowercase() }
            SortOrder.NAME_DESC         -> result.sortedByDescending { it.name.lowercase() }
            SortOrder.AMOUNT_ASC        -> result.sortedBy { it.amount }
            SortOrder.AMOUNT_DESC       -> result.sortedByDescending { it.amount }
            SortOrder.NEXT_BILLING_DATE -> result.sortedBy { it.nextBillingDate }
        }

        return result
    }
}
