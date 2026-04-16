import Foundation
import Combine
import Observation

@Observable
final class CategoriesViewModel {
    var uiState = CategoriesUiState()

    private let categoryRepository: CategoryRepository
    private var cancellables = Set<AnyCancellable>()

    init(categoryRepository: CategoryRepository) {
        self.categoryRepository = categoryRepository
        loadCategories()
    }

    private func loadCategories() {
        categoryRepository.getAllCategoriesWithCount()
            .receive(on: DispatchQueue.main)
            .sink { [weak self] categories in
                guard let self else { return }
                uiState.categories = categories
                uiState.isLoading = false
                uiState.error = nil
            }
            .store(in: &cancellables)
    }

    // MARK: - Sheet Management

    func openAddSheet() {
        uiState.editingCategory = nil
        uiState.saveError = nil
        uiState.showAddEditSheet = true
    }

    func openEditSheet(category: Category) {
        uiState.editingCategory = category
        uiState.saveError = nil
        uiState.showAddEditSheet = true
    }

    func closeSheet() {
        uiState.showAddEditSheet = false
        uiState.editingCategory = nil
        uiState.saveError = nil
    }

    func saveCategory(displayName: String, emoji: String, colorHex: String) {
        let trimmed = displayName.trimmingCharacters(in: .whitespaces)
        guard !trimmed.isEmpty else {
            uiState.saveError = "Name cannot be empty"
            return
        }

        let editingId = uiState.editingCategory?.id
        let duplicate = uiState.categories.contains {
            $0.category.id != editingId &&
            $0.category.displayName.caseInsensitiveCompare(trimmed) == .orderedSame
        }
        if duplicate {
            uiState.saveError = "A category named \(trimmed) already exists"
            return
        }

        uiState.isSaving = true
        uiState.saveError = nil

        Task { @MainActor in
            do {
                if let editing = uiState.editingCategory {
                    try await categoryRepository.updateCategory(
                        Category(
                            id: editing.id,
                            name: trimmed.uppercased().replacingOccurrences(of: " ", with: "_"),
                            displayName: trimmed,
                            emoji: emoji,
                            colorHex: colorHex
                        )
                    )
                } else {
                    try await categoryRepository.insertCategory(
                        Category(
                            id: UUID(),
                            name: trimmed.uppercased().replacingOccurrences(of: " ", with: "_"),
                            displayName: trimmed,
                            emoji: emoji,
                            colorHex: colorHex
                        )
                    )
                }
                uiState.isSaving = false
                uiState.showAddEditSheet = false
                uiState.editingCategory = nil
            } catch {
                uiState.isSaving = false
                uiState.saveError = error.localizedDescription
            }
        }
    }

    // MARK: - Delete Management

    func requestDelete(_ item: CategoryWithCount) {
        uiState.deleteCandidate = item
        uiState.deleteError = nil
    }

    func dismissDelete() {
        uiState.deleteCandidate = nil
        uiState.deleteError = nil
    }

    func confirmDelete() {
        guard let candidate = uiState.deleteCandidate else { return }
        uiState.isDeleting = true
        uiState.deleteError = nil

        Task { @MainActor in
            do {
                try await categoryRepository.deleteCategory(candidate.category)
                uiState.isDeleting = false
                uiState.deleteCandidate = nil
            } catch {
                uiState.isDeleting = false
                uiState.deleteError = error.localizedDescription
            }
        }
    }
}
