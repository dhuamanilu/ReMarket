package com.example.remarket.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.remarket.ui.auth.login.LoginScreen
import com.example.remarket.ui.auth.register.RegisterScreen
import com.example.remarket.ui.home.HomeScreen
import com.example.remarket.ui.product.create.CreateProductScreen
import com.example.remarket.ui.product.detail.ProductDetailScreen
import com.example.remarket.ui.profile.ProfileScreen

object Destinations {
    const val LOGIN      = "login"
    const val REGISTER   = "register"
    const val HOME       = "home"
    const val DETAIL     = "detail/{productId}"
    const val CREATE     = "create"
    const val PROFILE    = "profile"
}

@Composable
fun RemarketNavGraph() {
    /*val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Destinations.LOGIN) {

        // Login
        composable(Destinations.LOGIN) {
            LoginScreen(onLoginSuccess = { navController.navigate(Destinations.HOME) })
        }

        // Registro
        composable(Destinations.REGISTER) {
            RegisterScreen(onRegisterSuccess = { navController.popBackStack() })
        }

        // Home
        composable(Destinations.HOME) {
            HomeScreen(onProductClick = { id ->
                navController.navigate("detail/$id")
            }, onCreateClick = {
                navController.navigate(Destinations.CREATE)
            })
        }

        // Detalle de producto (recibe parámetro productId)
        composable(
            route = Destinations.DETAIL,
            arguments = listOf(navArgument("productId") { type = NavType.StringType })
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")!!
            ProductDetailScreen(productId = productId, onBack = {
                navController.popBackStack()
            })
        }

        // Crear publicación
        composable(Destinations.CREATE) {
            CreateProductScreen(onNext = {
                // tras publicar, regresamos al Home
                navController.popBackStack(Destinations.HOME, inclusive = false)
            })
        }

        // Perfil
        composable(Destinations.PROFILE) {
            ProfileScreen(onBack = { navController.popBackStack() })
        }
    }*/
}