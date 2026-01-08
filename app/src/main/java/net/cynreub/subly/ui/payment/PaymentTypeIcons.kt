package net.cynreub.subly.ui.payment

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector
import net.cynreub.subly.domain.model.PaymentType

/**
 * Get the Material Icon for a given PaymentType
 * Note: Using simplified icons as extended Material Icons require additional dependencies
 */
fun getPaymentTypeIcon(type: PaymentType): ImageVector {
    return when (type) {
        // Credit Card Brands - using Star icon
        PaymentType.VISA,
        PaymentType.MASTERCARD,
        PaymentType.DISCOVER,
        PaymentType.AMEX,
        PaymentType.DEBIT_CARD -> Icons.Default.Star

        // Digital Payment Services - using Info icon
        PaymentType.PAYPAL,
        PaymentType.VENMO,
        PaymentType.CASHAPP,
        PaymentType.AFFIRM,
        PaymentType.KLARNA -> Icons.Default.Info

        // Traditional Methods - using Star icon
        PaymentType.BANK_TRANSFER,
        PaymentType.CASH -> Icons.Default.Star

        // Fallback
        PaymentType.OTHER -> Icons.Default.MoreVert
    }
}

/**
 * Format PaymentType enum as human-readable string
 */
fun formatPaymentType(type: PaymentType): String {
    return when (type) {
        PaymentType.VISA -> "Visa"
        PaymentType.MASTERCARD -> "Mastercard"
        PaymentType.DISCOVER -> "Discover"
        PaymentType.AMEX -> "American Express"
        PaymentType.PAYPAL -> "PayPal"
        PaymentType.VENMO -> "Venmo"
        PaymentType.CASHAPP -> "Cash App"
        PaymentType.AFFIRM -> "Affirm"
        PaymentType.KLARNA -> "Klarna"
        PaymentType.DEBIT_CARD -> "Debit Card"
        PaymentType.BANK_TRANSFER -> "Bank Transfer"
        PaymentType.CASH -> "Cash"
        PaymentType.OTHER -> "Other"
    }
}
