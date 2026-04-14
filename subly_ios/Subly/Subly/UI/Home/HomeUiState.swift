import Foundation

struct HomeUiState {
    var upcomingSubscriptions: [Subscription] = []
    var stats: SubscriptionStats = SubscriptionStats(
        totalMonthly: 0,
        totalYearly: 0,
        activeCount: 0,
        categoryBreakdown: [:]
    )
    var categorySpend: [CategorySpend] = []
    var isLoading: Bool = true
    var error: String? = nil

    /// Top 3 subscriptions by monthly cost equivalent
    var topSubscriptions: [Subscription] { Array(upcomingSubscriptions.sorted { $0.monthlyAmount > $1.monthlyAmount }.prefix(3)) }
}
