import Foundation

final class UpdateNextBillingDateUseCase {
    private let subscriptionRepository: SubscriptionRepository

    init(subscriptionRepository: SubscriptionRepository) {
        self.subscriptionRepository = subscriptionRepository
    }

    func execute(subscription: Subscription) async throws {
        let nextDate = calculateNextBillingDate(for: subscription)
        var updated = subscription
        updated.nextBillingDate = nextDate
        try await subscriptionRepository.updateSubscription(updated)
    }

    private func calculateNextBillingDate(for subscription: Subscription) -> Date {
        let cal = Calendar.current
        let current = subscription.nextBillingDate
        switch subscription.frequency {
        case .weekly:
            return cal.date(byAdding: .weekOfYear, value: 1, to: current)!
        case .monthly:
            return cal.date(byAdding: .month, value: 1, to: current)!
        case .quarterly:
            return cal.date(byAdding: .month, value: 3, to: current)!
        case .semiAnnual:
            return cal.date(byAdding: .month, value: 6, to: current)!
        case .annual:
            return cal.date(byAdding: .year, value: 1, to: current)!
        case .custom:
            return cal.date(byAdding: .month, value: 1, to: current)!
        }
    }
}
