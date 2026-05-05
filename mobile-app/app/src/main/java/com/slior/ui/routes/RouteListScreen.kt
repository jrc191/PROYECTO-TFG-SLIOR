package com.slior.ui.routes

import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.slior.data.local.entity.RouteEntity
import com.slior.ui.components.hardShadow
import com.slior.ui.theme.*
import com.slior.viewmodel.AuthViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.animation.core.tween

@Composable
fun RouteListScreen(
    repartidorId: String,
    onRouteClick: (String) -> Unit,
    onCreateRoute: () -> Unit,
    onLogout: () -> Unit,
    viewModel: RouteViewModel = hiltViewModel(),
    authViewModel: AuthViewModel = hiltViewModel()
) {
    val state      by viewModel.listState.collectAsStateWithLifecycle()
    val drawerOpen by viewModel.drawerOpen.collectAsStateWithLifecycle()

    LaunchedEffect(repartidorId) {
        viewModel.loadRoutes(repartidorId)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // ── Pantalla principal ─────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BrutalistWhite)
        ) {
            // TopAppBar
            RouteListTopBar(
                onMenuClick    = { viewModel.openDrawer(RouteViewModel.DrawerType.MENU) },
                onProfileClick = { viewModel.openDrawer(RouteViewModel.DrawerType.PROFILE) }
            )

            Box(modifier = Modifier.weight(1f)) {
                when (state) {
                    is RouteListState.Loading -> RouteListLoading()
                    is RouteListState.Error   -> RouteListOffline(
                        message = (state as RouteListState.Error).message
                    )
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
                                .border(2.dp, BrutalistBlack)
                                .background(BrutalistBlack)
                                .clickable { onCreateRoute() }
                        else
                            Modifier
                                .border(2.dp, BrutalistBlack)
                                .background(Color(0xFFB0B0B0))
                    )
                    .padding(horizontal = 20.dp)
                    .height(64.dp),
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Add, null, tint = BrutalistWhite, modifier = Modifier.size(24.dp))
                Text(
                    text       = "NUEVA RUTA",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 18.sp,
                    letterSpacing = 1.sp,
                    color      = BrutalistWhite
                )
            }
        }

        // ── Overlay oscuro ─────────────────────────────────────────────
        AnimatedVisibility(
            visible = drawerOpen != null,
            enter   = fadeIn(tween(200)),
            exit    = fadeOut(tween(200)),
            modifier = Modifier.zIndex(2f)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0x99000000))
                    .clickable { viewModel.closeDrawer() }
            )
        }

        // ── Drawer izquierdo: MENÚ ─────────────────────────────────────
        AnimatedVisibility(
            visible  = drawerOpen == RouteViewModel.DrawerType.MENU,
            enter    = slideInHorizontally(tween(250)) { -it },
            exit     = slideOutHorizontally(tween(250)) { -it },
            modifier = Modifier.zIndex(3f)
        ) {
            MenuDrawer(
                onClose = { viewModel.closeDrawer() }
            )
        }

        // ── Drawer derecho: PERFIL ─────────────────────────────────────
        AnimatedVisibility(
            visible  = drawerOpen == RouteViewModel.DrawerType.PROFILE,
            enter    = slideInHorizontally(tween(250)) { it },
            exit     = slideOutHorizontally(tween(250)) { it },
            modifier = Modifier
                .align(Alignment.TopEnd)
                .zIndex(3f)
        ) {
            ProfileDrawer(
                onClose  = { viewModel.closeDrawer() },
                onLogout = {
                    viewModel.closeDrawer()
                    authViewModel.logout()
                    onLogout()
                }
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// TopAppBar
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun RouteListTopBar(
    onMenuClick: () -> Unit,
    onProfileClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(BrutalistWhite)
            .drawBehind {
                drawLine(BrutalistBlack, Offset(0f, size.height), Offset(size.width, size.height), 2.dp.toPx())
            }
            .padding(horizontal = 16.dp)
            .height(64.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Hamburguesa
        Column(
            modifier = Modifier
                .size(48.dp)
                .clickable { onMenuClick() },
            verticalArrangement   = Arrangement.Center,
            horizontalAlignment   = Alignment.CenterHorizontally
        ) {
            repeat(3) {
                Box(Modifier.width(22.dp).height(2.dp).background(BrutalistBlack))
                if (it < 2) Spacer(Modifier.height(5.dp))
            }
        }

        Text(
            text          = "MIS RUTAS",
            fontFamily    = SpaceGroteskFamily,
            fontWeight    = FontWeight.Bold,
            fontSize      = 20.sp,
            letterSpacing = 1.sp,
            color         = BrutalistBlack
        )

        // Avatar
        Box(
            modifier = Modifier
                .size(48.dp)
                .border(2.dp, BrutalistBlack)
                .background(Color(0xFFF4F4F5))
                .clickable { onProfileClick() },
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Default.Person, null, tint = BrutalistBlack, modifier = Modifier.size(28.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Drawer izquierdo — MENÚ
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun MenuDrawer(onClose: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(BrutalistWhite)
            .drawBehind {
                drawLine(BrutalistBlack, Offset(size.width, 0f), Offset(size.width, size.height), 2.dp.toPx())
            }
    ) {
        // Cabecera
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(BrutalistBlack)
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Column {
                Text(
                    text       = "SLIOR",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Black,
                    fontSize   = 32.sp,
                    color      = NeonGreen
                )
                Text(
                    text       = "SISTEMA DE RUTAS",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 11.sp,
                    letterSpacing = 2.sp,
                    color      = BrutalistWhite
                )
            }
        }

        Spacer(Modifier.height(8.dp))

        // Items del menú
        MenuDrawerItem(
            icon  = Icons.Default.Route,
            label = "MIS RUTAS",
            badge = null,
            onClick = { onClose() }
        )
        MenuDrawerItem(
            icon  = Icons.Default.BarChart,
            label = "ESTADÍSTICAS",
            badge = "PRÓX.",
            onClick = { /* futuro */ }
        )
        MenuDrawerItem(
            icon  = Icons.Default.Map,
            label = "MAPA GENERAL",
            badge = "PRÓX.",
            onClick = { /* futuro */ }
        )
        MenuDrawerItem(
            icon  = Icons.Default.Inventory2,
            label = "PAQUETES",
            badge = "PRÓX.",
            onClick = { /* futuro */ }
        )
        MenuDrawerItem(
            icon  = Icons.Default.Settings,
            label = "AJUSTES",
            badge = null,
            onClick = { /* futuro */ }
        )

        Spacer(Modifier.weight(1f))

        // Versión al pie
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawLine(BrutalistBlack, Offset(0f, 0f), Offset(size.width, 0f), 1.dp.toPx())
                }
                .padding(20.dp)
        ) {
            Text(
                text       = "V 1.0.0 — TFG DAM 2026",
                fontFamily = SpaceGroteskFamily,
                fontSize   = 11.sp,
                letterSpacing = 1.sp,
                color      = Color(0xFF9E9E9E)
            )
        }
    }
}

@Composable
private fun MenuDrawerItem(
    icon: ImageVector,
    label: String,
    badge: String?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(Color(0xFFE4E4E7), Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
            }
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(icon, null, tint = BrutalistBlack, modifier = Modifier.size(22.dp))
        Text(
            text          = label,
            fontFamily    = SpaceGroteskFamily,
            fontWeight    = FontWeight.Bold,
            fontSize      = 15.sp,
            letterSpacing = 0.5.sp,
            color         = BrutalistBlack,
            modifier      = Modifier.weight(1f)
        )
        if (badge != null) {
            Box(
                modifier = Modifier
                    .border(1.dp, BrutalistBlack)
                    .background(Color(0xFFF4F4F5))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text       = badge,
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 10.sp,
                    color      = Color(0xFF71717A)
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Drawer derecho — PERFIL
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ProfileDrawer(
    onClose: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(BrutalistWhite)
            .drawBehind {
                drawLine(BrutalistBlack, Offset(0f, 0f), Offset(0f, size.height), 2.dp.toPx())
            }
    ) {
        // Cabecera perfil
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(BrutalistBlack)
                .padding(20.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .border(2.dp, NeonGreen)
                    .background(Color(0xFF1A1A1A)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Person, null, tint = NeonGreen, modifier = Modifier.size(36.dp))
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text       = "REPARTIDOR",
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Black,
                fontSize   = 18.sp,
                color      = BrutalistWhite
            )
            Text(
                text       = "Perfil activo",
                fontFamily = SpaceGroteskFamily,
                fontSize   = 12.sp,
                color      = Color(0xFF9E9E9E)
            )
        }

        Spacer(Modifier.height(8.dp))

        // Opciones
        ProfileDrawerItem(
            icon    = Icons.Default.Person,
            label   = "EDITAR PERFIL",
            sublabel = "Nombre y datos",
            badge   = "PRÓX.",
            color   = BrutalistBlack,
            onClick = { }
        )
        ProfileDrawerItem(
            icon     = Icons.Default.Lock,
            label    = "CAMBIAR CONTRASEÑA",
            sublabel = "Seguridad de la cuenta",
            badge    = "PRÓX.",
            color    = BrutalistBlack,
            onClick  = { }
        )
        ProfileDrawerItem(
            icon     = Icons.Default.Notifications,
            label    = "NOTIFICACIONES",
            sublabel = "Preferencias de avisos",
            badge    = "PRÓX.",
            color    = BrutalistBlack,
            onClick  = { }
        )

        Spacer(Modifier.weight(1f))

        // Cerrar sesión
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .drawBehind {
                    drawLine(BrutalistBlack, Offset(0f, 0f), Offset(size.width, 0f), 2.dp.toPx())
                }
                .clickable { onLogout() }
                .padding(20.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(Icons.Default.Logout, null, tint = SafetyOrange, modifier = Modifier.size(22.dp))
            Column {
                Text(
                    text       = "CERRAR SESIÓN",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Black,
                    fontSize   = 15.sp,
                    color      = SafetyOrange
                )
                Text(
                    text       = "Salir de la cuenta actual",
                    fontFamily = SpaceGroteskFamily,
                    fontSize   = 11.sp,
                    color      = Color(0xFF9E9E9E)
                )
            }
        }
    }
}

@Composable
private fun ProfileDrawerItem(
    icon: ImageVector,
    label: String,
    sublabel: String,
    badge: String?,
    color: Color,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                drawLine(Color(0xFFE4E4E7), Offset(0f, size.height), Offset(size.width, size.height), 1.dp.toPx())
            }
            .clickable { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = label,
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize   = 14.sp,
                color      = color
            )
            Text(
                text       = sublabel,
                fontFamily = SpaceGroteskFamily,
                fontSize   = 11.sp,
                color      = Color(0xFF9E9E9E)
            )
        }
        if (badge != null) {
            Box(
                modifier = Modifier
                    .border(1.dp, BrutalistBlack)
                    .background(Color(0xFFF4F4F5))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(badge, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 10.sp, color = Color(0xFF71717A))
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// Estados de la lista (sin cambios)
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun RouteListLoading() {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) { items(4) { SkeletonRouteCard() } }
}

@Composable
private fun SkeletonRouteCard() {
    val transition = androidx.compose.animation.core.rememberInfiniteTransition(label = "shimmer")
    val shimmerX by transition.animateFloat(
        initialValue = -200f, targetValue = 200f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(1400, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ), label = "shimmerX"
    )
    val shimmerBrush = androidx.compose.ui.graphics.Brush.horizontalGradient(
        colors = listOf(Color(0xFFF4F4F5), Color(0xFFE4E4E7), Color(0xFFF4F4F5)),
        startX = shimmerX, endX = shimmerX + 400f
    )
    Box(
        modifier = Modifier.fillMaxWidth().height(140.dp)
            .hardShadow().border(2.dp, BrutalistBlack).background(shimmerBrush).padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Box(Modifier.fillMaxWidth(0.6f).height(20.dp).background(Color(0xFFD4D4D8)))
                Box(Modifier.width(60.dp).height(18.dp).background(Color(0xFFD4D4D8)))
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                Box(Modifier.width(80.dp).height(14.dp).background(Color(0xFFD4D4D8)))
                Box(Modifier.width(100.dp).height(26.dp).background(Color(0xFFD4D4D8)))
            }
        }
    }
}

@Composable
private fun RouteListOffline(message: String) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().background(SafetyOrange)
                .drawBehind { drawLine(BrutalistBlack, Offset(0f, size.height), Offset(size.width, size.height), 2.dp.toPx()) }
                .padding(horizontal = 16.dp).height(64.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text("⚠", fontSize = 22.sp, color = BrutalistBlack, modifier = Modifier.padding(end = 10.dp))
            Text("SIN CONEXIÓN — MODO LOCAL", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, letterSpacing = 1.sp, color = BrutalistBlack)
        }
        Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("SIN DATOS EN CACHÉ", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 18.sp, color = BrutalistBlack)
                Spacer(Modifier.height(8.dp))
                Text(message, fontFamily = SpaceGroteskFamily, fontSize = 13.sp, color = Color(0xFF757575))
            }
        }
    }
}

@Composable
private fun RouteListSuccess(routes: List<RouteEntity>, onRouteClick: (String) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 16.dp, bottom = 96.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) { items(routes) { RouteCard(route = it, onClick = { onRouteClick(it.id) }) } }
}

@Composable
private fun RouteCard(route: RouteEntity, onClick: () -> Unit) {
    val statusColor = when (route.status.uppercase()) {
        "EN_CURSO"    -> SafetyOrange
        "PLANIFICADA" -> Color(0xFFFFD700)
        "COMPLETADA"  -> NeonGreen
        "CANCELADA"   -> Color(0xFFEF9A9A)
        else          -> Color(0xFFF4F4F5)
    }
    val statusLabel = when (route.status.uppercase()) {
        "EN_CURSO"    -> "En_Curso"
        "PLANIFICADA" -> "Planificada"
        "COMPLETADA"  -> "Completada"
        "CANCELADA"   -> "Cancelada"
        else          -> route.status
    }
    val syncIcon  = if (route.syncStatus == "PENDING") "☁↑" else "☁✓"
    val syncColor = if (route.syncStatus == "PENDING") Color(0xFFFFD700) else BrutalistBlack

    Box(
        modifier = Modifier.fillMaxWidth().height(140.dp)
            .hardShadow().border(2.dp, BrutalistBlack).background(BrutalistWhite)
            .clickable { onClick() }.padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize(), verticalArrangement = Arrangement.SpaceBetween) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                Column {
                    Box(Modifier.border(2.dp, BrutalistBlack).background(statusColor).padding(horizontal = 10.dp, vertical = 4.dp)) {
                        Text(statusLabel.uppercase(), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp, color = BrutalistBlack)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(route.nombre.uppercase(), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 18.sp, letterSpacing = (-0.5).sp, color = BrutalistBlack)
                }
                Text(if (route.tiempoEstimado != null) "${route.tiempoEstimado} MIN" else "-- MIN", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = BrutalistBlack)
            }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(route.fechaPlanificada, fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Medium, fontSize = 13.sp, color = Color(0xFF71717A))
                    Text(syncIcon, fontSize = 16.sp, color = syncColor)
                }
                route.distanciaTotal?.let {
                    Text("${"%.1f".format(it)} KM", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = BrutalistBlack)
                }
            }
        }
    }
}

@Composable
private fun RouteListEmpty() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("SIN RUTAS", fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 24.sp, letterSpacing = 2.sp, color = BrutalistBlack)
            Spacer(Modifier.height(8.dp))
            Text("Crea tu primera ruta con el botón +", fontFamily = SpaceGroteskFamily, fontSize = 14.sp, color = Color(0xFF71717A))
        }
    }
}