// ui/navigation/AppNavGraph.kt
package com.example.remarket.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.navigation
import com.example.remarket.ui.auth.login.LoginScreen
import com.example.remarket.ui.auth.login.LoginViewModel
import com.example.remarket.ui.auth.login.NavigationEvent
import com.example.remarket.ui.auth.register.Register1Screen
import com.example.remarket.ui.auth.register.Register2Screen
import com.example.remarket.ui.auth.register.RegisterViewModel
import com.example.remarket.ui.home.HomeScreen
import com.example.remarket.ui.product.detail.ProductDetailScreen
import com.example.remarket.ui.home.HomeScreen // Importa la pantalla real
import com.example.remarket.ui.home.HomeViewModel // Importa el ViewModel real
import com.example.remarket.ui.product.create.CreateProductViewModel
import com.example.remarket.ui.product.create.ReviewScreen
import com.example.remarket.ui.product.create.Step1Screen
import com.example.remarket.ui.product.create.Step2Screen
import com.example.remarket.ui.product.create.Step3Screen
import com.example.remarket.ui.product.detail.ProductDetailViewModel
import com.google.firebase.auth.FirebaseAuth


// Definición de rutas centralizada y clara
object Routes {
    const val LOGIN = "login"
    const val REGISTER_FLOW = "register_flow"
    const val REGISTER_1 = "register_1"
    const val REGISTER_2 = "register_2"
    const val HOME = "home"
    const val PRODUCT_DETAIL = "product_detail/{productId}"
    const val PRODUCT_CREATE = "product_create"
    const val PRODUCT_EDIT = "product_edit/{productId}" // <-- AÑADIDO
    const val ADMIN_HOME = "admin_home"
    const val FORGOT_PASSWORD = "forgot_password"
    const val PURCHASE = "purchase/{productId}"
}

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    // Inyecta FirebaseAuth para verificar el usuario actual
    firebaseAuth: FirebaseAuth
) {
    // Determina la ruta inicial basándose en si el usuario está logueado
    val startDestination = if (firebaseAuth.currentUser != null) {
        Routes.HOME
    } else {
        Routes.LOGIN
    }

    NavHost(
        navController = navController,
        startDestination = startDestination // La ruta de inicio ahora es dinámica
    ) {
        // Pantalla de Login
        composable(Routes.LOGIN) {
            val loginViewModel: LoginViewModel = hiltViewModel()

            // Manejar eventos de navegación del LoginViewModel
            LaunchedEffect(key1 = loginViewModel.navigationEvent) {
                loginViewModel.navigationEvent.collect { event ->
                    when (event) {
                        is NavigationEvent.NavigateToHome -> {
                            navController.navigate(Routes.HOME) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                            loginViewModel.clearNavigationEvent()
                        }
                        is NavigationEvent.NavigateToAdmin -> {
                            navController.navigate(Routes.ADMIN_HOME) {
                                popUpTo(Routes.LOGIN) { inclusive = true }
                            }
                            loginViewModel.clearNavigationEvent()
                        }
                        is NavigationEvent.NavigateToForgotPassword -> {
                            navController.navigate(Routes.FORGOT_PASSWORD)
                            loginViewModel.clearNavigationEvent()
                        }
                        null -> { /* No hacer nada */ }
                    }
                }
            }

            LoginScreen(
                viewModel = loginViewModel,
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER_FLOW)
                },
                onNavigateToHome = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToAdmin = {
                    navController.navigate(Routes.ADMIN_HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Routes.FORGOT_PASSWORD)
                }
            )
        }

        // Gráfico de Navegación Anidado para el Registro
        navigation(
            startDestination = Routes.REGISTER_1,
            route = Routes.REGISTER_FLOW
        ) {
            composable(Routes.REGISTER_1) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Routes.REGISTER_FLOW)
                }
                val registerViewModel: RegisterViewModel = hiltViewModel(parentEntry)

                Register1Screen(
                    onBack = { navController.popBackStack() },
                    onNext = { navController.navigate(Routes.REGISTER_2) },
                    viewModel = registerViewModel
                )
            }

            composable(Routes.REGISTER_2) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Routes.REGISTER_FLOW)
                }
                val registerViewModel: RegisterViewModel = hiltViewModel(parentEntry)

                Register2Screen(
                    onBack = { navController.popBackStack() },
                    onRegister = {
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(Routes.REGISTER_FLOW) { inclusive = true }
                        }
                    },
                    viewModel = registerViewModel
                )
            }
        }

        // Pantalla principal (Home)
        composable(Routes.HOME) {
            val homeViewModel: HomeViewModel = hiltViewModel()
            val uiState by homeViewModel.uiState.collectAsState()

            // Al iniciar Home, si el token está vacío, intenta obtenerlo.
            LaunchedEffect(Unit) {
                homeViewModel.fetchToken()
            }

            HomeScreen(
                uiState = uiState,
                onSearchQueryChanged = homeViewModel::onSearchQueryChanged,
                onRefresh = homeViewModel::onRefresh,
                onNavigateToProductDetail = { productId ->
                    navController.navigate(Routes.PRODUCT_DETAIL.replace("{productId}", productId))
                },
                onNavigateToCreateProduct = {
                    navController.navigate(Routes.PRODUCT_CREATE)
                },
                onLogout = {
                    homeViewModel.onLogout()
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }

        // Detalle de producto
        composable(Routes.PRODUCT_DETAIL) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            val viewModel: ProductDetailViewModel = hiltViewModel()

            ProductDetailScreen(
                productId = productId,
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onBuyProduct = { prodId ->
                    navController.navigate(Routes.PURCHASE.replace("{productId}", prodId))
                },
                onNavigateToEdit = { prodId ->
                    navController.navigate(Routes.PRODUCT_EDIT.replace("{productId}", prodId))
                }
            )
        }

        // Flujo de creación de producto
        composable(Routes.PRODUCT_CREATE) {
            ProductCreateFlow(navController = navController)
        }

        // Flujo de edición de producto
        composable(Routes.PRODUCT_EDIT) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId")
            ProductCreateFlow(navController = navController, productIdForEdit = productId)
        }

        // --- Pantallas Placeholder ---
        composable(Routes.ADMIN_HOME) {
            AdminHomeScreen(onLogout = {
                navController.navigate(Routes.LOGIN) {
                    popUpTo(Routes.ADMIN_HOME) { inclusive = true }
                }
            })
        }

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(onNavigateBack = { navController.popBackStack() })
        }

        composable(Routes.PURCHASE) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            PurchaseScreen(
                productId = productId,
                onNavigateBack = { navController.popBackStack() },
                onPurchaseComplete = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.PURCHASE.replace("{productId}", productId)) { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
private fun ProductCreateFlow(navController: NavHostController, productIdForEdit: String? = null) {
    val createVm: CreateProductViewModel = hiltViewModel()
    val createNavController = rememberNavController()

    // Si es para editar, carga los datos del producto
    LaunchedEffect(productIdForEdit) {
        if (productIdForEdit != null) {
            createVm.loadProductForEdit(productIdForEdit)
        }
    }

    NavHost(
        navController = createNavController,
        startDestination = "step1",
        modifier = Modifier.fillMaxSize()
    ) {
        composable("step1") {
            Step1Screen(
                viewModel = createVm,
                onNext = { createNavController.navigate("step2") }
            )
        }
        composable("step2") {
            Step2Screen(
                viewModel = createVm,
                onNext = { createNavController.navigate("step3") },
                onBack = { createNavController.popBackStack() }
            )
        }
        composable("step3") {
            Step3Screen(
                viewModel = createVm,
                onBack = { createNavController.popBackStack() },
                onSubmit = { createNavController.navigate("review") }
            )
        }
        composable("review") {
            ReviewScreen(
                viewModel = createVm,
                onBack = {
                    // Vuelve al home y limpia la pila de creación/edición
                    navController.navigate(Routes.HOME) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
            )
        }
    }
}

// --- Implementaciones básicas para que el NavGraph compile ---

@Composable
fun AdminHomeScreen(onLogout: () -> Unit) {
    androidx.compose.material3.Text("Admin Home Screen - TODO")
}

@Composable
fun ForgotPasswordScreen(onNavigateBack: () -> Unit) {
    androidx.compose.material3.Text("Forgot Password Screen - TODO")
}

@Composable
fun PurchaseScreen(
    productId: String,
    onNavigateBack: () -> Unit,
    onPurchaseComplete: () -> Unit
) {
    androidx.compose.material3.Text("Purchase Screen for product $productId - TODO")
}
