package net.cynreub.subly.domain.model

import java.util.UUID

data class Category(
    val id: UUID,
    val name: String,        // internal identifier, e.g. "STREAMING"
    val displayName: String, // user-facing, e.g. "Streaming"
    val emoji: String,
    val colorHex: String
) {
    companion object {
        // Fixed UUIDs matching AppDatabase migration constants — never change these
        val ID_STREAMING  = UUID.fromString("00000000-0000-4000-a000-000000000001")
        val ID_MAGAZINE   = UUID.fromString("00000000-0000-4000-a000-000000000002")
        val ID_SERVICE    = UUID.fromString("00000000-0000-4000-a000-000000000003")
        val ID_MEMBERSHIP = UUID.fromString("00000000-0000-4000-a000-000000000004")
        val ID_CLUB       = UUID.fromString("00000000-0000-4000-a000-000000000005")
        val ID_UTILITY    = UUID.fromString("00000000-0000-4000-a000-000000000006")
        val ID_SOFTWARE   = UUID.fromString("00000000-0000-4000-a000-000000000007")
        val ID_OTHER      = UUID.fromString("00000000-0000-4000-a000-000000000008")

        val DEFAULTS = listOf(
            Category(ID_STREAMING,  "STREAMING",  "Streaming",  "📺", "#E91E63"),
            Category(ID_MAGAZINE,   "MAGAZINE",   "Magazine",   "📰", "#9C27B0"),
            Category(ID_SERVICE,    "SERVICE",    "Service",    "⚙️", "#2196F3"),
            Category(ID_MEMBERSHIP, "MEMBERSHIP", "Membership", "🏷️", "#FF9800"),
            Category(ID_CLUB,       "CLUB",       "Club",       "🎯", "#4CAF50"),
            Category(ID_UTILITY,    "UTILITY",    "Utility",    "💡", "#607D8B"),
            Category(ID_SOFTWARE,   "SOFTWARE",   "Software",   "💻", "#00BCD4"),
            Category(ID_OTHER,      "OTHER",      "Other",      "📦", "#795548")
        )

        val DEFAULT_ID = ID_OTHER
    }
}
