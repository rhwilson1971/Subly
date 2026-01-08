package net.cynreub.subly.ui.home

import net.cynreub.subly.domain.model.Subscription
import net.cynreub.subly.domain.usecase.SubscriptionStats

data class HomeUiState(
    val upcomingSubscriptions: List<Subscription> = emptyList(),
    val stats: SubscriptionStats = SubscriptionStats(
        totalMonthly = 0.0,
        totalYearly = 0.0,
        activeCount = 0,
        categoryBreakdown = emptyMap()
    ),
    val isLoading: Boolean = true,
    val error: String? = null
)
