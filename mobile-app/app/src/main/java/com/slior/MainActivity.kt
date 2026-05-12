package com.slior

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.slior.ui.auth.LoginScreen
import com.slior.ui.auth.RegisterScreen
import com.slior.ui.routes.CreateRouteScreen
import com.slior.ui.routes.RouteDetailScreen
import com.slior.ui.routes.RouteListScreen
import com.slior.ui.settings.SettingsScreen
import com.slior.ui.theme.BrutalistBlack
import com.slior.ui.theme.BrutalistWhite
import com.slior.ui.theme.SliorTheme
import com.slior.ui.components.ConnectivityBanner
import com.slior.viewmodel.AuthViewModel
import com.slior.viewmodel.ConnectivityViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : androidx.appcompat.app.AppCompatActivity() {

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

                // Emitimos cambios de conectividad
                LaunchedEffect(isConnected) {
                    globalEventBus.emitConnectivityChanged(isConnected)
                }

                // Redirigir al login si no hay sesión (y no estamos cargando)
                if (sessionUserId == null) {
                    Box(Modifier.fillMaxSize().background(BrutalistWhite))
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
                                            popUpTo("register") { inclusive = true }
                                        }
                                    },
                                    onGoToLogin = { navController.popBackStack() }
                                )
                            }
                            composable("routes/{repartidorId}") { backStackEntry ->
                                val repartidorId = backStackEntry.arguments?.getString("repartidorId") ?: ""
                                RouteListScreen(
                                    repartidorId  = repartidorId,
                                    onRouteClick  = { routeId -> navController.navigate("route_detail/$routeId") },
                                    onCreateRoute = { navController.navigate("create_route/$repartidorId") },
                                    onLogout      = { navController.navigate("login") { popUpTo(0) { inclusive = true } } },
                                    onSettings    = { navController.navigate("settings") },
                                    onEditProfile = { scope.launch { snackbarHostState.showSnackbar("Editar Perfil: Próximamente") } },
                                    onChangePassword = { scope.launch { snackbarHostState.showSnackbar("Cambiar Contraseña: Próximamente") } }
                                )
                            }
                            composable("route_detail/{routeId}") { backStackEntry ->
                                val routeId = backStackEntry.arguments?.getString("routeId") ?: ""
                                RouteDetailScreen(
                                    routeId = routeId,
                                    onBack  = { navController.popBackStack() },
                                    onEdit  = { id -> navController.navigate("create_route/${sessionUserId}/$id") },
                                    onStopClick = { stopId -> navController.navigate("stop_detail/$stopId") }
                                )
                            }
                            composable("stop_detail/{stopId}") { backStackEntry ->
                                val stopId = backStackEntry.arguments?.getString("stopId") ?: ""
                                com.slior.ui.routes.StopDetailScreen(
                                    stopId = stopId,
                                    onBack = { navController.popBackStack() },
                                    onScan = { id -> navController.navigate("scan/$id") }
                                )
                            }
                            composable("scan/{stopId}") { backStackEntry ->
                                val stopId = backStackEntry.arguments?.getString("stopId") ?: ""
                                com.slior.ui.routes.ScanScreen(
                                    stopId = stopId,
                                    onBack = { navController.popBackStack() },
                                    onDeliveryConfirmed = { navController.popBackStack() }
                                )
                            }
                            composable("create_route/{repartidorId}") { backStackEntry ->
                                val repartidorId = backStackEntry.arguments?.getString("repartidorId") ?: ""
                                CreateRouteScreen(
                                    repartidorId  = repartidorId,
                                    onBack        = { navController.popBackStack() },
                                    onRouteCreated = { navController.popBackStack() }
                                )
                            }
                            composable("create_route/{repartidorId}/{routeId}") { backStackEntry ->
                                val repartidorId = backStackEntry.arguments?.getString("repartidorId") ?: ""
                                val routeId = backStackEntry.arguments?.getString("routeId")
                                CreateRouteScreen(
                                    repartidorId  = repartidorId,
                                    routeId       = routeId,
                                    onBack        = { navController.popBackStack() },
                                    onRouteCreated = { navController.popBackStack() }
                                )
                            }
                            composable("settings") {
                                SettingsScreen(
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }
                }

                SnackbarHost(hostState = snackbarHostState)
                
                // Efecto global de desautorización
                LaunchedEffect(unauthenticated) {
                    if (unauthenticated) {
                        scope.launch { snackbarHostState.showSnackbar("Sesión expirada") }
                        navController.navigate("login") { popUpTo(0) { inclusive = true } }
                        authViewModel.consumeUnauthorizedEvent()
                    }
                }
            }
        }
    }
}
