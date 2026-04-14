import Foundation
import Combine
import Observation

@Observable
final class SubscriptionsViewModel {
    var uiState = SubscriptionsUiState()

    private let subscriptionRepository: SubscriptionRepository
    private let categoryRepository: CategoryRepository
    private var cancellables = Set<AnyCancellable>()

    init(subscriptionRepository: SubscriptionRepository, categoryRepository: CategoryRepository) {
        self.subscriptionRepository = subscriptionRepository
        self.categoryRepository = categoryRepository
        loadData()
    }

    private func loadData() {
        Publishers.CombineLatest(
            subscriptionRepository.getAllSubscriptions(),
            categoryRepository.getAllCategories()
        )
        .receive(on: DispatchQueue.main)
        .sink { [weak self] subscriptions, categories in
            guard let self else { return }
            uiState.subscriptions = subscriptions
            uiState.availableCategories = categories
            uiState.filteredSubscriptions = applyFiltersAndSort(subscriptions)
            uiState.isLoading = false
            uiState.error = nil
        }
        .store(in: &cancellables)
    }

    func onSearchQueryChange(_ query: String) {
        uiState.searchQuery = query
        uiState.filteredSubscriptions = applyFiltersAndSort(uiState.subscriptions)
    }

    func onFilterChange(_ filter: SubscriptionFilter) {
        uiState.selectedFilter = filter
        uiState.filteredSubscriptions = applyFiltersAndSort(uiState.subscriptions)
    }

    func onCategoryFilterChange(_ categoryId: UUID?) {
        uiState.selectedCategoryId = categoryId
        uiState.filteredSubscriptions = applyFiltersAndSort(uiState.subscriptions)
    }

    func onSortOrderChange(_ sortOrder: SortOrder) {
        uiState.sortOrder = sortOrder
        uiState.filteredSubscriptions = applyFiltersAndSort(uiState.subscriptions)
    }

    func deleteSubscription(_ subscription: Subscription) {
        Task {
            try? await subscriptionRepository.deleteSubscription(subscription)
        }
    }

    private func applyFiltersAndSort(_ subscriptions: [Subscription]) -> [Subscription] {
        var result = subscriptions

        if !uiState.searchQuery.isEmpty {
            result = result.filter { $0.name.localizedCaseInsensitiveContains(uiState.searchQuery) }
        }

        switch uiState.selectedFilter {
        case .all:      break
        case .active:   result = result.filter { $0.isActive }
        case .inactive: result = result.filter { !$0.isActive }
        }

        if let categoryId = uiState.selectedCategoryId {
            result = result.filter { $0.categoryId == categoryId }
        }

        switch uiState.sortOrder {
        case .nameAsc:         result = result.sorted { $0.name.lowercased() < $1.name.lowercased() }
        case .nameDesc:        result = result.sorted { $0.name.lowercased() > $1.name.lowercased() }
        case .amountAsc:       result = result.sorted { $0.amount < $1.amount }
        case .amountDesc:      result = result.sorted { $0.amount > $1.amount }
        case .nextBillingDate: result = result.sorted { $0.nextBillingDate < $1.nextBillingDate }
        }

        return result
    }
}
