import Foundation
import SwiftData
import Combine

final class PaymentMethodRepositoryImpl: PaymentMethodRepository {
    private let modelContext: ModelContext
    private let subject = PassthroughSubject<Void, Never>()

    init(modelContext: ModelContext) {
        self.modelContext = modelContext
    }

    // MARK: - Reactive reads

    func getAllPaymentMethods() -> AnyPublisher<[PaymentMethod], Never> {
        subject
            .prepend(())
            .map { [weak self] _ in self?.fetchAll() ?? [] }
            .eraseToAnyPublisher()
    }

    func getPaymentMethodById(_ id: UUID) -> AnyPublisher<PaymentMethod?, Never> {
        subject
            .prepend(())
            .map { [weak self] _ in self?.fetchAll().first(where: { $0.id == id }) }
            .eraseToAnyPublisher()
    }

    // MARK: - Mutations

    func insertPaymentMethod(_ paymentMethod: PaymentMethod) async throws {
        let model = PaymentMethodModel(from: paymentMethod)
        modelContext.insert(model)
        try modelContext.save()
        subject.send()
    }

    func updatePaymentMethod(_ paymentMethod: PaymentMethod) async throws {
        let descriptor = FetchDescriptor<PaymentMethodModel>(
            predicate: #Predicate { $0.id == paymentMethod.id }
        )
        if let existing = try modelContext.fetch(descriptor).first {
            existing.update(from: paymentMethod)
            try modelContext.save()
            subject.send()
        }
    }

    func deletePaymentMethod(_ paymentMethod: PaymentMethod) async throws {
        try await deletePaymentMethod(id: paymentMethod.id)
    }

    func deletePaymentMethod(id: UUID) async throws {
        let descriptor = FetchDescriptor<PaymentMethodModel>(
            predicate: #Predicate { $0.id == id }
        )
        if let existing = try modelContext.fetch(descriptor).first {
            modelContext.delete(existing)
            try modelContext.save()
            subject.send()
        }
    }

    func getSubscriptionCount(for paymentMethodId: UUID) async -> Int {
        let descriptor = FetchDescriptor<SubscriptionModel>(
            predicate: #Predicate { $0.paymentMethodId == paymentMethodId }
        )
        return (try? modelContext.fetch(descriptor).count) ?? 0
    }

    // MARK: - Private

    private func fetchAll() -> [PaymentMethod] {
        let descriptor = FetchDescriptor<PaymentMethodModel>(sortBy: [SortDescriptor(\.nickname)])
        return (try? modelContext.fetch(descriptor))?.map { $0.toDomain() } ?? []
    }
}
