import Foundation
import SwiftData

@Model
final class PaymentMethodModel {
    @Attribute(.unique) var id: UUID
    var nickname: String
    var type: String  // PaymentType.rawValue
    var lastFourDigits: String?

    init(from paymentMethod: PaymentMethod) {
        self.id = paymentMethod.id
        self.nickname = paymentMethod.nickname
        self.type = paymentMethod.type.rawValue
        self.lastFourDigits = paymentMethod.lastFourDigits
    }

    func toDomain() -> PaymentMethod {
        PaymentMethod(
            id: id,
            nickname: nickname,
            type: PaymentType(rawValue: type) ?? .other,
            lastFourDigits: lastFourDigits
        )
    }

    func update(from paymentMethod: PaymentMethod) {
        nickname = paymentMethod.nickname
        type = paymentMethod.type.rawValue
        lastFourDigits = paymentMethod.lastFourDigits
    }
}
