package com.oasis.tracker.ui.diary

import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.oasis.tracker.data.LogEntryWithGame
import com.oasis.tracker.data.Platforms
import com.oasis.tracker.ui.components.NeonPanel
import com.oasis.tracker.ui.rememberOasisApp
import com.oasis.tracker.ui.theme.CharcoalBackground
import com.oasis.tracker.ui.theme.NeonBlue
import com.oasis.tracker.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Every logged session across every game and platform, most recent first —
 * Letterboxd's diary tab, applied to game hours instead of film watches.
 */
@Composable
fun DiaryFeedScreen(onBack: () -> Unit, onOpenGame: (Long) -> Unit) {
    val app = rememberOasisApp()
    val entries by app.gameRepository.allEntriesWithGame().collectAsState(initial = emptyList())

    // groupBy preserves key encounter order, and entries already arrive sorted
    // epochDay DESC from the query, so this comes out newest month first.
    val byMonth = remember(entries) {
        entries.groupBy { YearMonth.from(LocalDate.ofEpochDay(it.epochDay)) }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("DIARY", style = MaterialTheme.typography.titleLarge) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = NeonBlue)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = CharcoalBackground)
        )

        if (entries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(
                    "Nothing logged yet. Log a session from any game to see it here.",
                    color = TextSecondary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
                byMonth.forEach { (month, monthEntries) ->
                    item {
                        Text(
                            text = month.format(DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault())).uppercase(),
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondary,
                            modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
                        )
                    }
                    items(monthEntries, key = { it.id }) { entry: LogEntryWithGame ->
                        DiaryFeedRow(entry = entry, onClick = { onOpenGame(entry.gameId) })
                    }
                }
            }
        }
    }
}

@Composable
private fun DiaryFeedRow(entry: LogEntryWithGame, onClick: () -> Unit) {
    NeonPanel(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp).clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .border(1.dp, TextSecondary, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(LocalDate.ofEpochDay(entry.epochDay).dayOfMonth.toString(), style = MaterialTheme.typography.titleMedium)
            }
            AsyncImage(
                model = entry.coverUrl,
                contentDescription = entry.gameTitle,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(48.dp).clip(RoundedCornerShape(6.dp))
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.gameTitle, style = MaterialTheme.typography.titleMedium, maxLines = 1)
                Text(Platforms.byId(entry.platformId).displayName, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    entry.rating?.let { rating ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Star, contentDescription = null, tint = NeonBlue, modifier = Modifier.size(14.dp))
                            val ratingText = if (rating % 1f == 0f) rating.toInt().toString() else rating.toString()
                            Text(" $ratingText/10", style = MaterialTheme.typography.bodyMedium, color = NeonBlue)
                        }
                    }
                    Text("${entry.hours}h", style = MaterialTheme.typography.bodyMedium, color = NeonBlue)
                }
            }
        }
    }
}
