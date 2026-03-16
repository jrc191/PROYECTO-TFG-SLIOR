package com.slior.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.slior.ui.components.SliorErrorBanner
import com.slior.ui.components.SliorFieldLabel
import com.slior.ui.components.SliorLoadingButton
import com.slior.ui.components.SliorPasswordField
import com.slior.ui.components.SliorPrimaryButton
import com.slior.ui.components.SliorTextField
import com.slior.ui.components.hardShadow
import com.slior.ui.theme.BrutalistBlack
import com.slior.ui.theme.BrutalistWhite
import com.slior.ui.theme.NeonGreen
import com.slior.ui.theme.OfflineRed
import com.slior.ui.theme.SpaceGroteskFamily
import com.slior.viewmodel.AuthViewModel

@Composable
fun LoginScreen(
    onLoginSuccess: (repartidorId: String) -> Unit,
    onGoToRegister: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val loginState  by viewModel.loginState.collectAsStateWithLifecycle()
    val serverStatus by viewModel.serverStatus.collectAsStateWithLifecycle()

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            onLoginSuccess((loginState as LoginState.Success).repartidorId)
        }
    }

    Box(
        modifier         = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 480.dp)
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            //  Card 
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .hardShadow(offsetX = 6.dp, offsetY = 6.dp)
                    .border(3.dp, BrutalistBlack)
                    .background(BrutalistWhite)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                //  Header 
                Text(
                    text          = "SLIOR",
                    fontFamily    = SpaceGroteskFamily,
                    fontWeight    = FontWeight.Black,
                    fontSize      = 72.sp,
                    letterSpacing = (-2).sp,
                    lineHeight    = 72.sp,
                    color         = BrutalistBlack
                )
                Box(
                    modifier = Modifier
                        .background(BrutalistBlack)
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text          = "SISTEMA DE RUTAS",
                        fontFamily    = SpaceGroteskFamily,
                        fontWeight    = FontWeight.Bold,
                        fontSize      = 13.sp,
                        letterSpacing = 4.sp,
                        color         = Color.White
                    )
                }

                Spacer(Modifier.height(32.dp))

                //  Banner de error 
                if (loginState is LoginState.Error) {
                    SliorErrorBanner(
                        message  = (loginState as LoginState.Error).message.uppercase(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(20.dp))
                }

                //  Email 
                SliorFieldLabel(text = "EMAIL", modifier = Modifier.fillMaxWidth())
                SliorTextField(
                    value         = email,
                    onValueChange = { email = it; viewModel.resetState() },
                    placeholder   = "USER@SYSTEM.COM",
                    keyboardType  = KeyboardType.Email,
                    modifier      = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(20.dp))

                //  Contraseña 
                SliorFieldLabel(text = "PASSWORD", modifier = Modifier.fillMaxWidth())
                SliorPasswordField(
                    value              = password,
                    onValueChange      = { password = it; viewModel.resetState() },
                    placeholder        = "••••••••",
                    visible            = passwordVisible,
                    onToggleVisibility = { passwordVisible = !passwordVisible },
                    loginStyle         = true,
                    modifier           = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(24.dp))

                //  Botón 
                if (loginState is LoginState.Loading) {
                    SliorLoadingButton(modifier = Modifier.fillMaxWidth())
                } else {
                    SliorPrimaryButton(
                        text     = "INICIAR SESIÓN",
                        onClick  = { viewModel.login(email, password) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                //  Links 
                Column(
                    modifier            = Modifier
                        .fillMaxWidth()
                        .padding(top = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text          = "¿OLVIDASTE TU CONTRASEÑA?",
                        fontFamily    = SpaceGroteskFamily,
                        fontWeight    = FontWeight.Bold,
                        fontSize      = 12.sp,
                        letterSpacing = 1.sp,
                        color         = BrutalistBlack
                    )
                    Spacer(Modifier.height(10.dp))
                    Row {
                        Text(
                            text       = "¿No tienes cuenta? ",
                            fontFamily = SpaceGroteskFamily,
                            fontSize   = 12.sp,
                            color      = Color(0xFF757575)
                        )
                        Text(
                            text          = "REGÍSTRATE AQUÍ",
                            fontFamily    = SpaceGroteskFamily,
                            fontWeight    = FontWeight.Black,
                            fontSize      = 12.sp,
                            color         = BrutalistBlack,
                            modifier      = Modifier.clickable { onGoToRegister() }
                        )
                    }
                }

                //  Footer: versión + estado del servidor 
                Row(
                    modifier          = Modifier
                        .fillMaxWidth()
                        .padding(top = 28.dp)
                        .alpha(0.25f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(
                        modifier  = Modifier.weight(1f),
                        color     = BrutalistBlack,
                        thickness = 2.dp
                    )
                    Text(
                        text          = "V 4.0.1 - RUGGED SYSTEM",
                        fontFamily    = SpaceGroteskFamily,
                        fontWeight    = FontWeight.Black,
                        fontSize      = 9.sp,
                        letterSpacing = 3.sp,
                        color         = BrutalistBlack,
                        modifier      = Modifier.padding(horizontal = 8.dp)
                    )
                    HorizontalDivider(
                        modifier  = Modifier.weight(1f),
                        color     = BrutalistBlack,
                        thickness = 2.dp
                    )
                }

                //  Indicador estado servidor 
                Row(
                    modifier          = Modifier.padding(top = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val (dotColor, label) = when (serverStatus) {
                        is ServerStatus.Online   -> NeonGreen  to "SERVIDOR EN LÍNEA"
                        is ServerStatus.Offline  -> OfflineRed to "SIN CONEXIÓN"
                        is ServerStatus.Checking -> Color(0xFFFFAA00) to "COMPROBANDO..."
                    }
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(dotColor, shape = CircleShape)
                    )
                    Spacer(Modifier.width(5.dp))
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