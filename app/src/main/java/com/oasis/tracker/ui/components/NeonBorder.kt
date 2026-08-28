package com.oasis.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.oasis.tracker.ui.theme.CharcoalSurface
import com.oasis.tracker.ui.theme.NeonBlue

/**
 * Draws a soft glowing outline behind the composable, then a crisp neon
 * stroke on top. Built from stacked translucent strokes rather than
 * BlurMaskFilter, which Android silently ignores on the hardware-accelerated
 * canvas Compose draws with — a real blur mask filter here would render as
 * just a plain stroke on any real device.
 */
fun Modifier.neonGlowBorder(
    cornerRadius: Dp = 12.dp,
    strokeWidth: Dp = 1.5.dp,
    glowRadius: Dp = 10.dp,
    color: Color = NeonBlue
): Modifier = this.drawBehind {
    val strokePx = strokeWidth.toPx()
    val radiusPx = cornerRadius.toPx()
    val cornerRadiusPx = CornerRadius(radiusPx, radiusPx)
    val maxExtraWidth = glowRadius.toPx()

    val glowLayers = 4
    for (layer in glowLayers downTo 1) {
        val fraction = layer / glowLayers.toFloat()
        val extraWidth = maxExtraWidth * fraction
        val totalWidth = strokePx + extraWidth
        val inset = totalWidth / 2
        drawRoundRect(
            color = color.copy(alpha = 0.28f * (1f - fraction) + 0.06f),
            topLeft = Offset(inset, inset),
            size = Size(size.width - inset * 2, size.height - inset * 2),
            cornerRadius = cornerRadiusPx,
            style = Stroke(width = totalWidth)
        )
    }

    val coreInset = strokePx / 2
    drawRoundRect(
        color = color,
        topLeft = Offset(coreInset, coreInset),
        size = Size(size.width - coreInset * 2, size.height - coreInset * 2),
        cornerRadius = cornerRadiusPx,
        style = Stroke(width = strokePx)
    )
}

/**
 * Simple reusable panel: dark charcoal surface with a glowing neon border,
 * used for menu tiles, cards, and dialogs throughout the app.
 */
@Composable
fun NeonPanel(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 14.dp,
    borderColor: Color = NeonBlue,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(CharcoalSurface)
            .neonGlowBorder(cornerRadius = cornerRadius, color = borderColor)
    ) {
        content()
    }
}
