package net.cynreub.subly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import net.cynreub.subly.data.preferences.PreferencesManager
import net.cynreub.subly.data.preferences.ThemePreference
import net.cynreub.subly.notification.NotificationHelper
import net.cynreub.subly.ui.components.SublyBottomBar
import net.cynreub.subly.ui.components.SublyTopBar
import net.cynreub.subly.ui.navigation.NavDestination
import net.cynreub.subly.ui.navigation.SublyNavHost
import net.cynreub.subly.ui.theme.SublyTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val subscriptionIdFromNotification = intent?.getStringExtra(NotificationHelper.EXTRA_SUBSCRIPTION_ID)

        setContent {
            val themePreference by preferencesManager.themePreference
                .collectAsStateWithLifecycle(initialValue = ThemePreference.SYSTEM)

            SublyTheme(themePreference = themePreference) {
                var isLoggedIn by remember { mutableStateOf(FirebaseAuth.getInstance().currentUser != null) }
                SublyApp(
                    isLoggedIn = isLoggedIn,
                    onAuthSuccess = { isLoggedIn = true },
                    subscriptionIdFromNotification = subscriptionIdFromNotification
                )
            }
        }
    }
}

@Composable
fun SublyApp(
    isLoggedIn: Boolean,
    onAuthSuccess: () -> Unit,
    subscriptionIdFromNotification: String? = null
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isAuthRoute = currentRoute == NavDestination.Login.route
        || currentRoute == NavDestination.Register.route
        || currentRoute == NavDestination.ProfileSetup.route
        || currentRoute == NavDestination.Onboarding.route

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (!isAuthRoute) SublyTopBar()
        },
        bottomBar = {
            if (!isAuthRoute) SublyBottomBar(navController = navController)
        }
    ) { innerPadding ->
        SublyNavHost(
            navController = navController,
            isLoggedIn = isLoggedIn,
            onAuthSuccess = onAuthSuccess,
            modifier = Modifier.padding(innerPadding)
        )
    }
}
