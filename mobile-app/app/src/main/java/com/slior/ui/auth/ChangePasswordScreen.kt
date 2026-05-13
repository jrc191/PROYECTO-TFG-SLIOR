package com.slior.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.slior.R
import com.slior.ui.components.*
import com.slior.ui.theme.*
import com.slior.util.Validators
import com.slior.viewmodel.AuthViewModel

/**
 * Pantalla para cambiar la contraseña de un usuario ya autenticado.
 * Accedida desde el panel de perfil.
 */
@Composable
fun ChangePasswordScreen(
    onBack: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val state by viewModel.forgotPasswordState.collectAsStateWithLifecycle()
    
    // Colores dinámicos
    val backgroundColor = MaterialTheme.colorScheme.background
    val surfaceColor = MaterialTheme.colorScheme.surface
    val onSurfaceColor = MaterialTheme.colorScheme.onSurface

    var oldPassword by rememberSaveable { mutableStateOf("") }
    var newPassword by rememberSaveable { mutableStateOf("") }
    var oldPasswordVisible by rememberSaveable { mutableStateOf(false) }
    var newPasswordVisible by rememberSaveable { mutableStateOf(false) }

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
                    .hardShadow(offsetX = 2.dp, offsetY = 2.dp, color = onSurfaceColor)
                    .border(3.dp, onSurfaceColor, RectangleShape)
                    .background(surfaceColor)
                    .clickable { onBack() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.btn_back),
                    tint = onSurfaceColor
                )
            }
            Spacer(Modifier.width(16.dp))
            Text(
                text = stringResource(R.string.menu_change_password),
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = onSurfaceColor
            )
        }

        SliorAccentBar()

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (state is ForgotPasswordState.Success) {
                // Éxito
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 40.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.CheckCircle, null, tint = NeonGreen, modifier = Modifier.size(80.dp))
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.msg_reset_success),
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Black,
                        fontSize = 20.sp,
                        color = onSurfaceColor
                    )
                    Spacer(Modifier.height(40.dp))
                    SliorPrimaryButton(
                        text = stringResource(R.string.btn_ok).uppercase(),
                        onClick = { viewModel.resetState(); onBack() },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            } else {
                // Formulario
                Text(
                    text = "Asegúrate de elegir una contraseña segura que no uses en otros sitios.",
                    fontFamily = SpaceGroteskFamily,
                    fontSize = 14.sp,
                    color = onSurfaceColor.copy(alpha = 0.7f),
                    modifier = Modifier.padding(bottom = 32.dp).fillMaxWidth()
                )

                if (state is ForgotPasswordState.Error) {
                    SliorErrorBanner(
                        message = (state as ForgotPasswordState.Error).message.uppercase(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(20.dp))
                }

                // Contraseña Actual
                SliorFieldLabel(text = "CONTRASEÑA ACTUAL")
                SliorPasswordField(
                    value = oldPassword,
                    onValueChange = { 
                        oldPassword = it
                        if (state is ForgotPasswordState.Error) viewModel.resetState()
                    },
                    placeholder = "••••••••",
                    visible = oldPasswordVisible,
                    onToggleVisibility = { oldPasswordVisible = !oldPasswordVisible },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(24.dp))

                // Nueva Contraseña
                SliorFieldLabel(text = stringResource(R.string.label_new_password))
                SliorPasswordField(
                    value = newPassword,
                    onValueChange = { 
                        newPassword = it
                        if (state is ForgotPasswordState.Error) viewModel.resetState()
                    },
                    placeholder = "••••••••",
                    visible = newPasswordVisible,
                    onToggleVisibility = { newPasswordVisible = !newPasswordVisible },
                    modifier = Modifier.fillMaxWidth()
                )

                // Hints de validación
                Column(
                    modifier = Modifier.fillMaxWidth().padding(top = 12.dp, start = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    ValidationHint(stringResource(R.string.val_pass_min_length), newPassword.length >= 8)
                    ValidationHint(stringResource(R.string.val_pass_uppercase), newPassword.any { it.isUpperCase() })
                    ValidationHint(stringResource(R.string.val_pass_number), newPassword.any { it.isDigit() })
                }

                Spacer(Modifier.height(40.dp))

                if (state is ForgotPasswordState.Loading) {
                    SliorLoadingButton(Modifier.fillMaxWidth())
                } else {
                    SliorPrimaryButton(
                        text = stringResource(R.string.btn_reset_password),
                        onClick = {
                            val isAtLeast8 = newPassword.length >= 8
                            val hasUpper = newPassword.any { it.isUpperCase() }
                            val hasNumber = newPassword.any { it.isDigit() }
                            
                            when {
                                oldPassword.isBlank() -> viewModel.setError("Introduce tu contraseña actual")
                                !isAtLeast8 -> viewModel.setError(context.getString(R.string.error_pass_too_short))
                                !hasUpper -> viewModel.setError(context.getString(R.string.error_pass_no_upper))
                                !hasNumber -> viewModel.setError(context.getString(R.string.error_pass_no_number))
                                else -> viewModel.updatePassword(oldPassword, newPassword)
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
