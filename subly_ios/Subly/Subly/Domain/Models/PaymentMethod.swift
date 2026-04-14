import Foundation

struct PaymentMethod: Identifiable, Equatable, Codable {
    var id: UUID
    var nickname: String
    var type: PaymentType
    var lastFourDigits: String?

    init(
        id: UUID = UUID(),
        nickname: String,
        type: PaymentType,
        lastFourDigits: String? = nil
    ) {
        self.id = id
        self.nickname = nickname
        self.type = type
        self.lastFourDigits = lastFourDigits
    }
}
