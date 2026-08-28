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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.oasis.tracker.data.GameEntity
import com.oasis.tracker.data.Platforms
import com.oasis.tracker.data.TopRankingStore
import com.oasis.tracker.ui.components.NeonPanel
import com.oasis.tracker.ui.components.ReorderableTileGrid
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
    val store = remember { TopRankingStore(context) }
    val allGames by app.gameRepository.allGames().collectAsState(initial = null)

    var rankingIds by remember { mutableStateOf<List<Long>>(emptyList()) }
    LaunchedEffect(allGames) {
        val games = allGames ?: return@LaunchedEffect
        rankingIds = store.loadRanking(games.map { it.id }.toSet())
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
                        onRemove = {
                            rankingIds = rankingIds - game.id
                            store.removeGame(game.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun TopRankRow(rank: Int, game: GameEntity, onRemove: () -> Unit) {
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
                Text(game.title, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(
                    Platforms.byId(game.platformId).displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            IconButton(onClick = onRemove) {
                Icon(Icons.Filled.Close, contentDescription = "Remove from ranking", tint = TextSecondary)
            }
        }
    }
}
