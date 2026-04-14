import Foundation
import SwiftData

@Model
final class SubscriptionModel {
    @Attribute(.unique) var id: UUID
    var name: String
    var categoryId: UUID
    var amount: Double
    var currency: String
    var frequency: String  // BillingFrequency.rawValue
    var startDate: Date
    var nextBillingDate: Date
    var paymentMethodId: UUID?
    var notes: String?
    var isActive: Bool
    var reminderDaysBefore: Int

    init(from subscription: Subscription) {
        self.id = subscription.id
        self.name = subscription.name
        self.categoryId = subscription.categoryId
        self.amount = subscription.amount
        self.currency = subscription.currency
        self.frequency = subscription.frequency.rawValue
        self.startDate = subscription.startDate
        self.nextBillingDate = subscription.nextBillingDate
        self.paymentMethodId = subscription.paymentMethodId
        self.notes = subscription.notes
        self.isActive = subscription.isActive
        self.reminderDaysBefore = subscription.reminderDaysBefore
    }

    func toDomain() -> Subscription {
        Subscription(
            id: id,
            name: name,
            categoryId: categoryId,
            amount: amount,
            currency: currency,
            frequency: BillingFrequency(rawValue: frequency) ?? .monthly,
            startDate: startDate,
            nextBillingDate: nextBillingDate,
            paymentMethodId: paymentMethodId,
            notes: notes,
            isActive: isActive,
            reminderDaysBefore: reminderDaysBefore
        )
    }

    func update(from subscription: Subscription) {
        name = subscription.name
        categoryId = subscription.categoryId
        amount = subscription.amount
        currency = subscription.currency
        frequency = subscription.frequency.rawValue
        startDate = subscription.startDate
        nextBillingDate = subscription.nextBillingDate
        paymentMethodId = subscription.paymentMethodId
        notes = subscription.notes
        isActive = subscription.isActive
        reminderDaysBefore = subscription.reminderDaysBefore
    }
}
