package com.oasis.tracker.ui.topranking

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.* // weight() as a single named import collides with an internal same-named property in this Compose version
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.oasis.tracker.data.GameEntity
import com.oasis.tracker.data.Platforms
import com.oasis.tracker.data.TopRankingStore
import com.oasis.tracker.ui.components.ConfirmDialog
import com.oasis.tracker.ui.components.MilestoneBanner
import com.oasis.tracker.ui.components.NeonPanel
import com.oasis.tracker.ui.components.ReorderableTileGrid
import com.oasis.tracker.ui.components.milestoneMessage
import com.oasis.tracker.ui.rememberOasisApp
import com.oasis.tracker.ui.theme.CharcoalBackground
import com.oasis.tracker.ui.theme.NeonBlue
import com.oasis.tracker.ui.theme.TextSecondary

@Composable
fun Top250Screen(
    onBack: () -> Unit,
    onOpenGame: (Long) -> Unit,
    onAddGame: () -> Unit
) {
    val app = rememberOasisApp()
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val store = remember { TopRankingStore(context) }
    val allGames by app.gameRepository.allGames().collectAsState(initial = null)
    val averageRatings by app.gameRepository.averageRatings().collectAsState(initial = emptyList())
    val ratingByGameId = remember(averageRatings) { averageRatings.associate { it.gameId to it.avgRating } }

    var rankingIds by remember { mutableStateOf<List<Long>>(emptyList()) }
    var gamePendingRemoval by remember { mutableStateOf<GameEntity?>(null) }
    // null until the ranking has loaded for real once — guards the very first load
    // (going from "nothing loaded yet" to whatever was already ranked) from being
    // mistaken for a burst of brand-new milestones.
    var previousRankedCount by remember { mutableStateOf<Int?>(null) }
    var milestone by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(allGames) {
        val games = allGames ?: return@LaunchedEffect
        val loaded = store.loadRanking(games.map { it.id }.toSet())
        val previous = previousRankedCount
        if (previous != null) {
            milestoneMessage(previous, loaded.size, TOP_RANKING_MILESTONES) { crossed ->
                if (crossed >= TopRankingStore.MAX_RANKED) "Top 250 complete — every slot ranked!" else "$crossed games ranked!"
            }?.let {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                milestone = it
            }
        }
        previousRankedCount = loaded.size
        rankingIds = loaded
    }

    // Ref-counted rather than a plain flag: a single press-then-drag can ask
    // to block scroll twice in a row (the press's bounded window, then the
    // drag's own lifecycle), and both requests must clear before scroll
    // resumes — a plain boolean can have the first request's release
    // re-enable scroll while the second (the actual drag) is still active.
    var scrollBlockCount by remember { mutableStateOf(0) }
    val scrollEnabled = scrollBlockCount == 0

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("TOP 250", style = MaterialTheme.typography.titleLarge) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = NeonBlue)
                }
            },
            actions = {
                IconButton(onClick = onAddGame) {
                    Icon(Icons.Filled.Add, contentDescription = "Add game", tint = NeonBlue)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = CharcoalBackground)
        )

        MilestoneBanner(message = milestone, onDismiss = { milestone = null })

        val games = allGames
        val rankedGames = remember(games, rankingIds) {
            val byId = games?.associateBy { it.id }.orEmpty()
            rankingIds.mapNotNull { byId[it] }
        }

        when {
            games == null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NeonBlue)
            }
            rankedGames.isEmpty() -> Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(
                    "Your Top 250 is empty. Tap + to search for a game and start ranking.",
                    color = TextSecondary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            else -> Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState(), enabled = scrollEnabled)
                    .padding(16.dp)
            ) {
                ReorderableTileGrid(
                    items = rankedGames,
                    itemId = { it.id.toString() },
                    columns = 1,
                    tileHeight = 84.dp,
                    onOrderChanged = { newOrder ->
                        rankingIds = newOrder.map { it.id }
                        store.saveRanking(rankingIds)
                    },
                    onItemClick = { game -> onOpenGame(game.id) },
                    onPressActiveChanged = { active -> scrollBlockCount += if (active) 1 else -1 },
                    modifier = Modifier.fillMaxWidth()
                ) { game, index ->
                    TopRankRow(
                        rank = index + 1,
                        game = game,
                        avgRating = ratingByGameId[game.id],
                        onRemove = { gamePendingRemoval = game }
                    )
                }
            }
        }
    }

    gamePendingRemoval?.let { game ->
        ConfirmDialog(
            title = "Remove from Top 250?",
            message = "\"${game.title}\" will be taken off your ranking. The game itself stays in your library.",
            confirmLabel = "REMOVE",
            onConfirm = {
                rankingIds = rankingIds - game.id
                store.removeGame(game.id)
                gamePendingRemoval = null
            },
            onDismiss = { gamePendingRemoval = null }
        )
    }
}

@Composable
private fun TopRankRow(rank: Int, game: GameEntity, avgRating: Float?, onRemove: () -> Unit) {
    NeonPanel(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier.size(32.dp).clip(CircleShape).background(CharcoalBackground),
                contentAlignment = Alignment.Center
            ) {
                Text("$rank", style = MaterialTheme.typography.labelLarge, color = NeonBlue)
            }
            AsyncImage(
                model = game.coverUrl,
                contentDescription = game.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(6.dp))
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    game.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        Platforms.byId(game.platformId).displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    avgRating?.let { rating ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = NeonBlue, modifier = Modifier.size(14.dp))
                            Text(" %.1f".format(rating), style = MaterialTheme.typography.bodyMedium, color = NeonBlue)
                        }
                    }
                }
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Close, contentDescription = "Remove from ranking", tint = TextSecondary)
            }
        }
    }
}

private val TOP_RANKING_MILESTONES = listOf(1, 10, 25, 50, 100, 150, 200, TopRankingStore.MAX_RANKED)
