package com.slior.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
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

@Composable
fun SliorAccentBar(modifier: Modifier = Modifier) {
    Row(modifier = modifier.height(12.dp).fillMaxWidth()) {
        Box(modifier = Modifier.weight(0.7f).fillMaxHeight().background(BrutalistBlack))
        Box(modifier = Modifier.weight(0.3f).fillMaxHeight().background(NeonGreen))
    }
}

@Composable
fun Modifier.hardShadow(
    offsetX: Dp = 4.dp,
    offsetY: Dp = 4.dp,
    color: Color = BrutalistBlack
) = this.drawBehind {
    drawRect(
        color = color,
        topLeft = Offset(offsetX.toPx(), offsetY.toPx()),
        size = size
    )
}

@Composable
fun SliorFieldLabel(text: String) {
    Text(
        text = text,
        fontFamily = SpaceGroteskFamily,
        fontWeight = FontWeight.Black,
        fontSize = 12.sp,
        letterSpacing = 2.sp,
        color = BrutalistBlack,
        modifier = Modifier.padding(bottom = 8.dp)
    )
}

@Composable
fun SliorTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    keyboardType: KeyboardType = KeyboardType.Text,
    withShadow: Boolean = false
) {
    val baseModifier = Modifier
        .fillMaxWidth()
        .height(56.dp)
        .border(2.dp, BrutalistBlack)
        .background(BrutalistWhite)
    
    val modifier = if (withShadow) baseModifier.hardShadow() else baseModifier

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = TextStyle(
            fontFamily = SpaceGroteskFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = BrutalistBlack
        ),
        cursorBrush = SolidColor(NeonGreen),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        decorationBox = { innerTextField ->
            Box(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        fontFamily = SpaceGroteskFamily,
                        fontSize = 16.sp,
                        color = Color(0xFFB0B0B0)
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
fun SliorPasswordField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    visible: Boolean,
    onToggleVisibility: () -> Unit,
    loginStyle: Boolean = true,
    withShadow: Boolean = false
) {
    val baseModifier = Modifier
        .fillMaxWidth()
        .height(56.dp)
        .border(2.dp, BrutalistBlack)
        .background(if (loginStyle) BrutalistWhite else Color.White)
    
    val modifier = if (withShadow) baseModifier.hardShadow() else baseModifier

    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = TextStyle(
            fontFamily = SpaceGroteskFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = BrutalistBlack
        ),
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        cursorBrush = SolidColor(NeonGreen),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        singleLine = true,
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterStart) {
                    if (value.isEmpty()) {
                        Text(
                            text = placeholder,
                            fontFamily = SpaceGroteskFamily,
                            fontSize = 16.sp,
                            color = Color(0xFFB0B0B0)
                        )
                    }
                    innerTextField()
                }
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        imageVector = if (visible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = null,
                        tint = BrutalistBlack
                    )
                }
            }
        }
    )
}

@Composable
fun SliorPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .then(if (enabled) Modifier.hardShadow(6.dp, 6.dp, NeonGreen) else Modifier)
            .height(64.dp)
            .border(2.dp, BrutalistBlack)
            .background(if (enabled) BrutalistBlack else Color(0xFFD4D4D8))
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = SpaceGroteskFamily,
            fontWeight = FontWeight.Black,
            fontSize = 18.sp,
            letterSpacing = 2.sp,
            color = if (enabled) BrutalistWhite else Color(0xFF71717A)
        )
    }
}

@Composable
fun SliorLoadingButton(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(64.dp)
            .border(2.dp, BrutalistBlack)
            .background(BrutalistBlack),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = NeonGreen,
            modifier = Modifier.size(28.dp),
            strokeWidth = 3.dp
        )
    }
}

@Composable
fun SliorErrorBanner(message: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .border(2.dp, BrutalistBlack)
            .background(OfflineRed)
            .padding(16.dp)
    ) {
        Text(
            text = message,
            fontFamily = SpaceGroteskFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = BrutalistWhite
        )
    }
}

@Composable
fun ConnectivityBanner(isConnected: Boolean, modifier: Modifier = Modifier) {
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
                .background(BrutalistLightGray)
        )
    }
}
