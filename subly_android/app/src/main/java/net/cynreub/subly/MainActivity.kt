package net.cynreub.subly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import dagger.hilt.android.AndroidEntryPoint
import net.cynreub.subly.data.preferences.PreferencesManager
import net.cynreub.subly.data.preferences.ThemePreference
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

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val subscriptionIdFromNotification = intent?.getStringExtra(NotificationHelper.EXTRA_SUBSCRIPTION_ID)

        setContent {
            val themePreference by preferencesManager.themePreference
                .collectAsStateWithLifecycle(initialValue = ThemePreference.SYSTEM)
            val startupState by mainViewModel.startupState.collectAsStateWithLifecycle()

            SublyTheme(themePreference = themePreference) {
                if (!startupState.isReady) {
                    // Blank loading screen while Firestore profile is fetched
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    SublyApp(
                        startDestination = startupState.startDestination,
                        subscriptionIdFromNotification = subscriptionIdFromNotification
                    )
                }
            }
        }
    }
}

@Composable
fun SublyApp(
    startDestination: String,
    subscriptionIdFromNotification: String? = null,
    mainViewModel: MainViewModel = hiltViewModel()
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isAuthRoute = currentRoute == NavDestination.Login.route
        || currentRoute == NavDestination.Register.route
        || currentRoute == NavDestination.ProfileSetup.route
        || currentRoute == NavDestination.Onboarding.route

    val currentUser by mainViewModel.currentUser.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (!isAuthRoute) SublyTopBar(
                user = currentUser,
                onSignOut = {
                    mainViewModel.signOut()
                    navController.navigate(NavDestination.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        },
        bottomBar = {
            if (!isAuthRoute) SublyBottomBar(navController = navController)
        }
    ) { innerPadding ->
        SublyNavHost(
            navController = navController,
            startDestination = startDestination,
            onAuthSuccess = {},
            modifier = Modifier.padding(innerPadding)
        )
    }
}
