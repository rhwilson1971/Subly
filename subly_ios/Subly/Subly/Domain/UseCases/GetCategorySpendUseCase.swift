import Foundation
import Combine

struct CategorySpend: Identifiable {
    let category: Category
    let monthlyAmount: Double
    let percentage: Float  // 0.0–1.0 share of total monthly spend
    var id: UUID { category.id }
}

final class GetCategorySpendUseCase {
    private let subscriptionRepository: SubscriptionRepository
    private let categoryRepository: CategoryRepository

    init(
        subscriptionRepository: SubscriptionRepository,
        categoryRepository: CategoryRepository
    ) {
        self.subscriptionRepository = subscriptionRepository
        self.categoryRepository = categoryRepository
    }

    func execute() -> AnyPublisher<[CategorySpend], Never> {
        Publishers.CombineLatest(
            subscriptionRepository.getActiveSubscriptions(),
            categoryRepository.getAllCategories()
        )
        .map { subscriptions, categories in
            let categoryMap = Dictionary(uniqueKeysWithValues: categories.map { ($0.id, $0) })

            let breakdown = Dictionary(
                grouping: subscriptions,
                by: { $0.categoryId }
            ).mapValues { subs in
                subs.reduce(0.0) { $0 + $1.monthlyAmount }
            }

            let total = max(breakdown.values.reduce(0, +), 1.0)

            return breakdown.compactMap { categoryId, amount -> CategorySpend? in
                guard let category = categoryMap[categoryId] else { return nil }
                return CategorySpend(
                    category: category,
                    monthlyAmount: amount,
                    percentage: Float(amount / total)
                )
            }
            .sorted { $0.monthlyAmount > $1.monthlyAmount }
        }
        .eraseToAnyPublisher()
    }
}
