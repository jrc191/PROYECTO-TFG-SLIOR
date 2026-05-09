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
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.slior.R
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
fun LoginScreen(
    onLoginSuccess: (String) -> Unit,
    onGoToRegister: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val loginState by viewModel.loginState.collectAsStateWithLifecycle()
    val serverStatus by viewModel.serverStatus.collectAsStateWithLifecycle()

    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(loginState) {
        if (loginState is LoginState.Success) {
            onLoginSuccess((loginState as LoginState.Success).userId)
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
                .padding(24.dp)
        ) {
            // Cabecera Decorativa
            SliorAccentBar()
            Spacer(Modifier.height(48.dp))

            // Logo / Título
            Text(
                text = stringResource(R.string.app_name),
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Black,
                fontSize = 64.sp,
                letterSpacing = (-2).sp,
                color = BrutalistBlack,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = stringResource(R.string.menu_system_status).uppercase(),
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                letterSpacing = 4.sp,
                color = BrutalistBlack,
                modifier = Modifier.alpha(0.6f)
            )

            Spacer(Modifier.height(48.dp))

            // Formulario
            SliorFieldLabel(stringResource(R.string.label_email))
            SliorTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "user@slior.com",
                withShadow = true
            )

            Spacer(Modifier.height(24.dp))

            SliorFieldLabel(stringResource(R.string.label_password))
            SliorPasswordField(
                value = password,
                onValueChange = { password = it },
                placeholder = "••••••••",
                visible = passwordVisible,
                onToggleVisibility = { passwordVisible = !passwordVisible },
                withShadow = true
            )

            Spacer(Modifier.height(16.dp))

            // Olvido de contraseña
            Text(
                text = stringResource(R.string.link_forgot_password),
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = BrutalistBlack,
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { /* TODO */ }
            )

            Spacer(Modifier.height(32.dp))

            // Botón Login
            if (loginState is LoginState.Loading) {
                SliorLoadingButton(modifier = Modifier.fillMaxWidth())
            } else {
                SliorPrimaryButton(
                    text = stringResource(R.string.btn_login),
                    onClick = { viewModel.login(email, password) },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = email.isNotBlank() && password.isNotBlank()
                )
            }

            // Error
            if (loginState is LoginState.Error) {
                Spacer(Modifier.height(16.dp))
                SliorErrorBanner(message = (loginState as LoginState.Error).message)
            }

            Spacer(Modifier.weight(1f))

            // Registro
            Row(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.link_no_account),
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 14.sp,
                    color = BrutalistBlack
                )
                Text(
                    text = stringResource(R.string.btn_register),
                    fontFamily = SpaceGroteskFamily,
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = BrutalistBlack,
                    modifier = Modifier.clickable { onGoToRegister() }
                )
            }
            
            Spacer(Modifier.height(16.dp))

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
