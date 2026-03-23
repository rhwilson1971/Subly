package net.cynreub.subly.domain.repository

import kotlinx.coroutines.flow.Flow
import net.cynreub.subly.domain.model.Category
import net.cynreub.subly.ui.categories.CategoryWithCount
import java.util.UUID

interface CategoryRepository {
    fun getAllCategories(): Flow<List<Category>>
    fun getAllCategoriesWithCount(): Flow<List<CategoryWithCount>>
    fun getCategoryById(id: UUID): Flow<Category?>
    suspend fun getUsageCount(id: UUID): Int
    suspend fun insertCategory(category: Category)
    suspend fun updateCategory(category: Category)
    /** @throws IllegalStateException if the category is used by one or more subscriptions */
    suspend fun deleteCategory(category: Category)
}
