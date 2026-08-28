package com.oasis.tracker.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.* // weight() as a single named import collides with an internal same-named property in this Compose version
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.oasis.tracker.ui.theme.CharcoalSurface
import com.oasis.tracker.ui.theme.CharcoalSurfaceRaised

/**
 * A softly sweeping gradient brush for skeleton placeholders — reads as
 * "loading" rather than the dead, static gray boxes a plain background color
 * would give. Cheaper and more reliable than a real blur: just a linear
 * gradient whose offset is animated across the shape.
 */
@Composable
fun rememberShimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translate by transition.animateFloat(
        initialValue = -400f,
        targetValue = 400f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )
    return Brush.linearGradient(
        colors = listOf(CharcoalSurface, CharcoalSurfaceRaised, CharcoalSurface),
        start = Offset(translate - 200f, 0f),
        end = Offset(translate + 200f, 0f)
    )
}

/** A single shimmering rectangle, for building skeleton rows out of. */
@Composable
fun ShimmerBlock(modifier: Modifier = Modifier, cornerRadius: androidx.compose.ui.unit.Dp = 6.dp) {
    Column(modifier = modifier.clip(RoundedCornerShape(cornerRadius)).background(rememberShimmerBrush())) {}
}

/** Mimics a game-cover-plus-two-lines-of-text row while the real list is still loading. */
@Composable
fun ShimmerResultRow(modifier: Modifier = Modifier) {
    NeonPanel(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ShimmerBlock(modifier = Modifier.size(56.dp), cornerRadius = 8.dp)
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ShimmerBlock(modifier = Modifier.fillMaxWidth(0.7f).height(16.dp))
                ShimmerBlock(modifier = Modifier.fillMaxWidth(0.45f).height(12.dp))
            }
        }
    }
}

/** A handful of [ShimmerResultRow]s, standing in for a list that's still loading over the network. */
@Composable
fun ShimmerResultList(modifier: Modifier = Modifier, rows: Int = 6) {
    Column(
        modifier = modifier.fillMaxWidth().padding(PaddingValues(12.dp)),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        repeat(rows) { ShimmerResultRow() }
    }
}
