import Foundation

struct CategoriesUiState {
    var categories: [CategoryWithCount] = []
    var isLoading: Bool = true
    var error: String? = nil

    // Add/Edit sheet
    var showAddEditSheet: Bool = false
    var editingCategory: Category? = nil   // nil = add mode
    var isSaving: Bool = false
    var saveError: String? = nil

    // Delete confirmation
    var deleteCandidate: CategoryWithCount? = nil
    var isDeleting: Bool = false
    var deleteError: String? = nil
}
