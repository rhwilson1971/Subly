package net.cynreub.subly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import net.cynreub.subly.notification.NotificationHelper
import net.cynreub.subly.ui.components.SublyBottomBar
import net.cynreub.subly.ui.navigation.NavDestination
import net.cynreub.subly.ui.navigation.SublyNavHost
import net.cynreub.subly.ui.theme.SublyTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Extract subscription ID from notification intent
        val subscriptionIdFromNotification = intent?.getStringExtra(NotificationHelper.EXTRA_SUBSCRIPTION_ID)

        setContent {
            SublyTheme {
                SublyApp(subscriptionIdFromNotification = subscriptionIdFromNotification)
            }
        }
    }
}

@Composable
fun SublyApp(subscriptionIdFromNotification: String? = null) {
    val navController = rememberNavController()

    // Navigate to subscription detail if opened from notification
    LaunchedEffect(subscriptionIdFromNotification) {
        if (subscriptionIdFromNotification != null) {
            navController.navigate(NavDestination.SubscriptionDetail.createRoute(subscriptionIdFromNotification))
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            SublyBottomBar(navController = navController)
        }
    ) { innerPadding ->
        SublyNavHost(
            navController = navController,
            modifier = Modifier.padding(innerPadding)
        )
    }
}