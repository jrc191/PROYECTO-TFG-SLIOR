package com.slior.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slior.R
import com.slior.ui.theme.*

//
// Design tokens
//
object SliorDesignTokens {
    val BorderWidth      = 2.dp
    val BorderWidthHeavy = 3.dp
    val ShadowOffset     = 4.dp
    val ShadowOffsetLg   = 6.dp
    val FieldHeight      = 64.dp
    val ButtonHeight     = 72.dp
}

//
// Sombras offset (estilo brutalist)
// SIEMPRE NEGRAS por identidad de marca, incluso en modo oscuro.
//
fun Modifier.hardShadow(
    offsetX: Dp  = SliorDesignTokens.ShadowOffset,
    offsetY: Dp  = SliorDesignTokens.ShadowOffset,
    color: Color = BrutalistBlack
): Modifier = this
    .padding(end = offsetX, bottom = offsetY)
    .drawBehind {
        drawRect(
            color   = color,
            topLeft = Offset(offsetX.toPx(), offsetY.toPx()),
            size    = Size(size.width, size.height)
        )
    }

/**
 * Alias de hardShadow para mantener compatibilidad, forzando negro.
 */
@Composable
fun Modifier.sliorShadow(
    offsetX: Dp = SliorDesignTokens.ShadowOffset,
    offsetY: Dp = SliorDesignTokens.ShadowOffset
): Modifier {
    return this.hardShadow(offsetX, offsetY, BrutalistBlack)
}

//
// SliorFieldLabel – etiqueta de campo
//
@Composable
fun SliorFieldLabel(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text          = text,
        fontFamily    = SpaceGroteskFamily,
        fontWeight    = FontWeight.Black,
        fontSize      = 13.sp,
        letterSpacing = 1.5.sp,
        color         = MaterialTheme.colorScheme.onSurface, // Texto dinámico
        modifier      = modifier.padding(bottom = 6.dp)
    )
}

//
// SliorTextField – campo de texto
//
@Composable
fun SliorTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    keyboardType: KeyboardType = KeyboardType.Text,
    withShadow: Boolean = false
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val surface = MaterialTheme.colorScheme.surface
    
    // Los bordes y sombras son SIEMPRE negros (BrutalistBlack)
    val fieldModifier = if (withShadow)
        modifier
            .hardShadow()
            .border(SliorDesignTokens.BorderWidth, BrutalistBlack)
    else
        modifier.border(SliorDesignTokens.BorderWidth, BrutalistBlack)

    BasicTextField(
        value           = value,
        onValueChange   = onValueChange,
        singleLine      = true,
        textStyle       = TextStyle(
            fontFamily  = SpaceGroteskFamily,
            fontWeight  = FontWeight.Bold,
            fontSize    = 18.sp,
            color       = onSurface
        ),
        cursorBrush     = SolidColor(NeonGreen),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier        = fieldModifier
            .background(surface)
            .height(SliorDesignTokens.FieldHeight),
        decorationBox   = { innerTextField ->
            Box(
                modifier         = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text       = placeholder,
                        fontFamily = SpaceGroteskFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize   = 18.sp,
                        color      = onSurface.copy(alpha = 0.4f)
                    )
                }
                innerTextField()
            }
        }
    )
}

//
// SliorPasswordField – campo contraseña con ojo de visibilidad
//
@Composable
fun SliorPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    visible: Boolean,
    onToggleVisibility: () -> Unit,
    modifier: Modifier = Modifier,
    loginStyle: Boolean = true,
    withShadow: Boolean = false
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    val surface = MaterialTheme.colorScheme.surface
    val visual = if (visible) VisualTransformation.None else PasswordVisualTransformation()

    if (loginStyle) {
        val rowModifier = if (withShadow)
            modifier
                .hardShadow()
                .border(SliorDesignTokens.BorderWidth, BrutalistBlack)
        else
            modifier.border(SliorDesignTokens.BorderWidth, BrutalistBlack)

        Row(
            modifier          = rowModifier
                .background(surface)
                .height(SliorDesignTokens.FieldHeight),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value                = value,
                onValueChange        = onValueChange,
                singleLine           = true,
                visualTransformation = visual,
                textStyle            = TextStyle(
                    fontFamily  = SpaceGroteskFamily,
                    fontWeight  = FontWeight.Bold,
                    fontSize    = 18.sp,
                    color       = onSurface
                ),
                cursorBrush          = SolidColor(NeonGreen),
                keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier             = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .padding(horizontal = 16.dp),
                decorationBox        = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Text(
                                text       = placeholder,
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize   = 18.sp,
                                color      = onSurface.copy(alpha = 0.4f)
                            )
                        }
                        innerTextField()
                    }
                }
            )
            Box(
                modifier = Modifier
                    .width(SliorDesignTokens.BorderWidth)
                    .fillMaxHeight()
                    .background(BrutalistBlack)
            )
            Box(
                modifier         = Modifier
                    .size(SliorDesignTokens.FieldHeight)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        onClick           = onToggleVisibility
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = null,
                    tint               = onSurface
                )
            }
        }
    } else {
        val boxModifier = if (withShadow)
            modifier
                .hardShadow()
                .border(SliorDesignTokens.BorderWidth, BrutalistBlack)
        else
            modifier.border(SliorDesignTokens.BorderWidth, BrutalistBlack)

        Box(
            modifier = boxModifier
                .background(surface)
                .height(SliorDesignTokens.FieldHeight)
        ) {
            BasicTextField(
                value                = value,
                onValueChange        = onValueChange,
                singleLine           = true,
                visualTransformation = visual,
                textStyle            = TextStyle(
                    fontFamily  = SpaceGroteskFamily,
                    fontWeight  = FontWeight.Bold,
                    fontSize    = 18.sp,
                    color       = onSurface
                ),
                cursorBrush          = SolidColor(NeonGreen),
                keyboardOptions      = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier             = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(start = 16.dp, end = 56.dp),
                decorationBox        = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (value.isEmpty()) {
                            Text(
                                text       = placeholder,
                                fontFamily = SpaceGroteskFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize   = 18.sp,
                                color      = onSurface.copy(alpha = 0.4f)
                            )
                        }
                        innerTextField()
                    }
                }
            )
            Box(
                modifier         = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
                    .size(36.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication        = null,
                        onClick           = onToggleVisibility
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector        = if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = null,
                    tint               = onSurface
                )
            }
        }
    }
}

//
// SliorPrimaryButton – botón de acción
//
@Composable
fun SliorPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    Box(
        modifier         = modifier
            .hardShadow(
                offsetX = SliorDesignTokens.ShadowOffsetLg,
                offsetY = SliorDesignTokens.ShadowOffsetLg,
                color   = BrutalistBlack
            )
            .border(SliorDesignTokens.BorderWidthHeavy, BrutalistBlack)
            .background(if (enabled) NeonGreen else Color(0xFFB0B0B0))
            .height(SliorDesignTokens.ButtonHeight)
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text          = text,
                fontFamily    = SpaceGroteskFamily,
                fontWeight    = FontWeight.Black,
                fontSize      = 20.sp,
                letterSpacing = 2.sp,
                color         = BrutalistBlack // Siempre negro sobre neón
            )
            if (trailingIcon != null) {
                Spacer(Modifier.width(8.dp))
                trailingIcon()
            }
        }
    }
}

//
// SliorLoadingButton – estado de carga del botón
//
@Composable
fun SliorLoadingButton(modifier: Modifier = Modifier) {
    Box(
        modifier         = modifier
            .border(SliorDesignTokens.BorderWidthHeavy, BrutalistBlack)
            .background(Color(0xFFB0B0B0))
            .height(SliorDesignTokens.ButtonHeight),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color       = BrutalistBlack,
            modifier    = Modifier.size(28.dp),
            strokeWidth = 3.dp
        )
    }
}

//
// SliorErrorBanner – banner de alerta
//
@Composable
fun SliorErrorBanner(
    message: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier              = modifier
            .hardShadow(color = BrutalistBlack)
            .border(SliorDesignTokens.BorderWidth, BrutalistBlack)
            .background(SafetyOrange)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector        = Icons.Default.Warning,
            contentDescription = null,
            tint               = Color.White,
            modifier           = Modifier.size(22.dp)
        )
        Text(
            text          = message,
            fontFamily    = SpaceGroteskFamily,
            fontWeight    = FontWeight.Black,
            fontSize      = 12.sp,
            letterSpacing = 1.5.sp,
            color         = Color.White
        )
    }
}

//
// ConnectivityBanner – banner de estado de conexión
//
@Composable
fun ConnectivityBanner(
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    if (!isConnected) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .background(BrutalistBlack)
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.banner_offline),
                color = NeonGreen,
                fontFamily = SpaceGroteskFamily,
                fontWeight = FontWeight.Black,
                fontSize = 11.sp,
                letterSpacing = 2.sp
            )
        }
    }
}

//
// SliorAccentBar – barra decorativa
//
@Composable
fun SliorAccentBar(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(8.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(SafetyOrange)
        )
        Box(
            modifier = Modifier
                .width(2.dp)
                .fillMaxHeight()
                .background(BrutalistBlack)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(NeonGreen)
        )
        Box(
            modifier = Modifier
                .width(2.dp)
                .fillMaxHeight()
                .background(BrutalistBlack)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surface)
        )
    }
}

@Composable
fun SliorDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(2.dp)
            .background(BrutalistBlack)
    )
}

@Composable
fun SliorSidebar() {
    val background = MaterialTheme.colorScheme.background
    Row(modifier = Modifier.fillMaxHeight().width(12.dp)) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(BrutalistBlack)
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .background(background)
        )
    }
}

/**
 * Indicador visual para requisitos de validación (usado en contraseñas).
 */
@Composable
fun ValidationHint(
    text: String,
    isValid: Boolean,
    modifier: Modifier = Modifier
) {
    val onSurface = MaterialTheme.colorScheme.onSurface
    Row(
        modifier          = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .border(2.dp, BrutalistBlack)
                .background(if (isValid) NeonGreen else onSurface.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            if (isValid) {
                Text(
                    text       = "✓",
                    fontSize   = 10.sp,
                    fontWeight = FontWeight.Black,
                    color      = Color.Black
                )
            }
        }
        Spacer(Modifier.width(10.dp))
        Text(
            text       = text,
            fontFamily = SpaceGroteskFamily,
            fontSize   = 12.sp,
            fontWeight = if (isValid) FontWeight.Bold else FontWeight.Medium,
            color      = if (isValid) onSurface else onSurface.copy(alpha = 0.5f)
        )
    }
}
