package com.slior.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
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
import com.slior.ui.components.SliorAccentBar
import com.slior.ui.components.SliorDesignTokens
import com.slior.ui.components.SliorErrorBanner
import com.slior.ui.components.SliorFieldLabel
import com.slior.ui.components.SliorLoadingButton
import com.slior.ui.components.SliorPasswordField
import com.slior.ui.components.SliorPrimaryButton
import com.slior.ui.components.SliorTextField
import com.slior.ui.components.hardShadow
import com.slior.ui.theme.BrutalistBlack
import com.slior.ui.theme.BrutalistLightGray
import com.slior.ui.theme.BrutalistWhite
import com.slior.ui.theme.NeonGreen
import com.slior.ui.theme.OfflineRed
import com.slior.ui.theme.SafetyOrange
import com.slior.ui.theme.SpaceGroteskFamily
import com.slior.util.Validators
import com.slior.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    onRegisterSuccess: (repartidorId: String) -> Unit,
    onGoToLogin: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val loginState   by viewModel.loginState.collectAsStateWithLifecycle()
    val serverStatus by viewModel.serverStatus.collectAsStateWithLifecycle()

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
            .background(BrutalistLightGray)
    ) {
        //  TopAppBar
        Row(
            modifier          = Modifier
                .fillMaxWidth()
                .background(BrutalistWhite)
                .drawBehind {
                    drawLine(
                        color       = BrutalistBlack,
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
                    .hardShadow(offsetX = 2.dp, offsetY = 2.dp)
                    .border(SliorDesignTokens.BorderWidth, BrutalistBlack, RectangleShape)
                    .background(BrutalistWhite)
                    .clickable { onGoToLogin() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.btn_back),
                    tint               = BrutalistBlack
                )
            }
            Spacer(Modifier.width(16.dp))
            Text(
                text          = stringResource(R.string.label_new_courier),
                fontFamily    = SpaceGroteskFamily,
                fontWeight    = FontWeight.Bold,
                fontSize      = 20.sp,
                letterSpacing = (-0.5).sp,
                color         = BrutalistBlack
            )
        }

        SliorAccentBar()

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp)
        ) {

            //  Title
            Box(
                modifier = Modifier
                    .background(BrutalistBlack)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text          = "SLIOR LOGISTICS V2.0",
                    fontFamily    = SpaceGroteskFamily,
                    fontWeight    = FontWeight.Black,
                    fontSize      = 10.sp,
                    letterSpacing = 2.sp,
                    color         = BrutalistWhite
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
                color      = Color(0xFF1A2332)
            )

            Spacer(Modifier.height(32.dp))

            //  Banner de error
            if (loginState is LoginState.Error) {
                SliorErrorBanner(
                    message  = (loginState as LoginState.Error).message.uppercase(),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(20.dp))
            }

            //  Nombre Completo
            SliorFieldLabel(text = stringResource(R.string.label_full_name), modifier = Modifier.fillMaxWidth())
            SliorTextField(
                value         = nombre,
                onValueChange = { nombre = it; viewModel.resetState() },
                placeholder   = "EJ. JUAN PÉREZ",
                withShadow    = true,
                modifier      = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            //  Correo Electrónico
            SliorFieldLabel(text = stringResource(R.string.label_email), modifier = Modifier.fillMaxWidth())
            SliorTextField(
                value         = email,
                onValueChange = { email = it; viewModel.resetState() },
                placeholder   = "EMAIL@SLIOR.APP",
                keyboardType  = KeyboardType.Email,
                withShadow    = true,
                modifier      = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            //  Contraseña
            SliorFieldLabel(text = stringResource(R.string.label_password_alt), modifier = Modifier.fillMaxWidth())
            SliorPasswordField(
                value              = password,
                onValueChange      = { password = it; viewModel.resetState() },
                placeholder        = "••••••••",
                visible            = passwordVisible,
                onToggleVisibility = { passwordVisible = !passwordVisible },
                loginStyle         = false,
                withShadow         = true,
                modifier           = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(20.dp))

            //  Tipo de Vehículo
            SliorFieldLabel(text = stringResource(R.string.label_vehicle_type), modifier = Modifier.fillMaxWidth())
            VehicleTypeSelector(
                selectedType = selectedVehicleType,
                onTypeSelected = { selectedVehicleType = it },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(32.dp))

            //  Crear Cuenta
            if (loginState is LoginState.Loading) {
                SliorLoadingButton(modifier = Modifier.fillMaxWidth())
            } else {
                SliorPrimaryButton(
                    text     = stringResource(R.string.btn_create_account),
                    onClick  = {
                        val emailError = Validators.getValidationError("email", email)
                        val passwordError = Validators.getValidationError("password", password)

                        when {
                            nombre.isBlank() -> viewModel.setError(context.getString(R.string.error_empty_name))
                            emailError != null -> viewModel.setError(emailError)
                            passwordError != null -> viewModel.setError(passwordError)
                            else -> viewModel.register(nombre, email, password, "REPARTIDOR", selectedVehicleType.name)
                        }
                    },
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

            //  Link a Login
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
                    color         = BrutalistBlack
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
                        color         = BrutalistWhite
                    )
                }
            }

            Spacer(Modifier.height(32.dp))
        }

        //  Footer con estado del servidor
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .background(Color(0xFFF1EDE9))
                .drawBehind {
                    drawLine(
                        color       = BrutalistBlack,
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
                    color         = Color(0xFF9E9E9E)
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
                    color         = Color(0xFF9E9E9E)
                )
                Text(
                    text       = "SL-992-TX",
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 12.sp,
                    color      = BrutalistBlack
                )
            }
        }
    }
}

@Composable
private fun VehicleTypeSelector(
    selectedType: VehicleType,
    onTypeSelected: (VehicleType) -> Unit,
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
                    .hardShadow(offsetX = 2.dp, offsetY = 2.dp)
                    .border(
                        width = SliorDesignTokens.BorderWidth,
                        color = if (selectedType == type) NeonGreen else BrutalistBlack,
                        shape = RectangleShape
                    )
                    .background(if (selectedType == type) Color(0xFFEEFFEE) else BrutalistWhite)
                    .clickable { onTypeSelected(type) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .border(2.dp, BrutalistBlack, RectangleShape)
                        .background(if (selectedType == type) NeonGreen else BrutalistWhite)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text       = label,
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize   = 14.sp,
                    color      = BrutalistBlack,
                    modifier   = Modifier.weight(1f)
                )
            }
            if (type != vehicleTypes.last().first) {
                Spacer(Modifier.height(8.dp))
            }
        }
    }
}
