import Foundation
import Combine
import Observation

@Observable
final class HomeViewModel {
    var uiState = HomeUiState()

    private let getUpcomingSubscriptions: GetUpcomingSubscriptionsUseCase
    private let getSubscriptionStats: GetSubscriptionStatsUseCase
    private let getCategorySpend: GetCategorySpendUseCase
    private var cancellables = Set<AnyCancellable>()

    init(
        getUpcomingSubscriptions: GetUpcomingSubscriptionsUseCase,
        getSubscriptionStats: GetSubscriptionStatsUseCase,
        getCategorySpend: GetCategorySpendUseCase
    ) {
        self.getUpcomingSubscriptions = getUpcomingSubscriptions
        self.getSubscriptionStats = getSubscriptionStats
        self.getCategorySpend = getCategorySpend
        loadDashboardData()
    }

    private func loadDashboardData() {
        Publishers.CombineLatest3(
            getUpcomingSubscriptions.execute(days: 30),
            getSubscriptionStats.execute(),
            getCategorySpend.execute()
        )
        .receive(on: DispatchQueue.main)
        .sink { [weak self] upcoming, stats, categorySpend in
            guard let self else { return }
            uiState = HomeUiState(
                upcomingSubscriptions: Array(upcoming.prefix(5)),
                stats: stats,
                categorySpend: categorySpend,
                isLoading: false,
                error: nil
            )
        }
        .store(in: &cancellables)
    }

    func refresh() {
        uiState.isLoading = true
        cancellables.removeAll()
        loadDashboardData()
    }
}
