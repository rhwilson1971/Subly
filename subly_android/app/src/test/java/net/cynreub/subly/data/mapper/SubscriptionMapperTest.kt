package net.cynreub.subly.data.mapper

import net.cynreub.subly.data.local.entity.SubscriptionEntity
import net.cynreub.subly.domain.model.BillingFrequency
import net.cynreub.subly.domain.model.Category
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.time.LocalDate
import java.util.UUID

class SubscriptionMapperTest {

    private val id = UUID.randomUUID()
    private val categoryId = Category.ID_STREAMING
    private val paymentMethodId = UUID.randomUUID()
    private val startDate = LocalDate.of(2026, 1, 1)
    private val nextBillingDate = LocalDate.of(2026, 2, 1)

    private fun entity(
        paymentMethodIdStr: String? = paymentMethodId.toString(),
        notes: String? = "test note"
    ) = SubscriptionEntity(
        id = id.toString(),
        name = "Netflix",
        categoryId = categoryId.toString(),
        amount = 15.99,
        currency = "USD",
        frequency = BillingFrequency.MONTHLY,
        startDate = startDate,
        nextBillingDate = nextBillingDate,
        paymentMethodId = paymentMethodIdStr,
        notes = notes,
        isActive = true,
        reminderDaysBefore = 2
    )

    @Test
    fun `toDomain preserves all fields`() {
        val domain = entity().toDomain()

        assertEquals(id, domain.id)
        assertEquals("Netflix", domain.name)
        assertEquals(categoryId, domain.categoryId)
        assertEquals(15.99, domain.amount, 0.0)
        assertEquals("USD", domain.currency)
        assertEquals(BillingFrequency.MONTHLY, domain.frequency)
        assertEquals(startDate, domain.startDate)
        assertEquals(nextBillingDate, domain.nextBillingDate)
        assertEquals(paymentMethodId, domain.paymentMethodId)
        assertEquals("test note", domain.notes)
        assertEquals(true, domain.isActive)
        assertEquals(2, domain.reminderDaysBefore)
    }

    @Test
    fun `toEntity preserves all fields`() {
        val domain = entity().toDomain()
        val back = domain.toEntity()

        assertEquals(id.toString(), back.id)
        assertEquals("Netflix", back.name)
        assertEquals(categoryId.toString(), back.categoryId)
        assertEquals(15.99, back.amount, 0.0)
        assertEquals("USD", back.currency)
        assertEquals(BillingFrequency.MONTHLY, back.frequency)
        assertEquals(startDate, back.startDate)
        assertEquals(nextBillingDate, back.nextBillingDate)
        assertEquals(paymentMethodId.toString(), back.paymentMethodId)
        assertEquals("test note", back.notes)
        assertEquals(true, back.isActive)
        assertEquals(2, back.reminderDaysBefore)
    }

    @Test
    fun `round-trip entity toDomain toEntity produces equal entity`() {
        val original = entity()
        assertEquals(original, original.toDomain().toEntity())
    }

    @Test
    fun `round-trip domain toEntity toDomain produces equal domain`() {
        val domain = entity().toDomain()
        assertEquals(domain, domain.toEntity().toDomain())
    }

    @Test
    fun `null paymentMethodId is preserved through round-trip`() {
        val domain = entity(paymentMethodIdStr = null).toDomain()
        assertNull(domain.paymentMethodId)
        assertNull(domain.toEntity().paymentMethodId)
    }

    @Test
    fun `null notes is preserved through round-trip`() {
        val domain = entity(notes = null).toDomain()
        assertNull(domain.notes)
        assertNull(domain.toEntity().notes)
    }

    @Test
    fun `UUID to String conversion is lossless`() {
        val knownId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        val e = entity().copy(id = knownId.toString())
        assertEquals(knownId, e.toDomain().id)
    }
}
