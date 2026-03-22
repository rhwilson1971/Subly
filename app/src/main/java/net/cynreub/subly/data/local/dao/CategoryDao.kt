package net.cynreub.subly.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import net.cynreub.subly.data.local.entity.CategoryEntity
import net.cynreub.subly.data.local.entity.CategoryWithCountEntity

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY displayName ASC")
    fun getAllCategories(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE id = :id")
    fun getCategoryById(id: String): Flow<CategoryEntity?>

    @Query("SELECT COUNT(*) FROM subscriptions WHERE categoryId = :categoryId")
    suspend fun getUsageCount(categoryId: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("""
        SELECT c.id, c.name, c.displayName, c.emoji, c.colorHex,
               COUNT(s.id) AS subscriptionCount
        FROM categories c
        LEFT JOIN subscriptions s ON s.categoryId = c.id
        GROUP BY c.id
        ORDER BY c.displayName ASC
    """)
    fun getAllCategoriesWithCount(): Flow<List<CategoryWithCountEntity>>

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCount(): Int
}
