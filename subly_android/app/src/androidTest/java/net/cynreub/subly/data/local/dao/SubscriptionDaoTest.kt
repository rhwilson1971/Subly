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
class SubscriptionDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: SubscriptionDao

    private val categoryId = "cat-1"
    private val category = CategoryEntity(
        id = categoryId,
        name = "STREAMING",
        displayName = "Streaming",
        emoji = "📺",
        colorHex = "#E91E63"
    )

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.subscriptionDao()
        runTest { db.categoryDao().insertCategory(category) }
    }

    @After
    fun tearDown() {
        db.close()
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private fun buildSubscription(
        id: String = "sub-1",
        name: String = "Netflix",
        isActive: Boolean = true,
        frequency: BillingFrequency = BillingFrequency.MONTHLY,
        amount: Double = 15.99,
        nextBillingDate: LocalDate = LocalDate.of(2026, 2, 1),
        paymentMethodId: String? = null
    ) = SubscriptionEntity(
        id = id,
        name = name,
        categoryId = categoryId,
        amount = amount,
        currency = "USD",
        frequency = frequency,
        startDate = LocalDate.of(2026, 1, 1),
        nextBillingDate = nextBillingDate,
        paymentMethodId = paymentMethodId,
        notes = null,
        isActive = isActive,
        reminderDaysBefore = 2
    )

    // -------------------------------------------------------------------------
    // CRUD
    // -------------------------------------------------------------------------

    @Test
    fun insertAndRetrieveById() = runTest {
        val sub = buildSubscription()
        dao.insertSubscription(sub)

        val result = dao.getSubscriptionById(sub.id).first()
        assertEquals(sub, result)
    }

    @Test
    fun updateSubscription() = runTest {
        val sub = buildSubscription()
        dao.insertSubscription(sub)

        val updated = sub.copy(name = "Disney+", amount = 10.99)
        dao.updateSubscription(updated)

        val result = dao.getSubscriptionById(sub.id).first()
        assertEquals("Disney+", result?.name)
        assertEquals(10.99, result?.amount)
    }

    @Test
    fun deleteSubscription() = runTest {
        val sub = buildSubscription()
        dao.insertSubscription(sub)
        dao.deleteSubscription(sub)

        val result = dao.getSubscriptionById(sub.id).first()
        assertNull(result)
    }

    @Test
    fun deleteSubscriptionById() = runTest {
        val sub = buildSubscription()
        dao.insertSubscription(sub)
        dao.deleteSubscriptionById(sub.id)

        val result = dao.getSubscriptionById(sub.id).first()
        assertNull(result)
    }

    @Test
    fun insertReplacesDuplicateId() = runTest {
        val original = buildSubscription(name = "Netflix")
        val replacement = buildSubscription(name = "Netflix Premium")
        dao.insertSubscription(original)
        dao.insertSubscription(replacement)

        val all = dao.getAllSubscriptions().first()
        assertEquals(1, all.size)
        assertEquals("Netflix Premium", all[0].name)
    }

    @Test
    fun getCount() = runTest {
        assertEquals(0, dao.getCount())
        dao.insertSubscription(buildSubscription(id = "sub-1"))
        dao.insertSubscription(buildSubscription(id = "sub-2"))
        assertEquals(2, dao.getCount())
    }

    // -------------------------------------------------------------------------
    // getActiveSubscriptions
    // -------------------------------------------------------------------------

    @Test
    fun getActiveSubscriptions_returnsOnlyActiveOnes() = runTest {
        dao.insertSubscription(buildSubscription(id = "sub-1", isActive = true))
        dao.insertSubscription(buildSubscription(id = "sub-2", isActive = false))
        dao.insertSubscription(buildSubscription(id = "sub-3", isActive = true))

        val active = dao.getActiveSubscriptions().first()
        assertEquals(2, active.size)
        assertTrue(active.all { it.isActive })
    }

    @Test
    fun getActiveSubscriptions_emptyWhenNoneActive() = runTest {
        dao.insertSubscription(buildSubscription(id = "sub-1", isActive = false))

        val active = dao.getActiveSubscriptions().first()
        assertTrue(active.isEmpty())
    }

    @Test
    fun getActiveSubscriptions_sortedByNextBillingDate() = runTest {
        val feb = buildSubscription(id = "sub-1", nextBillingDate = LocalDate.of(2026, 2, 1))
        val jan = buildSubscription(id = "sub-2", nextBillingDate = LocalDate.of(2026, 1, 1))
        dao.insertSubscription(feb)
        dao.insertSubscription(jan)

        val active = dao.getActiveSubscriptions().first()
        assertEquals(LocalDate.of(2026, 1, 1), active[0].nextBillingDate)
        assertEquals(LocalDate.of(2026, 2, 1), active[1].nextBillingDate)
    }

    // -------------------------------------------------------------------------
    // getSubscriptionsBetweenDates — boundary conditions
    // -------------------------------------------------------------------------

    @Test
    fun getSubscriptionsBetweenDates_includesExactStartBoundary() = runTest {
        val start = LocalDate.of(2026, 1, 1)
        val end = LocalDate.of(2026, 1, 31)
        dao.insertSubscription(buildSubscription(nextBillingDate = start))

        val result = dao.getSubscriptionsBetweenDates(start, end).first()
        assertEquals(1, result.size)
    }

    @Test
    fun getSubscriptionsBetweenDates_includesExactEndBoundary() = runTest {
        val start = LocalDate.of(2026, 1, 1)
        val end = LocalDate.of(2026, 1, 31)
        dao.insertSubscription(buildSubscription(nextBillingDate = end))

        val result = dao.getSubscriptionsBetweenDates(start, end).first()
        assertEquals(1, result.size)
    }

    @Test
    fun getSubscriptionsBetweenDates_excludesBeforeStart() = runTest {
        val start = LocalDate.of(2026, 1, 1)
        val end = LocalDate.of(2026, 1, 31)
        dao.insertSubscription(buildSubscription(nextBillingDate = start.minusDays(1)))

        val result = dao.getSubscriptionsBetweenDates(start, end).first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun getSubscriptionsBetweenDates_excludesAfterEnd() = runTest {
        val start = LocalDate.of(2026, 1, 1)
        val end = LocalDate.of(2026, 1, 31)
        dao.insertSubscription(buildSubscription(nextBillingDate = end.plusDays(1)))

        val result = dao.getSubscriptionsBetweenDates(start, end).first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun getSubscriptionsBetweenDates_returnsOnlyInRange() = runTest {
        val start = LocalDate.of(2026, 1, 1)
        val end = LocalDate.of(2026, 1, 31)
        dao.insertSubscription(buildSubscription(id = "sub-1", nextBillingDate = LocalDate.of(2026, 1, 10)))
        dao.insertSubscription(buildSubscription(id = "sub-2", nextBillingDate = LocalDate.of(2026, 1, 20)))
        dao.insertSubscription(buildSubscription(id = "sub-3", nextBillingDate = LocalDate.of(2026, 2, 5)))

        val result = dao.getSubscriptionsBetweenDates(start, end).first()
        assertEquals(2, result.size)
    }

    // -------------------------------------------------------------------------
    // getMonthlyTotal
    // -------------------------------------------------------------------------

    @Test
    fun getMonthlyTotal_returnsNullForEmptyTable() = runTest {
        val total = dao.getMonthlyTotal().first()
        assertNull(total)
    }

    @Test
    fun getMonthlyTotal_sumsActiveMonthlySubscriptions() = runTest {
        dao.insertSubscription(buildSubscription(id = "sub-1", isActive = true, frequency = BillingFrequency.MONTHLY, amount = 10.0))
        dao.insertSubscription(buildSubscription(id = "sub-2", isActive = true, frequency = BillingFrequency.MONTHLY, amount = 5.0))

        val total = dao.getMonthlyTotal().first()
        assertEquals(15.0, total!!, 0.001)
    }

    @Test
    fun getMonthlyTotal_excludesInactiveSubscriptions() = runTest {
        dao.insertSubscription(buildSubscription(id = "sub-1", isActive = true, frequency = BillingFrequency.MONTHLY, amount = 10.0))
        dao.insertSubscription(buildSubscription(id = "sub-2", isActive = false, frequency = BillingFrequency.MONTHLY, amount = 99.0))

        val total = dao.getMonthlyTotal().first()
        assertEquals(10.0, total!!, 0.001)
    }

    @Test
    fun getMonthlyTotal_excludesNonMonthlyFrequencies() = runTest {
        dao.insertSubscription(buildSubscription(id = "sub-1", isActive = true, frequency = BillingFrequency.MONTHLY, amount = 10.0))
        dao.insertSubscription(buildSubscription(id = "sub-2", isActive = true, frequency = BillingFrequency.ANNUAL, amount = 120.0))
        dao.insertSubscription(buildSubscription(id = "sub-3", isActive = true, frequency = BillingFrequency.WEEKLY, amount = 5.0))

        val total = dao.getMonthlyTotal().first()
        assertEquals(10.0, total!!, 0.001)
    }

    @Test
    fun getMonthlyTotal_returnsNullWhenNoActiveMonthlySubscriptions() = runTest {
        dao.insertSubscription(buildSubscription(id = "sub-1", isActive = true, frequency = BillingFrequency.ANNUAL, amount = 120.0))

        val total = dao.getMonthlyTotal().first()
        assertNull(total)
    }

    // -------------------------------------------------------------------------
    // getSubscriptionsByCategoryId
    // -------------------------------------------------------------------------

    @Test
    fun getSubscriptionsByCategoryId_returnsMatchingCategory() = runTest {
        val otherCat = CategoryEntity(id = "cat-2", name = "SOFTWARE", displayName = "Software", emoji = "💻", colorHex = "#00BCD4")
        db.categoryDao().insertCategory(otherCat)

        val sub1 = buildSubscription(id = "sub-1").copy(categoryId = "cat-1")
        val sub2 = buildSubscription(id = "sub-2").copy(categoryId = "cat-2")
        dao.insertSubscription(sub1)
        dao.insertSubscription(sub2)

        val result = dao.getSubscriptionsByCategoryId("cat-1").first()
        assertEquals(1, result.size)
        assertEquals("sub-1", result[0].id)
    }
}
