package com.israadev.nuxlauncher.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.israadev.nuxlauncher.ui.theme.NuxColors
import com.israadev.nuxlauncher.ui.theme.NuxSizes
import com.israadev.nuxlauncher.ui.theme.LocalNuxScale
import com.israadev.nuxlauncher.ui.theme.resp

/**
 * Bayangan keras ala kartun: persegi bulat warna solid yang digeser ke kanan-bawah,
 * tanpa blur. Digambar di belakang komponen sehingga murah untuk GPU kecil.
 */
private fun Modifier.hardShadow(color: Color, offset: Dp, cornerRadius: Dp): Modifier =
    if (offset.value <= 0f || color.alpha <= 0f) {
        this
    } else {
        this.drawBehind {
            val o = offset.toPx()
            val r = cornerRadius.toPx()
            drawRoundRect(
                color = color,
                topLeft = Offset(o, o),
                size = size,
                cornerRadius = CornerRadius(r, r)
            )
        }
    }

/**
 * Kartu cartoon: latar putih, garis tepi tinta tebal, bayangan keras.
 */
@Composable
fun NuxCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color = NuxColors.SurfaceWhite,
    borderColor: Color = NuxColors.CardBorder,
    shadowColor: Color = NuxColors.CardBorder,
    shadowOffset: Dp = NuxSizes.ShadowOffset,
    cornerRadius: Dp = (20.dp).resp(),
    borderWidth: Dp = NuxSizes.BorderWidth,
    fillMaxHeight: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val cardModifier = if (fillMaxHeight) modifier.fillMaxSize() else modifier.fillMaxWidth()
    val shape = RoundedCornerShape(cornerRadius)

    Box(
        modifier = cardModifier
            .hardShadow(shadowColor, shadowOffset, cornerRadius)
            .background(backgroundColor, shape)
            .border(borderWidth, borderColor, shape)
            .clip(shape)
    ) {
        content()
    }
}

/**
 * Tombol cartoon: saat ditekan, wajah tombol bergeser masuk ke bayangannya.
 */
@Composable
fun NuxButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    backgroundColor: Color = NuxColors.ForestGreen,
    contentColor: Color = NuxColors.DarkGray,
    borderColor: Color = NuxColors.CardBorder,
    enabled: Boolean = true,
    shadowOffset: Dp = NuxSizes.ShadowOffset,
    cornerRadius: Dp = (14.dp).resp(),
    contentPadding: PaddingValues = PaddingValues(horizontal = (12.dp).resp(), vertical = (4.dp).resp()),
    content: @Composable RowScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val press by animateFloatAsState(
        targetValue = if (isPressed && enabled) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "btnPress"
    )

    val shape = RoundedCornerShape(cornerRadius)
    val activeShadow = if (enabled) shadowOffset * (1f - press) else 0.dp

    Box(
        modifier = modifier
            .graphicsLayer {
                val shift = if (enabled) shadowOffset.toPx() * press else 0f
                translationX = shift
                translationY = shift
            }
            .hardShadow(borderColor, activeShadow, cornerRadius)
            .clip(shape)
            .background(
                if (enabled) backgroundColor else NuxColors.SurfaceElevated.copy(alpha = 0.6f),
                shape
            )
            .border(
                width = NuxSizes.BorderWidth,
                color = if (enabled) borderColor else NuxColors.DarkGray.copy(alpha = 0.25f),
                shape = shape
            )
            .clickable(
                enabled = enabled,
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .defaultMinSize(minHeight = (32.dp).resp())
            .padding(contentPadding),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.runtime.CompositionLocalProvider(
            androidx.compose.material3.LocalContentColor provides contentColor
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                content()
            }
        }
    }
}

/**
 * Lencana kecil berbentuk pil dengan garis tepi tinta.
 */
@Composable
fun NuxBadge(
    text: String,
    backgroundColor: Color = NuxColors.SoftLime,
    textColor: Color = NuxColors.DarkGray,
    borderColor: Color? = null,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape((8.dp).resp())
    val effectiveBorderColor = borderColor ?: NuxColors.CardBorder

    Box(
        modifier = modifier
            .background(backgroundColor, shape)
            .border(1.5.dp, effectiveBorderColor, shape)
            .padding(horizontal = (8.dp).resp(), vertical = (3.dp).resp())
    ) {
        Text(
            text = text,
            color = textColor,
            fontSize = (10.sp).resp(),
            fontWeight = FontWeight.Bold,
            letterSpacing = (0.5.sp).resp(),
            maxLines = 1,
            softWrap = false
        )
    }
}

/**
 * Kolom isian dengan garis tepi tinta tebal.
 */
@Composable
fun NuxTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    textStyle: TextStyle? = null,
    visualTransformation: androidx.compose.ui.text.input.VisualTransformation = androidx.compose.ui.text.input.VisualTransformation.None,
    trailingContent: (@Composable () -> Unit)? = null
) {
    val shape = RoundedCornerShape((14.dp).resp())
    val resolvedTextStyle = textStyle ?: TextStyle(
        color = NuxColors.DarkGray,
        fontSize = (13.sp).resp(),
        fontWeight = FontWeight.SemiBold
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(NuxColors.SurfaceInput, shape)
            .border(NuxSizes.BorderWidth, NuxColors.CardBorder, shape)
            .padding(horizontal = (12.dp).resp(), vertical = (8.dp).resp()),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    color = NuxColors.GrayNeutral.copy(alpha = 0.7f),
                    fontSize = (13.sp).resp(),
                    fontWeight = FontWeight.Normal
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = resolvedTextStyle,
                cursorBrush = SolidColor(NuxColors.ForestGreen),
                singleLine = true,
                visualTransformation = visualTransformation,
                modifier = Modifier.fillMaxWidth()
            )
        }
        if (trailingContent != null) {
            Spacer(modifier = Modifier.width((8.dp).resp()))
            trailingContent()
        }
    }
}

/**
 * Dialog cartoon: kartu krem terang dengan tepi tinta dan latar redup.
 */
@Composable
fun NuxDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    fillMaxHeight: Boolean = false,
    content: @Composable () -> Unit
) {
    val isTablet = LocalNuxScale.current.isTablet
    val defaultWidthFraction = if (isTablet) 0.82f else 0.96f

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            decorFitsSystemWindows = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NuxColors.DarkGray.copy(alpha = 0.55f))
                .padding(horizontal = (12.dp).resp(), vertical = (8.dp).resp()),
            contentAlignment = Alignment.Center
        ) {
            val dialogModifier = if (fillMaxHeight) {
                modifier.fillMaxWidth(defaultWidthFraction).fillMaxHeight(0.96f)
            } else {
                modifier.fillMaxWidth(defaultWidthFraction).wrapContentHeight()
            }
            NuxCard(
                modifier = dialogModifier,
                backgroundColor = NuxColors.SurfaceElevated,
                borderColor = NuxColors.CardBorder,
                borderWidth = NuxSizes.BorderWidth,
                cornerRadius = (24.dp).resp(),
                fillMaxHeight = fillMaxHeight
            ) {
                content()
            }
        }
    }
}
