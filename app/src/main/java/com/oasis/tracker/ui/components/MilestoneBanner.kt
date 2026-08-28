package com.oasis.tracker.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.oasis.tracker.ui.theme.NeonPurple
import kotlinx.coroutines.delay

/**
 * A brief celebratory banner for milestones (hours played, favorites filled,
 * Top 250 progress) — pinned to the top of whatever screen hosts it, auto-
 * dismissing itself so callers don't need their own timer/state for that.
 */
@Composable
fun MilestoneBanner(message: String?, onDismiss: () -> Unit) {
    LaunchedEffect(message) {
        if (message != null) {
            delay(2400)
            onDismiss()
        }
    }
    Box(modifier = Modifier.fillMaxWidth()) {
        AnimatedVisibility(
            visible = message != null,
            enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
            exit = fadeOut() + slideOutVertically(targetOffsetY = { -it })
        ) {
            NeonPanel(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                borderColor = NeonPurple
            ) {
                Text(
                    text = message.orEmpty(),
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp, horizontal = 16.dp)
                )
            }
        }
    }
}

/**
 * Tracks a growing count (favorites filled, games ranked, hours on a game) and
 * returns a milestone message the moment it crosses a threshold worth
 * celebrating — null otherwise. Fires at most once per crossing: the caller
 * remembers the previous count itself via [previous], so a milestone already
 * reached before this screen opened doesn't re-fire on first composition.
 */
fun milestoneMessage(previous: Int?, current: Int, thresholds: List<Int>, message: (Int) -> String): String? {
    if (previous == null || current <= previous) return null
    val crossed = thresholds.lastOrNull { it in (previous + 1)..current } ?: return null
    return message(crossed)
}
