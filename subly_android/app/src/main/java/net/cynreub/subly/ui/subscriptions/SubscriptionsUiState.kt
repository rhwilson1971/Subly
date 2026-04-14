package net.cynreub.subly.ui.subscriptions

import net.cynreub.subly.domain.model.Category
import net.cynreub.subly.domain.model.Subscription
import java.util.UUID

sealed class DashboardFilter(val label: String) {
    object Monthly : DashboardFilter("Monthly Subscriptions")
    object Yearly : DashboardFilter("Yearly Subscriptions")
    object Active : DashboardFilter("Active Subscriptions")

    /** Filters by a specific category. [categoryId] is the UUID, [displayName] is for the label. */
    data class ByCategory(val categoryId: UUID, val displayName: String) :
        DashboardFilter("$displayName Subscriptions")

    companion object {
        private const val CAT_PREFIX = "cat|"

        fun fromRouteArg(arg: String): DashboardFilter? = when (arg) {
            "monthly" -> Monthly
            "yearly"  -> Yearly
            "active"  -> Active
            else -> if (arg.startsWith(CAT_PREFIX)) {
                val parts = arg.removePrefix(CAT_PREFIX).split("|", limit = 2)
                runCatching {
                    ByCategory(
                        categoryId  = UUID.fromString(parts[0]),
                        displayName = parts.getOrElse(1) { "Category" }
                    )
                }.getOrNull()
            } else null
        }

        fun toRouteArg(filter: DashboardFilter): String = when (filter) {
            is Monthly    -> "monthly"
            is Yearly     -> "yearly"
            is Active     -> "active"
            is ByCategory -> "$CAT_PREFIX${filter.categoryId}|${filter.displayName}"
        }
    }
}

data class SubscriptionsUiState(
    val subscriptions: List<Subscription> = emptyList(),
    val filteredSubscriptions: List<Subscription> = emptyList(),
    val searchQuery: String = "",
    val selectedFilter: SubscriptionFilter = SubscriptionFilter.ALL,
    val selectedCategoryId: UUID? = null,       // null = all categories
    val availableCategories: List<Category> = emptyList(),
    val sortOrder: SortOrder = SortOrder.NEXT_BILLING_DATE,
    val activeDashboardFilter: DashboardFilter? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

enum class SubscriptionFilter {
    ALL,
    ACTIVE,
    INACTIVE
}

enum class SortOrder {
    NAME_ASC,
    NAME_DESC,
    AMOUNT_ASC,
    AMOUNT_DESC,
    NEXT_BILLING_DATE
}
