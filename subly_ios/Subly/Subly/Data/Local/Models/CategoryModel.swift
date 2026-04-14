import Foundation
import SwiftData

@Model
final class CategoryModel {
    @Attribute(.unique) var id: UUID
    var name: String
    var displayName: String
    var emoji: String
    var colorHex: String

    init(from category: Category) {
        self.id = category.id
        self.name = category.name
        self.displayName = category.displayName
        self.emoji = category.emoji
        self.colorHex = category.colorHex
    }

    func toDomain() -> Category {
        Category(
            id: id,
            name: name,
            displayName: displayName,
            emoji: emoji,
            colorHex: colorHex
        )
    }

    func update(from category: Category) {
        name = category.name
        displayName = category.displayName
        emoji = category.emoji
        colorHex = category.colorHex
    }
}
