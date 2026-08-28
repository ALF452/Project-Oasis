package com.oasis.tracker.ui.favorites

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.* // weight() as a single named import collides with an internal same-named property in this Compose version
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.oasis.tracker.data.FavoritesStore
import com.oasis.tracker.data.GameEntity
import com.oasis.tracker.data.Platforms
import com.oasis.tracker.ui.components.NeonPanel
import com.oasis.tracker.ui.rememberOasisApp
import com.oasis.tracker.ui.theme.CharcoalBackground
import com.oasis.tracker.ui.theme.NeonBlue
import com.oasis.tracker.ui.theme.TextSecondary

/** Picks a game from the user's existing library to pin as a favorite on the main menu. */
@Composable
fun FavoritesPickerScreen(onBack: () -> Unit) {
    val app = rememberOasisApp()
    val context = LocalContext.current
    val store = remember { FavoritesStore(context) }
    val allGames by app.gameRepository.allGames().collectAsState(initial = null)
    var query by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("ADD FAVORITE", style = MaterialTheme.typography.titleLarge) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = NeonBlue)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = CharcoalBackground)
        )

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            singleLine = true,
            placeholder = { Text("Search your logged games") },
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonBlue,
                unfocusedBorderColor = TextSecondary,
                cursorColor = NeonBlue
            )
        )

        val games = allGames
        val alreadyFavorited = remember(games) {
            val ids = games?.map { it.id }?.toSet().orEmpty()
            store.loadFavorites(ids).toSet()
        }
        val candidates = remember(games, query, alreadyFavorited) {
            games.orEmpty()
                .filter { it.id !in alreadyFavorited && (query.isBlank() || it.title.contains(query, ignoreCase = true)) }
                .sortedBy { it.title }
        }

        when {
            games == null -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NeonBlue)
            }
            candidates.isEmpty() -> Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(
                    if (games.isEmpty()) {
                        "You haven't logged any games yet. Add and log a game first, then pin it here."
                    } else {
                        "No matches — every logged game is either a favorite already or filtered out."
                    },
                    color = TextSecondary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            else -> LazyColumn(
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(candidates, key = { it.id }) { game: GameEntity ->
                    CandidateRow(
                        game = game,
                        onClick = {
                            val ids = games.map { it.id }.toSet()
                            store.addFavorite(game.id, ids)
                            onBack()
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CandidateRow(game: GameEntity, onClick: () -> Unit) {
    NeonPanel(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
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
        }
    }
}
