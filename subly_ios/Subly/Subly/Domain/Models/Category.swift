import Foundation

struct Category: Identifiable, Equatable, Codable {
    var id: UUID
    var name: String        // internal key, e.g. "STREAMING"
    var displayName: String // user-facing, e.g. "Streaming"
    var emoji: String
    var colorHex: String

    // Fixed UUIDs matching Android AppDatabase constants — never change these
    static let idStreaming  = UUID(uuidString: "00000000-0000-4000-a000-000000000001")!
    static let idMagazine   = UUID(uuidString: "00000000-0000-4000-a000-000000000002")!
    static let idService    = UUID(uuidString: "00000000-0000-4000-a000-000000000003")!
    static let idMembership = UUID(uuidString: "00000000-0000-4000-a000-000000000004")!
    static let idClub       = UUID(uuidString: "00000000-0000-4000-a000-000000000005")!
    static let idUtility    = UUID(uuidString: "00000000-0000-4000-a000-000000000006")!
    static let idSoftware   = UUID(uuidString: "00000000-0000-4000-a000-000000000007")!
    static let idOther      = UUID(uuidString: "00000000-0000-4000-a000-000000000008")!
    static let defaultId    = idOther

    static let defaults: [Category] = [
        Category(id: idStreaming,  name: "STREAMING",  displayName: "Streaming",  emoji: "📺", colorHex: "#E91E63"),
        Category(id: idMagazine,   name: "MAGAZINE",   displayName: "Magazine",   emoji: "📰", colorHex: "#9C27B0"),
        Category(id: idService,    name: "SERVICE",    displayName: "Service",    emoji: "⚙️", colorHex: "#2196F3"),
        Category(id: idMembership, name: "MEMBERSHIP", displayName: "Membership", emoji: "🏷️", colorHex: "#FF9800"),
        Category(id: idClub,       name: "CLUB",       displayName: "Club",       emoji: "🎯", colorHex: "#4CAF50"),
        Category(id: idUtility,    name: "UTILITY",    displayName: "Utility",    emoji: "💡", colorHex: "#607D8B"),
        Category(id: idSoftware,   name: "SOFTWARE",   displayName: "Software",   emoji: "💻", colorHex: "#00BCD4"),
        Category(id: idOther,      name: "OTHER",      displayName: "Other",      emoji: "📦", colorHex: "#795548"),
    ]
}

struct CategoryWithCount: Identifiable {
    var category: Category
    var subscriptionCount: Int
    var id: UUID { category.id }
}
