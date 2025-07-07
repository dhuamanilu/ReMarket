// ui/navigation/AppNavGraph.kt
package com.example.remarket.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
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
import com.example.remarket.ui.admin.AdminPendingProductsScreen
import com.example.remarket.ui.admin.AdminProductDetailScreen
import com.example.remarket.ui.admin.ManageReportsScreen
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
import com.example.remarket.ui.myproducts.MyProductsScreen
import com.example.remarket.ui.myproducts.MyProductsViewModel
import com.example.remarket.ui.product.create.CreateProductViewModel
import com.example.remarket.ui.product.create.ReviewScreen
import com.example.remarket.ui.product.create.Step1Screen
import com.example.remarket.ui.product.create.Step2Screen
import com.example.remarket.ui.product.create.Step3Screen
import com.example.remarket.ui.product.detail.ProductDetailViewModel
import com.example.remarket.ui.profile.ProfileScreen
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
    const val PRODUCT_EDIT = "product_edit/{productId}"
    const val ADMIN_HOME = "admin_home"
    const val FORGOT_PASSWORD = "forgot_password"
    const val PURCHASE = "purchase/{productId}"
    const val ADMIN_PRODUCT_DETAIL = "admin_product_detail/{productId}"
    const val MY_PRODUCTS   = "my_products"
    const val PROFILE       = "profile"
    const val ADMIN_REPORTS = "admin_reports"
}

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
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
        startDestination = startDestination
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
                        null -> { /* No hacer nada */ }
                        is NavigationEvent.Error -> {
                            // Manejar errores si es necesario
                        }
                        NavigationEvent.Idle -> {
                            // Estado idle, no hacer nada
                        }
                        else -> {}
                    }
                }
            }

            // Pasa todos los callbacks pero como funciones vacías
            // ya que la navegación real se maneja por eventos
            LoginScreen(
                viewModel = loginViewModel,
                onNavigateToRegister = {
                    navController.navigate(Routes.REGISTER_FLOW)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(Routes.FORGOT_PASSWORD)
                },
                onNavigateToHome = {
                    // Función vacía - la navegación se maneja por eventos
                },
                onNavigateToAdmin = {
                    // Función vacía - la navegación se maneja por eventos
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

        composable(Routes.HOME) {
            val vm: HomeViewModel = hiltViewModel()
            val ui by vm.uiState.collectAsState()

            Scaffold(bottomBar = { BottomNavigationBar(navController, isAdmin = false) }) { padd ->
                HomeScreen(
                    uiState = ui,
                    onSearchQueryChanged = vm::onSearchQueryChanged,
                    onRefresh = vm::onRefresh,
                    onNavigateToProductDetail = { id ->
                        navController.navigate(Routes.PRODUCT_DETAIL.replace("{productId}", id))
                    },
                    onNavigateToCreateProduct = { navController.navigate(Routes.PRODUCT_CREATE) },
                    onLogout = {
                        vm.onLogout()
                        navController.navigate(Routes.LOGIN) { popUpTo(Routes.HOME) { inclusive = true } }
                    }
                )
            }
        }

        // --- Mis Productos ---
        composable(Routes.MY_PRODUCTS) {
            val vm: MyProductsViewModel = hiltViewModel()
            val ui by vm.uiState.collectAsState()
            Scaffold(bottomBar = { BottomNavigationBar(navController, isAdmin = false) }) { padd ->
                MyProductsScreen(uiState = ui, paddingValues = padd)
            }
        }

        // --- Perfil ---
        composable(Routes.PROFILE) {
            Scaffold(bottomBar = { BottomNavigationBar(navController, isAdmin = false) }) { padd ->
                ProfileScreen()                       // o tu pantalla real de perfil
            }
        }

        // --- Reportes (admin) ---
        composable(Routes.ADMIN_REPORTS) {
            Scaffold(bottomBar = { BottomNavigationBar(navController, isAdmin = true) }) { padd ->
                ManageReportsScreen()                 // ya existe como stub
            }
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

        // Pantalla de administrador
        composable(Routes.ADMIN_HOME) {
            AdminPendingProductsScreen(
                onProductClick = { id ->
                            navController.navigate(
                                   Routes.ADMIN_PRODUCT_DETAIL.replace("{productId}", id)
                                        )
                       },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.ADMIN_HOME) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.ADMIN_PRODUCT_DETAIL) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            AdminProductDetailScreen(
                productId = productId,
                onBack    = { navController.popBackStack() }
            )
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