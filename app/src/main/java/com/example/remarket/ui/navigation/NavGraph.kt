// ui/navigation/AppNavGraph.kt
package com.example.remarket.ui.navigation

import androidx.compose.runtime.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.remarket.ui.auth.login.LoginScreen
import com.example.remarket.ui.auth.login.LoginViewModel
import com.example.remarket.ui.auth.login.NavigationEvent
import com.example.remarket.ui.auth.register.Register1Screen
import com.example.remarket.ui.auth.register.Register2Screen
import com.example.remarket.ui.auth.register.RegisterViewModel
import com.example.remarket.ui.home.HomeScreen
import com.example.remarket.ui.product.detail.ProductDetailScreen
import com.example.remarket.ui.product.create.CreateProductScreen
import com.example.remarket.ui.home.HomeScreen

// Definición de rutas
object Routes {
    const val LOGIN = "login"
    const val REGISTER_1 = "register_1"
    const val REGISTER_2 = "register_2"
    const val HOME = "home"
    const val PRODUCT_DETAIL = "product_detail"
    const val PRODUCT_CREATE = "product_create"
    const val ADMIN_HOME = "admin_home"
    const val FORGOT_PASSWORD = "forgot_password"
    const val PURCHASE = "purchase"
}

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Routes.LOGIN
    ) {
        // Pantalla de Login
        composable(Routes.LOGIN) {
            val loginViewModel: LoginViewModel = hiltViewModel()
            val navigationEvent by loginViewModel.navigationEvent.collectAsState()

            // Manejar eventos de navegación del LoginViewModel
            LaunchedEffect(navigationEvent) {
                when (navigationEvent) {
                    NavigationEvent.NavigateToHome -> {
                        navController.navigate(Routes.HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                        loginViewModel.clearNavigationEvent()
                    }
                    NavigationEvent.NavigateToAdmin -> {
                        navController.navigate(Routes.ADMIN_HOME) {
                            popUpTo(Routes.LOGIN) { inclusive = true }
                        }
                        loginViewModel.clearNavigationEvent()
                    }
                    NavigationEvent.NavigateToForgotPassword -> {
                        navController.navigate(Routes.FORGOT_PASSWORD)
                        loginViewModel.clearNavigationEvent()
                    }
                    null -> { /* No hacer nada */ }
                }
            }

            LoginScreen(
                viewModel = loginViewModel,
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER_1)
                }
            )
        }

        // Registro - Paso 1
        composable(Routes.REGISTER_1) {
            val registerViewModel: RegisterViewModel = hiltViewModel()

            Register1Screen(
                onBack = {
                    navController.popBackStack()
                },
                onNext = {
                    navController.navigate(Routes.REGISTER_2)
                },
                viewModel = registerViewModel
            )
        }

        // Registro - Paso 2
        composable(Routes.REGISTER_2) {
            val registerViewModel: RegisterViewModel = hiltViewModel()

            Register2Screen(
                onBack = {
                    navController.popBackStack()
                },
                onRegister = {
                    // Registro exitoso, ir al login
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.REGISTER_1) { inclusive = true }
                    }
                },
                viewModel = registerViewModel
            )
        }

        // Pantalla principal (Home)
        composable(Routes.HOME) {
            HomeScreen(
                onNavigateToProductDetail = { productId ->
                    navController.navigate("${Routes.PRODUCT_DETAIL}/$productId")
                },
                onNavigateToCreateProduct = {
                    navController.navigate(Routes.PRODUCT_CREATE)
                },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

        // Detalle de producto
        composable("${Routes.PRODUCT_DETAIL}/{productId}") { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""

            ProductDetailScreen(
                productId = productId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onBuyProduct = { productId ->
                    navController.navigate("${Routes.PURCHASE}/$productId")
                }
            )
        }

        // Crear producto
        composable(Routes.PRODUCT_CREATE) {
            CreateProductScreen(
                onNext = {
                    // Producto creado exitosamente, volver al home
                    navController.popBackStack()
                },
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }

        // Pantalla de compra/reserva
        composable("${Routes.PURCHASE}/{productId}") { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""

            PurchaseScreen(
                productId = productId,
                onNavigateBack = {
                    navController.popBackStack()
                },
                onPurchaseComplete = {
                    // Compra completada, volver al home
                    navController.navigate(Routes.HOME) {
                        popUpTo("${Routes.PURCHASE}/$productId") { inclusive = true }
                    }
                }
            )
        }

        // Admin Home (si es necesario)
        composable(Routes.ADMIN_HOME) {
            AdminHomeScreen(
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.ADMIN_HOME) { inclusive = true }
                    }
                }
            )
        }

        // Forgot Password (pantalla placeholder)
        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(
                onNavigateBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}

// Pantallas placeholder que necesitarás crear
@Composable
fun HomeScreen(
    onNavigateToProductDetail: (String) -> Unit,
    onNavigateToCreateProduct: () -> Unit,
    onLogout: () -> Unit
) {
    // TODO: Implementar HomeScreen
    androidx.compose.material3.Text("Home Screen - TODO")
}

@Composable
fun PurchaseScreen(
    productId: String,
    onNavigateBack: () -> Unit,
    onPurchaseComplete: () -> Unit
) {
    // TODO: Implementar PurchaseScreen
    androidx.compose.material3.Text("Purchase Screen - TODO")
}

@Composable
fun AdminHomeScreen(
    onLogout: () -> Unit
) {
    // TODO: Implementar AdminHomeScreen
    androidx.compose.material3.Text("Admin Home Screen - TODO")
}

@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit
) {
    // TODO: Implementar ForgotPasswordScreen
    androidx.compose.material3.Text("Forgot Password Screen - TODO")
}