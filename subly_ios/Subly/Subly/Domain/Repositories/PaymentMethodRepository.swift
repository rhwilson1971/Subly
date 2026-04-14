import Foundation
import Combine

protocol PaymentMethodRepository {
    func getAllPaymentMethods() -> AnyPublisher<[PaymentMethod], Never>
    func getPaymentMethodById(_ id: UUID) -> AnyPublisher<PaymentMethod?, Never>
    func insertPaymentMethod(_ paymentMethod: PaymentMethod) async throws
    func updatePaymentMethod(_ paymentMethod: PaymentMethod) async throws
    func deletePaymentMethod(_ paymentMethod: PaymentMethod) async throws
    func deletePaymentMethod(id: UUID) async throws
    func getSubscriptionCount(for paymentMethodId: UUID) async -> Int
}
