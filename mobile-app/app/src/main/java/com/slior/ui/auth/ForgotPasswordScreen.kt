package com.slior.ui.auth

import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.slior.R
import com.slior.ui.components.*
import com.slior.ui.theme.*
import com.slior.util.Validators
import com.slior.viewmodel.AuthViewModel

/**
 * Pantalla para el flujo de "Olvidaste tu contraseña".
 * Soporta modo vertical y horizontal, además de temas claro/oscuro.
 */
@Composable
fun ForgotPasswordScreen(
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val state by viewModel.forgotPasswordState.collectAsStateWithLifecycle()
    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    
    // Colores del tema
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    
    // Recursos de texto
    val titleText = stringResource(R.string.title_forgot_password)
    val backDesc = stringResource(R.string.btn_back)

    var email by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(surfaceColor)
                .drawBehind {
                    drawLine(
                        color = onSurfaceColor,
                        start = Offset(0f, size.height),
                        end = Offset(size.width, size.height),
                        strokeWidth = 4.dp.toPx()
                    )
                }
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .hardShadow(offsetX = 2.dp, offsetY = 2.dp)
                    .border(3.dp, onSurfaceColor, RectangleShape)
                    .background(surfaceColor)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = backDesc,
                    tint = onSurfaceColor
                )
            }
            Spacer(Modifier.width(16.dp))
            Text(
                text = titleText,
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = onSurfaceColor
            )
        }

        SliorAccentBar()

        if (isLandscape && state !is ForgotPasswordState.Success) {
            // DISEÑO RESPONSIVO LANDSCAPE
            Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
                // Columna izquierda: Instrucciones
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
                            text = "SECURITY CHECK",
                            fontFamily = SpaceGroteskFamily,
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            letterSpacing = 2.sp,
                            color = surfaceColor
                        )
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = if (state is ForgotPasswordState.CodeSent) 
                                stringResource(R.string.desc_reset_password)
                               else stringResource(R.string.desc_forgot_password),
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 15.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Medium,
                        color = onSurfaceColor
                    )
                }

                // Columna derecha: Formulario con Scroll
                Box(modifier = Modifier.weight(1.2f).fillMaxHeight()) {
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
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        ForgotPasswordFormContent(
                            state = state,
                            email = email,
                            onEmailChange = { email = it; if (state is ForgotPasswordState.Error) viewModel.resetState() },
                            code = code,
                            onCodeChange = { code = it; if (state is ForgotPasswordState.Error) viewModel.resetState() },
                            newPassword = newPassword,
                            onNewPasswordChange = { newPassword = it; if (state is ForgotPasswordState.Error) viewModel.resetState() },
                            passwordVisible = passwordVisible,
                            onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                            onSendCode = { viewModel.forgotPassword(email) },
                            onResetPassword = { viewModel.resetPassword(email, code, newPassword) },
                            onError = { viewModel.setError(it) }
                        )
                    }

                    // Hint de scroll landscape
                    if (scrollState.value < 50 && (state is ForgotPasswordState.CodeSent)) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(bottom = 8.dp)
                                .background(onSurfaceColor.copy(alpha = 0.8f))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.ArrowDropDown, null, tint = NeonGreen, modifier = Modifier.size(14.dp))
                                Text("SCROLL", color = surfaceColor, fontSize = 9.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        } else {
            // DISEÑO PORTRAIT (O ÉXITO)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = if (state is ForgotPasswordState.Success) Arrangement.Center else Arrangement.Top
            ) {
                if (state is ForgotPasswordState.Success) {
                    SuccessView(onBack)
                } else {
                    Text(
                        text = stringResource(R.string.desc_forgot_password),
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 14.sp,
                        color = onSurfaceColor.copy(alpha = 0.7f),
                        modifier = Modifier.padding(bottom = 32.dp).fillMaxWidth()
                    )
                    
                    ForgotPasswordFormContent(
                        state = state,
                        email = email,
                        onEmailChange = { email = it; if (state is ForgotPasswordState.Error) viewModel.resetState() },
                        code = code,
                        onCodeChange = { code = it; if (state is ForgotPasswordState.Error) viewModel.resetState() },
                        newPassword = newPassword,
                        onNewPasswordChange = { newPassword = it; if (state is ForgotPasswordState.Error) viewModel.resetState() },
                        passwordVisible = passwordVisible,
                        onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                        onSendCode = { viewModel.forgotPassword(email) },
                        onResetPassword = { viewModel.resetPassword(email, code, newPassword) },
                        onError = { viewModel.setError(it) }
                    )
                }
            }
        }
    }
}

@Composable
private fun ForgotPasswordFormContent(
    state: ForgotPasswordState,
    email: String,
    onEmailChange: (String) -> Unit,
    code: String,
    onCodeChange: (String) -> Unit,
    newPassword: String,
    onNewPasswordChange: (String) -> Unit,
    passwordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    onSendCode: () -> Unit,
    onResetPassword: () -> Unit,
    onError: (String) -> Unit
) {
    val invalidCodeMsg = stringResource(R.string.error_invalid_code)
    val passTooShortMsg = stringResource(R.string.error_pass_too_short)
    val passNoUpperMsg = stringResource(R.string.error_pass_no_upper)
    val passNoNumberMsg = stringResource(R.string.error_pass_no_number)

    if (state is ForgotPasswordState.Idle || state is ForgotPasswordState.Loading || state is ForgotPasswordState.Error) {
        if (state is ForgotPasswordState.Error) {
            SliorErrorBanner((state as ForgotPasswordState.Error).message.uppercase(), Modifier.fillMaxWidth())
            Spacer(Modifier.height(20.dp))
        }

        SliorFieldLabel(stringResource(R.string.label_email))
        SliorTextField(
            value = email,
            onValueChange = onEmailChange,
            placeholder = "TU@EMAIL.COM",
            keyboardType = KeyboardType.Email,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(32.dp))

        if (state is ForgotPasswordState.Loading) {
            SliorLoadingButton(Modifier.fillMaxWidth())
        } else {
            SliorPrimaryButton(
                text = stringResource(R.string.btn_send_code),
                onClick = {
                    val err = Validators.getValidationError("email", email)
                    if (err != null) onError(err) else onSendCode()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    } else if (state is ForgotPasswordState.CodeSent) {
        Box(
            modifier = Modifier.fillMaxWidth().background(NeonGreen).border(2.dp, BrutalistBlack).padding(12.dp)
        ) {
            Text(stringResource(R.string.msg_code_sent), fontFamily = SpaceGroteskFamily, fontWeight = FontWeight.Black, fontSize = 12.sp, color = Color.Black)
        }
        
        Spacer(Modifier.height(20.dp))

        SliorFieldLabel(stringResource(R.string.label_reset_code))
        SliorTextField(
            value = code,
            onValueChange = onCodeChange,
            placeholder = "000000",
            keyboardType = KeyboardType.Number,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(20.dp))

        SliorFieldLabel(stringResource(R.string.label_new_password))
        SliorPasswordField(
            value = newPassword,
            onValueChange = onNewPasswordChange,
            placeholder = "••••••••",
            visible = passwordVisible,
            onToggleVisibility = onTogglePasswordVisibility,
            modifier = Modifier.fillMaxWidth()
        )

        Column(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp, start = 4.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            ValidationHint(stringResource(R.string.val_pass_min_length), newPassword.length >= 8)
            ValidationHint(stringResource(R.string.val_pass_uppercase), newPassword.any { it.isUpperCase() })
            ValidationHint(stringResource(R.string.val_pass_number), newPassword.any { it.isDigit() })
        }

        Spacer(Modifier.height(32.dp))

        SliorPrimaryButton(
            text = stringResource(R.string.btn_reset_password),
            onClick = {
                val isAtLeast8 = newPassword.length >= 8
                val hasUpper = newPassword.any { it.isUpperCase() }
                val hasNumber = newPassword.any { it.isDigit() }
                
                when {
                    code.length < 6 -> onError(invalidCodeMsg)
                    !isAtLeast8 -> onError(passTooShortMsg)
                    !hasUpper -> onError(passNoUpperMsg)
                    !hasNumber -> onError(passNoNumberMsg)
                    else -> onResetPassword()
                }
            },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun SuccessView(onBack: () -> Unit) {
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface
    
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(Icons.Default.CheckCircle, null, tint = NeonGreen, modifier = Modifier.size(80.dp))
        Spacer(Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.msg_reset_success), 
            fontFamily = SpaceGroteskFamily, 
            fontWeight = FontWeight.Black, 
            fontSize = 24.sp,
            color = onSurfaceColor
        )
        Spacer(Modifier.height(40.dp))
        SliorPrimaryButton(stringResource(R.string.btn_back_to_login), onBack, Modifier.fillMaxWidth())
    }
}
