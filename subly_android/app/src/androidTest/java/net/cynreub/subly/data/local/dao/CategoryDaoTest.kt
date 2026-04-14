package net.cynreub.subly.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import net.cynreub.subly.data.local.database.AppDatabase
import net.cynreub.subly.data.local.entity.CategoryEntity
import net.cynreub.subly.data.local.entity.SubscriptionEntity
import net.cynreub.subly.domain.model.BillingFrequency
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class CategoryDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: CategoryDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.categoryDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private fun buildCategory(
        id: String = "cat-1",
        name: String = "STREAMING",
        displayName: String = "Streaming",
        emoji: String = "📺",
        colorHex: String = "#E91E63"
    ) = CategoryEntity(id = id, name = name, displayName = displayName, emoji = emoji, colorHex = colorHex)

    private suspend fun insertSubscriptionForCategory(subId: String, categoryId: String) {
        db.subscriptionDao().insertSubscription(
            SubscriptionEntity(
                id = subId,
                name = "Sub $subId",
                categoryId = categoryId,
                amount = 9.99,
                currency = "USD",
                frequency = BillingFrequency.MONTHLY,
                startDate = LocalDate.of(2026, 1, 1),
                nextBillingDate = LocalDate.of(2026, 2, 1),
                paymentMethodId = null,
                notes = null,
                isActive = true,
                reminderDaysBefore = 2
            )
        )
    }

    // -------------------------------------------------------------------------
    // CRUD
    // -------------------------------------------------------------------------

    @Test
    fun insertAndRetrieveById() = runTest {
        val cat = buildCategory()
        dao.insertCategory(cat)

        val result = dao.getCategoryById(cat.id).first()
        assertEquals(cat, result)
    }

    @Test
    fun insertMultipleAndGetAll() = runTest {
        val streaming = buildCategory(id = "cat-1", name = "STREAMING", displayName = "Streaming")
        val software = buildCategory(id = "cat-2", name = "SOFTWARE", displayName = "Software")
        dao.insertCategory(streaming)
        dao.insertCategory(software)

        val all = dao.getAllCategories().first()
        assertEquals(2, all.size)
    }

    @Test
    fun getAllCategories_sortedByDisplayNameAscending() = runTest {
        dao.insertCategory(buildCategory(id = "cat-1", displayName = "Streaming"))
        dao.insertCategory(buildCategory(id = "cat-2", displayName = "Magazine"))
        dao.insertCategory(buildCategory(id = "cat-3", displayName = "Utility"))

        val all = dao.getAllCategories().first()
        assertEquals(listOf("Magazine", "Streaming", "Utility"), all.map { it.displayName })
    }

    @Test
    fun updateCategory() = runTest {
        val cat = buildCategory()
        dao.insertCategory(cat)

        val updated = cat.copy(displayName = "Movies & TV", colorHex = "#FF0000")
        dao.updateCategory(updated)

        val result = dao.getCategoryById(cat.id).first()
        assertEquals("Movies & TV", result?.displayName)
        assertEquals("#FF0000", result?.colorHex)
    }

    @Test
    fun deleteCategory() = runTest {
        val cat = buildCategory()
        dao.insertCategory(cat)
        dao.deleteCategory(cat)

        val result = dao.getCategoryById(cat.id).first()
        assertNull(result)
    }

    @Test
    fun insertReplacesDuplicateId() = runTest {
        val original = buildCategory(displayName = "Streaming")
        val replacement = buildCategory(displayName = "Video Streaming")
        dao.insertCategory(original)
        dao.insertCategory(replacement)

        val all = dao.getAllCategories().first()
        assertEquals(1, all.size)
        assertEquals("Video Streaming", all[0].displayName)
    }

    @Test
    fun getCount() = runTest {
        assertEquals(0, dao.getCount())
        dao.insertCategory(buildCategory(id = "cat-1"))
        dao.insertCategory(buildCategory(id = "cat-2"))
        assertEquals(2, dao.getCount())
    }

    @Test
    fun getCategoryById_returnsNullForUnknownId() = runTest {
        val result = dao.getCategoryById("nonexistent").first()
        assertNull(result)
    }

    // -------------------------------------------------------------------------
    // getUsageCount
    // -------------------------------------------------------------------------

    @Test
    fun getUsageCount_returnsZeroWhenNoSubscriptions() = runTest {
        dao.insertCategory(buildCategory())

        val count = dao.getUsageCount("cat-1")
        assertEquals(0, count)
    }

    @Test
    fun getUsageCount_countsSubscriptionsForCategory() = runTest {
        dao.insertCategory(buildCategory(id = "cat-1"))
        insertSubscriptionForCategory("sub-1", "cat-1")
        insertSubscriptionForCategory("sub-2", "cat-1")

        val count = dao.getUsageCount("cat-1")
        assertEquals(2, count)
    }

    @Test
    fun getUsageCount_onlyCountsMatchingCategory() = runTest {
        dao.insertCategory(buildCategory(id = "cat-1"))
        dao.insertCategory(buildCategory(id = "cat-2", name = "SOFTWARE", displayName = "Software"))
        insertSubscriptionForCategory("sub-1", "cat-1")
        insertSubscriptionForCategory("sub-2", "cat-2")

        val count = dao.getUsageCount("cat-1")
        assertEquals(1, count)
    }

    // -------------------------------------------------------------------------
    // getAllCategoriesWithCount
    // -------------------------------------------------------------------------

    @Test
    fun getAllCategoriesWithCount_returnsZeroCountWhenNoSubscriptions() = runTest {
        dao.insertCategory(buildCategory())

        val result = dao.getAllCategoriesWithCount().first()
        assertEquals(1, result.size)
        assertEquals(0, result[0].subscriptionCount)
    }

    @Test
    fun getAllCategoriesWithCount_reflectsCorrectSubscriptionCounts() = runTest {
        dao.insertCategory(buildCategory(id = "cat-1", displayName = "Streaming"))
        dao.insertCategory(buildCategory(id = "cat-2", name = "SOFTWARE", displayName = "Software"))
        insertSubscriptionForCategory("sub-1", "cat-1")
        insertSubscriptionForCategory("sub-2", "cat-1")
        insertSubscriptionForCategory("sub-3", "cat-2")

        val result = dao.getAllCategoriesWithCount().first()
        val streaming = result.find { it.id == "cat-1" }
        val software = result.find { it.id == "cat-2" }
        assertEquals(2, streaming?.subscriptionCount)
        assertEquals(1, software?.subscriptionCount)
    }

    @Test
    fun getAllCategoriesWithCount_sortedByDisplayName() = runTest {
        dao.insertCategory(buildCategory(id = "cat-1", displayName = "Streaming"))
        dao.insertCategory(buildCategory(id = "cat-2", name = "MAGAZINE", displayName = "Magazine"))

        val result = dao.getAllCategoriesWithCount().first()
        assertTrue(result[0].displayName < result[1].displayName)
    }
}
