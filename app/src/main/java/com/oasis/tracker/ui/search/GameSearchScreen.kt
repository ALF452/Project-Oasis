package com.oasis.tracker.ui.search

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.* // weight() as a single named import collides with an internal same-named property in this Compose version
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.oasis.tracker.data.FavoritesStore
import com.oasis.tracker.data.Platforms
import com.oasis.tracker.data.TopRankingStore
import com.oasis.tracker.network.GameSearchResult
import com.oasis.tracker.network.SearchOutcome
import com.oasis.tracker.network.SearchSource
import com.oasis.tracker.ui.components.NeonPanel
import com.oasis.tracker.ui.rememberOasisApp
import com.oasis.tracker.ui.theme.CharcoalBackground
import com.oasis.tracker.ui.theme.NeonBlue
import com.oasis.tracker.ui.theme.TextSecondary
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

/** Where a search result goes: tracked under a platform, parked in the backlog with no
 *  platform yet, or pinned as a favorite/ranked in the Top 250 (both also need a
 *  platform, chosen after picking the result). */
sealed interface GameSearchMode {
    data class AddToLibrary(val platformId: String) : GameSearchMode
    data object AddToBacklog : GameSearchMode
    data object AddToFavorites : GameSearchMode
    data object AddToTopRanking : GameSearchMode
}

@Composable
fun GameSearchScreen(
    mode: GameSearchMode,
    onBack: () -> Unit,
    onGameAdded: () -> Unit
) {
    val app = rememberOasisApp()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var query by remember { mutableStateOf("") }
    // Set when a result is tapped in AddToFavorites/AddToTopRanking mode: both still
    // need a platform (every tracked game has one), so the actual add waits on this.
    var pendingPlatformPick by remember { mutableStateOf<GameSearchResult?>(null) }
    var results by remember { mutableStateOf<List<GameSearchResult>>(emptyList()) }
    var loading by remember { mutableStateOf(false) }
    var searched by remember { mutableStateOf(false) }
    var searchFailed by remember { mutableStateOf(false) }
    // Guards against a fast double-tap on a result adding the same game twice:
    // the add is a suspend DB write, and onGameAdded() only navigates away once
    // it completes, leaving the row clickable for that whole window otherwise.
    var adding by remember { mutableStateOf(false) }
    // Cancelled and replaced on every new search: without this, firing a second
    // search before the first responds (e.g. editing the query and hitting search
    // again quickly) could let the older, slower request land after the newer one
    // and overwrite its results with stale data.
    var searchJob by remember { mutableStateOf<Job?>(null) }

    fun runSearch() {
        if (query.isBlank()) return
        loading = true
        searched = true
        searchFailed = false
        searchJob?.cancel()
        searchJob = scope.launch {
            when (val outcome = app.searchRepository.search(query.trim())) {
                is SearchOutcome.Success -> results = outcome.results
                is SearchOutcome.BothSourcesFailed -> {
                    results = emptyList()
                    searchFailed = true
                }
            }
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        val title = when (mode) {
            GameSearchMode.AddToBacklog -> "Add to Backlog"
            GameSearchMode.AddToFavorites -> "Add Favorite"
            GameSearchMode.AddToTopRanking -> "Add to Top 250"
            is GameSearchMode.AddToLibrary -> "Add Game"
        }
        TopAppBar(
            title = { Text(title, style = MaterialTheme.typography.titleLarge) },
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
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            singleLine = true,
            placeholder = { Text("Search Wikipedia & archive.org…") },
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Search
            ),
            keyboardActions = androidx.compose.foundation.text.KeyboardActions(onSearch = { runSearch() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonBlue,
                unfocusedBorderColor = TextSecondary,
                cursorColor = NeonBlue
            )
        )

        when {
            loading -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = NeonBlue)
            }

            searchFailed -> Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    "Couldn't reach Wikipedia or archive.org. Check your connection and try again.",
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                TextButton(onClick = { runSearch() }) { Text("RETRY") }
            }

            searched && results.isEmpty() -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No results. Try a different search.", color = TextSecondary)
            }

            else -> LazyColumn(
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(results) { result: GameSearchResult ->
                    SearchResultRow(
                        result = result,
                        enabled = !adding,
                        onClick = {
                            when (mode) {
                                is GameSearchMode.AddToLibrary -> {
                                    adding = true
                                    scope.launch {
                                        app.gameRepository.addGame(
                                            platformId = mode.platformId,
                                            title = result.title,
                                            coverUrl = result.coverUrl,
                                            sourceUrl = result.sourceUrl,
                                            summary = result.subtitle
                                        )
                                        onGameAdded()
                                    }
                                }
                                GameSearchMode.AddToBacklog -> {
                                    adding = true
                                    scope.launch {
                                        app.gameRepository.addToBacklog(
                                            title = result.title,
                                            coverUrl = result.coverUrl,
                                            sourceUrl = result.sourceUrl,
                                            summary = result.subtitle
                                        )
                                        onGameAdded()
                                    }
                                }
                                GameSearchMode.AddToFavorites, GameSearchMode.AddToTopRanking -> pendingPlatformPick = result
                            }
                        }
                    )
                }
            }
        }
    }

    pendingPlatformPick?.let { result ->
        PlatformPickerDialog(
            onSelect = { platformId ->
                pendingPlatformPick = null
                adding = true
                scope.launch {
                    val gameId = app.gameRepository.addGame(
                        platformId = platformId,
                        title = result.title,
                        coverUrl = result.coverUrl,
                        sourceUrl = result.sourceUrl,
                        summary = result.subtitle
                    )
                    when (mode) {
                        GameSearchMode.AddToFavorites -> FavoritesStore(context).addFavorite(gameId)
                        GameSearchMode.AddToTopRanking -> TopRankingStore(context).addGame(gameId)
                        else -> Unit
                    }
                    onGameAdded()
                }
            },
            onDismiss = { pendingPlatformPick = null }
        )
    }
}

@Composable
private fun PlatformPickerDialog(onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Which platform?", color = NeonBlue) },
        text = {
            // A plain scrollable Column, not LazyColumn: AlertDialog's own content area
            // can itself be scrollable when it overflows, and a lazy list nested inside
            // another scrollable in the same orientation is a known Compose crash
            // ("measured with an infinite height"). 28 platforms is small enough that
            // there's no real virtualization benefit to a LazyColumn here anyway.
            Column(
                modifier = Modifier
                    .heightIn(max = 360.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Platforms.ALL.forEach { platform ->
                    Text(
                        platform.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(platform.id) }
                            .padding(vertical = 12.dp)
                    )
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("CANCEL") } }
    )
}

@Composable
private fun SearchResultRow(result: GameSearchResult, enabled: Boolean, onClick: () -> Unit) {
    NeonPanel(modifier = Modifier.fillMaxWidth().clickable(enabled = enabled, onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = result.coverUrl,
                contentDescription = result.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(56.dp).clip(RoundedCornerShape(8.dp))
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(result.title, style = MaterialTheme.typography.titleMedium)
                result.subtitle?.let {
                    Text(it, style = MaterialTheme.typography.bodyMedium, maxLines = 2)
                }
                Text(
                    text = if (result.source == SearchSource.WIKIPEDIA) "WIKIPEDIA" else "ARCHIVE.ORG",
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}
