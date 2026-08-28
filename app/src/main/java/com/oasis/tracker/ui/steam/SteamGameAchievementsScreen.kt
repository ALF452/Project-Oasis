package com.oasis.tracker.ui.steam

import androidx.compose.foundation.layout.* // weight() as a single named import collides with an internal same-named property in this Compose version
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.oasis.tracker.network.steam.SteamAchievement
import com.oasis.tracker.network.steam.SteamConnectionState
import com.oasis.tracker.network.steam.SteamGameAchievements
import com.oasis.tracker.ui.components.NeonPanel
import com.oasis.tracker.ui.rememberOasisApp
import com.oasis.tracker.ui.theme.CharcoalBackground
import com.oasis.tracker.ui.theme.NeonBlue
import com.oasis.tracker.ui.theme.TextSecondary

@Composable
fun SteamGameAchievementsScreen(appId: Int, onBack: () -> Unit) {
    val app = rememberOasisApp()
    val connectionState by app.steamRepository.connectionState.collectAsState()
    var result by remember(appId) { mutableStateOf<SteamGameAchievements?>(null) }

    LaunchedEffect(appId, connectionState) {
        val steamId = (connectionState as? SteamConnectionState.Connected)?.profile?.steamId
        result = if (steamId != null) {
            app.steamRepository.getAchievements(steamId, appId)
        } else {
            SteamGameAchievements(gameName = null, achievements = emptyList(), unavailableReason = "Not connected to Steam.")
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = {
                Text(
                    result?.gameName ?: "Achievements",
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = NeonBlue)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = CharcoalBackground)
        )

        val current = result
        when {
            current == null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NeonBlue)
            }
            current.unavailableReason != null -> Box(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(current.unavailableReason, color = TextSecondary)
            }
            else -> {
                val unlockedCount = current.achievements.count { it.unlocked }
                Text(
                    "$unlockedCount / ${current.achievements.size} unlocked",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                LazyColumn(
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(current.achievements, key = { it.apiName }) { achievement: SteamAchievement ->
                        AchievementRow(achievement)
                    }
                }
            }
        }
    }
}

@Composable
private fun AchievementRow(achievement: SteamAchievement) {
    NeonPanel(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = achievement.iconUrl,
                contentDescription = achievement.displayName,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .alpha(if (achievement.unlocked) 1f else 0.4f)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    achievement.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (achievement.unlocked) NeonBlue else TextSecondary
                )
                achievement.description?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}
