package net.cynreub.subly.ui.components

import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import net.cynreub.subly.ui.navigation.NavDestination

@Composable
fun SublyBottomBar(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    NavigationBar(modifier = modifier) {
        NavDestination.bottomNavItems.forEach { destination ->
            val selected = currentDestination?.hierarchy?.any {
                it.route == destination.route
            } == true

            NavigationBarItem(
                icon = {
                    destination.icon?.let { icon ->
                        Icon(
                            imageVector = icon,
                            contentDescription = destination.title
                        )
                    }
                },
                label = { Text(destination.title) },
                selected = selected,
                onClick = {
                    navController.navigate(destination.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
