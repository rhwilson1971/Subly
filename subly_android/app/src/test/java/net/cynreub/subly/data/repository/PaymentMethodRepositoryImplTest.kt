package net.cynreub.subly.data.repository

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import net.cynreub.subly.data.local.dao.PaymentMethodDao
import net.cynreub.subly.data.mapper.toEntity
import net.cynreub.subly.domain.sync.SyncProvider
import net.cynreub.subly.util.MainCoroutineRule
import net.cynreub.subly.util.testPaymentMethod
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class PaymentMethodRepositoryImplTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private val dao: PaymentMethodDao = mockk(relaxed = true)
    private val syncProvider: SyncProvider = mockk(relaxed = true)
    private lateinit var repository: PaymentMethodRepositoryImpl

    @Before
    fun setUp() {
        repository = PaymentMethodRepositoryImpl(dao, syncProvider)
    }

    // --- Flow mapping ---

    @Test
    fun `getAllPaymentMethods maps entities to domain models`() = runTest {
        val pm = testPaymentMethod()
        every { dao.getAllPaymentMethods() } returns flowOf(listOf(pm.toEntity()))

        repository.getAllPaymentMethods().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(pm, result.first())
            awaitComplete()
        }
    }

    // --- Insert calls both DAO and SyncProvider ---

    @Test
    fun `insertPaymentMethod calls dao and syncProvider`() = runTest {
        val pm = testPaymentMethod()
        coEvery { dao.insertPaymentMethod(any()) } returns Unit
        coEvery { syncProvider.upsertPaymentMethod(any()) } returns Unit

        repository.insertPaymentMethod(pm)

        coVerify(exactly = 1) { dao.insertPaymentMethod(pm.toEntity()) }
        coVerify(exactly = 1) { syncProvider.upsertPaymentMethod(pm) }
    }

    // --- Update calls both DAO and SyncProvider ---

    @Test
    fun `updatePaymentMethod calls dao and syncProvider`() = runTest {
        val pm = testPaymentMethod()
        coEvery { dao.updatePaymentMethod(any()) } returns Unit
        coEvery { syncProvider.upsertPaymentMethod(any()) } returns Unit

        repository.updatePaymentMethod(pm)

        coVerify(exactly = 1) { dao.updatePaymentMethod(pm.toEntity()) }
        coVerify(exactly = 1) { syncProvider.upsertPaymentMethod(pm) }
    }

    // --- Delete calls both DAO and SyncProvider ---

    @Test
    fun `deletePaymentMethod calls dao and syncProvider`() = runTest {
        val pm = testPaymentMethod()
        coEvery { dao.deletePaymentMethod(any()) } returns Unit
        coEvery { syncProvider.deletePaymentMethod(any()) } returns Unit

        repository.deletePaymentMethod(pm)

        coVerify(exactly = 1) { dao.deletePaymentMethod(pm.toEntity()) }
        coVerify(exactly = 1) { syncProvider.deletePaymentMethod(pm.id) }
    }

    @Test
    fun `deletePaymentMethodById calls dao and syncProvider`() = runTest {
        val pm = testPaymentMethod()
        coEvery { dao.deletePaymentMethodById(any()) } returns Unit
        coEvery { syncProvider.deletePaymentMethod(any()) } returns Unit

        repository.deletePaymentMethodById(pm.id)

        coVerify(exactly = 1) { dao.deletePaymentMethodById(pm.id.toString()) }
        coVerify(exactly = 1) { syncProvider.deletePaymentMethod(pm.id) }
    }
}
