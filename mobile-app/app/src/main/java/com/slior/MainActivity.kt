package com.slior

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.slior.ui.auth.LoginScreen
import com.slior.ui.auth.RegisterScreen
import com.slior.ui.routes.RouteListScreen
import com.slior.ui.theme.SliorTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            SliorTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "login"
                ) {
                    composable("login") {
                        LoginScreen(
                            onLoginSuccess = { repartidorId ->
                                navController.navigate("routes/$repartidorId") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onGoToRegister = {
                                navController.navigate("register")
                            }
                        )
                    }

                    composable("routes/{repartidorId}") { backStackEntry ->
                        val repartidorId = backStackEntry.arguments?.getString("repartidorId") ?: ""
                        RouteListScreen(repartidorId = repartidorId)
                    }

                    composable("register") {
                        RegisterScreen(
                            onRegisterSuccess = { repartidorId ->
                                navController.navigate("routes/$repartidorId") {
                                    popUpTo("login") { inclusive = true }
                                }
                            },
                            onGoToLogin = {
                                navController.popBackStack()
                            }
                        )
                    }
                }
            }
        }
    }
}