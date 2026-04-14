package net.cynreub.subly.data.mapper

import net.cynreub.subly.data.local.entity.CategoryEntity
import net.cynreub.subly.data.local.entity.CategoryWithCountEntity
import net.cynreub.subly.domain.model.Category
import net.cynreub.subly.ui.categories.CategoryWithCount
import java.util.UUID

fun CategoryEntity.toDomain(): Category = Category(
    id = UUID.fromString(id),
    name = name,
    displayName = displayName,
    emoji = emoji,
    colorHex = colorHex
)

fun Category.toEntity(): CategoryEntity = CategoryEntity(
    id = id.toString(),
    name = name,
    displayName = displayName,
    emoji = emoji,
    colorHex = colorHex
)

fun CategoryWithCountEntity.toCategoryWithCount(): CategoryWithCount = CategoryWithCount(
    category = Category(
        id = UUID.fromString(id),
        name = name,
        displayName = displayName,
        emoji = emoji,
        colorHex = colorHex
    ),
    subscriptionCount = subscriptionCount
)
