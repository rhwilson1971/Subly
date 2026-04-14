import Foundation

enum PaymentType: String, Codable, CaseIterable {
    // Credit Card Brands
    case visa        = "VISA"
    case mastercard  = "MASTERCARD"
    case discover    = "DISCOVER"
    case amex        = "AMEX"

    // Digital Payment Services
    case paypal      = "PAYPAL"
    case venmo       = "VENMO"
    case cashApp     = "CASHAPP"
    case affirm      = "AFFIRM"
    case klarna      = "KLARNA"

    // Traditional Methods
    case debitCard   = "DEBIT_CARD"
    case bankTransfer = "BANK_TRANSFER"
    case cash        = "CASH"

    // Fallback
    case other       = "OTHER"

    var displayName: String {
        switch self {
        case .visa:         return "Visa"
        case .mastercard:   return "Mastercard"
        case .discover:     return "Discover"
        case .amex:         return "Amex"
        case .paypal:       return "PayPal"
        case .venmo:        return "Venmo"
        case .cashApp:      return "Cash App"
        case .affirm:       return "Affirm"
        case .klarna:       return "Klarna"
        case .debitCard:    return "Debit Card"
        case .bankTransfer: return "Bank Transfer"
        case .cash:         return "Cash"
        case .other:        return "Other"
        }
    }

    /// SF Symbol name for this payment type
    var sfSymbol: String {
        switch self {
        case .visa, .mastercard, .discover, .amex, .debitCard:
            return "creditcard"
        case .paypal, .venmo, .cashApp:
            return "dollarsign.circle"
        case .affirm, .klarna:
            return "cart"
        case .bankTransfer:
            return "building.columns"
        case .cash:
            return "banknote"
        case .other:
            return "creditcard.and.123"
        }
    }
}
