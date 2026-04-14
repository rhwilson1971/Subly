import Foundation
import Combine
import Observation

@Observable
final class SubscriptionDetailViewModel {
    var uiState = SubscriptionDetailUiState()

    private let subscriptionId: UUID
    private let subscriptionRepository: SubscriptionRepository
    private let categoryRepository: CategoryRepository
    private let paymentMethodRepository: PaymentMethodRepository
    private var cancellables = Set<AnyCancellable>()

    init(
        subscriptionId: UUID,
        subscriptionRepository: SubscriptionRepository,
        categoryRepository: CategoryRepository,
        paymentMethodRepository: PaymentMethodRepository
    ) {
        self.subscriptionId = subscriptionId
        self.subscriptionRepository = subscriptionRepository
        self.categoryRepository = categoryRepository
        self.paymentMethodRepository = paymentMethodRepository
        loadData()
    }

    private func loadData() {
        Publishers.CombineLatest3(
            subscriptionRepository.getSubscriptionById(subscriptionId),
            categoryRepository.getAllCategories(),
            paymentMethodRepository.getAllPaymentMethods()
        )
        .receive(on: DispatchQueue.main)
        .sink { [weak self] subscription, categories, paymentMethods in
            guard let self else { return }
            uiState.subscription = subscription
            uiState.category = categories.first { $0.id == subscription?.categoryId }
            uiState.paymentMethod = paymentMethods.first { $0.id == subscription?.paymentMethodId }
            uiState.isLoading = false
        }
        .store(in: &cancellables)
    }

    func toggleActive() {
        guard var subscription = uiState.subscription else { return }
        uiState.isTogglingActive = true
        subscription.isActive.toggle()

        Task { @MainActor in
            do {
                try await subscriptionRepository.updateSubscription(subscription)
                uiState.isTogglingActive = false
            } catch {
                uiState.isTogglingActive = false
                uiState.error = "Failed to update: \(error.localizedDescription)"
            }
        }
    }

    func deleteSubscription(onSuccess: @escaping () -> Void) {
        guard let subscription = uiState.subscription else { return }
        uiState.isDeleting = true

        Task { @MainActor in
            do {
                try await subscriptionRepository.deleteSubscription(subscription)
                uiState.isDeleting = false
                uiState.deleteSuccess = true
                onSuccess()
            } catch {
                uiState.isDeleting = false
                uiState.error = "Failed to delete: \(error.localizedDescription)"
            }
        }
    }
}
