package net.cynreub.subly.data.remote.firestore

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import net.cynreub.subly.domain.model.BillingFrequency
import net.cynreub.subly.domain.model.PaymentMethod
import net.cynreub.subly.domain.model.PaymentType
import net.cynreub.subly.domain.model.Subscription
import net.cynreub.subly.domain.model.SubscriptionType
import java.time.LocalDate
import java.util.UUID

fun Subscription.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id.toString(),
    "name" to name,
    "type" to type.name,
    "amount" to amount,
    "currency" to currency,
    "frequency" to frequency.name,
    "startDate" to startDate.toString(),
    "nextBillingDate" to nextBillingDate.toString(),
    "paymentMethodId" to paymentMethodId?.toString(),
    "notes" to notes,
    "isActive" to isActive,
    "reminderDaysBefore" to reminderDaysBefore,
    "updatedAt" to FieldValue.serverTimestamp()
)

fun DocumentSnapshot.toSubscription(): Subscription? = runCatching {
    Subscription(
        id = UUID.fromString(getString("id")!!),
        name = getString("name")!!,
        type = SubscriptionType.valueOf(getString("type")!!),
        amount = getDouble("amount")!!,
        currency = getString("currency")!!,
        frequency = BillingFrequency.valueOf(getString("frequency")!!),
        startDate = LocalDate.parse(getString("startDate")!!),
        nextBillingDate = LocalDate.parse(getString("nextBillingDate")!!),
        paymentMethodId = getString("paymentMethodId")?.let { UUID.fromString(it) },
        notes = getString("notes"),
        isActive = getBoolean("isActive") ?: true,
        reminderDaysBefore = getLong("reminderDaysBefore")?.toInt() ?: 2
    )
}.getOrNull()

fun PaymentMethod.toFirestoreMap(): Map<String, Any?> = mapOf(
    "id" to id.toString(),
    "nickname" to nickname,
    "type" to type.name,
    "lastFourDigits" to lastFourDigits
    // icon excluded — drawable resource ID, meaningless across devices
)

fun DocumentSnapshot.toPaymentMethod(): PaymentMethod? = runCatching {
    PaymentMethod(
        id = UUID.fromString(getString("id")!!),
        nickname = getString("nickname")!!,
        type = PaymentType.valueOf(getString("type")!!),
        lastFourDigits = getString("lastFourDigits"),
        icon = null // not synced to Firestore
    )
}.getOrNull()
