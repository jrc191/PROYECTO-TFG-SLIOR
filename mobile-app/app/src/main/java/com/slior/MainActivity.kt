package com.slior

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.slior.ui.auth.ChangePasswordScreen
import com.slior.ui.auth.ForgotPasswordScreen
import com.slior.ui.auth.LoginScreen
import com.slior.ui.auth.RegisterScreen
import com.slior.ui.components.hardShadow
import com.slior.ui.routes.CreateRouteScreen
import com.slior.ui.routes.RouteDetailScreen
import com.slior.ui.routes.RouteListScreen
import com.slior.ui.routes.ScanScreen
import com.slior.ui.settings.SettingsScreen
import com.slior.ui.theme.*
import dagger.hilt.android.AndroidEntryPoint
import kotlin.system.exitProcess

import android.os.Build
import androidx.compose.runtime.saveable.rememberSaveable
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var notificationHelper: com.slior.util.NotificationHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val context = LocalContext.current
            val authViewModel: com.slior.viewmodel.AuthViewModel = hiltViewModel()
            val themePref by authViewModel.appTheme.collectAsState()
            
            val darkTheme = when (themePref) {
                "light" -> false
                "dark" -> true
                else -> androidx.compose.foundation.isSystemInDarkTheme()
            }

            // Estado de permisos
            var locationGranted by remember {
                mutableStateOf(
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
                )
            }
            var notificationsGranted by remember {
                mutableStateOf(
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
                    } else true
                )
            }
            var showDeniedDialog by remember { mutableStateOf(false) }

            // Launcher para pedir permisos
            val launcher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { perms ->
                locationGranted = perms[Manifest.permission.ACCESS_FINE_LOCATION] == true || 
                                perms[Manifest.permission.ACCESS_COARSE_LOCATION] == true
                
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationsGranted = perms[Manifest.permission.POST_NOTIFICATIONS] == true
                }

                if (!locationGranted) {
                    showDeniedDialog = true
                }
            }

            SliorTheme(darkTheme = darkTheme) {
                if (!locationGranted || (!notificationsGranted && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)) {
                    PermissionRequirementScreen(
                        onRequestPermissions = {
                            val permissions = mutableListOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                permissions.add(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            launcher.launch(permissions.toTypedArray())
                        }
                    )

                    if (showDeniedDialog) {
                        AlertDialog(
                            onDismissRequest = { },
                            containerColor = MaterialTheme.colorScheme.surface,
                            shape = RectangleShape,
                            modifier = Modifier.border(2.dp, BrutalistBlack),
                            title = { Text(stringResource(R.string.perm_denied_title), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface) },
                            text = { Text(stringResource(R.string.perm_denied_desc), fontFamily = SpaceGroteskFamily, color = MaterialTheme.colorScheme.onSurface) },
                            confirmButton = {
                                Button(
                                    onClick = { finishAffinity(); exitProcess(0) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SafetyOrange),
                                    shape = RectangleShape,
                                    modifier = Modifier.border(2.dp, BrutalistBlack)
                                ) {
                                    Text(stringResource(R.string.perm_btn_exit), color = Color.White, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold)
                                }
                            }
                        )
                    }
                } else {
                    MainContent(notificationHelper)
                }
            }
        }
    }
}

@Composable
fun PermissionRequirementScreen(onRequestPermissions: () -> Unit) {
    Box(
        modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background).padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Surface(
                modifier = Modifier.size(80.dp).border(3.dp, BrutalistBlack).hardShadow(4.dp, 4.dp),
                color = NeonGreen
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Lock, null, modifier = Modifier.size(40.dp), tint = BrutalistBlack)
                }
            }

            Text(
                text = stringResource(R.string.perm_required_title),
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = stringResource(R.string.perm_required_desc),
                fontFamily = SpaceGroteskFamily,
                fontSize = 16.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
            )

            Button(
                onClick = onRequestPermissions,
                modifier = Modifier.fillMaxWidth().height(56.dp).hardShadow(4.dp, 4.dp).border(2.dp, BrutalistBlack),
                colors = ButtonDefaults.buttonColors(containerColor = BrutalistBlack),
                shape = RectangleShape
            ) {
                Text(
                    stringResource(R.string.perm_btn_grant),
                    color = Color.White,
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp
                )
            }
        }
    }
}

@Composable
fun MainContent(notificationHelper: com.slior.util.NotificationHelper) {
    val authViewModel: com.slior.viewmodel.AuthViewModel = hiltViewModel()
    val routeViewModel: com.slior.ui.routes.RouteViewModel = hiltViewModel()
    val connViewModel: com.slior.viewmodel.ConnectivityViewModel = hiltViewModel()
    
    val navController = rememberNavController()
    val authState by authViewModel.authState.collectAsState()
    val currentUser by authViewModel.currentUser.collectAsState()
    val isConnected by connViewModel.isConnected.collectAsState()

    // Observar conectividad para mostrar notificaciones
    var lastConnectionState by rememberSaveable { mutableStateOf<Boolean?>(null) }
    
    LaunchedEffect(isConnected, currentUser) {
        val userConsent = currentUser?.consentimientoNotificaciones == true
        android.util.Log.d("SliorNotif", "Conn changed: $isConnected, Last: $lastConnectionState, UserConsent: $userConsent")
        
        if (lastConnectionState != null && lastConnectionState != isConnected) {
            if (userConsent) {
                android.util.Log.d("SliorNotif", "Triggering Connectivity Alert")
                notificationHelper.showConnectivityAlert(isConnected)
            } else {
                android.util.Log.d("SliorNotif", "Notification skipped: No user consent")
            }
        }
        lastConnectionState = isConnected
    }

    // Iniciar rastreo global de ubicación nada más cargar el contenido principal
    LaunchedEffect(Unit) {
        routeViewModel.startLocationTracking()
    }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        when (authState) {
            is com.slior.viewmodel.AuthState.Loading -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = BrutalistBlack)
                }
            }
            else -> {
                NavHost(
                    navController = navController,
                    startDestination = if (authState is com.slior.viewmodel.AuthState.Authenticated && 
                                          (authState as com.slior.viewmodel.AuthState.Authenticated).userId.isNotBlank()) 
                                        "routes" else "login"
                ) {
                    composable("login") {
                        LoginScreen(
                            onLoginSuccess = { navController.navigate("routes") { popUpTo("login") { inclusive = true } } },
                            onGoToRegister = { navController.navigate("register") },
                            onGoToForgotPassword = { navController.navigate("forgot_password") }
                        )
                    }
                    composable("register") {
                        RegisterScreen(
                            onRegisterSuccess = { navController.navigate("login") { popUpTo("register") { inclusive = true } } },
                            onGoToLogin = { navController.popBackStack() }
                        )
                    }
                    composable("forgot_password") {
                        ForgotPasswordScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("routes") {
                        val userId = (authState as? com.slior.viewmodel.AuthState.Authenticated)?.userId ?: ""
                        RouteListScreen(
                            repartidorId = userId,
                            onCreateRoute = { navController.navigate("createRoute") },
                            onRouteClick = { routeId -> navController.navigate("routeDetail/$routeId") },
                            onSettings = { navController.navigate("settings") },
                            onLogout = { authViewModel.logout(); navController.navigate("login") { popUpTo(0) { inclusive = true } } },
                            onEditProfile = { /* TODO */ },
                            onChangePassword = { navController.navigate("change_password") }
                        )
                    }
                    composable("change_password") {
                        ChangePasswordScreen(
                            onBack = { navController.popBackStack() }
                        )
                    }
                    composable("createRoute") {
                        val userId = (authState as? com.slior.viewmodel.AuthState.Authenticated)?.userId ?: ""
                        CreateRouteScreen(
                            repartidorId = userId,
                            onBack = { navController.popBackStack() },
                            onRouteCreated = { navController.popBackStack() }
                        )
                    }
                    composable("routeDetail/{routeId}") { backStackEntry ->
                        val routeId = backStackEntry.arguments?.getString("routeId") ?: ""
                        RouteDetailScreen(
                            routeId = routeId,
                            onBack = { navController.popBackStack() },
                            onEdit = { rId -> navController.navigate("editRoute/$rId") },
                            onStopClick = { stopId -> navController.navigate("scan/$stopId") }
                        )
                    }
                    composable("editRoute/{routeId}") { backStackEntry ->
                        val routeId = backStackEntry.arguments?.getString("routeId") ?: ""
                        val userId = (authState as? com.slior.viewmodel.AuthState.Authenticated)?.userId ?: ""
                        CreateRouteScreen(
                            repartidorId = userId,
                            routeId = routeId,
                            onBack = { navController.popBackStack() },
                            onRouteCreated = { navController.popBackStack() }
                        )
                    }
                    composable("scan/{stopId}") { backStackEntry ->
                        val stopId = backStackEntry.arguments?.getString("stopId") ?: ""
                        ScanScreen(
                            stopId = stopId,
                            onBack = { navController.popBackStack() },
                            onDeliveryConfirmed = { navController.popBackStack() }
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

        // Observar estado de autenticación para redirigir si se pierde la sesión
        LaunchedEffect(authState) {
            val currentRoute = navController.currentBackStackEntry?.destination?.route
            
            // Si el estado es Unauthenticated, redirigimos a login
            // EXCEPCIÓN: No redirigir si ya estamos en pantallas de auth o si el estado es Loading
            if (authState is com.slior.viewmodel.AuthState.Unauthenticated) {
                if (currentRoute == null || 
                    currentRoute == "login" || 
                    currentRoute == "register" || 
                    currentRoute == "forgot_password" || 
                    currentRoute == "change_password") {
                    return@LaunchedEffect
                }
                
                android.util.Log.d("MainActivity", "Estado Unauthenticated detectado: Redirigiendo a Login desde $currentRoute")
                navController.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }
        }

        // Observar eventos de desautorización (token expirado)
        val unauthorizedEvent by authViewModel.unauthorizedEvent.collectAsState()
        LaunchedEffect(unauthorizedEvent) {
            if (unauthorizedEvent) {
                android.util.Log.d("MainActivity", "Detectado 401: Forzando logout")
                // El logout ya se llama en AuthViewModel al detectar el evento, 
                // y el LaunchedEffect(authState) de arriba se encargará de navegar.
                authViewModel.consumeUnauthorizedEvent()
            }
        }
    }
}
