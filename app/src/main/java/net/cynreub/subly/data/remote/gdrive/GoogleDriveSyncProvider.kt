package net.cynreub.subly.data.remote.gdrive

import net.cynreub.subly.domain.model.Category
import net.cynreub.subly.domain.model.PaymentMethod
import net.cynreub.subly.domain.model.Subscription
import net.cynreub.subly.domain.sync.SyncProvider
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Stores all data as three JSON files in the user's Drive appDataFolder:
 * subscriptions.json, payment_methods.json, categories.json.
 * Each upsert/delete does a read-modify-write on the relevant file.
 */
@Singleton
class GoogleDriveSyncProvider @Inject constructor(
    private val driveClient: GoogleDriveClient
) : SyncProvider {

    companion object {
        private const val SUBSCRIPTIONS_FILE = "subscriptions.json"
        private const val PAYMENT_METHODS_FILE = "payment_methods.json"
        private const val CATEGORIES_FILE = "categories.json"
    }

    // --- Subscriptions ---

    override suspend fun upsertSubscription(subscription: Subscription) {
        val list = readSubscriptions().toMutableList()
        list.removeAll { it.id == subscription.id }
        list.add(subscription)
        driveClient.writeFile(SUBSCRIPTIONS_FILE, list.toJsonArray { it.toJson() }.toString())
    }

    override suspend fun deleteSubscription(id: UUID) {
        val list = readSubscriptions().filter { it.id != id }
        driveClient.writeFile(SUBSCRIPTIONS_FILE, list.toJsonArray { it.toJson() }.toString())
    }

    override suspend fun fetchAllSubscriptions(uid: String): List<Subscription> =
        readSubscriptions()

    private suspend fun readSubscriptions(): List<Subscription> =
        parseJsonArray(driveClient.readFile(SUBSCRIPTIONS_FILE)) { it.toSubscription() }

    // --- Payment Methods ---

    override suspend fun upsertPaymentMethod(paymentMethod: PaymentMethod) {
        val list = readPaymentMethods().toMutableList()
        list.removeAll { it.id == paymentMethod.id }
        list.add(paymentMethod)
        driveClient.writeFile(PAYMENT_METHODS_FILE, list.toJsonArray { it.toJson() }.toString())
    }

    override suspend fun deletePaymentMethod(id: UUID) {
        val list = readPaymentMethods().filter { it.id != id }
        driveClient.writeFile(PAYMENT_METHODS_FILE, list.toJsonArray { it.toJson() }.toString())
    }

    override suspend fun fetchAllPaymentMethods(uid: String): List<PaymentMethod> =
        readPaymentMethods()

    private suspend fun readPaymentMethods(): List<PaymentMethod> =
        parseJsonArray(driveClient.readFile(PAYMENT_METHODS_FILE)) { it.toPaymentMethod() }

    // --- Categories ---

    override suspend fun upsertCategory(category: Category) {
        val list = readCategories().toMutableList()
        list.removeAll { it.id == category.id }
        list.add(category)
        driveClient.writeFile(CATEGORIES_FILE, list.toJsonArray { it.toJson() }.toString())
    }

    override suspend fun deleteCategory(id: UUID) {
        val list = readCategories().filter { it.id != id }
        driveClient.writeFile(CATEGORIES_FILE, list.toJsonArray { it.toJson() }.toString())
    }

    override suspend fun fetchAllCategories(uid: String): List<Category> = readCategories()

    private suspend fun readCategories(): List<Category> =
        parseJsonArray(driveClient.readFile(CATEGORIES_FILE)) { it.toCategory() }

    // --- Helpers ---

    private fun <T> parseJsonArray(raw: String?, transform: (JSONObject) -> T?): List<T> {
        raw ?: return emptyList()
        return runCatching {
            val arr = JSONArray(raw)
            (0 until arr.length()).mapNotNull { transform(arr.getJSONObject(it)) }
        }.getOrDefault(emptyList())
    }

    private fun <T> List<T>.toJsonArray(transform: (T) -> JSONObject): JSONArray =
        JSONArray().also { arr -> forEach { arr.put(transform(it)) } }
}
