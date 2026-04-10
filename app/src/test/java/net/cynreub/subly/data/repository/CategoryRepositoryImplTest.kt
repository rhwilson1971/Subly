package net.cynreub.subly.data.repository

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import net.cynreub.subly.data.local.dao.CategoryDao
import net.cynreub.subly.data.mapper.toEntity
import net.cynreub.subly.domain.sync.SyncProvider
import net.cynreub.subly.util.MainCoroutineRule
import net.cynreub.subly.util.testCategory
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class CategoryRepositoryImplTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private val dao: CategoryDao = mockk(relaxed = true)
    private val syncProvider: SyncProvider = mockk(relaxed = true)
    private lateinit var repository: CategoryRepositoryImpl

    @Before
    fun setUp() {
        repository = CategoryRepositoryImpl(dao, syncProvider)
    }

    // --- Flow mapping ---

    @Test
    fun `getAllCategories maps entities to domain models`() = runTest {
        val cat = testCategory()
        every { dao.getAllCategories() } returns flowOf(listOf(cat.toEntity()))

        repository.getAllCategories().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(cat, result.first())
            awaitComplete()
        }
    }

    // --- Insert calls both DAO and SyncProvider ---

    @Test
    fun `insertCategory calls dao and syncProvider`() = runTest {
        val cat = testCategory()
        coEvery { dao.insertCategory(any()) } returns Unit
        coEvery { syncProvider.upsertCategory(any()) } returns Unit

        repository.insertCategory(cat)

        coVerify(exactly = 1) { dao.insertCategory(cat.toEntity()) }
        coVerify(exactly = 1) { syncProvider.upsertCategory(cat) }
    }

    // --- Update calls both DAO and SyncProvider ---

    @Test
    fun `updateCategory calls dao and syncProvider`() = runTest {
        val cat = testCategory()
        coEvery { dao.updateCategory(any()) } returns Unit
        coEvery { syncProvider.upsertCategory(any()) } returns Unit

        repository.updateCategory(cat)

        coVerify(exactly = 1) { dao.updateCategory(cat.toEntity()) }
        coVerify(exactly = 1) { syncProvider.upsertCategory(cat) }
    }

    // --- Delete enforces usage check ---

    @Test
    fun `deleteCategory calls dao and syncProvider when usage count is zero`() = runTest {
        val cat = testCategory()
        coEvery { dao.getUsageCount(cat.id.toString()) } returns 0
        coEvery { dao.deleteCategory(any()) } returns Unit
        coEvery { syncProvider.deleteCategory(any()) } returns Unit

        repository.deleteCategory(cat)

        coVerify(exactly = 1) { dao.deleteCategory(cat.toEntity()) }
        coVerify(exactly = 1) { syncProvider.deleteCategory(cat.id) }
    }

    @Test
    fun `deleteCategory throws when category is in use`() = runTest {
        val cat = testCategory()
        coEvery { dao.getUsageCount(cat.id.toString()) } returns 3

        var threw = false
        try {
            repository.deleteCategory(cat)
        } catch (e: IllegalStateException) {
            threw = true
        }

        assertEquals(true, threw)
        coVerify(exactly = 0) { dao.deleteCategory(any()) }
        coVerify(exactly = 0) { syncProvider.deleteCategory(any()) }
    }
}
