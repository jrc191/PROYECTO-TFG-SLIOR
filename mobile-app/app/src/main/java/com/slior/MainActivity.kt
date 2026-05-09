package com.slior

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.slior.ui.auth.LoginScreen
import com.slior.ui.auth.RegisterScreen
import com.slior.ui.routes.CreateRouteScreen
import com.slior.ui.routes.RouteDetailScreen
import com.slior.ui.routes.RouteListScreen
import com.slior.ui.theme.BrutalistBlack
import com.slior.ui.theme.BrutalistWhite
import com.slior.ui.theme.SliorTheme
import com.slior.viewmodel.AuthViewModel
import com.slior.ui.components.ConnectivityBanner
import com.slior.viewmodel.ConnectivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var globalEventBus: com.slior.util.GlobalEventBus

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SliorTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = hiltViewModel()
                val connViewModel: ConnectivityViewModel = hiltViewModel()
                
                val sessionUserId by authViewModel.sessionUserId.collectAsStateWithLifecycle()
                val isConnected by connViewModel.isConnected.collectAsStateWithLifecycle()
                val unauthenticated by authViewModel.unauthorizedEvent.collectAsStateWithLifecycle(initialValue = false)

                val snackbarHostState = remember { SnackbarHostState() }
                val scope = rememberCoroutineScope()

                // Emitimos cambios de conectividad al bus global para que otros (AuthViewModel) reaccionen
                LaunchedEffect(isConnected) {
                    globalEventBus.emitConnectivityChanged(isConnected)
                }

                // Si detectamos 401 (Unauthorized) redirigimos al login
                LaunchedEffect(unauthenticated) {
                    if (unauthenticated) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Sesión expirada. Por favor, identifícate de nuevo.")
                        }
                        navController.navigate("login") {
                            popUpTo(0) { inclusive = true }
                        }
                        authViewModel.consumeUnauthorizedEvent()
                    }
                }

                // Mientras comprobamos la sesión, mostrar pantalla de carga
                if (sessionUserId == null) {
                    Box(
                        modifier         = Modifier
                            .fillMaxSize()
                            .background(BrutalistWhite),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color       = BrutalistBlack,
                            strokeWidth = 3.dp
                        )
                    }
                    return@SliorTheme
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    ConnectivityBanner(isConnected = isConnected)
                    
                    Box(modifier = Modifier.weight(1f)) {
                        val startDestination = if (sessionUserId!!.isNotBlank()) {
                            "routes/${sessionUserId}"
                        } else {
                            "login"
                        }

                        NavHost(
                            navController    = navController,
                            startDestination = startDestination
                        ) {
                            composable("login") {
                                LoginScreen(
                                    onLoginSuccess = { repartidorId ->
                                        navController.navigate("routes/$repartidorId") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    },
                                    onGoToRegister = { navController.navigate("register") }
                                )
                            }

                            composable("register") {
                                RegisterScreen(
                                    onRegisterSuccess = { repartidorId ->
                                        navController.navigate("routes/$repartidorId") {
                                            popUpTo("login") { inclusive = true }
                                        }
                                    },
                                    onGoToLogin = { navController.popBackStack() }
                                )
                            }

                            composable("routes/{repartidorId}") { backStackEntry ->
                                val repartidorId = backStackEntry.arguments
                                    ?.getString("repartidorId") ?: ""
                                RouteListScreen(
                                    repartidorId = repartidorId,
                                    onRouteClick = { routeId ->
                                        navController.navigate("route-detail/$routeId")
                                    },
                                    onCreateRoute = {
                                        navController.navigate("create-route/$repartidorId")
                                    },
                                    onLogout = {
                                        navController.navigate("login") {
                                            popUpTo(0) { inclusive = true }
                                        }
                                    }
                                )
                            }

                            composable("route-detail/{routeId}") { backStackEntry ->
                                val routeId = backStackEntry.arguments
                                    ?.getString("routeId") ?: ""
                                RouteDetailScreen(
                                    routeId = routeId,
                                    onBack  = { navController.popBackStack() },
                                    onEdit  = { id ->
                                        navController.navigate("create-route/${sessionUserId}?routeId=$id")
                                    }
                                )
                            }

                            composable(
                                "create-route/{repartidorId}?routeId={routeId}",
                                arguments = listOf(
                                    navArgument("repartidorId") { type = NavType.StringType },
                                    navArgument("routeId") { 
                                        type = NavType.StringType
                                        nullable = true
                                        defaultValue = null
                                    }
                                )
                            ) { backStackEntry ->
                                val repartidorId = backStackEntry.arguments?.getString("repartidorId") ?: ""
                                val routeId = backStackEntry.arguments?.getString("routeId")
                                CreateRouteScreen(
                                    repartidorId  = repartidorId,
                                    routeId       = routeId,
                                    onBack        = { navController.popBackStack() },
                                    onRouteCreated = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }

                SnackbarHost(hostState = snackbarHostState)
            }
        }
    }
}
