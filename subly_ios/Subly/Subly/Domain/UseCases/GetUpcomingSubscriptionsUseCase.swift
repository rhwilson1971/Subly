import Foundation
import Combine

final class GetUpcomingSubscriptionsUseCase {
    private let subscriptionRepository: SubscriptionRepository

    init(subscriptionRepository: SubscriptionRepository) {
        self.subscriptionRepository = subscriptionRepository
    }

    func execute(days: Int = 30) -> AnyPublisher<[Subscription], Never> {
        let targetDate = Calendar.current.date(byAdding: .day, value: days, to: Date()) ?? Date()
        return subscriptionRepository.getUpcomingSubscriptions(before: targetDate)
    }
}
