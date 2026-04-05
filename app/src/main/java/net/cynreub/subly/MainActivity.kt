package net.cynreub.subly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import net.cynreub.subly.data.preferences.PreferencesManager
import net.cynreub.subly.data.preferences.ThemePreference
import net.cynreub.subly.domain.repository.AuthRepository
import net.cynreub.subly.notification.NotificationHelper
import net.cynreub.subly.ui.components.SublyBottomBar
import net.cynreub.subly.ui.components.SublyTopBar
import net.cynreub.subly.ui.main.MainViewModel
import net.cynreub.subly.ui.navigation.NavDestination
import net.cynreub.subly.ui.navigation.SublyNavHost
import net.cynreub.subly.ui.theme.SublyTheme
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var preferencesManager: PreferencesManager

    @Inject
    lateinit var authRepository: AuthRepository

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val subscriptionIdFromNotification = intent?.getStringExtra(NotificationHelper.EXTRA_SUBSCRIPTION_ID)

        setContent {
            val themePreference by preferencesManager.themePreference
                .collectAsStateWithLifecycle(initialValue = ThemePreference.SYSTEM)

            val authState by authRepository.authStateFlow
                .collectAsStateWithLifecycle(initialValue = authRepository.currentUser)

            // Ensure default categories are seeded on cold start when already logged in.
            // On sign-in/registration syncForUser handles this; here we catch the case
            // where the user is already authenticated but the local DB is empty.
            LaunchedEffect(authState?.uid) {
                authState?.uid?.let { uid -> mainViewModel.onUserLoggedIn(uid) }
            }

            SublyTheme(themePreference = themePreference) {
                SublyApp(
                    isLoggedIn = authState != null,
                    subscriptionIdFromNotification = subscriptionIdFromNotification
                )
            }
        }
    }
}

@Composable
fun SublyApp(
    isLoggedIn: Boolean,
    subscriptionIdFromNotification: String? = null
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isAuthRoute = currentRoute == NavDestination.Login.route || currentRoute == NavDestination.Register.route

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
            modifier = Modifier.padding(innerPadding)
        )
    }
}
