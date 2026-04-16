import Foundation

struct Subscription: Identifiable, Equatable, Hashable, Codable {
    var id: UUID
    var name: String
    var categoryId: UUID
    var amount: Double
    var currency: String
    var frequency: BillingFrequency
    var startDate: Date
    var nextBillingDate: Date
    var paymentMethodId: UUID?
    var notes: String?
    var isActive: Bool
    var reminderDaysBefore: Int

    init(
        id: UUID = UUID(),
        name: String,
        categoryId: UUID = Category.defaultId,
        amount: Double,
        currency: String = "USD",
        frequency: BillingFrequency,
        startDate: Date = Date(),
        nextBillingDate: Date,
        paymentMethodId: UUID? = nil,
        notes: String? = nil,
        isActive: Bool = true,
        reminderDaysBefore: Int = 2
    ) {
        self.id = id
        self.name = name
        self.categoryId = categoryId
        self.amount = amount
        self.currency = currency
        self.frequency = frequency
        self.startDate = startDate
        self.nextBillingDate = nextBillingDate
        self.paymentMethodId = paymentMethodId
        self.notes = notes
        self.isActive = isActive
        self.reminderDaysBefore = reminderDaysBefore
    }

    /// Amount normalised to a monthly equivalent
    var monthlyAmount: Double {
        amount * frequency.monthlyMultiplier
    }
}
