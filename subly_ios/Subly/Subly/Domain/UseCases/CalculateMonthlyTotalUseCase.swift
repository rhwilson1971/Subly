import Foundation
import Combine

final class CalculateMonthlyTotalUseCase {
    private let subscriptionRepository: SubscriptionRepository

    init(subscriptionRepository: SubscriptionRepository) {
        self.subscriptionRepository = subscriptionRepository
    }

    func execute() -> AnyPublisher<Double, Never> {
        subscriptionRepository.getActiveSubscriptions()
            .map { subscriptions in
                subscriptions.reduce(0.0) { $0 + $1.monthlyAmount }
            }
            .eraseToAnyPublisher()
    }
}
