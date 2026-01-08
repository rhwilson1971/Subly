package net.cynreub.subly.ui.subscriptions

import net.cynreub.subly.domain.model.Subscription
import net.cynreub.subly.domain.model.SubscriptionType

data class SubscriptionsUiState(
    val subscriptions: List<Subscription> = emptyList(),
    val filteredSubscriptions: List<Subscription> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: SubscriptionFilter = SubscriptionFilter.ALL,
    val sortOrder: SortOrder = SortOrder.NEXT_BILLING_DATE,
    val isLoading: Boolean = true,
    val error: String? = null
)

enum class SubscriptionFilter {
    ALL,
    ACTIVE,
    INACTIVE,
    BY_TYPE
}

enum class SortOrder {
    NAME_ASC,
    NAME_DESC,
    AMOUNT_ASC,
    AMOUNT_DESC,
    NEXT_BILLING_DATE
}
