package com.slior.ui.auth

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.slior.R
import com.slior.data.local.entity.VehicleType
import com.slior.ui.components.*
import com.slior.ui.theme.*
import com.slior.util.Validators
import com.slior.viewmodel.AuthViewModel

/**
 * Pantalla de Registro de SLIOR.
 * Soporta temas dinámicos (Claro/Oscuro) y modo Landscape.
 */
@Composable
fun RegisterScreen(
    onRegisterSuccess: (repartidorId: String) -> Unit,
    onGoToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val loginState   by viewModel.loginState.collectAsStateWithLifecycle()
    val serverStatus by viewModel.serverStatus.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    // Colores del tema
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    var nombre by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var selectedVehicleType by rememberSaveable { mutableStateOf(VehicleType.VAN) }

    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            onRegisterSuccess((loginState as LoginState.Success).repartidorId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        //  TopAppBar
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .background(surfaceColor)
                .drawBehind {
                    drawLine(
                        color       = onSurfaceColor,
                        start       = Offset(0f, size.height),
                        end         = Offset(size.width, size.height),
                        strokeWidth = 4.dp.toPx()
                    )
                }
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier         = Modifier
                    .size(48.dp)
                    .hardShadow(offsetX = 2.dp, offsetY = 2.dp, color = onSurfaceColor)
                    .border(SliorDesignTokens.BorderWidth, onSurfaceColor, RectangleShape)
                    .background(surfaceColor)
                    .clickable { onGoToLogin() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.btn_back),
                    tint               = onSurfaceColor
                )
            }
            Spacer(Modifier.width(16.dp))
            Text(
                text          = stringResource(R.string.label_new_courier),
                fontFamily    = SpaceGroteskFamily,
                fontWeight    = FontWeight.Bold,
                fontSize      = 20.sp,
                letterSpacing = (-0.5).sp,
                color         = onSurfaceColor
            )
        }

        SliorAccentBar()

        if (isLandscape) {
            // Diseño para LANDSCAPE
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Columna izquierda: Branding / Título
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .background(onSurfaceColor)
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text          = "SLIOR LOGISTICS V2.0",
                            fontFamily    = SpaceGroteskFamily,
                            fontWeight    = FontWeight.Black,
                            fontSize      = 10.sp,
                            letterSpacing = 2.sp,
                            color         = surfaceColor
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        text       = stringResource(R.string.label_join_fleet),
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontStyle  = FontStyle.Italic,
                        fontSize   = 40.sp,
                        lineHeight = 40.sp,
                        color      = onSurfaceColor
                    )
                }

                // Columna derecha: Formulario con Scroll
                Box(modifier = Modifier.weight(1.2f)) {
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(surfaceColor)
                            .drawBehind {
                                drawLine(
                                    color = onSurfaceColor,
                                    start = Offset(0f, 0f),
                                    end = Offset(0f, size.height),
                                    strokeWidth = 2.dp.toPx()
                                )
                            }
                            .verticalScroll(scrollState)
                            .padding(24.dp)
                    ) {
                        RegisterFormFields(
                            nombre = nombre,
                            onNombreChange = { nombre = it; viewModel.resetState() },
                            email = email,
                            onEmailChange = { email = it; viewModel.resetState() },
                            password = password,
                            onPasswordChange = { password = it; viewModel.resetState() },
                            passwordVisible = passwordVisible,
                            onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                            selectedVehicleType = selectedVehicleType,
                            onVehicleTypeSelected = { selectedVehicleType = it },
                            loginState = loginState,
                            onSurfaceColor = onSurfaceColor,
                            onCreateAccountClick = {
                                val emailError = Validators.getValidationError("email", email)
                                val passwordError = Validators.getValidationError("password", password)

                                when {
                                    nombre.isBlank() -> viewModel.setError(context.getString(R.string.error_empty_name))
                                    emailError != null -> viewModel.setError(emailError)
                                    passwordError != null -> viewModel.setError(passwordError)
                                    else -> viewModel.register(nombre, email, password, "REPARTIDOR", selectedVehicleType.name)
                                }
                            },
                            onGoToLogin = onGoToLogin
                        )
                    }

                    // Indicador de Scroll
                    if (scrollState.value < 100) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp)
                                .background(onSurfaceColor.copy(alpha = 0.8f))
                                .padding(horizontal = 12.dp, vertical = 4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = NeonGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    text = "SCROLL PARA MÁS",
                                    color = surfaceColor,
                                    fontFamily = SpaceGroteskFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.sp
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Diseño para PORTRAIT
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 32.dp)
            ) {
                Box(
                    modifier = Modifier
                        .background(onSurfaceColor)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text          = "SLIOR LOGISTICS V2.0",
                        fontFamily    = SpaceGroteskFamily,
                        fontWeight    = FontWeight.Black,
                        fontSize      = 10.sp,
                        letterSpacing = 2.sp,
                        color         = surfaceColor
                    )
                }
                Spacer(Modifier.height(6.dp))
                Text(
                    text       = stringResource(R.string.label_join_fleet),
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontStyle  = FontStyle.Italic,
                    fontSize   = 40.sp,
                    lineHeight = 40.sp,
                    color      = onSurfaceColor
                )

                Spacer(Modifier.height(32.dp))

                RegisterFormFields(
                    nombre = nombre,
                    onNombreChange = { nombre = it; viewModel.resetState() },
                    email = email,
                    onEmailChange = { email = it; viewModel.resetState() },
                    password = password,
                    onPasswordChange = { password = it; viewModel.resetState() },
                    passwordVisible = passwordVisible,
                    onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                    selectedVehicleType = selectedVehicleType,
                    onVehicleTypeSelected = { selectedVehicleType = it },
                    loginState = loginState,
                    onSurfaceColor = onSurfaceColor,
                    onCreateAccountClick = {
                        val emailError = Validators.getValidationError("email", email)
                        val passwordError = Validators.getValidationError("password", password)

                        when {
                            nombre.isBlank() -> viewModel.setError(context.getString(R.string.error_empty_name))
                            emailError != null -> viewModel.setError(emailError)
                            passwordError != null -> viewModel.setError(passwordError)
                            else -> viewModel.register(nombre, email, password, "REPARTIDOR", selectedVehicleType.name)
                        }
                    },
                    onGoToLogin = onGoToLogin
                )
                Spacer(Modifier.height(32.dp))
            }
        }

        RegisterFooter(serverStatus, onSurfaceColor, surfaceColor)
    }
}

@Composable
private fun RegisterFormFields(
    nombre: String,
    onNombreChange: (String) -> Unit,
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    selectedVehicleType: VehicleType,
    onVehicleTypeSelected: (VehicleType) -> Unit,
    loginState: LoginState,
    onSurfaceColor: Color,
    onCreateAccountClick: () -> Unit,
    onGoToLogin: () -> Unit
) {
    if (loginState is LoginState.Error) {
        SliorErrorBanner(
            message  = (loginState as LoginState.Error).message.uppercase(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(20.dp))
    }

    SliorFieldLabel(text = stringResource(R.string.label_full_name))
    SliorTextField(
        value         = nombre,
        onValueChange = onNombreChange,
        placeholder   = "EJ. JUAN PÉREZ",
        withShadow    = true,
        modifier      = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(20.dp))

    SliorFieldLabel(text = stringResource(R.string.label_email))
    SliorTextField(
        value         = email,
        onValueChange = onEmailChange,
        placeholder   = "EMAIL@SLIOR.APP",
        keyboardType  = KeyboardType.Email,
        withShadow    = true,
        modifier      = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(20.dp))

    SliorFieldLabel(text = stringResource(R.string.label_password_alt))
    SliorPasswordField(
        value              = password,
        onValueChange      = onPasswordChange,
        placeholder        = "••••••••",
        visible            = passwordVisible,
        onToggleVisibility = onTogglePasswordVisibility,
        loginStyle         = false,
        withShadow         = true,
        modifier           = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(20.dp))

    SliorFieldLabel(text = stringResource(R.string.label_vehicle_type))
    VehicleTypeSelector(
        selectedType = selectedVehicleType,
        onTypeSelected = onVehicleTypeSelected,
        onSurfaceColor = onSurfaceColor,
        modifier = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(40.dp))

    if (loginState is LoginState.Loading) {
        SliorLoadingButton(modifier = Modifier.fillMaxWidth())
    } else {
        SliorPrimaryButton(
            text     = stringResource(R.string.btn_create_account),
            onClick  = onCreateAccountClick,
            modifier     = Modifier.fillMaxWidth(),
            trailingIcon = {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint               = BrutalistBlack,
                    modifier           = Modifier.size(24.dp)
                )
            }
        )
    }

    Spacer(Modifier.height(24.dp))

    Row(
        modifier              = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Text(
            text          = stringResource(R.string.link_has_account),
            fontFamily    = SpaceGroteskFamily,
            fontWeight    = FontWeight.Bold,
            fontSize      = 14.sp,
            letterSpacing = (-0.3).sp,
            color         = onSurfaceColor
        )
        Box(
            modifier = Modifier
                .background(SafetyOrange)
                .padding(horizontal = 8.dp, vertical = 4.dp)
                .clickable { onGoToLogin() }
        ) {
            Text(
                text          = stringResource(R.string.btn_login).split(" ")[0],
                fontFamily    = SpaceGroteskFamily,
                fontWeight    = FontWeight.Black,
                fontSize      = 14.sp,
                letterSpacing = 0.5.sp,
                color         = Color.White
            )
        }
    }
}

@Composable
private fun RegisterFooter(serverStatus: ServerStatus, onSurfaceColor: Color, surfaceColor: Color) {
    Row(
        modifier              = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF1EDE9).copy(alpha = if (onSurfaceColor == Color.White) 0.1f else 1f))
            .drawBehind {
                drawLine(
                    color       = onSurfaceColor,
                    start       = Offset(0f, 0f),
                    end         = Offset(size.width, 0f),
                    strokeWidth = 4.dp.toPx()
                )
            }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment     = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text          = stringResource(R.string.menu_system_status),
                fontFamily    = SpaceGroteskFamily,
                fontWeight    = FontWeight.Black,
                fontSize      = 10.sp,
                letterSpacing = 1.sp,
                color         = onSurfaceColor.copy(alpha = 0.5f)
            )
            val (dotColor, label) = when (serverStatus) {
                is ServerStatus.Online   -> NeonGreen  to stringResource(R.string.status_online)
                is ServerStatus.Offline  -> OfflineRed to stringResource(R.string.status_offline)
                is ServerStatus.Checking -> Color(0xFFFFAA00) to stringResource(R.string.status_checking)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(dotColor, shape = CircleShape)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text       = label,
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 12.sp,
                    color      = dotColor
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text          = stringResource(R.string.menu_device_id),
                fontFamily    = SpaceGroteskFamily,
                fontWeight    = FontWeight.Black,
                fontSize      = 10.sp,
                letterSpacing = 1.sp,
                color         = onSurfaceColor.copy(alpha = 0.5f)
            )
            Text(
                text       = "SL-992-TX",
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize   = 12.sp,
                color      = onSurfaceColor
            )
        }
    }
}

@Composable
private fun VehicleTypeSelector(
    selectedType: VehicleType,
    onTypeSelected: (VehicleType) -> Unit,
    onSurfaceColor: Color,
    modifier: Modifier = Modifier
) {
    val vehicleTypes = listOf(
        VehicleType.CAR to stringResource(R.string.vehicle_car),
        VehicleType.VAN to stringResource(R.string.vehicle_van),
        VehicleType.TRUCK to stringResource(R.string.vehicle_truck)
    )

    Column(modifier = modifier) {
        vehicleTypes.forEach { (type, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .hardShadow(offsetX = 2.dp, offsetY = 2.dp, color = onSurfaceColor)
                    .border(
                        width = SliorDesignTokens.BorderWidth,
                        color = if (selectedType == type) NeonGreen else onSurfaceColor,
                        shape = RectangleShape
                    )
                    .background(if (selectedType == type) NeonGreen.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface)
                    .clickable { onTypeSelected(type) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .border(2.dp, onSurfaceColor, RectangleShape)
                        .background(if (selectedType == type) NeonGreen else Color.Transparent),
                    contentAlignment = Alignment.Center
                ) {
                    if (selectedType == type) {
                        Text("✓", color = BrutalistBlack, fontWeight = FontWeight.Black, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.width(12.dp))
                Text(
                    text       = label,
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 14.sp,
                    color      = if (selectedType == type) BrutalistBlack else onSurfaceColor,
                    modifier   = Modifier.weight(1f)
                )
            }
            if (type != vehicleTypes.last().first) {
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
