package net.cynreub.subly.ui.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.cynreub.subly.domain.model.PaymentType

private val VisaBlue = Color(0xFF1A1F71)
private val MastercardRed = Color(0xFFEB001B)
private val MastercardOrange = Color(0xFFF79E1B)
private val AmexBlue = Color(0xFF006FCF)
private val DiscoverOrange = Color(0xFFFF6600)
private val PayPalDarkBlue = Color(0xFF003087)
private val PayPalLightBlue = Color(0xFF009CDE)
private val VenmoBlue = Color(0xFF008CFF)
private val AffirmAccentBlue = Color(0xFF4A4AF4)
private val KlarnaPink = Color(0xFFFFB3C7)

/**
 * Chip-style brand mark for a [PaymentType]: a colored/text mark for recognizable
 * card networks and payment services, or a tinted generic icon for the rest.
 */
@Composable
fun PaymentBrandIcon(
    type: PaymentType,
    modifier: Modifier = Modifier,
    size: Dp = 44.dp
) {
    val cornerRadius = size * 0.22f
    val neutralBackground = MaterialTheme.colorScheme.surface
    val neutralBorder = MaterialTheme.colorScheme.outlineVariant

    when (type) {
        PaymentType.VISA -> BrandChip(modifier, size, cornerRadius, neutralBackground, neutralBorder) {
            BrandText(text = "VISA", color = VisaBlue, size = size, italic = true)
        }

        PaymentType.MASTERCARD -> BrandChip(modifier, size, cornerRadius, neutralBackground, neutralBorder) {
            val circleSize = size * 0.5f
            Box(
                Modifier
                    .offset(x = -circleSize * 0.25f)
                    .size(circleSize)
                    .background(MastercardRed, CircleShape)
            )
            Box(
                Modifier
                    .offset(x = circleSize * 0.25f)
                    .size(circleSize)
                    .background(MastercardOrange, CircleShape)
            )
        }

        PaymentType.AMEX -> BrandChip(modifier, size, cornerRadius, AmexBlue, AmexBlue) {
            BrandText(text = "AMEX", color = Color.White, size = size)
        }

        PaymentType.DISCOVER -> BrandChip(modifier, size, cornerRadius, neutralBackground, neutralBorder) {
            BrandText(
                text = "Discover",
                color = MaterialTheme.colorScheme.onSurface,
                size = size,
                fontSizeRatio = 0.16f
            )
            Box(
                Modifier
                    .offset(x = size * 0.22f, y = size * 0.14f)
                    .size(size * 0.14f)
                    .background(DiscoverOrange, CircleShape)
            )
        }

        PaymentType.PAYPAL -> BrandChip(modifier, size, cornerRadius, neutralBackground, neutralBorder) {
            TwoToneText(
                first = "Pay",
                firstColor = PayPalDarkBlue,
                second = "Pal",
                secondColor = PayPalLightBlue,
                size = size
            )
        }

        PaymentType.VENMO -> BrandChip(modifier, size, cornerRadius, VenmoBlue, VenmoBlue) {
            BrandText(text = "V", color = Color.White, size = size, fontSizeRatio = 0.42f)
        }

        PaymentType.CASHAPP -> BrandChip(modifier, size, cornerRadius, Color.Black, Color.Black) {
            BrandText(text = "$", color = Color.White, size = size, fontSizeRatio = 0.42f)
        }

        PaymentType.AFFIRM -> BrandChip(modifier, size, cornerRadius, Color.Black, Color.Black) {
            BrandText(text = "affirm", color = Color.White, size = size, fontSizeRatio = 0.16f)
            Box(
                Modifier
                    .offset(x = size * 0.27f, y = -size * 0.12f)
                    .size(size * 0.09f)
                    .background(AffirmAccentBlue, CircleShape)
            )
        }

        PaymentType.KLARNA -> BrandChip(modifier, size, cornerRadius, KlarnaPink, KlarnaPink) {
            BrandText(text = "Klarna", color = Color.Black, size = size, fontSizeRatio = 0.17f)
        }

        PaymentType.DEBIT_CARD -> GenericChip(
            modifier, size, cornerRadius, neutralBackground, neutralBorder, Icons.Filled.CreditCard
        )

        PaymentType.BANK_TRANSFER -> GenericChip(
            modifier, size, cornerRadius, neutralBackground, neutralBorder, Icons.Filled.AccountBalance
        )

        PaymentType.CASH -> GenericChip(
            modifier, size, cornerRadius, neutralBackground, neutralBorder, Icons.Filled.Payments
        )

        PaymentType.OTHER -> GenericChip(
            modifier, size, cornerRadius, neutralBackground, neutralBorder, Icons.Filled.HelpOutline
        )
    }
}

@Composable
private fun BrandChip(
    modifier: Modifier,
    size: Dp,
    cornerRadius: Dp,
    background: Color,
    border: Color,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .size(size)
            .background(background, RoundedCornerShape(cornerRadius))
            .border(1.dp, border, RoundedCornerShape(cornerRadius)),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun GenericChip(
    modifier: Modifier,
    size: Dp,
    cornerRadius: Dp,
    background: Color,
    border: Color,
    icon: ImageVector
) {
    BrandChip(modifier, size, cornerRadius, background, border) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(size * 0.55f),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun BrandText(
    text: String,
    color: Color,
    size: Dp,
    fontSizeRatio: Float = 0.24f,
    italic: Boolean = false
) {
    Text(
        text = text,
        color = color,
        fontFamily = FontFamily.Serif,
        fontWeight = FontWeight.Black,
        fontStyle = if (italic) FontStyle.Italic else FontStyle.Normal,
        fontSize = (size.value * fontSizeRatio).sp,
        maxLines = 1
    )
}

@Composable
private fun TwoToneText(
    first: String,
    firstColor: Color,
    second: String,
    secondColor: Color,
    size: Dp
) {
    Row {
        BrandText(text = first, color = firstColor, size = size, fontSizeRatio = 0.17f)
        BrandText(text = second, color = secondColor, size = size, fontSizeRatio = 0.17f)
    }
}

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
