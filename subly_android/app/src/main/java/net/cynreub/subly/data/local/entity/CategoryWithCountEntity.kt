package net.cynreub.subly.data.local.entity

/** Room query result POJO — not a table entity, just a projection. */
data class CategoryWithCountEntity(
    val id: String,
    val name: String,
    val displayName: String,
    val emoji: String,
    val colorHex: String,
    val subscriptionCount: Int
)
