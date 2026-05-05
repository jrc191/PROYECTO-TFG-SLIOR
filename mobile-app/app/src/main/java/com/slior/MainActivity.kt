package com.slior

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.slior.ui.auth.LoginScreen
import com.slior.ui.auth.RegisterScreen
import com.slior.ui.routes.CreateRouteScreen
import com.slior.ui.routes.RouteDetailScreen
import com.slior.ui.routes.RouteListScreen
import com.slior.ui.theme.BrutalistBlack
import com.slior.ui.theme.BrutalistWhite
import com.slior.ui.theme.SliorTheme
import com.slior.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SliorTheme {
                val navController = rememberNavController()
                val authViewModel: AuthViewModel = hiltViewModel()
                val sessionUserId by authViewModel.sessionUserId.collectAsStateWithLifecycle()

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

                // Destino inicial según sesión
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
                            onBack  = { navController.popBackStack() }
                        )
                    }

                    composable("create-route/{repartidorId}") { backStackEntry ->
                        val repartidorId = backStackEntry.arguments
                            ?.getString("repartidorId") ?: ""
                        CreateRouteScreen(
                            repartidorId  = repartidorId,
                            onBack        = { navController.popBackStack() },
                            onRouteCreated = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }
}