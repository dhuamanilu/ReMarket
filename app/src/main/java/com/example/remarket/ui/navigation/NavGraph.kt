// ui/navigation/AppNavGraph.kt
package com.example.remarket.ui.navigation

import android.util.Log
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
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.example.remarket.ui.admin.AdminPendingProductsScreen
import com.example.remarket.ui.admin.AdminProductDetailScreen
import com.example.remarket.ui.auth.login.LoginScreen
import com.example.remarket.ui.auth.login.LoginViewModel
import com.example.remarket.ui.auth.login.NavigationEvent
import com.example.remarket.ui.auth.register.Register1Screen
import com.example.remarket.ui.auth.register.Register2Screen
import com.example.remarket.ui.auth.register.RegisterViewModel
import com.example.remarket.ui.home.HomeScreen
import com.example.remarket.ui.product.detail.ProductDetailScreen
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
import com.example.remarket.ui.profile.ProfileViewModel
import com.google.firebase.auth.FirebaseAuth
import com.example.remarket.ui.admin.AdminPendingProductsViewModel
import com.example.remarket.ui.admin.reports.ManageReportsScreen
import com.example.remarket.ui.admin.reports.ManageReportsViewModel
import com.example.remarket.ui.admin.review.AdminReviewStep1Screen
import com.example.remarket.ui.admin.review.AdminReviewStep2Screen
import com.example.remarket.ui.admin.review.AdminReviewViewModel
import com.example.remarket.ui.admin.user.AdminPendingUsersScreen
import com.example.remarket.ui.admin.user.AdminPendingUsersViewModel
import com.example.remarket.ui.admin.user.AdminUserDetailScreen
import com.example.remarket.ui.chat.ChatListScreen // <-- AÑADE
import com.example.remarket.ui.chat.ChatScreen // <-- AÑADE
import com.example.remarket.ui.purchase.PurchaseScreen

// Definición de rutas centralizada y clara
object Routes {
    const val ROOT = "root" // <-- AÑADIDO
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
    const val REQUESTS       = "requests"      // ← NUEVA
    const val REPORTS        = "reports"       // ← NUEVA
    const val CHATS = "chats" // <-- AÑADE ESTA LÍNEA
    const val CHAT_DETAIL = "chats/{chatId}" // <-- AÑADE ESTA LÍNEA
    // ⬇️ Agrega justo debajo de CHAT_DETAIL
    const val ADMIN_REVIEW_FLOW = "admin_review_flow/{productId}"
    const val ADMIN_REV_STEP1  = "admin_rev_step1"
    const val ADMIN_REV_STEP2  = "admin_rev_step2"
    const val ADMIN_USERS       = "admin_users"
    const val ADMIN_USER_DETAIL = "admin_user_detail/{userId}"
}

@Composable
fun AppNavGraph(
    navController: NavHostController = rememberNavController(),
    firebaseAuth: FirebaseAuth
) {
    // Siempre iniciamos en la pantalla de productos. Si el usuario
    // no está autenticado podrá navegar y solo se le pedirá iniciar
    // sesión cuando intente realizar una compra.
    val startDestination = Routes.ROOT

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // --- NUEVA RUTA DE INICIO ---
        composable(Routes.ROOT) {
            RootScreen(navController = navController)
        }
        // Pantalla de Login
        composable(Routes.LOGIN) {
            val loginViewModel: LoginViewModel = hiltViewModel()

            // Manejar eventos de navegación del LoginViewModel
            LaunchedEffect(key1 = loginViewModel.navigationEvent) {
                loginViewModel.navigationEvent.collect { event ->
                    when (event) {
                        is NavigationEvent.NavigateToHome -> {
                            navController.navigate(Routes.HOME) {
                                // 🔸 vacía TODA la pila, incluido el RootScreen
                                popUpTo(0)          // id 0 ⇒ raíz del NavHost
                                launchSingleTop = true
                            }
                            loginViewModel.clearNavigationEvent()
                        }
                        is NavigationEvent.NavigateToAdmin -> {
                            navController.navigate(Routes.ADMIN_HOME) {
                                // 🔸 vacía TODA la pila, incluido el RootScreen
                                popUpTo(0)          // id 0 ⇒ raíz del NavHost
                                launchSingleTop = true
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
        // ---- Lista de usuarios por aprobar ----
        composable(Routes.ADMIN_USERS) {
            val vm: AdminPendingUsersViewModel = hiltViewModel()
            Scaffold(bottomBar = { BottomNavigationBar(navController, isAdmin = true, firebaseAuth) }) { padd ->
                AdminPendingUsersScreen(
                    vm = vm,
                    navController = navController,
                    onUserClick = { id ->
                        navController.navigate(Routes.ADMIN_USER_DETAIL.replace("{userId}", id))
                    },
                    onLogout = {
                        vm.onLogout()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0); launchSingleTop = true
                        }
                    }
                )
            }
        }

// ---- Detalle de usuario ----
        composable(Routes.ADMIN_USER_DETAIL) { backStack ->
            val id = backStack.arguments?.getString("userId") ?: ""
            AdminUserDetailScreen(userId = id,
               navController = navController
                ,onBack = { navController.popBackStack() })
        }
        composable(Routes.HOME) {
            val vm: HomeViewModel = hiltViewModel()
            val ui by vm.uiState.collectAsState()

            Scaffold(
                bottomBar = { BottomNavigationBar(navController, isAdmin = false, firebaseAuth = firebaseAuth) }
            ) { innerPadding ->
                HomeScreen(
                    uiState = ui,
                    paddingValues = innerPadding,          // ← NUEVO
                    onSearchQueryChanged = vm::onSearchQueryChanged,
                    onRefresh = vm::onRefresh,
                    onNavigateToProductDetail = { id ->
                        navController.navigate(
                            Routes.PRODUCT_DETAIL.replace("{productId}", id)
                        )
                    },
                    onNavigateToCreateProduct = {
                        navController.navigate(Routes.PRODUCT_CREATE)
                    },
                    onLogout = {
                        vm.onLogout()
                        navController.navigate(Routes.LOGIN) {
                            // ⬇️ esto borra absolutamente TODO el back-stack
                            popUpTo(0)            // 0 es el root del NavHost
                            launchSingleTop = true
                        }
                    }
                )
            }
        }

        // --- Mis Productos ---
        composable(Routes.MY_PRODUCTS) {
            if (firebaseAuth.currentUser == null) {
                LaunchedEffect(Unit) { navController.navigate(Routes.LOGIN) }
            } else {
                val vm: MyProductsViewModel = hiltViewModel()
                val ui by vm.uiState.collectAsState()
                Scaffold(bottomBar = { BottomNavigationBar(navController, isAdmin = false, firebaseAuth = firebaseAuth) }) { padd ->
                    MyProductsScreen(uiState = ui, paddingValues = padd, onNavigateToProductDetail = { id ->
                        navController.navigate(
                            Routes.PRODUCT_DETAIL.replace("{productId}", id)
                        )
                    })
                }
            }
        }
// --- RUTA PARA LA LISTA DE CHATS ---
        composable(Routes.CHATS) {
            if (firebaseAuth.currentUser == null) {
                LaunchedEffect(Unit) { navController.navigate(Routes.LOGIN) }
            } else {
                Scaffold(bottomBar = { BottomNavigationBar(navController, isAdmin = false, firebaseAuth = firebaseAuth) }) { padd ->
                    ChatListScreen(
                        paddingValues = padd,
                        onChatClick = { chatId -> navController.navigate(Routes.CHAT_DETAIL.replace("{chatId}", chatId)) }
                    )
                }
            }
        }

        // --- RUTA PARA LA PANTALLA DE CONVERSACIÓN ---
        composable(Routes.CHAT_DETAIL) { backStackEntry ->
            if (firebaseAuth.currentUser == null) {
                LaunchedEffect(Unit) { navController.navigate(Routes.LOGIN) }
            } else {
                val chatId = backStackEntry.arguments?.getString("chatId") ?: ""
                ChatScreen(
                    chatId = chatId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
        // --- Perfil ---
        composable(Routes.PROFILE) {
            if (firebaseAuth.currentUser == null) {
                LaunchedEffect(Unit) { navController.navigate(Routes.LOGIN) }
            } else {
                val vm: ProfileViewModel = hiltViewModel()
                val ui by vm.uiState.collectAsState()
                Scaffold(bottomBar = { BottomNavigationBar(navController, isAdmin = false, firebaseAuth = firebaseAuth) }) { padd ->
                    ProfileScreen(state = ui, padding = padd)
                }
            }
        }

        // --- Reportes (admin) ---
        composable(Routes.ADMIN_REPORTS) {
            val vm: ManageReportsViewModel = hiltViewModel()

            Scaffold(
                bottomBar = { BottomNavigationBar(navController, isAdmin = true, firebaseAuth = firebaseAuth) }
            ) { padd ->
                ManageReportsScreen(
                    viewModel = vm,
                    paddingValues = padd,
                    onNavigateToProduct = { id ->
                        // ⬇️  Usa la ruta de detalle ya existente
                        navController.navigate(
                            Routes.PRODUCT_DETAIL.replace("{productId}", id)
                        )
                    }
                )
            }
        }


        // Detalle de producto
        composable(Routes.PRODUCT_DETAIL) {
            val viewModel: ProductDetailViewModel = hiltViewModel()

            ProductDetailScreen(
                productId = it.arguments?.getString("productId") ?: "",
                viewModel = viewModel,
                onNavigateBack = { navController.popBackStack() },
                onBuyProduct = { prodId ->
                    if (firebaseAuth.currentUser != null) {
                        navController.navigate(Routes.PURCHASE.replace("{productId}", prodId))
                    } else {
                        navController.navigate(Routes.LOGIN)
                    }
                },
                onNavigateToEdit = { prodId -> navController.navigate(Routes.PRODUCT_EDIT.replace("{productId}", prodId)) },

                // --- AÑADE ESTA LÍNEA ---
                onNavigateToChat = { chatId -> navController.navigate(Routes.CHAT_DETAIL.replace("{chatId}", chatId)) }
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
            val vm: AdminPendingProductsViewModel = hiltViewModel()

            Scaffold(bottomBar = { BottomNavigationBar(navController, isAdmin = true, firebaseAuth = firebaseAuth) }) { padd ->
                AdminPendingProductsScreen(
                    vm = vm,
                    navController = navController,
                    onProductClick = { id ->
                        Log.d("NAV", "→ ReviewFlow con id=$id")
                        navController.navigate(
                            Routes.ADMIN_REVIEW_FLOW.replace("{productId}", id)
                        )
                    },
                    onLogout = {
                        vm.onLogout()
                        navController.navigate(Routes.LOGIN) {
                            popUpTo(0)            // 0 es el root del NavHost
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
        // Flujo de revisión de productos (admin)
        composable(Routes.ADMIN_PRODUCT_DETAIL) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: ""
            AdminProductDetailScreen(
                productId = productId,
                onBack    = { navController.popBackStack() }
            )
        }
        // ░░░░░  FLUJO DE REVISIÓN DE PRODUCTO (solo-lectura)  ░░░░░
        navigation(
            startDestination = Routes.ADMIN_REV_STEP1,
            route = Routes.ADMIN_REVIEW_FLOW
        ) {
            // Paso 1 – campos de texto
            composable(
                Routes.ADMIN_REV_STEP1,) { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Routes.ADMIN_REVIEW_FLOW)
                }
                val productId = parentEntry.arguments?.getString("productId")!!
                val vm: AdminReviewViewModel = hiltViewModel(parentEntry)   //  ← usa vm único
                LaunchedEffect(Unit) { vm.load(productId) }

                AdminReviewStep1Screen(
                    viewModel = vm,
                    onNext = { navController.navigate(Routes.ADMIN_REV_STEP2) }
                )
            }

            // Paso 2 – imágenes
            composable(
                Routes.ADMIN_REV_STEP2
            ) { val parentEntry = remember(it) {
                navController.getBackStackEntry(Routes.ADMIN_REVIEW_FLOW)
            }
                val vm: AdminReviewViewModel = hiltViewModel(parentEntry)
                AdminReviewStep2Screen(vm,navController)
            }
        }

        composable(Routes.FORGOT_PASSWORD) {
            ForgotPasswordScreen(onNavigateBack = { navController.popBackStack() })
        }

        // Reemplaza únicamente el composable de PURCHASE:

        composable(Routes.PURCHASE) { back ->
            val productId = back.arguments?.getString("productId") ?: ""
            PurchaseScreen(
                productId = productId,
                onBack = { navController.popBackStack() },
                onPurchaseComplete = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.HOME) { inclusive = true }
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
fun RootScreen(navController: NavHostController, viewModel: RootViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    // Este efecto se ejecutará cuando el 'target' cambie (de Loading a Home, Admin, o Login)
    LaunchedEffect(uiState.target) {
        val destination = when (uiState.target) {
            is NavigationTarget.Home -> Routes.HOME
            is NavigationTarget.Admin -> Routes.ADMIN_HOME
            is NavigationTarget.Login -> Routes.LOGIN
            is NavigationTarget.Loading -> null // Aún no hay destino
        }

        destination?.let { dest ->
            navController.navigate(dest) {
                // 🔸 ESTA es la única línea que cambias
                popUpTo(0)          // ← elimina todo el back-stack

                launchSingleTop = true   // (opcional, evita duplicados)
            }
        }
    }

    // Muestra un indicador de carga en pantalla completa mientras se decide el destino
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        androidx.compose.material3.CircularProgressIndicator()
    }
}


@Composable
fun ForgotPasswordScreen(onNavigateBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("Pantalla de Olvidé Contraseña - TODO")
    }
}
