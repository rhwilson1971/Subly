package net.cynreub.subly.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import net.cynreub.subly.data.local.dao.CategoryDao
import net.cynreub.subly.data.mapper.toCategoryWithCount
import net.cynreub.subly.data.mapper.toDomain
import net.cynreub.subly.data.mapper.toEntity
import net.cynreub.subly.domain.model.Category
import net.cynreub.subly.domain.repository.CategoryRepository
import net.cynreub.subly.domain.sync.SyncProvider
import net.cynreub.subly.ui.categories.CategoryWithCount
import java.util.UUID
import javax.inject.Inject

class CategoryRepositoryImpl @Inject constructor(
    private val categoryDao: CategoryDao,
    private val syncProvider: SyncProvider
) : CategoryRepository {

    override fun getAllCategories(): Flow<List<Category>> =
        categoryDao.getAllCategories().map { it.map { entity -> entity.toDomain() } }

    override fun getAllCategoriesWithCount(): Flow<List<CategoryWithCount>> =
        categoryDao.getAllCategoriesWithCount().map { it.map { entity -> entity.toCategoryWithCount() } }

    override fun getCategoryById(id: UUID): Flow<Category?> =
        categoryDao.getCategoryById(id.toString()).map { it?.toDomain() }

    override suspend fun getUsageCount(id: UUID): Int =
        categoryDao.getUsageCount(id.toString())

    override suspend fun insertCategory(category: Category) {
        categoryDao.insertCategory(category.toEntity())
        syncProvider.upsertCategory(category)
    }

    override suspend fun updateCategory(category: Category) {
        categoryDao.updateCategory(category.toEntity())
        syncProvider.upsertCategory(category)
    }

    override suspend fun deleteCategory(category: Category) {
        val count = categoryDao.getUsageCount(category.id.toString())
        if (count > 0) {
            throw IllegalStateException(
                "Cannot delete \"${category.displayName}\" — it is used by $count subscription(s). Reassign them first."
            )
        }
        categoryDao.deleteCategory(category.toEntity())
        syncProvider.deleteCategory(category.id)
    }
}
