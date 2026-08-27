package com.oasis.tracker.ui.tracker

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.oasis.tracker.data.Platforms
import com.oasis.tracker.ui.components.NeonPanel
import com.oasis.tracker.ui.rememberOasisApp
import com.oasis.tracker.ui.theme.CharcoalBackground
import com.oasis.tracker.ui.theme.NeonBlue
import com.oasis.tracker.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.Month
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun YearlyTrackerScreen(onBack: () -> Unit) {
    val app = rememberOasisApp()
    var year by remember { mutableIntStateOf(LocalDate.now().year) }
    val entries by app.gameRepository.entriesForYear(year).collectAsState(initial = emptyList())

    val totalHours = entries.sumOf { it.hours.toDouble() }.toFloat()

    val byMonth = remember(entries) {
        entries.groupBy { LocalDate.ofEpochDay(it.epochDay).month }
            .mapValues { (_, list) -> list.sumOf { e -> e.hours.toDouble() }.toFloat() }
    }

    val byGame = remember(entries) {
        entries.groupBy { Triple(it.gameId, it.gameTitle, it.platformId) }
            .map { (key, list) -> GameHoursSummary(key.second, key.third, list.sumOf { e -> e.hours.toDouble() }.toFloat()) }
            .sortedByDescending { it.hours }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text("Yearly Tracker", style = MaterialTheme.typography.titleLarge) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = NeonBlue)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = CharcoalBackground)
        )

        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { year -= 1 }) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous year", tint = NeonBlue)
            }
            Text(year.toString(), style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = { year += 1 }) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "Next year", tint = NeonBlue)
            }
        }

        NeonPanel(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text("TOTAL HOURS", style = MaterialTheme.typography.labelLarge)
                Text(
                    "%.1f h".format(totalHours),
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
            item {
                Text("BY MONTH", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(bottom = 6.dp))
            }
            items(Month.values().toList()) { month: Month ->
                val hours = byMonth[month] ?: 0f
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(month.getDisplayName(TextStyle.FULL, Locale.getDefault()), style = MaterialTheme.typography.bodyLarge)
                    Text("%.1f h".format(hours), color = if (hours > 0f) NeonBlue else TextSecondary)
                }
            }

            item {
                Text("BY GAME", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 16.dp, bottom = 6.dp))
            }
            if (byGame.isEmpty()) {
                item { Text("No sessions logged this year.", color = TextSecondary) }
            } else {
                items(byGame) { summary: GameHoursSummary ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(summary.gameTitle, style = MaterialTheme.typography.titleMedium)
                            Text(Platforms.byId(summary.platformId).displayName, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text("%.1f h".format(summary.hours), style = MaterialTheme.typography.titleMedium, color = NeonBlue)
                    }
                }
            }
        }
    }
}
