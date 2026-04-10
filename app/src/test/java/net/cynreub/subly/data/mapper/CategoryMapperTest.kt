package net.cynreub.subly.data.mapper

import net.cynreub.subly.data.local.entity.CategoryEntity
import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.UUID

class CategoryMapperTest {

    private val id = UUID.randomUUID()

    private fun entity() = CategoryEntity(
        id = id.toString(),
        name = "STREAMING",
        displayName = "Streaming",
        emoji = "📺",
        colorHex = "#E91E63"
    )

    @Test
    fun `toDomain preserves all fields`() {
        val domain = entity().toDomain()

        assertEquals(id, domain.id)
        assertEquals("STREAMING", domain.name)
        assertEquals("Streaming", domain.displayName)
        assertEquals("📺", domain.emoji)
        assertEquals("#E91E63", domain.colorHex)
    }

    @Test
    fun `toEntity preserves all fields`() {
        val back = entity().toDomain().toEntity()

        assertEquals(id.toString(), back.id)
        assertEquals("STREAMING", back.name)
        assertEquals("Streaming", back.displayName)
        assertEquals("📺", back.emoji)
        assertEquals("#E91E63", back.colorHex)
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
    fun `UUID to String conversion is lossless`() {
        val knownId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000")
        val e = entity().copy(id = knownId.toString())
        assertEquals(knownId, e.toDomain().id)
    }
}
