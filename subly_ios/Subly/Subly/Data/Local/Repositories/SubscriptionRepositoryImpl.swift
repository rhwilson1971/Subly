import Foundation
import SwiftData
import Combine

final class SubscriptionRepositoryImpl: SubscriptionRepository {
    private let modelContext: ModelContext
    private let subject = PassthroughSubject<Void, Never>()

    init(modelContext: ModelContext) {
        self.modelContext = modelContext
    }

    // MARK: - Reactive reads

    func getAllSubscriptions() -> AnyPublisher<[Subscription], Never> {
        subject
            .prepend(())
            .map { [weak self] _ in self?.fetchAll() ?? [] }
            .eraseToAnyPublisher()
    }

    func getActiveSubscriptions() -> AnyPublisher<[Subscription], Never> {
        subject
            .prepend(())
            .map { [weak self] _ in
                (self?.fetchAll() ?? []).filter { $0.isActive }
            }
            .eraseToAnyPublisher()
    }

    func getSubscriptionById(_ id: UUID) -> AnyPublisher<Subscription?, Never> {
        subject
            .prepend(())
            .map { [weak self] _ in self?.fetchAll().first(where: { $0.id == id }) }
            .eraseToAnyPublisher()
    }

    func getSubscriptionsBetweenDates(start: Date, end: Date) -> AnyPublisher<[Subscription], Never> {
        subject
            .prepend(())
            .map { [weak self] _ in
                (self?.fetchAll() ?? []).filter { $0.nextBillingDate >= start && $0.nextBillingDate <= end }
            }
            .eraseToAnyPublisher()
    }

    func getUpcomingSubscriptions(before date: Date) -> AnyPublisher<[Subscription], Never> {
        subject
            .prepend(())
            .map { [weak self] _ in
                let now = Date()
                return (self?.fetchAll() ?? [])
                    .filter { $0.isActive && $0.nextBillingDate >= now && $0.nextBillingDate <= date }
                    .sorted { $0.nextBillingDate < $1.nextBillingDate }
            }
            .eraseToAnyPublisher()
    }

    func getMonthlyTotal() -> AnyPublisher<Double, Never> {
        getActiveSubscriptions()
            .map { $0.reduce(0.0) { $0 + $1.monthlyAmount } }
            .eraseToAnyPublisher()
    }

    // MARK: - Mutations

    func insertSubscription(_ subscription: Subscription) async throws {
        let model = SubscriptionModel(from: subscription)
        modelContext.insert(model)
        try modelContext.save()
        subject.send()
    }

    func updateSubscription(_ subscription: Subscription) async throws {
        let descriptor = FetchDescriptor<SubscriptionModel>(
            predicate: #Predicate { $0.id == subscription.id }
        )
        if let existing = try modelContext.fetch(descriptor).first {
            existing.update(from: subscription)
            try modelContext.save()
            subject.send()
        }
    }

    func deleteSubscription(_ subscription: Subscription) async throws {
        try await deleteSubscription(id: subscription.id)
    }

    func deleteSubscription(id: UUID) async throws {
        let descriptor = FetchDescriptor<SubscriptionModel>(
            predicate: #Predicate { $0.id == id }
        )
        if let existing = try modelContext.fetch(descriptor).first {
            modelContext.delete(existing)
            try modelContext.save()
            subject.send()
        }
    }

    // MARK: - Private

    private func fetchAll() -> [Subscription] {
        let descriptor = FetchDescriptor<SubscriptionModel>(
            sortBy: [SortDescriptor(\.nextBillingDate)]
        )
        return (try? modelContext.fetch(descriptor))?.map { $0.toDomain() } ?? []
    }
}
