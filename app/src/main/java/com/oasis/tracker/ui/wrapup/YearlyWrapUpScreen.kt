package com.oasis.tracker.ui.wrapup

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.* // weight() as a single named import collides with an internal same-named property in this Compose version
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Share
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.oasis.tracker.data.LogEntryWithGame
import com.oasis.tracker.data.Platforms
import com.oasis.tracker.ui.components.NeonPanel
import com.oasis.tracker.ui.components.ShimmerResultList
import com.oasis.tracker.ui.rememberOasisApp
import com.oasis.tracker.ui.theme.CharcoalBackground
import com.oasis.tracker.ui.theme.NeonBlue
import com.oasis.tracker.ui.theme.NeonPurple
import com.oasis.tracker.ui.theme.TextSecondary

private data class WrapUpStats(
    val totalHours: Float,
    val totalSessions: Int,
    val uniqueGames: Int,
    val topGame: LogEntryWithGame?,
    val topGameHours: Float,
    val topRatedGame: LogEntryWithGame?,
    val topRatedAverage: Float
)

private fun List<LogEntryWithGame>.toWrapUpStats(): WrapUpStats? {
    if (isEmpty()) return null

    val byGame = groupBy { it.gameId }
    val hoursByGame = byGame.mapValues { (_, list) -> list.sumOf { it.hours.toDouble() }.toFloat() }
    val topGameId = hoursByGame.maxByOrNull { it.value }?.key
    val topGame = topGameId?.let { id -> byGame.getValue(id).first() }

    val ratingsByGame = byGame.mapValues { (_, list) ->
        list.mapNotNull { it.rating }
    }.filterValues { it.isNotEmpty() }
    val topRatedEntry = ratingsByGame.maxByOrNull { (_, ratings) -> ratings.average() }
    val topRatedGame = topRatedEntry?.let { (id, _) -> byGame.getValue(id).first() }
    val topRatedAverage = topRatedEntry?.value?.average()?.toFloat() ?: 0f

    return WrapUpStats(
        totalHours = sumOf { it.hours.toDouble() }.toFloat(),
        totalSessions = size,
        uniqueGames = byGame.size,
        topGame = topGame,
        topGameHours = topGameId?.let { hoursByGame.getValue(it) } ?: 0f,
        topRatedGame = topRatedGame,
        topRatedAverage = topRatedAverage
    )
}

/** A Letterboxd/Spotify-Wrapped-style recap card for one year of logged play. */
@Composable
fun YearlyWrapUpScreen(year: Int, onBack: () -> Unit, onOpenGame: (Long) -> Unit) {
    val app = rememberOasisApp()
    val context = LocalContext.current
    val entries by app.gameRepository.entriesForYear(year).collectAsState(initial = null)
    val stats = remember(entries) { entries?.toWrapUpStats() }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("$year WRAP-UP", style = MaterialTheme.typography.titleLarge) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = NeonBlue)
                }
            },
            actions = {
                if (stats != null) {
                    IconButton(onClick = { shareWrapUp(context, year, stats) }) {
                        Icon(Icons.Filled.Share, contentDescription = "Share", tint = NeonBlue)
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = CharcoalBackground)
        )

        when {
            entries == null -> ShimmerResultList(modifier = Modifier.fillMaxSize())
            stats == null -> Box(modifier = Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                Text(
                    "Nothing logged in $year yet. Play something and come back!",
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            else -> Column(
                modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                NeonPanel(modifier = Modifier.fillMaxWidth(), borderColor = NeonPurple) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("YOUR $year", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                        Text(
                            "%.1f hours".format(stats.totalHours),
                            style = MaterialTheme.typography.headlineLarge,
                            textAlign = TextAlign.Center
                        )
                        Text("played across ${stats.uniqueGames} games", color = TextSecondary)
                    }
                }

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatTile(label = "SESSIONS LOGGED", value = stats.totalSessions.toString(), modifier = Modifier.weight(1f))
                    StatTile(label = "GAMES PLAYED", value = stats.uniqueGames.toString(), modifier = Modifier.weight(1f))
                }

                stats.topGame?.let { game ->
                    Text("MOST PLAYED", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                    WrapUpGameCard(
                        entry = game,
                        detail = "%.1f hours".format(stats.topGameHours),
                        onClick = { onOpenGame(game.gameId) }
                    )
                }

                stats.topRatedGame?.let { game ->
                    Text("HIGHEST RATED", style = MaterialTheme.typography.labelLarge, color = TextSecondary)
                    WrapUpGameCard(
                        entry = game,
                        detail = "%.1f / 10".format(stats.topRatedAverage),
                        onClick = { onOpenGame(game.gameId) }
                    )
                }
            }
        }
    }
}

@Composable
private fun StatTile(label: String, value: String, modifier: Modifier = Modifier) {
    NeonPanel(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.headlineMedium, color = NeonBlue)
            Text(label, style = MaterialTheme.typography.labelLarge, color = TextSecondary, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun WrapUpGameCard(entry: LogEntryWithGame, detail: String, onClick: () -> Unit) {
    NeonPanel(modifier = Modifier.fillMaxWidth().clickable(onClick = onClick)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = entry.coverUrl,
                contentDescription = entry.gameTitle,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(64.dp).clip(RoundedCornerShape(8.dp))
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(entry.gameTitle, style = MaterialTheme.typography.titleMedium)
                Text(Platforms.byId(entry.platformId).displayName, style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Star, contentDescription = null, tint = NeonBlue, modifier = Modifier.size(14.dp))
                    Text(" $detail", style = MaterialTheme.typography.bodyMedium, color = NeonBlue)
                }
            }
        }
    }
}

private fun shareWrapUp(context: Context, year: Int, stats: WrapUpStats) {
    val lines = buildList {
        add("My $year in Oasis:")
        add("%.1f hours played across %d games".format(stats.totalHours, stats.uniqueGames))
        add("${stats.totalSessions} sessions logged")
        stats.topGame?.let { add("Most played: ${it.gameTitle} (%.1f h)".format(stats.topGameHours)) }
        stats.topRatedGame?.let { add("Highest rated: ${it.gameTitle} (%.1f/10)".format(stats.topRatedAverage)) }
    }
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, lines.joinToString("\n"))
    }
    context.startActivity(Intent.createChooser(intent, "Share your $year wrap-up"))
}
