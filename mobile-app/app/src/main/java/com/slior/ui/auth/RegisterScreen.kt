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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.slior.R
import com.slior.data.model.enums.VehicleType
import com.slior.ui.components.SliorAccentBar
import com.slior.ui.components.SliorErrorBanner
import com.slior.ui.components.SliorFieldLabel
import com.slior.ui.components.SliorPasswordField
import com.slior.ui.components.SliorPrimaryButton
import com.slior.ui.components.SliorLoadingButton
import com.slior.ui.components.SliorTextField
import com.slior.ui.theme.BrutalistBlack
import com.slior.ui.theme.BrutalistWhite
import com.slior.ui.theme.NeonGreen
import com.slior.ui.theme.OfflineRed
import com.slior.ui.theme.SpaceGroteskFamily
import com.slior.viewmodel.AuthViewModel

@Composable
fun RegisterScreen(
    onRegisterSuccess: (String) -> Unit,
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val loginState by viewModel.loginState.collectAsStateWithLifecycle()
    val serverStatus by viewModel.serverStatus.collectAsStateWithLifecycle()

    var nombre by rememberSaveable { mutableStateOf("") }
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var vehicleType by rememberSaveable { mutableStateOf(VehicleType.CAR) }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            onRegisterSuccess((loginState as LoginState.Success).userId)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BrutalistWhite)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp)
        ) {
            // Cabecera Decorativa
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .border(2.dp, BrutalistBlack)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, stringResource(R.string.btn_back), tint = BrutalistBlack)
                }
                SliorAccentBar(modifier = Modifier.weight(1f).padding(start = 24.dp))
            }

            Spacer(Modifier.height(32.dp))

            // Título
            Text(
                text = stringResource(R.string.label_join_fleet),
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Black,
                fontSize = 40.sp,
                lineHeight = 38.sp,
                letterSpacing = (-1).sp,
                color = BrutalistBlack
            )

            Spacer(Modifier.height(32.dp))

            // Formulario
            SliorFieldLabel(stringResource(R.string.label_full_name))
            SliorTextField(
                value = nombre,
                onValueChange = { nombre = it },
                placeholder = "EJ. JUAN PÉREZ",
                withShadow = true
            )

            Spacer(Modifier.height(20.dp))

            SliorFieldLabel(stringResource(R.string.label_email))
            SliorTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "user@slior.com",
                withShadow = true
            )

            Spacer(Modifier.height(20.dp))

            SliorFieldLabel(stringResource(R.string.label_password_alt))
            SliorPasswordField(
                value = password,
                onValueChange = { password = it },
                placeholder = "••••••••",
                visible = passwordVisible,
                onToggleVisibility = { passwordVisible = !passwordVisible },
                loginStyle = false,
                withShadow = true
            )

            Spacer(Modifier.height(20.dp))

            SliorFieldLabel(stringResource(R.string.label_vehicle_type))
            VehicleSelector(
                selected = vehicleType,
                onSelected = { vehicleType = it }
            )

            Spacer(Modifier.height(40.dp))

            // Botón Registro
            if (loginState is LoginState.Loading) {
                SliorLoadingButton(modifier = Modifier.fillMaxWidth())
            } else {
                SliorPrimaryButton(
                    text = stringResource(R.string.btn_create_account),
                    onClick = { viewModel.register(nombre, email, password, "REPARTIDOR", vehicleType) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = nombre.isNotBlank() && email.isNotBlank() && password.isNotBlank()
                )
            }

            // Error
            if (loginState is LoginState.Error) {
                Spacer(Modifier.height(16.dp))
                SliorErrorBanner(message = (loginState as LoginState.Error).message)
            }

            Spacer(Modifier.height(24.dp))

            // Login link
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.link_has_account),
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = BrutalistBlack
                )
                Text(
                    text = stringResource(R.string.btn_login).split(" ")[0], // "INICIAR"
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = BrutalistBlack,
                    modifier = Modifier.clickable { onBack() }
                )
            }
            
            Spacer(Modifier.height(32.dp))

            // Status del servidor
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, BrutalistBlack)
                    .background(BrutalistBlack)
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val (dotColor, label) = when (serverStatus) {
                        is ServerStatus.Online   -> NeonGreen  to stringResource(R.string.status_online)
                        is ServerStatus.Offline  -> OfflineRed to stringResource(R.string.status_offline)
                        is ServerStatus.Checking -> Color(0xFFFFAA00) to stringResource(R.string.status_checking)
                    }
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(dotColor, shape = CircleShape)
                    )
                    Spacer(Modifier.padding(horizontal = 4.dp))
                    Text(
                        text          = label,
                        fontFamily    = SpaceGroteskFamily,
                        fontWeight    = FontWeight.Black,
                        fontSize      = 9.sp,
                        letterSpacing = 1.5.sp,
                        color         = dotColor
                    )
                }
            }
        }
    }
}

@Composable
private fun VehicleSelector(
    selected: VehicleType,
    onSelected: (VehicleType) -> Unit
) {
    val vehicleTypes = listOf(
        VehicleType.CAR to stringResource(R.string.vehicle_car),
        VehicleType.VAN to stringResource(R.string.vehicle_van),
        VehicleType.TRUCK to stringResource(R.string.vehicle_truck)
    )

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        vehicleTypes.forEach { (type, label) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(2.dp, BrutalistBlack)
                    .background(if (selected == type) NeonGreen else BrutalistWhite)
                    .clickable { onSelected(type) }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .border(2.dp, BrutalistBlack)
                        .background(if (selected == type) BrutalistBlack else BrutalistWhite)
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
        }
    }
}
