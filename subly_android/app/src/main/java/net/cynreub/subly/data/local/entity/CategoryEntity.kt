package net.cynreub.subly.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    val id: String,
    val name: String,        // internal identifier, e.g. "STREAMING"
    val displayName: String, // user-facing, e.g. "Streaming"
    val emoji: String,
    val colorHex: String
)
