import Foundation
import SwiftData

/// Holds all repository and use-case instances for injection via SwiftUI Environment.
/// Mirrors Android's Hilt module graph — each entry corresponds to one @Singleton binding.
@MainActor
final class ServiceContainer: ObservableObject {
    // MARK: - SwiftData
    let modelContainer: ModelContainer

    // MARK: - Repositories
    let subscriptionRepository: SubscriptionRepository
    let categoryRepository: CategoryRepository
    let paymentMethodRepository: PaymentMethodRepository

    // MARK: - Use Cases
    let calculateMonthlyTotal: CalculateMonthlyTotalUseCase
    let getUpcomingSubscriptions: GetUpcomingSubscriptionsUseCase
    let getSubscriptionStats: GetSubscriptionStatsUseCase
    let getCategorySpend: GetCategorySpendUseCase
    let updateNextBillingDate: UpdateNextBillingDateUseCase

    init() {
        let schema = Schema([
            SubscriptionModel.self,
            CategoryModel.self,
            PaymentMethodModel.self,
        ])
        let config = ModelConfiguration(schema: schema, isStoredInMemoryOnly: false)
        let container = try! ModelContainer(for: schema, configurations: config)
        self.modelContainer = container

        let context = container.mainContext

        let subRepo = SubscriptionRepositoryImpl(modelContext: context)
        let catRepo = CategoryRepositoryImpl(modelContext: context, subscriptionRepository: subRepo)
        let pmRepo  = PaymentMethodRepositoryImpl(modelContext: context)

        self.subscriptionRepository = subRepo
        self.categoryRepository     = catRepo
        self.paymentMethodRepository = pmRepo

        self.calculateMonthlyTotal    = CalculateMonthlyTotalUseCase(subscriptionRepository: subRepo)
        self.getUpcomingSubscriptions = GetUpcomingSubscriptionsUseCase(subscriptionRepository: subRepo)
        self.getSubscriptionStats     = GetSubscriptionStatsUseCase(subscriptionRepository: subRepo)
        self.getCategorySpend         = GetCategorySpendUseCase(subscriptionRepository: subRepo, categoryRepository: catRepo)
        self.updateNextBillingDate    = UpdateNextBillingDateUseCase(subscriptionRepository: subRepo)

        seedDefaultCategoriesIfNeeded(context: context)
    }

    private func seedDefaultCategoriesIfNeeded(context: ModelContext) {
        let descriptor = FetchDescriptor<CategoryModel>()
        let existing = (try? context.fetch(descriptor)) ?? []
        guard existing.isEmpty else { return }
        Category.defaults.forEach { context.insert(CategoryModel(from: $0)) }
        try? context.save()
    }
}
