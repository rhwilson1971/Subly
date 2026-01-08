package net.cynreub.subly.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.graphics.vector.ImageVector

sealed class NavDestination(
    val route: String,
    val title: String,
    val icon: ImageVector? = null
) {
    data object Home : NavDestination(
        route = "home",
        title = "Home",
        icon = Icons.Default.Home
    )

    data object Subscriptions : NavDestination(
        route = "subscriptions",
        title = "Subscriptions",
        icon = Icons.Default.List
    )

    data object PaymentMethods : NavDestination(
        route = "payment_methods",
        title = "Payments",
        icon = Icons.Default.Star
    )

    data object Settings : NavDestination(
        route = "settings",
        title = "Settings",
        icon = Icons.Default.Settings
    )

    data object SubscriptionDetail : NavDestination(
        route = "subscription_detail/{subscriptionId}",
        title = "Subscription Detail"
    ) {
        fun createRoute(subscriptionId: String) = "subscription_detail/$subscriptionId"
    }

    data object AddEditSubscription : NavDestination(
        route = "add_edit_subscription?subscriptionId={subscriptionId}",
        title = "Add Subscription"
    ) {
        fun createRoute(subscriptionId: String? = null): String {
            return if (subscriptionId != null) {
                "add_edit_subscription?subscriptionId=$subscriptionId"
            } else {
                "add_edit_subscription"
            }
        }
    }

    data object AddEditPaymentMethod : NavDestination(
        route = "add_edit_payment_method?paymentMethodId={paymentMethodId}",
        title = "Add Payment Method"
    ) {
        fun createRoute(paymentMethodId: String? = null): String {
            return if (paymentMethodId != null) {
                "add_edit_payment_method?paymentMethodId=$paymentMethodId"
            } else {
                "add_edit_payment_method"
            }
        }
    }

    companion object {
        val bottomNavItems = listOf(Home, Subscriptions, PaymentMethods, Settings)
    }
}
