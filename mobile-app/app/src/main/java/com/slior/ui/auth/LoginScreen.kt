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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.slior.R
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
import com.slior.util.Validators
import com.slior.viewmodel.AuthViewModel

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.platform.LocalConfiguration

@Composable
fun LoginScreen(
    onLoginSuccess: (repartidorId: String) -> Unit,
    onGoToRegister: () -> Unit,
    onGoToForgotPassword: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val loginState  by viewModel.loginState.collectAsStateWithLifecycle()
    val serverStatus by viewModel.serverStatus.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

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
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        if (isLandscape) {
            // Diseño para LANDSCAPE - Optimizando para que quepa sin scroll
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Columna izquierda: Branding (Más compacto)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text          = stringResource(R.string.app_name),
                        fontFamily    = SpaceGroteskFamily,
                        fontWeight    = FontWeight.Black,
                        fontSize      = 60.sp, 
                        letterSpacing = (-2).sp,
                        lineHeight    = 60.sp,
                        color         = MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.onSurface)
                            .padding(horizontal = 12.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text          = stringResource(R.string.menu_system_status),
                            fontFamily    = SpaceGroteskFamily,
                            fontWeight    = FontWeight.Bold,
                            fontSize      = 11.sp, 
                            letterSpacing = 2.sp,
                            color         = MaterialTheme.colorScheme.surface
                        )
                    }
                    
                    Spacer(Modifier.height(16.dp))
                    
                    ServerStatusIndicator(serverStatus)
                }

                // Columna derecha: Formulario (Compacto)
                Column(
                    modifier = Modifier
                        .weight(1.3f)
                        .hardShadow(offsetX = 4.dp, offsetY = 4.dp)
                        .border(3.dp, MaterialTheme.colorScheme.onSurface)
                        .background(MaterialTheme.colorScheme.surface)
                        .verticalScroll(rememberScrollState()) 
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    LoginFormFields(
                        email = email,
                        onEmailChange = { email = it; viewModel.resetState() },
                        password = password,
                        onPasswordChange = { password = it; viewModel.resetState() },
                        passwordVisible = passwordVisible,
                        onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                        loginState = loginState,
                        isLandscape = isLandscape, 
                        onLoginClick = {
                            val emailError = Validators.getValidationError("email", email)
                            val passwordError = Validators.getValidationError("password", password)

                            when {
                                emailError != null -> viewModel.setError(emailError)
                                passwordError != null -> viewModel.setError(passwordError)
                                else -> viewModel.login(email, password)
                            }
                        },
                        onGoToRegister = onGoToRegister,
                        onGoToForgotPassword = onGoToForgotPassword
                    )
                }
            }
        } else {
            // Diseño para PORTRAIT (Existente)
            Column(
                modifier = Modifier
                    .widthIn(max = 480.dp)
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .hardShadow(offsetX = 6.dp, offsetY = 6.dp)
                        .border(3.dp, MaterialTheme.colorScheme.onSurface)
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header Portrait
                    Text(
                        text          = stringResource(R.string.app_name),
                        fontFamily    = SpaceGroteskFamily,
                        fontWeight    = FontWeight.Black,
                        fontSize      = 72.sp,
                        letterSpacing = (-2).sp,
                        lineHeight    = 72.sp,
                        color         = MaterialTheme.colorScheme.onSurface
                    )
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.onSurface)
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text          = stringResource(R.string.menu_system_status),
                            fontFamily    = SpaceGroteskFamily,
                            fontWeight    = FontWeight.Bold,
                            fontSize      = 13.sp,
                            letterSpacing = 4.sp,
                            color         = MaterialTheme.colorScheme.surface
                        )
                    }

                    Spacer(Modifier.height(32.dp))

                    LoginFormFields(
                        email = email,
                        onEmailChange = { email = it; viewModel.resetState() },
                        password = password,
                        onPasswordChange = { password = it; viewModel.resetState() },
                        passwordVisible = passwordVisible,
                        onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                        loginState = loginState,
                        isLandscape = isLandscape, 
                        onLoginClick = {
                            val emailError = Validators.getValidationError("email", email)
                            val passwordError = Validators.getValidationError("password", password)

                            when {
                                emailError != null -> viewModel.setError(emailError)
                                passwordError != null -> viewModel.setError(passwordError)
                                else -> viewModel.login(email, password)
                            }
                        },
                        onGoToRegister = onGoToRegister,
                        onGoToForgotPassword = onGoToForgotPassword
                    )
                    
                    // Footer Portrait
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Row(
                            modifier          = Modifier
                                .fillMaxWidth()
                                .padding(top = 28.dp)
                                .alpha(0.25f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(
                                modifier  = Modifier.weight(1f),
                                color     = MaterialTheme.colorScheme.onSurface,
                                thickness = 2.dp
                            )
                            Text(
                                text          = stringResource(R.string.menu_version_info),
                                fontFamily    = SpaceGroteskFamily,
                                fontWeight    = FontWeight.Black,
                                fontSize      = 9.sp,
                                letterSpacing = 3.sp,
                                color         = MaterialTheme.colorScheme.onSurface,
                                modifier      = Modifier.padding(horizontal = 8.dp)
                            )
                            HorizontalDivider(
                                modifier  = Modifier.weight(1f),
                                color     = MaterialTheme.colorScheme.onSurface,
                                thickness = 2.dp
                            )
                        }
                        ServerStatusIndicator(serverStatus)
                    }
                }
            }
        }
    }
}

@Composable
private fun LoginFormFields(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    loginState: LoginState,
    isLandscape: Boolean,
    onLoginClick: () -> Unit,
    onGoToRegister: () -> Unit,
    onGoToForgotPassword: () -> Unit
) {
    //  Banner de error (Más compacto en landscape)
    if (loginState is LoginState.Error) {
        SliorErrorBanner(
            message  = (loginState as LoginState.Error).message.uppercase(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(if (isLandscape) 8.dp else 20.dp))
    }

    //  Email
    if (!isLandscape) SliorFieldLabel(text = stringResource(R.string.label_email), modifier = Modifier.fillMaxWidth())
    SliorTextField(
        value         = email,
        onValueChange = onEmailChange,
        placeholder   = if (isLandscape) stringResource(R.string.label_email) else "USER@SYSTEM.COM",
        keyboardType  = KeyboardType.Email,
        modifier      = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(if (isLandscape) 12.dp else 20.dp))

    //  Contraseña
    if (!isLandscape) SliorFieldLabel(text = stringResource(R.string.label_password), modifier = Modifier.fillMaxWidth())
    SliorPasswordField(
        value              = password,
        onValueChange      = onPasswordChange,
        placeholder        = if (isLandscape) stringResource(R.string.label_password) else "••••••••",
        visible            = passwordVisible,
        onToggleVisibility = onTogglePasswordVisibility,
        loginStyle         = true,
        modifier           = Modifier.fillMaxWidth()
    )

    Spacer(Modifier.height(if (isLandscape) 16.dp else 24.dp))

    //  Botón
    if (loginState is LoginState.Loading) {
        SliorLoadingButton(modifier = Modifier.fillMaxWidth())
    } else {
        SliorPrimaryButton(
            text     = stringResource(R.string.btn_login),
            onClick  = onLoginClick,
            modifier = Modifier.fillMaxWidth()
        )
    }

    //  Links
    if (isLandscape) {
        // En horizontal, usamos una columna centrada para evitar que el texto se corte
        // pero con espaciado mínimo para no obligar a scrollear.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text          = stringResource(R.string.link_forgot_password),
                fontFamily    = SpaceGroteskFamily,
                fontWeight    = FontWeight.Bold,
                fontSize      = 11.sp,
                color         = MaterialTheme.colorScheme.onSurface,
                modifier      = Modifier.clickable { onGoToForgotPassword() }
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text       = stringResource(R.string.link_no_account),
                    fontFamily = SpaceGroteskFamily,
                    fontSize   = 11.sp,
                    color      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text          = stringResource(R.string.btn_register),
                    fontFamily    = SpaceGroteskFamily,
                    fontWeight    = FontWeight.Black,
                    fontSize      = 11.sp,
                    color         = MaterialTheme.colorScheme.onSurface,
                    modifier      = Modifier.clickable { onGoToRegister() }
                )
            }
        }
    } else {
        // En vertical, diseño original
        Column(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(top = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text          = stringResource(R.string.link_forgot_password),
                fontFamily    = SpaceGroteskFamily,
                fontWeight    = FontWeight.Bold,
                fontSize      = 12.sp,
                letterSpacing = 1.sp,
                color         = MaterialTheme.colorScheme.onSurface,
                modifier      = Modifier.clickable { onGoToForgotPassword() }
            )
            Spacer(Modifier.height(10.dp))
            Row {
                Text(
                    text       = stringResource(R.string.link_no_account),
                    fontFamily = SpaceGroteskFamily,
                    fontSize   = 12.sp,
                    color      = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
                Text(
                    text          = stringResource(R.string.btn_register),
                    fontFamily    = SpaceGroteskFamily,
                    fontWeight    = FontWeight.Black,
                    fontSize      = 12.sp,
                    color         = MaterialTheme.colorScheme.onSurface,
                    modifier      = Modifier.clickable { onGoToRegister() }
                )
            }
        }
    }
}

@Composable
private fun ServerStatusIndicator(serverStatus: ServerStatus) {
    Row(
        modifier          = Modifier.padding(top = 10.dp),
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

