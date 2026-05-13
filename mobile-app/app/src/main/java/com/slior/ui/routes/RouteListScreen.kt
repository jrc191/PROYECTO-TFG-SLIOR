package com.slior.ui.routes

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.slior.R
import com.slior.data.local.entity.RouteEntity
import com.slior.ui.components.*
import com.slior.ui.theme.*
import com.slior.viewmodel.AuthViewModel
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.ExperimentalMaterial3Api

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RouteListScreen(
    repartidorId: String,
    onRouteClick: (String) -> Unit,
    onCreateRoute: () -> Unit,
    onLogout: () -> Unit,
    onSettings: () -> Unit,
    onEditProfile: () -> Unit,
    onChangePassword: () -> Unit,
    viewModel: RouteViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel(),
    connViewModel: com.slior.viewmodel.ConnectivityViewModel = hiltViewModel()
) {
    val state      by viewModel.listState.collectAsStateWithLifecycle()
    val drawerOpen by viewModel.drawerOpen.collectAsStateWithLifecycle()
    val isConnected by connViewModel.isConnected.collectAsStateWithLifecycle()
    val currentUser by viewModel.currentUser.collectAsStateWithLifecycle()
    
    val isRefreshing = state is RouteListState.Loading
    val onSurface = MaterialTheme.colorScheme.onSurface
    val surface = MaterialTheme.colorScheme.surface
    val backgroundColor = MaterialTheme.colorScheme.background

    LaunchedEffect(repartidorId) {
        viewModel.loadRoutes(repartidorId)
    }

    Box(modifier = Modifier.fillMaxSize().background(backgroundColor)) {

        // ── Pantalla principal ─────────────────────────────────────────
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // TopAppBar
            RouteListTopBar(
                onMenuClick    = { viewModel.openDrawer(RouteViewModel.DrawerType.MENU) },
                onProfileClick = { viewModel.openDrawer(RouteViewModel.DrawerType.PROFILE) }
            )

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { 
                    viewModel.loadRoutes(repartidorId)
                    authViewModel.checkServerConnectivity()
                },
                modifier = Modifier.weight(1f)
            ) {
                when (state) {
                    is RouteListState.Loading -> if (isRefreshing && (state as? RouteListState.Success)?.routes?.isNotEmpty() == true) {
                        RouteListSuccess(routes = (state as RouteListState.Success).routes, onRouteClick = onRouteClick)
                    } else {
                        RouteListLoading()
                    }
                    is RouteListState.Error   -> {
                        val currentRoutes = (state as? RouteListState.Success)?.routes ?: emptyList()
                        if (currentRoutes.isEmpty() && !isConnected) {
                            RouteListOffline(message = (state as RouteListState.Error).message)
                        } else {
                            RouteListError(message = (state as RouteListState.Error).message)
                        }
                    }
                    is RouteListState.Success -> {
                        val routes = (state as RouteListState.Success).routes
                        
                        if (routes.isEmpty()) RouteListEmpty()
                        else RouteListSuccess(routes = routes, onRouteClick = onRouteClick)
                    }
                }
            }
        }

        // FAB
        val isLoading = state is RouteListState.Loading
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
                .zIndex(1f)
        ) {
            Row(
                modifier = Modifier
                    .then(
                        if (!isLoading)
                            Modifier
                                .hardShadow(offsetX = 8.dp, offsetY = 8.dp, color = SafetyOrange)
                                .border(2.dp, onSurface)
                                .background(onSurface)
                                .clickable { onCreateRoute() }
                        else
                            Modifier
                                .border(2.dp, onSurface)
                                .background(onSurface.copy(alpha = 0.4f))
                    )
                    .padding(horizontal = 20.dp)
                    .height(64.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Add, null, tint = surface, modifier = Modifier.size(24.dp))
                Text(
                    text       = stringResource(R.string.btn_new_route),
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 18.sp,
                    letterSpacing = 1.sp,
                    color      = surface
                )
            }
        }

        // ── Overlays y Drawers ──────────────────────────────────────────
        AnimatedVisibility(visible = drawerOpen != null, enter = fadeIn(tween(200)), exit = fadeOut(tween(200)), modifier = Modifier.zIndex(2f)) {
            Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.6f)).clickable { viewModel.closeDrawer() })
        }

        AnimatedVisibility(visible = drawerOpen == RouteViewModel.DrawerType.MENU, enter = slideInHorizontally(tween(250)) { -it }, exit = slideOutHorizontally(tween(250)) { -it }, modifier = Modifier.zIndex(3f)) {
            MenuDrawer(onClose = { viewModel.closeDrawer() }, onSettings = onSettings)
        }

        AnimatedVisibility(visible = drawerOpen == RouteViewModel.DrawerType.PROFILE, enter = slideInHorizontally(tween(250)) { it }, exit = slideOutHorizontally(tween(250)) { it }, modifier = Modifier.align(Alignment.TopEnd).zIndex(3f)) {
            ProfileDrawer(
                user = currentUser,
                onClose = { viewModel.closeDrawer() }, 
                onEditProfile = { viewModel.closeDrawer(); onEditProfile() },
                onChangePassword = { viewModel.closeDrawer(); onChangePassword() },
                onLogout = { viewModel.closeDrawer(); authViewModel.logout(); onLogout() },
                onNotificationToggle = { enabled -> currentUser?.id?.let { authViewModel.updateNotifications(it, enabled) } }
            )
        }
    }
}

@Composable
private fun RouteListTopBar(onMenuClick: () -> Unit, onProfileClick: () -> Unit) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val surface = MaterialTheme.colorScheme.surface
    
    Row(modifier = Modifier.fillMaxWidth().background(surface).drawBehind { drawLine(onSurface, Offset(0f, size.height), Offset(size.width, size.height), 2.dp.toPx()) }.padding(horizontal = 16.dp).height(64.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        Column(modifier = Modifier.size(48.dp).clickable { onMenuClick() }, verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
            repeat(3) { Box(Modifier.width(22.dp).height(2.dp).background(onSurface)); if (it < 2) Spacer(Modifier.height(5.dp)) }
        }
        Text(text = stringResource(R.string.title_routes), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 20.sp, letterSpacing = 1.sp, color = onSurface)
        Box(modifier = Modifier.size(48.dp).border(2.dp, onSurface).background(onSurface.copy(alpha = 0.05f)).clickable { onProfileClick() }, contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Person, null, tint = onSurface, modifier = Modifier.size(28.dp))
        }
    }
}

@Composable
private fun MenuDrawer(onClose: () -> Unit, onSettings: () -> Unit) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val surface = MaterialTheme.colorScheme.surface
    
    Column(modifier = Modifier.fillMaxHeight().width(300.dp).background(surface).drawBehind { drawLine(onSurface, Offset(size.width, 0f), Offset(size.width, size.height), 2.dp.toPx()) }) {
        // Cabecera: SIEMPRE NEGRA por diseño de alta seguridad
        Box(Modifier.fillMaxWidth().background(BrutalistBlack).drawBehind { drawLine(onSurface, Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx()) }.padding(horizontal = 20.dp, vertical = 24.dp)) {
            Column {
                Text(text = stringResource(R.string.app_name), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 32.sp, color = NeonGreen)
                Text(text = stringResource(R.string.menu_system_status), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 2.sp, color = BrutalistWhite)
            }
        }
        
        LazyColumn(modifier = Modifier.weight(1f)) {
            item { MenuDrawerItem(Icons.Default.Route, stringResource(R.string.menu_my_routes), null, { onClose() }) }
            item { MenuDrawerItem(Icons.Default.BarChart, stringResource(R.string.menu_statistics), stringResource(R.string.menu_badge_soon), { }) }
            item { MenuDrawerItem(Icons.Default.Map, stringResource(R.string.menu_general_map), stringResource(R.string.menu_badge_soon), { }) }
            item { MenuDrawerItem(Icons.Default.Inventory2, stringResource(R.string.menu_packages), stringResource(R.string.menu_badge_soon), { }) }
            item { MenuDrawerItem(Icons.Default.Settings, stringResource(R.string.menu_settings), null, { onClose(); onSettings() }) }
        }

        Box(Modifier.fillMaxWidth().drawBehind { drawLine(onSurface, Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx()) }.padding(20.dp)) {
            Text(text = stringResource(R.string.menu_version_info), fontFamily = SpaceGroteskFamily, fontSize = 11.sp, letterSpacing = 1.sp, color = onSurface.copy(alpha = 0.5f))
        }
    }
}

@Composable
private fun MenuDrawerItem(icon: ImageVector, label: String, badge: String?, onClick: () -> Unit) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    Row(modifier = Modifier.fillMaxWidth().drawBehind { drawLine(onSurface.copy(alpha = 0.1f), Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx()) }.clickable { onClick() }.padding(horizontal = 20.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Icon(icon, null, tint = onSurface, modifier = Modifier.size(22.dp))
        Text(text = label, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 0.5.sp, color = onSurface, modifier = Modifier.weight(1f))
        if (badge != null) {
            Box(Modifier.border(1.dp, onSurface).background(onSurface.copy(alpha = 0.05f)).padding(horizontal = 6.dp, vertical = 2.dp)) {
                Text(badge, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = onSurface.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
private fun ProfileDrawer(
    user: com.slior.data.local.entity.UserEntity?,
    onClose: () -> Unit, 
    onEditProfile: () -> Unit,
    onChangePassword: () -> Unit,
    onLogout: () -> Unit,
    onNotificationToggle: (Boolean) -> Unit
) {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
    val onSurface = MaterialTheme.colorScheme.onSurface
    val surface = MaterialTheme.colorScheme.surface

    Column(modifier = Modifier.fillMaxHeight().width(if (isLandscape) 360.dp else 280.dp).background(surface).drawBehind { drawLine(onSurface, Offset(0f, 0f), Offset(0f, size.height), 2.dp.toPx()) }) {
        // Cabecera: SIEMPRE NEGRA por diseño de alta seguridad
        Column(Modifier.fillMaxWidth().background(BrutalistBlack).padding(if (isLandscape) 16.dp else 20.dp)) {
            if (isLandscape) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(Modifier.size(56.dp).border(2.dp, NeonGreen).background(Color.Black.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) { 
                        Icon(Icons.Default.Person, null, tint = NeonGreen, modifier = Modifier.size(32.dp)) 
                    }
                    Column {
                        Text(text = user?.nombre?.uppercase() ?: stringResource(R.string.menu_courier), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 16.sp, color = BrutalistWhite, maxLines = 1)
                        Text(text = user?.email ?: stringResource(R.string.menu_active_profile), fontFamily = SpaceGroteskFamily, fontSize = 11.sp, color = BrutalistWhite.copy(alpha = 0.7f), maxLines = 1)
                    }
                }
            } else {
                Box(Modifier.size(64.dp).border(2.dp, NeonGreen).background(Color.Black.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) { 
                    Icon(Icons.Default.Person, null, tint = NeonGreen, modifier = Modifier.size(36.dp)) 
                }
                Spacer(Modifier.height(12.dp))
                Text(text = user?.nombre?.uppercase() ?: stringResource(R.string.menu_courier), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 18.sp, color = BrutalistWhite)
                Text(text = user?.email ?: stringResource(R.string.menu_active_profile), fontFamily = SpaceGroteskFamily, fontSize = 12.sp, color = BrutalistWhite.copy(alpha = 0.7f))
            }
        }
        
        LazyColumn(modifier = Modifier.weight(1f)) {
            item { ProfileDrawerItem(Icons.Default.Person, stringResource(R.string.menu_edit_profile), stringResource(R.string.menu_edit_profile_desc), null, onSurface, onEditProfile) }
            item { ProfileDrawerItem(Icons.Default.Lock, stringResource(R.string.menu_change_password), stringResource(R.string.menu_change_password_desc), null, onSurface, onChangePassword) }
            item { 
                NotificationToggleItem(
                    enabled = user?.consentimientoNotificaciones ?: false,
                    onToggle = onNotificationToggle
                )
            }
        }

        Row(Modifier.fillMaxWidth().drawBehind { drawLine(onSurface, Offset(0f, 0f), Offset(size.width, 0f), 2.dp.toPx()) }.clickable { onLogout() }.padding(20.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Icon(Icons.AutoMirrored.Filled.Logout, null, tint = SafetyOrange, modifier = Modifier.size(22.dp))
            Column {
                Text(text = stringResource(R.string.menu_logout), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 15.sp, color = SafetyOrange)
                Text(text = stringResource(R.string.menu_logout_desc), fontFamily = SpaceGroteskFamily, fontSize = 11.sp, color = onSurface.copy(alpha = 0.5f))
            }
        }
    }
}

@Composable
private fun ProfileDrawerItem(icon: ImageVector, label: String, sublabel: String, badge: String?, color: Color, onClick: () -> Unit) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    Row(modifier = Modifier.fillMaxWidth().drawBehind { drawLine(onSurface.copy(alpha = 0.1f), Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx()) }.clickable { onClick() }.padding(horizontal = 20.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(label, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = color)
            Text(sublabel, fontFamily = SpaceGroteskFamily, fontSize = 11.sp, color = onSurface.copy(alpha = 0.5f))
        }
        if (badge != null) {
            Box(Modifier.border(1.dp, onSurface).background(onSurface.copy(alpha = 0.05f)).padding(horizontal = 6.dp, vertical = 2.dp)) { Text(badge, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = onSurface.copy(alpha = 0.6f)) }
        }
    }
}

@Composable
private fun NotificationToggleItem(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    Row(modifier = Modifier.fillMaxWidth().drawBehind { drawLine(onSurface.copy(alpha = 0.1f), Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx()) }.padding(horizontal = 20.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Icon(Icons.Default.Notifications, null, tint = onSurface, modifier = Modifier.size(22.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.menu_notifications), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = onSurface)
            Text(stringResource(R.string.menu_notifications_desc), fontFamily = SpaceGroteskFamily, fontSize = 11.sp, color = onSurface.copy(alpha = 0.5f))
        }
        androidx.compose.material3.Switch(
            checked = enabled,
            onCheckedChange = onToggle,
            colors = androidx.compose.material3.SwitchDefaults.colors(
                checkedThumbColor = NeonGreen,
                checkedTrackColor = BrutalistBlack,
                uncheckedThumbColor = onSurface.copy(alpha = 0.4f),
                uncheckedTrackColor = onSurface.copy(alpha = 0.1f)
            )
        )
    }
}

@Composable
private fun RouteListLoading() {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { items(4) { SkeletonRouteCard() } }
}

@Composable
private fun SkeletonRouteCard() {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val transition = rememberInfiniteTransition(label = "shimmer")
    val shimmerX by transition.animateFloat(initialValue = -200f, targetValue = 200f, animationSpec = infiniteRepeatable(animation = tween(1400, easing = androidx.compose.animation.core.LinearEasing), repeatMode = RepeatMode.Restart), label = "shimmerX")
    val shimmerBrush = androidx.compose.ui.graphics.Brush.horizontalGradient(colors = listOf(onSurface.copy(alpha = 0.05f), onSurface.copy(alpha = 0.15f), onSurface.copy(alpha = 0.05f)), startX = shimmerX, endX = shimmerX + 400f)
    Box(Modifier.fillMaxWidth().height(140.dp).sliorShadow().border(2.dp, onSurface).background(shimmerBrush).padding(16.dp)) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Box(Modifier.fillMaxWidth(0.6f).height(20.dp).background(onSurface.copy(alpha = 0.2f))); Box(Modifier.width(60.dp).height(18.dp).background(onSurface.copy(alpha = 0.2f))) }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) { Box(Modifier.width(80.dp).height(14.dp).background(onSurface.copy(alpha = 0.2f))); Box(Modifier.width(100.dp).height(26.dp).background(onSurface.copy(alpha = 0.2f))) }
        }
    }
}

@Composable
private fun RouteListOffline(message: String) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    Column(modifier = Modifier.fillMaxSize()) {
        Row(modifier = Modifier.fillMaxWidth().background(SafetyOrange).drawBehind { drawLine(onSurface, Offset(0f, size.height), Offset(size.width, size.height), 2.dp.toPx()) }.padding(horizontal = 16.dp).height(64.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Text("⚠", fontSize = 22.sp, color = Color.Black, modifier = Modifier.padding(end = 10.dp))
            Text(stringResource(R.string.banner_offline_local), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 1.sp, color = Color.Black)
        }
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(stringResource(R.string.label_no_cache), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 18.sp, color = onSurface)
                Spacer(Modifier.height(8.dp))
                Text(message, fontFamily = SpaceGroteskFamily, fontSize = 13.sp, color = onSurface.copy(alpha = 0.6f))
            }
        }
    }
}

@Composable
private fun RouteListSuccess(routes: List<RouteEntity>, onRouteClick: (String) -> Unit) {
    LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) { items(routes) { RouteCard(route = it, onClick = { onRouteClick(it.id) }) } }
}

@Composable
private fun RouteCard(route: RouteEntity, onClick: () -> Unit) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val surface = MaterialTheme.colorScheme.surface
    
    val statusColor = when (route.status.uppercase()) { 
        "EN_CURSO"    -> SafetyOrange 
        "PLANIFICADA" -> Color(0xFFFFD700) 
        "COMPLETADA"  -> NeonGreen 
        "CANCELADA"   -> Color(0xFFEF9A9A) 
        else          -> onSurface.copy(alpha = 0.1f)
    }
    
    val statusLabel = when (route.status.uppercase()) { 
        "EN_CURSO"    -> stringResource(R.string.status_in_progress)
        "PLANIFICADA" -> stringResource(R.string.status_planned)
        "COMPLETADA"  -> stringResource(R.string.status_completed)
        "CANCELADA"   -> stringResource(R.string.status_cancelled)
        else          -> route.status 
    }
    
    val syncIcon = if (route.syncStatus == "PENDING") "☁↑" else "☁✓"
    val syncColor = if (route.syncStatus == "PENDING") Color(0xFFFFD700) else onSurface

    Box(modifier = Modifier.fillMaxWidth().height(140.dp).sliorShadow().border(2.dp, onSurface).background(surface).clickable { onClick() }.padding(16.dp)) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    Box(Modifier.border(2.dp, onSurface).background(statusColor).padding(horizontal = 10.dp, vertical = 4.dp)) { 
                        Text(statusLabel.uppercase(), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp, color = Color.Black) 
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(route.nombre.uppercase(), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp, letterSpacing = (-0.5).sp, color = onSurface)
                }
                Text(text = if (route.tiempoEstimado != null) stringResource(R.string.format_minutes, route.tiempoEstimado) else "-- MIN", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = onSurface)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) { Text(route.fechaPlanificada, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = onSurface.copy(alpha = 0.6f)); Text(syncIcon, fontSize = 16.sp, color = syncColor) }
                route.distanciaTotal?.let { Text(text = stringResource(R.string.format_km, it), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = onSurface) }
            }
        }
    }
}

@Composable
private fun RouteListError(message: String) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.state_error_loading), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 18.sp, color = SafetyOrange)
            Spacer(Modifier.height(8.dp))
            Text(message, fontFamily = SpaceGroteskFamily, fontSize = 13.sp, color = onSurface.copy(alpha = 0.6f))
            Spacer(Modifier.height(16.dp))
            Text(stringResource(R.string.state_pull_to_refresh), fontFamily = SpaceGroteskFamily, fontSize = 12.sp, color = onSurface)
        }
    }
}

@Composable
private fun RouteListEmpty() {
    val onSurface = MaterialTheme.colorScheme.onSurface
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.state_no_routes), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 24.sp, letterSpacing = 2.sp, color = onSurface)
            Spacer(Modifier.height(8.dp))
            Text(stringResource(R.string.state_create_first), fontFamily = SpaceGroteskFamily, fontSize = 14.sp, color = onSurface.copy(alpha = 0.6f))
        }
    }
}
