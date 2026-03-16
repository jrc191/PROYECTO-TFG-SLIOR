package com.slior.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.slior.ui.theme.BrutalistBlack
import com.slior.ui.theme.BrutalistLightGray
import com.slior.ui.theme.BrutalistWhite
import com.slior.ui.theme.SafetyOrange
import com.slior.ui.theme.SpaceGroteskFamily

object SliorDesignTokens {
    val BorderWidth = 2.dp
    val BorderWidthHeavy = 4.dp
    val ShadowOffset = 6.dp
    val CornerRadius = 6.dp
}

fun Modifier.hardShadow(
    offsetX: Dp = SliorDesignTokens.ShadowOffset,
    offsetY: Dp = SliorDesignTokens.ShadowOffset,
    elevation: Dp = 8.dp
): Modifier {
    // Sombra simple; no se usa offset manual para evitar layouts extra.
    return this.shadow(elevation = elevation, spotColor = Color.Black.copy(alpha = 0.25f))
}

@Composable
fun SliorFieldLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        color = BrutalistBlack
    )
}

@Composable
fun SliorTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    modifier: Modifier = Modifier,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: (@Composable (() -> Unit))? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(text = placeholder, fontFamily = SpaceGroteskFamily, color = BrutalistLightGray) },
        modifier = modifier.fillMaxWidth(),
        textStyle = TextStyle(fontFamily = SpaceGroteskFamily, color = BrutalistBlack),
        shape = RoundedCornerShape(SliorDesignTokens.CornerRadius),
        keyboardOptions = keyboardOptions,
        trailingIcon = trailingIcon,
        colors = TextFieldDefaults.colors(
            unfocusedIndicatorColor = BrutalistBlack,
            focusedIndicatorColor = BrutalistBlack,
            cursorColor = BrutalistBlack,
            focusedContainerColor = BrutalistWhite,
            unfocusedContainerColor = BrutalistWhite
        )
    )
}

@Composable
fun SliorPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    trailingIcon: (@Composable (() -> Unit))? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(SliorDesignTokens.CornerRadius),
        colors = ButtonDefaults.buttonColors(
            containerColor = SafetyOrange,
            contentColor = BrutalistBlack,
            disabledContainerColor = BrutalistLightGray,
            disabledContentColor = BrutalistBlack.copy(alpha = 0.5f)
        ),
        border = BorderStroke(SliorDesignTokens.BorderWidth, BrutalistBlack)
    ) {
        Text(
            text = text.uppercase(),
            fontFamily = SpaceGroteskFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
        if (trailingIcon != null) {
            trailingIcon()
        }
    }
}
