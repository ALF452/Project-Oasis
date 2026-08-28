package com.oasis.tracker.ui.steam

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.oasis.tracker.network.steam.SteamConnectionState
import com.oasis.tracker.network.steam.SteamGameSummary
import com.oasis.tracker.network.steam.SteamProfile
import com.oasis.tracker.ui.components.NeonPanel
import com.oasis.tracker.ui.rememberOasisApp
import com.oasis.tracker.ui.theme.CharcoalBackground
import com.oasis.tracker.ui.theme.NeonBlue
import com.oasis.tracker.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.time.LocalDate

@Composable
fun SteamScreen(onBack: () -> Unit, onOpenLogin: () -> Unit, onOpenGameAchievements: (Int) -> Unit) {
    val app = rememberOasisApp()
    val scope = rememberCoroutineScope()
    val repo = app.steamRepository
    val connectionState by repo.connectionState.collectAsState()

    LaunchedEffect(Unit) {
        repo.restoreSession()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Steam", style = MaterialTheme.typography.titleLarge) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = NeonBlue)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = CharcoalBackground)
        )

        if (!repo.isConfigured) {
            CenteredMessage("Steam integration isn't configured yet. Add a STEAM_API_KEY repository secret to enable it.")
            return@Column
        }

        when (val state = connectionState) {
            is SteamConnectionState.Disconnected -> ConnectPrompt(onConnect = onOpenLogin)
            is SteamConnectionState.Connecting -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = NeonBlue)
            }
            is SteamConnectionState.Error -> CenteredMessage(state.message)
            is SteamConnectionState.Connected -> ConnectedLibrary(
                profile = state.profile,
                onOpenGame = onOpenGameAchievements,
                onDisconnect = { scope.launch { repo.disconnect() } }
            )
        }
    }
}

@Composable
private fun ConnectPrompt(onConnect: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            "Connect your Steam account to see your library and achievements here.",
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 20.dp)
        )
        NeonPanel(modifier = Modifier.fillMaxWidth().clickable(onClick = onConnect)) {
            Text(
                "CONNECT STEAM ACCOUNT",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                color = NeonBlue
            )
        }
    }
}

@Composable
private fun ConnectedLibrary(
    profile: SteamProfile,
    onOpenGame: (Int) -> Unit,
    onDisconnect: () -> Unit
) {
    var games by remember(profile.steamId) { mutableStateOf<List<SteamGameSummary>?>(null) }
    var importing by remember { mutableStateOf(false) }
    var importResult by remember { mutableStateOf<String?>(null) }
    val app = rememberOasisApp()
    val scope = rememberCoroutineScope()

    LaunchedEffect(profile.steamId) {
        games = app.steamRepository.getOwnedGames(profile.steamId)
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = profile.avatarUrl,
                contentDescription = profile.personaName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(56.dp).clip(CircleShape)
            )
            Column(modifier = Modifier.padding(start = 12.dp).weight(1f)) {
                Text(profile.personaName, style = MaterialTheme.typography.titleMedium)
                if (!profile.isPublic) {
                    Text(
                        "Profile is private — achievement data may be unavailable",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
            }
            TextButton(onClick = onDisconnect) { Text("DISCONNECT") }
        }

        val currentGames = games
        if (currentGames != null) {
            NeonPanel(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clickable(enabled = !importing) {
                        importing = true
                        scope.launch {
                            val playedGames = currentGames.filter { it.playtimeMinutes > 0 }
                            val alreadyImported = app.gameRepository.importedSteamAppIds()
                            val newGames = playedGames.filter { it.appId !in alreadyImported }
                            for (game in newGames) {
                                val gameId = app.gameRepository.addGameFromSteam(
                                    title = game.name,
                                    coverUrl = "https://cdn.cloudflare.steamstatic.com/steam/apps/${game.appId}/library_600x900.jpg",
                                    sourceUrl = "https://store.steampowered.com/app/${game.appId}",
                                    steamAppId = game.appId
                                )
                                app.gameRepository.logSession(
                                    gameId = gameId,
                                    date = LocalDate.now(),
                                    hours = game.playtimeMinutes / 60f,
                                    notes = "Imported from Steam — lifetime playtime as of ${LocalDate.now()}"
                                )
                            }
                            val skipped = playedGames.size - newGames.size
                            importResult = "Imported ${newGames.size} game(s)" +
                                if (skipped > 0) " · $skipped already in your PC library" else ""
                            importing = false
                        }
                    }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        if (importing) "IMPORTING…" else "IMPORT PLAYED GAMES TO PC LIBRARY",
                        style = MaterialTheme.typography.labelLarge
                    )
                    if (importing) CircularProgressIndicator(modifier = Modifier.size(18.dp), color = NeonBlue)
                }
            }
            importResult?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
            }
        }

        when {
            currentGames == null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NeonBlue)
            }
            currentGames.isEmpty() -> CenteredMessage("No games found on this Steam library.")
            else -> LazyColumn(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(currentGames, key = { it.appId }) { game: SteamGameSummary ->
                    SteamGameRow(game = game, onClick = { onOpenGame(game.appId) })
                }
            }
        }
    }
}

@Composable
private fun SteamGameRow(game: SteamGameSummary, onClick: () -> Unit) {
    NeonPanel(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = game.iconUrl,
                contentDescription = game.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(6.dp))
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    game.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val hours = game.playtimeMinutes / 60f
                Text("%.1f hrs played".format(hours), style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
            }
        }
    }
}

@Composable
private fun CenteredMessage(message: String) {
    Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(message, color = TextSecondary)
    }
}
