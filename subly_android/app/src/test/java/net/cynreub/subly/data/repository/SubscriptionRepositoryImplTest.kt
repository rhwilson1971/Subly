package net.cynreub.subly.data.repository

import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import net.cynreub.subly.data.local.dao.SubscriptionDao
import net.cynreub.subly.data.mapper.toEntity
import net.cynreub.subly.domain.sync.SyncProvider
import net.cynreub.subly.util.MainCoroutineRule
import net.cynreub.subly.util.testSubscription
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class SubscriptionRepositoryImplTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private val dao: SubscriptionDao = mockk(relaxed = true)
    private val syncProvider: SyncProvider = mockk(relaxed = true)
    private lateinit var repository: SubscriptionRepositoryImpl

    @Before
    fun setUp() {
        repository = SubscriptionRepositoryImpl(dao, syncProvider)
    }

    // --- Flow mapping ---

    @Test
    fun `getAllSubscriptions maps entities to domain models`() = runTest {
        val sub = testSubscription()
        every { dao.getAllSubscriptions() } returns flowOf(listOf(sub.toEntity()))

        repository.getAllSubscriptions().test {
            val result = awaitItem()
            assertEquals(1, result.size)
            assertEquals(sub, result.first())
            awaitComplete()
        }
    }

    @Test
    fun `getMonthlyTotal maps null DAO value to 0_0`() = runTest {
        every { dao.getMonthlyTotal() } returns flowOf(null)

        repository.getMonthlyTotal().test {
            assertEquals(0.0, awaitItem(), 0.0)
            awaitComplete()
        }
    }

    @Test
    fun `getMonthlyTotal passes through non-null value`() = runTest {
        every { dao.getMonthlyTotal() } returns flowOf(42.50)

        repository.getMonthlyTotal().test {
            assertEquals(42.50, awaitItem(), 0.0)
            awaitComplete()
        }
    }

    // --- Insert calls both DAO and SyncProvider ---

    @Test
    fun `insertSubscription calls dao and syncProvider`() = runTest {
        val sub = testSubscription()
        coEvery { dao.insertSubscription(any()) } returns Unit
        coEvery { syncProvider.upsertSubscription(any()) } returns Unit

        repository.insertSubscription(sub)

        coVerify(exactly = 1) { dao.insertSubscription(sub.toEntity()) }
        coVerify(exactly = 1) { syncProvider.upsertSubscription(sub) }
    }

    // --- Update calls both DAO and SyncProvider ---

    @Test
    fun `updateSubscription calls dao and syncProvider`() = runTest {
        val sub = testSubscription()
        coEvery { dao.updateSubscription(any()) } returns Unit
        coEvery { syncProvider.upsertSubscription(any()) } returns Unit

        repository.updateSubscription(sub)

        coVerify(exactly = 1) { dao.updateSubscription(sub.toEntity()) }
        coVerify(exactly = 1) { syncProvider.upsertSubscription(sub) }
    }

    // --- Delete calls both DAO and SyncProvider ---

    @Test
    fun `deleteSubscription calls dao and syncProvider`() = runTest {
        val sub = testSubscription()
        coEvery { dao.deleteSubscription(any()) } returns Unit
        coEvery { syncProvider.deleteSubscription(any()) } returns Unit

        repository.deleteSubscription(sub)

        coVerify(exactly = 1) { dao.deleteSubscription(sub.toEntity()) }
        coVerify(exactly = 1) { syncProvider.deleteSubscription(sub.id) }
    }

    @Test
    fun `deleteSubscriptionById calls dao and syncProvider`() = runTest {
        val sub = testSubscription()
        coEvery { dao.deleteSubscriptionById(any()) } returns Unit
        coEvery { syncProvider.deleteSubscription(any()) } returns Unit

        repository.deleteSubscriptionById(sub.id)

        coVerify(exactly = 1) { dao.deleteSubscriptionById(sub.id.toString()) }
        coVerify(exactly = 1) { syncProvider.deleteSubscription(sub.id) }
    }
}
