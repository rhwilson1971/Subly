package net.cynreub.subly.ui.categories

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import net.cynreub.subly.domain.model.Category
import net.cynreub.subly.domain.repository.CategoryRepository
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class CategoriesViewModel @Inject constructor(
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CategoriesUiState())
    val uiState: StateFlow<CategoriesUiState> = _uiState.asStateFlow()

    init {
        loadCategories()
    }

    private fun loadCategories() {
        viewModelScope.launch {
            categoryRepository.getAllCategoriesWithCount()
                .catch { e ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "Failed to load categories"
                    )
                }
                .collect { categories ->
                    _uiState.value = _uiState.value.copy(
                        categories = categories,
                        isLoading = false,
                        error = null
                    )
                }
        }
    }

    // ── Sheet management ───────────────────────────────────────────────────────

    fun openAddSheet() {
        _uiState.value = _uiState.value.copy(
            showAddEditSheet = true,
            editingCategory = null,
            saveError = null
        )
    }

    fun openEditSheet(category: Category) {
        _uiState.value = _uiState.value.copy(
            showAddEditSheet = true,
            editingCategory = category,
            saveError = null
        )
    }

    fun closeSheet() {
        _uiState.value = _uiState.value.copy(
            showAddEditSheet = false,
            editingCategory = null,
            saveError = null
        )
    }

    fun saveCategory(name: String, emoji: String, colorHex: String) {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) {
            _uiState.value = _uiState.value.copy(saveError = "Name cannot be empty")
            return
        }

        // Uniqueness check (case-insensitive, exclude self when editing)
        val editingId = _uiState.value.editingCategory?.id
        val duplicate = _uiState.value.categories.any { cwc ->
            cwc.category.id != editingId &&
                cwc.category.displayName.equals(trimmedName, ignoreCase = true)
        }
        if (duplicate) {
            _uiState.value = _uiState.value.copy(saveError = "A category named \"$trimmedName\" already exists")
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, saveError = null)
            try {
                val editing = _uiState.value.editingCategory
                if (editing != null) {
                    categoryRepository.updateCategory(
                        editing.copy(
                            displayName = trimmedName,
                            name = trimmedName.uppercase().replace(" ", "_"),
                            emoji = emoji,
                            colorHex = colorHex
                        )
                    )
                } else {
                    categoryRepository.insertCategory(
                        Category(
                            id = UUID.randomUUID(),
                            name = trimmedName.uppercase().replace(" ", "_"),
                            displayName = trimmedName,
                            emoji = emoji,
                            colorHex = colorHex
                        )
                    )
                }
                _uiState.value = _uiState.value.copy(isSaving = false, showAddEditSheet = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    saveError = e.message ?: "Failed to save category"
                )
            }
        }
    }

    // ── Delete management ──────────────────────────────────────────────────────

    fun requestDelete(categoryWithCount: CategoryWithCount) {
        _uiState.value = _uiState.value.copy(
            deleteCandidate = categoryWithCount,
            deleteError = null
        )
    }

    fun dismissDeleteDialog() {
        _uiState.value = _uiState.value.copy(
            deleteCandidate = null,
            deleteError = null
        )
    }

    fun confirmDelete() {
        val candidate = _uiState.value.deleteCandidate ?: return
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isDeleting = true, deleteError = null)
            try {
                categoryRepository.deleteCategory(candidate.category)
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    deleteCandidate = null
                )
            } catch (e: IllegalStateException) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    deleteError = e.message
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isDeleting = false,
                    deleteError = "Failed to delete: ${e.message}"
                )
            }
        }
    }
}
