import Foundation
import Combine

protocol CategoryRepository {
    func getAllCategories() -> AnyPublisher<[Category], Never>
    func getAllCategoriesWithCount() -> AnyPublisher<[CategoryWithCount], Never>
    func getCategoryById(_ id: UUID) -> AnyPublisher<Category?, Never>
    func getUsageCount(for id: UUID) async -> Int
    func insertCategory(_ category: Category) async throws
    func updateCategory(_ category: Category) async throws
    /// Throws if the category is used by one or more subscriptions
    func deleteCategory(_ category: Category) async throws
}
