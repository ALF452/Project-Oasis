package com.oasis.tracker.ui.tracker

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.oasis.tracker.data.LogEntryWithGame
import com.oasis.tracker.ui.components.NeonPanel

data class GameHoursSummary(val gameTitle: String, val platformId: String, val hours: Float)

/** Shared by the Monthly and Yearly trackers: total hours per game, descending. */
fun List<LogEntryWithGame>.summarizeByGame(): List<GameHoursSummary> =
    groupBy { Triple(it.gameId, it.gameTitle, it.platformId) }
        .map { (key, list) -> GameHoursSummary(key.second, key.third, list.sumOf { e -> e.hours.toDouble() }.toFloat()) }
        .sortedByDescending { it.hours }

@Composable
fun TotalHoursPanel(totalHours: Float) {
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
}
