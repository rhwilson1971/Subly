package net.cynreub.subly.data.local.dao

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import net.cynreub.subly.data.local.database.AppDatabase
import net.cynreub.subly.data.local.entity.CategoryEntity
import net.cynreub.subly.data.local.entity.PaymentMethodEntity
import net.cynreub.subly.data.local.entity.SubscriptionEntity
import net.cynreub.subly.domain.model.BillingFrequency
import net.cynreub.subly.domain.model.PaymentType
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.LocalDate

@RunWith(AndroidJUnit4::class)
class PaymentMethodDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: PaymentMethodDao

    @Before
    fun setUp() {
        db = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).allowMainThreadQueries().build()
        dao = db.paymentMethodDao()
    }

    @After
    fun tearDown() {
        db.close()
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private fun buildPaymentMethod(
        id: String = "pm-1",
        nickname: String = "Visa Rewards",
        type: PaymentType = PaymentType.VISA,
        lastFourDigits: String? = "4242",
        icon: Int? = null
    ) = PaymentMethodEntity(id = id, nickname = nickname, type = type, lastFourDigits = lastFourDigits, icon = icon)

    private suspend fun insertSubscriptionWithPaymentMethod(subId: String, paymentMethodId: String) {
        val category = CategoryEntity(id = "cat-1", name = "STREAMING", displayName = "Streaming", emoji = "📺", colorHex = "#E91E63")
        db.categoryDao().insertCategory(category)
        db.subscriptionDao().insertSubscription(
            SubscriptionEntity(
                id = subId,
                name = "Sub $subId",
                categoryId = "cat-1",
                amount = 9.99,
                currency = "USD",
                frequency = BillingFrequency.MONTHLY,
                startDate = LocalDate.of(2026, 1, 1),
                nextBillingDate = LocalDate.of(2026, 2, 1),
                paymentMethodId = paymentMethodId,
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
        val pm = buildPaymentMethod()
        dao.insertPaymentMethod(pm)

        val result = dao.getPaymentMethodById(pm.id).first()
        assertEquals(pm, result)
    }

    @Test
    fun insertMultipleAndGetAll() = runTest {
        dao.insertPaymentMethod(buildPaymentMethod(id = "pm-1", nickname = "Visa Rewards"))
        dao.insertPaymentMethod(buildPaymentMethod(id = "pm-2", nickname = "Amex Platinum", type = PaymentType.AMEX))

        val all = dao.getAllPaymentMethods().first()
        assertEquals(2, all.size)
    }

    @Test
    fun getAllPaymentMethods_sortedByNicknameAscending() = runTest {
        dao.insertPaymentMethod(buildPaymentMethod(id = "pm-1", nickname = "Visa Rewards"))
        dao.insertPaymentMethod(buildPaymentMethod(id = "pm-2", nickname = "Amex Platinum"))
        dao.insertPaymentMethod(buildPaymentMethod(id = "pm-3", nickname = "PayPal", type = PaymentType.PAYPAL))

        val all = dao.getAllPaymentMethods().first()
        assertEquals(listOf("Amex Platinum", "PayPal", "Visa Rewards"), all.map { it.nickname })
    }

    @Test
    fun updatePaymentMethod() = runTest {
        val pm = buildPaymentMethod()
        dao.insertPaymentMethod(pm)

        val updated = pm.copy(nickname = "Visa Signature", lastFourDigits = "1234")
        dao.updatePaymentMethod(updated)

        val result = dao.getPaymentMethodById(pm.id).first()
        assertEquals("Visa Signature", result?.nickname)
        assertEquals("1234", result?.lastFourDigits)
    }

    @Test
    fun deletePaymentMethod() = runTest {
        val pm = buildPaymentMethod()
        dao.insertPaymentMethod(pm)
        dao.deletePaymentMethod(pm)

        val result = dao.getPaymentMethodById(pm.id).first()
        assertNull(result)
    }

    @Test
    fun deletePaymentMethodById() = runTest {
        val pm = buildPaymentMethod()
        dao.insertPaymentMethod(pm)
        dao.deletePaymentMethodById(pm.id)

        val result = dao.getPaymentMethodById(pm.id).first()
        assertNull(result)
    }

    @Test
    fun insertReplacesDuplicateId() = runTest {
        val original = buildPaymentMethod(nickname = "Visa Rewards")
        val replacement = buildPaymentMethod(nickname = "Visa Platinum")
        dao.insertPaymentMethod(original)
        dao.insertPaymentMethod(replacement)

        val all = dao.getAllPaymentMethods().first()
        assertEquals(1, all.size)
        assertEquals("Visa Platinum", all[0].nickname)
    }

    @Test
    fun getPaymentMethodById_returnsNullForUnknownId() = runTest {
        val result = dao.getPaymentMethodById("nonexistent").first()
        assertNull(result)
    }

    // -------------------------------------------------------------------------
    // Optional fields
    // -------------------------------------------------------------------------

    @Test
    fun insertPaymentMethodWithNullLastFourDigits() = runTest {
        val pm = buildPaymentMethod(lastFourDigits = null)
        dao.insertPaymentMethod(pm)

        val result = dao.getPaymentMethodById(pm.id).first()
        assertNull(result?.lastFourDigits)
    }

    @Test
    fun insertPaymentMethodWithAllPaymentTypes() = runTest {
        PaymentType.entries.forEachIndexed { index, type ->
            dao.insertPaymentMethod(buildPaymentMethod(id = "pm-$index", type = type))
        }

        val all = dao.getAllPaymentMethods().first()
        assertEquals(PaymentType.entries.size, all.size)
        assertTrue(all.map { it.type }.containsAll(PaymentType.entries))
    }

    // -------------------------------------------------------------------------
    // getSubscriptionCountForPaymentMethod
    // -------------------------------------------------------------------------

    @Test
    fun getSubscriptionCountForPaymentMethod_returnsZeroWhenNone() = runTest {
        dao.insertPaymentMethod(buildPaymentMethod())

        val count = dao.getSubscriptionCountForPaymentMethod("pm-1")
        assertEquals(0, count)
    }

    @Test
    fun getSubscriptionCountForPaymentMethod_countsLinkedSubscriptions() = runTest {
        dao.insertPaymentMethod(buildPaymentMethod(id = "pm-1"))
        insertSubscriptionWithPaymentMethod("sub-1", "pm-1")
        insertSubscriptionWithPaymentMethod("sub-2", "pm-1")

        val count = dao.getSubscriptionCountForPaymentMethod("pm-1")
        assertEquals(2, count)
    }

    @Test
    fun getSubscriptionCountForPaymentMethod_onlyCountsMatchingPaymentMethod() = runTest {
        dao.insertPaymentMethod(buildPaymentMethod(id = "pm-1"))
        dao.insertPaymentMethod(buildPaymentMethod(id = "pm-2", nickname = "Amex"))
        insertSubscriptionWithPaymentMethod("sub-1", "pm-1")
        insertSubscriptionWithPaymentMethod("sub-2", "pm-2")

        val count = dao.getSubscriptionCountForPaymentMethod("pm-1")
        assertEquals(1, count)
    }
}
