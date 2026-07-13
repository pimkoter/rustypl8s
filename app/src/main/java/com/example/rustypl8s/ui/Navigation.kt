package com.example.rustypl8s.ui

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.rustypl8s.ui.workout.WorkoutScreen

sealed class Screen(val route: String, val title: String, val icon: androidx.compose.ui.graphics.vector.ImageVector) {
    object Profile : Screen("profile", "Profile", Icons.Default.Person)
    object Workout : Screen("workout", "Workout", Icons.Default.PlayArrow)
    object Exercises : Screen("exercises", "Exercises", Icons.Default.Search)
    object History : Screen("history", "History", Icons.Default.List)
}

@Composable
fun MainNavigation(navController: NavHostController) {
    NavHost(navController, startDestination = Screen.Workout.route) {
        composable(Screen.Profile.route) { PlaceholderScreen("Profile") }
        composable(Screen.Workout.route) { WorkoutScreen() }
        composable(Screen.Exercises.route) { PlaceholderScreen("Exercises") }
        composable(Screen.History.route) { PlaceholderScreen("History") }
    }
}

@Composable
fun BottomNavigationBar(navController: NavHostController) {
    val items = listOf(Screen.Profile, Screen.Workout, Screen.Exercises, Screen.History)
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route
        
        items.forEach { screen ->
            NavigationBarItem(
                icon = { Icon(screen.icon, contentDescription = screen.title) },
                label = { Text(screen.title) },
                selected = currentRoute == screen.route,
                onClick = {
                    navController.navigate(screen.route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}

@Composable
fun PlaceholderScreen(name: String) {
    Surface(modifier = androidx.compose.ui.Modifier.fillMaxSize()) {
        androidx.compose.foundation.layout.Box(contentAlignment = androidx.compose.ui.Alignment.Center) {
            Text(text = "$name Screen (Coming Soon)", style = MaterialTheme.typography.headlineMedium)
        }
    }
}
