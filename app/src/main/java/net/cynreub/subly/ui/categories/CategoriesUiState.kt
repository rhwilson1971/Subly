package net.cynreub.subly.ui.categories

import net.cynreub.subly.domain.model.Category

data class CategoryWithCount(
    val category: Category,
    val subscriptionCount: Int
)

data class CategoriesUiState(
    val categories: List<CategoryWithCount> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null,
    // Add/Edit sheet
    val showAddEditSheet: Boolean = false,
    val editingCategory: Category? = null,   // null = "add" mode
    val isSaving: Boolean = false,
    val saveError: String? = null,
    // Delete dialog
    val deleteCandidate: CategoryWithCount? = null,
    val isDeleting: Boolean = false,
    val deleteError: String? = null
)
