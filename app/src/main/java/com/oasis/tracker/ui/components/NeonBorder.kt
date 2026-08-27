package com.oasis.tracker.ui.components

import android.graphics.BlurMaskFilter
import android.graphics.Paint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asAndroidPath
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.oasis.tracker.ui.theme.CharcoalSurface
import com.oasis.tracker.ui.theme.NeonBlue

/**
 * Draws a soft glowing outline behind the composable using a software blur
 * mask filter, then a crisp neon stroke on top. Offscreen compositing is
 * required for BlurMaskFilter to render on a hardware-accelerated canvas.
 */
fun Modifier.neonGlowBorder(
    cornerRadius: Dp = 12.dp,
    strokeWidth: Dp = 1.5.dp,
    glowRadius: Dp = 10.dp,
    color: Color = NeonBlue
): Modifier = this
    .graphicsLayer(compositingStrategy = CompositingStrategy.Offscreen)
    .drawBehind {
        val strokePx = strokeWidth.toPx()
        val glowPx = glowRadius.toPx()
        val radiusPx = cornerRadius.toPx()
        val inset = strokePx / 2
        val rect = Rect(
            offset = Offset(inset, inset),
            size = Size(size.width - strokePx, size.height - strokePx)
        )
        val androidPath = Path().apply {
            addRoundRect(RoundRect(rect, CornerRadius(radiusPx, radiusPx)))
        }.asAndroidPath()

        drawIntoCanvas { canvas ->
            val glowPaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeWidth = strokePx * 2.5f
                this.color = color.copy(alpha = 0.9f).toArgb()
                maskFilter = BlurMaskFilter(glowPx, BlurMaskFilter.Blur.NORMAL)
            }
            canvas.nativeCanvas.drawPath(androidPath, glowPaint)

            val corePaint = Paint().apply {
                isAntiAlias = true
                style = Paint.Style.STROKE
                strokeWidth = strokePx
                this.color = color.toArgb()
            }
            canvas.nativeCanvas.drawPath(androidPath, corePaint)
        }
    }

/**
 * Simple reusable panel: dark charcoal surface with a glowing neon border,
 * used for menu tiles, cards, and dialogs throughout the app.
 */
@Composable
fun NeonPanel(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 14.dp,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(cornerRadius))
            .background(CharcoalSurface)
            .neonGlowBorder(cornerRadius = cornerRadius)
    ) {
        content()
    }
}
