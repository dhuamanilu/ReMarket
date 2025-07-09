package com.example.remarket.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Inbox

/**  Modelo de ítem **/
data class BottomNavItem(val label: String,
                         val route: String,
                         val icon: ImageVector)

/**  Composable reutilizable **/
@Composable
fun BottomNavigationBar(
    navController: NavHostController,
    isAdmin: Boolean,
    firebaseAuth: com.google.firebase.auth.FirebaseAuth
) {
    val items = if (isAdmin) listOf(
        BottomNavItem("Solicitudes", Routes.ADMIN_HOME, Icons.Default.Inbox),
        BottomNavItem("Reportes",    Routes.ADMIN_REPORTS, Icons.Default.Assessment),
    ) else listOf(
        BottomNavItem("Productos",      Routes.HOME,        Icons.Default.Home),
        BottomNavItem("Mis Productos",  Routes.MY_PRODUCTS, Icons.Default.ShoppingCart),
        BottomNavItem("Perfil",         Routes.PROFILE,     Icons.Default.Person)
    )

    NavigationBar {
        val currentDestination = navController.currentDestination
        items.forEach { item ->
            val selected = currentDestination?.hierarchy?.any { it.route == item.route } ?: false
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        val requiresAuth = item.route == Routes.MY_PRODUCTS || item.route == Routes.PROFILE
                        if (requiresAuth && firebaseAuth.currentUser == null) {
                            navController.navigate(Routes.LOGIN) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        } else {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                },
                icon = { Icon(item.icon, item.label) },
                label = { Text(item.label) }
            )
        }
    }
}
