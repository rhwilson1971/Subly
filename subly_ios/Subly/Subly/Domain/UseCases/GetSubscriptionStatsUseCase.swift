import Foundation
import Combine

struct SubscriptionStats {
    let totalMonthly: Double
    let totalYearly: Double
    let activeCount: Int
    let categoryBreakdown: [UUID: Double] // categoryId → monthly spend equivalent
}

final class GetSubscriptionStatsUseCase {
    private let subscriptionRepository: SubscriptionRepository

    init(subscriptionRepository: SubscriptionRepository) {
        self.subscriptionRepository = subscriptionRepository
    }

    func execute() -> AnyPublisher<SubscriptionStats, Never> {
        subscriptionRepository.getActiveSubscriptions()
            .map { subscriptions in
                let totalMonthly = subscriptions.reduce(0.0) { $0 + $1.monthlyAmount }

                let categoryBreakdown = Dictionary(
                    grouping: subscriptions,
                    by: { $0.categoryId }
                ).mapValues { subs in
                    subs.reduce(0.0) { $0 + $1.monthlyAmount }
                }

                return SubscriptionStats(
                    totalMonthly: totalMonthly,
                    totalYearly: totalMonthly * 12,
                    activeCount: subscriptions.count,
                    categoryBreakdown: categoryBreakdown
                )
            }
            .eraseToAnyPublisher()
    }
}
