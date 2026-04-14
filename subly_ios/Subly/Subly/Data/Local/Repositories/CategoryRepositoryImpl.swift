import Foundation
import SwiftData
import Combine

enum CategoryError: LocalizedError {
    case inUse(count: Int)

    var errorDescription: String? {
        switch self {
        case .inUse(let count):
            return "Cannot delete category: used by \(count) subscription\(count == 1 ? "" : "s")."
        }
    }
}

final class CategoryRepositoryImpl: CategoryRepository {
    private let modelContext: ModelContext
    private let subscriptionRepository: SubscriptionRepository
    private let subject = PassthroughSubject<Void, Never>()

    init(modelContext: ModelContext, subscriptionRepository: SubscriptionRepository) {
        self.modelContext = modelContext
        self.subscriptionRepository = subscriptionRepository
    }

    // MARK: - Reactive reads

    func getAllCategories() -> AnyPublisher<[Category], Never> {
        subject
            .prepend(())
            .map { [weak self] _ in self?.fetchAll() ?? [] }
            .eraseToAnyPublisher()
    }

    func getAllCategoriesWithCount() -> AnyPublisher<[CategoryWithCount], Never> {
        Publishers.CombineLatest(getAllCategories(), subscriptionRepository.getAllSubscriptions())
            .map { categories, subscriptions in
                let counts = Dictionary(grouping: subscriptions, by: { $0.categoryId })
                    .mapValues { $0.count }
                return categories.map { CategoryWithCount(category: $0, subscriptionCount: counts[$0.id] ?? 0) }
            }
            .eraseToAnyPublisher()
    }

    func getCategoryById(_ id: UUID) -> AnyPublisher<Category?, Never> {
        subject
            .prepend(())
            .map { [weak self] _ in self?.fetchAll().first(where: { $0.id == id }) }
            .eraseToAnyPublisher()
    }

    func getUsageCount(for id: UUID) async -> Int {
        let descriptor = FetchDescriptor<SubscriptionModel>(
            predicate: #Predicate { $0.categoryId == id }
        )
        return (try? modelContext.fetch(descriptor).count) ?? 0
    }

    // MARK: - Mutations

    func insertCategory(_ category: Category) async throws {
        let model = CategoryModel(from: category)
        modelContext.insert(model)
        try modelContext.save()
        subject.send()
    }

    func updateCategory(_ category: Category) async throws {
        let descriptor = FetchDescriptor<CategoryModel>(
            predicate: #Predicate { $0.id == category.id }
        )
        if let existing = try modelContext.fetch(descriptor).first {
            existing.update(from: category)
            try modelContext.save()
            subject.send()
        }
    }

    func deleteCategory(_ category: Category) async throws {
        let count = await getUsageCount(for: category.id)
        guard count == 0 else { throw CategoryError.inUse(count: count) }

        let descriptor = FetchDescriptor<CategoryModel>(
            predicate: #Predicate { $0.id == category.id }
        )
        if let existing = try modelContext.fetch(descriptor).first {
            modelContext.delete(existing)
            try modelContext.save()
            subject.send()
        }
    }

    // MARK: - Private

    private func fetchAll() -> [Category] {
        let descriptor = FetchDescriptor<CategoryModel>(sortBy: [SortDescriptor(\.displayName)])
        return (try? modelContext.fetch(descriptor))?.map { $0.toDomain() } ?? []
    }
}
