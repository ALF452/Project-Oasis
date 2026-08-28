package com.oasis.tracker.ui.platform

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.oasis.tracker.data.GameEntity
import com.oasis.tracker.data.Platforms
import com.oasis.tracker.ui.components.NeonPanel
import com.oasis.tracker.ui.rememberOasisApp
import com.oasis.tracker.ui.theme.CharcoalBackground
import com.oasis.tracker.ui.theme.NeonBlue
import com.oasis.tracker.ui.theme.TextSecondary

private enum class LibrarySort(val label: String) {
    TITLE("Title (A-Z)"),
    MOST_PLAYED("Most played"),
    HIGHEST_RATED("Highest rated")
}

@Composable
fun PlatformDetailScreen(
    platformId: String,
    onBack: () -> Unit,
    onAddGame: () -> Unit,
    onOpenGame: (Long) -> Unit
) {
    val app = rememberOasisApp()
    val platform = Platforms.byId(platformId)
    val games by app.gameRepository.gamesForPlatform(platformId).collectAsState(initial = emptyList())
    val totalHours by app.gameRepository.totalHoursByGame().collectAsState(initial = emptyList())
    val averageRatings by app.gameRepository.averageRatings().collectAsState(initial = emptyList())
    val hoursByGameId = remember(totalHours) { totalHours.associate { it.gameId to it.totalHours } }
    val ratingByGameId = remember(averageRatings) { averageRatings.associate { it.gameId to it.avgRating } }

    var sort by remember { mutableStateOf(LibrarySort.TITLE) }
    var sortMenuOpen by remember { mutableStateOf(false) }
    val sortedGames = remember(games, sort, hoursByGameId, ratingByGameId) {
        when (sort) {
            LibrarySort.TITLE -> games.sortedBy { it.title.lowercase() }
            LibrarySort.MOST_PLAYED -> games.sortedByDescending { hoursByGameId[it.id] ?: 0f }
            LibrarySort.HIGHEST_RATED -> games.sortedByDescending { ratingByGameId[it.id] ?: -1f }
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(platform.displayName, style = MaterialTheme.typography.titleLarge) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = NeonBlue)
                }
            },
            actions = {
                Box {
                    IconButton(onClick = { sortMenuOpen = true }) {
                        Icon(Icons.Filled.Sort, contentDescription = "Sort", tint = NeonBlue)
                    }
                    DropdownMenu(expanded = sortMenuOpen, onDismissRequest = { sortMenuOpen = false }) {
                        LibrarySort.entries.forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.label) },
                                onClick = {
                                    sort = option
                                    sortMenuOpen = false
                                }
                            )
                        }
                    }
                }
                IconButton(onClick = onAddGame) {
                    Icon(Icons.Filled.Add, contentDescription = "Add game", tint = NeonBlue)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = CharcoalBackground)
        )

        if (games.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No games yet. Tap + to search and add one.", color = TextSecondary)
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(sortedGames, key = { it.id }) { game: GameEntity ->
                    GameRow(
                        game = game,
                        hours = hoursByGameId[game.id],
                        rating = ratingByGameId[game.id],
                        onClick = { onOpenGame(game.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun GameRow(game: GameEntity, hours: Float?, rating: Float?, onClick: () -> Unit) {
    NeonPanel(modifier = Modifier
        .fillMaxWidth()
        .clickable(onClick = onClick)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = game.coverUrl,
                contentDescription = game.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(8.dp))
            )
            Column {
                Text(game.title, style = MaterialTheme.typography.titleMedium)
                game.summary?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                }
                if (hours != null || rating != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        hours?.let { Text("%.1f h".format(it), style = MaterialTheme.typography.bodyMedium, color = NeonBlue) }
                        rating?.let {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Star, contentDescription = null, tint = NeonBlue, modifier = Modifier.size(14.dp))
                                Text(" %.1f".format(it), style = MaterialTheme.typography.bodyMedium, color = NeonBlue)
                            }
                        }
                    }
                }
            }
        }
    }
}
