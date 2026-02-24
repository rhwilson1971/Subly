package net.cynreub.subly.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import net.cynreub.subly.ui.auth.LoginScreen
import net.cynreub.subly.ui.auth.RegisterScreen
import net.cynreub.subly.ui.home.HomeScreen
import net.cynreub.subly.ui.subscriptions.SubscriptionsScreen
import net.cynreub.subly.ui.subscriptions.addedit.AddEditSubscriptionScreen
import net.cynreub.subly.ui.subscriptions.detail.SubscriptionDetailScreen
import net.cynreub.subly.ui.payment.PaymentMethodsScreen
import net.cynreub.subly.ui.payment.addedit.AddEditPaymentMethodScreen
import net.cynreub.subly.ui.settings.SettingsScreen

@Composable
fun SublyNavHost(
    navController: NavHostController,
    isLoggedIn: Boolean,
    onAuthSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) NavDestination.Home.route else NavDestination.Login.route,
        modifier = modifier
    ) {
        // Auth graph
        composable(NavDestination.Login.route) {
            LoginScreen(
                onNavigateToRegister = { navController.navigate(NavDestination.Register.route) },
                onAuthSuccess = {
                    onAuthSuccess()
                    navController.navigate(NavDestination.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(NavDestination.Register.route) {
            RegisterScreen(
                onNavigateToLogin = { navController.popBackStack() },
                onAuthSuccess = {
                    onAuthSuccess()
                    navController.navigate(NavDestination.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        // Main graph
        composable(NavDestination.Home.route) {
            HomeScreen(
                onNavigateToSubscriptionDetail = { subscriptionId ->
                    navController.navigate(NavDestination.SubscriptionDetail.createRoute(subscriptionId))
                },
                onNavigateToAddSubscription = {
                    navController.navigate(NavDestination.AddEditSubscription.createRoute())
                }
            )
        }

        composable(NavDestination.Subscriptions.route) {
            SubscriptionsScreen(
                onNavigateToDetail = { subscriptionId ->
                    navController.navigate(NavDestination.SubscriptionDetail.createRoute(subscriptionId))
                },
                onNavigateToAdd = {
                    navController.navigate(NavDestination.AddEditSubscription.createRoute())
                }
            )
        }

        composable(NavDestination.PaymentMethods.route) {
            PaymentMethodsScreen(
                onNavigateToAdd = {
                    navController.navigate(NavDestination.AddEditPaymentMethod.createRoute())
                },
                onNavigateToEdit = { paymentMethodId ->
                    navController.navigate(NavDestination.AddEditPaymentMethod.createRoute(paymentMethodId))
                }
            )
        }

        composable(NavDestination.Settings.route) {
            SettingsScreen()
        }

        composable(
            route = NavDestination.SubscriptionDetail.route,
            arguments = listOf(
                navArgument("subscriptionId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val subscriptionId = backStackEntry.arguments?.getString("subscriptionId") ?: return@composable
            SubscriptionDetailScreen(
                subscriptionId = subscriptionId,
                onNavigateBack = { navController.popBackStack() },
                onNavigateToEdit = { id ->
                    navController.navigate(NavDestination.AddEditSubscription.createRoute(id))
                }
            )
        }

        composable(
            route = NavDestination.AddEditSubscription.route,
            arguments = listOf(
                navArgument("subscriptionId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val subscriptionId = backStackEntry.arguments?.getString("subscriptionId")
            AddEditSubscriptionScreen(
                subscriptionId = subscriptionId,
                onNavigateBack = { navController.popBackStack() }
            )
        }

        composable(
            route = NavDestination.AddEditPaymentMethod.route,
            arguments = listOf(
                navArgument("paymentMethodId") {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                }
            )
        ) { backStackEntry ->
            val paymentMethodId = backStackEntry.arguments?.getString("paymentMethodId")
            AddEditPaymentMethodScreen(
                paymentMethodId = paymentMethodId,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}
