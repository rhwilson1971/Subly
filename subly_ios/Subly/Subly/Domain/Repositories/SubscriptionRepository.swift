import Foundation
import Combine

protocol SubscriptionRepository {
    func getAllSubscriptions() -> AnyPublisher<[Subscription], Never>
    func getActiveSubscriptions() -> AnyPublisher<[Subscription], Never>
    func getSubscriptionById(_ id: UUID) -> AnyPublisher<Subscription?, Never>
    func getSubscriptionsBetweenDates(start: Date, end: Date) -> AnyPublisher<[Subscription], Never>
    func getUpcomingSubscriptions(before date: Date) -> AnyPublisher<[Subscription], Never>
    func getMonthlyTotal() -> AnyPublisher<Double, Never>
    func insertSubscription(_ subscription: Subscription) async throws
    func updateSubscription(_ subscription: Subscription) async throws
    func deleteSubscription(_ subscription: Subscription) async throws
    func deleteSubscription(id: UUID) async throws
}
