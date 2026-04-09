package net.cynreub.subly.data.remote.gdrive

import net.cynreub.subly.domain.model.BillingFrequency
import net.cynreub.subly.domain.model.Category
import net.cynreub.subly.domain.model.PaymentMethod
import net.cynreub.subly.domain.model.PaymentType
import net.cynreub.subly.domain.model.Subscription
import org.json.JSONObject
import java.time.LocalDate
import java.util.UUID

fun Subscription.toJson(): JSONObject = JSONObject().apply {
    put("id", id.toString())
    put("name", name)
    put("categoryId", categoryId.toString())
    put("amount", amount)
    put("currency", currency)
    put("frequency", frequency.name)
    put("startDate", startDate.toString())
    put("nextBillingDate", nextBillingDate.toString())
    paymentMethodId?.let { put("paymentMethodId", it.toString()) }
    notes?.let { put("notes", it) }
    put("isActive", isActive)
    put("reminderDaysBefore", reminderDaysBefore)
}

fun JSONObject.toSubscription(): Subscription? = runCatching {
    Subscription(
        id = UUID.fromString(getString("id")),
        name = getString("name"),
        categoryId = UUID.fromString(getString("categoryId")),
        amount = getDouble("amount"),
        currency = getString("currency"),
        frequency = BillingFrequency.valueOf(getString("frequency")),
        startDate = LocalDate.parse(getString("startDate")),
        nextBillingDate = LocalDate.parse(getString("nextBillingDate")),
        paymentMethodId = optString("paymentMethodId").takeIf { it.isNotEmpty() }
            ?.let { UUID.fromString(it) },
        notes = optString("notes").takeIf { it.isNotEmpty() },
        isActive = optBoolean("isActive", true),
        reminderDaysBefore = optInt("reminderDaysBefore", 2)
    )
}.getOrNull()

fun PaymentMethod.toJson(): JSONObject = JSONObject().apply {
    put("id", id.toString())
    put("nickname", nickname)
    put("type", type.name)
    lastFourDigits?.let { put("lastFourDigits", it) }
}

fun JSONObject.toPaymentMethod(): PaymentMethod? = runCatching {
    PaymentMethod(
        id = UUID.fromString(getString("id")),
        nickname = getString("nickname"),
        type = PaymentType.valueOf(getString("type")),
        lastFourDigits = optString("lastFourDigits").takeIf { it.isNotEmpty() },
        icon = null
    )
}.getOrNull()

fun Category.toJson(): JSONObject = JSONObject().apply {
    put("id", id.toString())
    put("name", name)
    put("displayName", displayName)
    put("emoji", emoji)
    put("colorHex", colorHex)
}

fun JSONObject.toCategory(): Category? = runCatching {
    Category(
        id = UUID.fromString(getString("id")),
        name = getString("name"),
        displayName = getString("displayName"),
        emoji = getString("emoji"),
        colorHex = getString("colorHex")
    )
}.getOrNull()
