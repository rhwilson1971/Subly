package net.cynreub.subly.domain.model

enum class PaymentType {
    // Credit Card Brands
    VISA,
    MASTERCARD,
    DISCOVER,
    AMEX,

    // Digital Payment Services
    PAYPAL,
    VENMO,
    CASHAPP,
    AFFIRM,
    KLARNA,

    // Traditional Methods
    DEBIT_CARD,
    BANK_TRANSFER,
    CASH,

    // Fallback
    OTHER
}
