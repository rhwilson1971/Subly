package net.cynreub.subly.data.mapper

import net.cynreub.subly.data.local.entity.PaymentMethodEntity
import net.cynreub.subly.domain.model.PaymentType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import java.util.UUID

class PaymentMethodMapperTest {

    private val id = UUID.randomUUID()

    private fun entity(
        lastFourDigits: String? = "4242",
        icon: Int? = 1
    ) = PaymentMethodEntity(
        id = id.toString(),
        nickname = "Visa Rewards",
        type = PaymentType.VISA,
        lastFourDigits = lastFourDigits,
        icon = icon
    )

    @Test
    fun `toDomain preserves all fields`() {
        val domain = entity().toDomain()

        assertEquals(id, domain.id)
        assertEquals("Visa Rewards", domain.nickname)
        assertEquals(PaymentType.VISA, domain.type)
        assertEquals("4242", domain.lastFourDigits)
        assertEquals(1, domain.icon)
    }

    @Test
    fun `toEntity preserves all fields`() {
        val back = entity().toDomain().toEntity()

        assertEquals(id.toString(), back.id)
        assertEquals("Visa Rewards", back.nickname)
        assertEquals(PaymentType.VISA, back.type)
        assertEquals("4242", back.lastFourDigits)
        assertEquals(1, back.icon)
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
    fun `null lastFourDigits is preserved through round-trip`() {
        val domain = entity(lastFourDigits = null).toDomain()
        assertNull(domain.lastFourDigits)
        assertNull(domain.toEntity().lastFourDigits)
    }

    @Test
    fun `null icon is preserved through round-trip`() {
        val domain = entity(icon = null).toDomain()
        assertNull(domain.icon)
        assertNull(domain.toEntity().icon)
    }

    @Test
    fun `UUID to String conversion is lossless`() {
        val knownId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        val e = entity().copy(id = knownId.toString())
        assertEquals(knownId, e.toDomain().id)
    }
}
