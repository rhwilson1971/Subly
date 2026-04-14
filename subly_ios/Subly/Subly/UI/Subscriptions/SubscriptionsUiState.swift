import Foundation

enum SubscriptionFilter: String, CaseIterable {
    case all      = "All"
    case active   = "Active"
    case inactive = "Inactive"
}

enum SortOrder: String, CaseIterable {
    case nameAsc         = "Name (A-Z)"
    case nameDesc        = "Name (Z-A)"
    case amountAsc       = "Amount (Low)"
    case amountDesc      = "Amount (High)"
    case nextBillingDate = "Next Due"
}

struct SubscriptionsUiState {
    var subscriptions: [Subscription] = []
    var filteredSubscriptions: [Subscription] = []
    var searchQuery: String = ""
    var selectedFilter: SubscriptionFilter = .all
    var selectedCategoryId: UUID? = nil
    var availableCategories: [Category] = []
    var sortOrder: SortOrder = .nextBillingDate
    var isLoading: Bool = true
    var error: String? = nil
}
