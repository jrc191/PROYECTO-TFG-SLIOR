package com.slior.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slior.ui.theme.BrutalistBlack
import com.slior.ui.theme.BrutalistLightGray
import com.slior.ui.theme.BrutalistWhite
import com.slior.ui.theme.NeonGreen
import com.slior.ui.theme.SafetyOrange
import com.slior.ui.theme.SpaceGroteskFamily

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
        color         = BrutalistBlack,
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
            color       = BrutalistBlack
        ),
        cursorBrush     = SolidColor(NeonGreen),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier        = fieldModifier
            .background(BrutalistWhite)
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
                        color      = Color(0xFF9E9E9E)
                    )
                }
                innerTextField()
            }
        }
    )
}

// 
// SliorPasswordField – campo contraseña con ojo de visibilidad
//   loginStyle = true  → ojo como sección separada con borde izquierdo (login)
//   loginStyle = false → ojo superpuesto dentro del campo (registro)
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
    val visual = if (visible) VisualTransformation.None else PasswordVisualTransformation()

    if (loginStyle) {
        // Estilo login: [input | | ]  borde envuelve input + botón
        val rowModifier = if (withShadow)
            modifier
                .hardShadow()
                .border(SliorDesignTokens.BorderWidth, BrutalistBlack)
        else
            modifier.border(SliorDesignTokens.BorderWidth, BrutalistBlack)

        Row(
            modifier          = rowModifier
                .background(BrutalistWhite)
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
                    color       = BrutalistBlack
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
                                color      = Color(0xFF9E9E9E)
                            )
                        }
                        innerTextField()
                    }
                }
            )
            // Separador vertical
            Box(
                modifier = Modifier
                    .width(SliorDesignTokens.BorderWidth)
                    .fillMaxHeight()
                    .background(BrutalistBlack)
            )
            // Botón ojo
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
                    contentDescription = if (visible) "Ocultar contraseña" else "Ver contraseña",
                    tint               = BrutalistBlack
                )
            }
        }
    } else {
        // Registro
        val boxModifier = if (withShadow)
            modifier
                .hardShadow()
                .border(SliorDesignTokens.BorderWidth, BrutalistBlack)
        else
            modifier.border(SliorDesignTokens.BorderWidth, BrutalistBlack)

        Box(
            modifier = boxModifier
                .background(BrutalistWhite)
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
                    color       = BrutalistBlack
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
                                color      = Color(0xFF9E9E9E)
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
                    contentDescription = if (visible) "Ocultar contraseña" else "Ver contraseña",
                    tint               = BrutalistBlack
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
                offsetY = SliorDesignTokens.ShadowOffsetLg
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
                color         = BrutalistBlack
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
            .hardShadow()
            .border(SliorDesignTokens.BorderWidth, BrutalistBlack)
            .background(SafetyOrange)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Icon(
            imageVector        = Icons.Default.Warning,
            contentDescription = null,
            tint               = BrutalistWhite,
            modifier           = Modifier.size(22.dp)
        )
        Text(
            text          = message,
            fontFamily    = SpaceGroteskFamily,
            fontWeight    = FontWeight.Black,
            fontSize      = 12.sp,
            letterSpacing = 1.5.sp,
            color         = BrutalistWhite
        )
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
                .background(BrutalistLightGray)
        )
    }
}
