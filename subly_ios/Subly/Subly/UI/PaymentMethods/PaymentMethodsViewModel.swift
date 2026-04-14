import Foundation
import Combine
import Observation

@Observable
final class PaymentMethodsViewModel {
    var uiState = PaymentMethodsUiState()

    private let paymentMethodRepository: PaymentMethodRepository
    private var cancellables = Set<AnyCancellable>()

    init(paymentMethodRepository: PaymentMethodRepository) {
        self.paymentMethodRepository = paymentMethodRepository
        loadPaymentMethods()
    }

    private func loadPaymentMethods() {
        paymentMethodRepository.getAllPaymentMethods()
            .receive(on: DispatchQueue.main)
            .sink { [weak self] methods in
                guard let self else { return }
                Task { @MainActor in
                    var withUsage: [PaymentMethodWithUsage] = []
                    for method in methods {
                        let count = await self.paymentMethodRepository.getSubscriptionCount(for: method.id)
                        withUsage.append(PaymentMethodWithUsage(paymentMethod: method, subscriptionCount: count))
                    }
                    self.uiState.paymentMethods = withUsage
                    self.uiState.isLoading = false
                    self.uiState.error = nil
                }
            }
            .store(in: &cancellables)
    }

    func requestDelete(_ item: PaymentMethodWithUsage) {
        if item.subscriptionCount > 0 {
            uiState.error = "Cannot delete: used by \(item.subscriptionCount) subscription\(item.subscriptionCount == 1 ? "" : "s")"
            return
        }
        uiState.deletingMethod = item
        uiState.showDeleteConfirmation = true
    }

    func dismissDelete() {
        uiState.deletingMethod = nil
        uiState.showDeleteConfirmation = false
    }

    func confirmDelete() {
        guard let item = uiState.deletingMethod else { return }
        uiState.isDeleting = true
        uiState.showDeleteConfirmation = false

        Task { @MainActor in
            do {
                try await paymentMethodRepository.deletePaymentMethod(id: item.paymentMethod.id)
                uiState.isDeleting = false
                uiState.deletingMethod = nil
            } catch {
                uiState.isDeleting = false
                uiState.deletingMethod = nil
                uiState.error = "Failed to delete: \(error.localizedDescription)"
            }
        }
    }

    func clearError() {
        uiState.error = nil
    }
}
