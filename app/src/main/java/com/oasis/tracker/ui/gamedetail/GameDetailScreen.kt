package com.oasis.tracker.ui.gamedetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.oasis.tracker.data.LogEntryEntity
import com.oasis.tracker.data.Platforms
import com.oasis.tracker.ui.components.ConfirmDialog
import com.oasis.tracker.ui.components.MonthCalendar
import com.oasis.tracker.ui.components.NeonPanel
import com.oasis.tracker.ui.rememberOasisApp
import com.oasis.tracker.ui.theme.CharcoalBackground
import com.oasis.tracker.ui.theme.NeonBlue
import com.oasis.tracker.ui.theme.TextSecondary
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Composable
fun GameDetailScreen(gameId: Long, onBack: () -> Unit) {
    val app = rememberOasisApp()
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val game by app.gameRepository.game(gameId).collectAsState(initial = null)
    val entries by app.gameRepository.entriesForGame(gameId).collectAsState(initial = emptyList())

    var currentMonth by remember { mutableStateOf(YearMonth.now()) }
    var dialogDate by remember { mutableStateOf<LocalDate?>(null) }
    var entryPendingDelete by remember { mutableStateOf<LogEntryEntity?>(null) }

    val entriesByDate = remember(entries) {
        entries.groupBy { LocalDate.ofEpochDay(it.epochDay) }
            .mapValues { (_, list) -> list.sumOf { it.hours.toDouble() }.toFloat() }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(game?.title ?: "", style = MaterialTheme.typography.titleLarge, maxLines = 1) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Back", tint = NeonBlue)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = CharcoalBackground)
        )

        LazyColumn(contentPadding = PaddingValues(bottom = 24.dp)) {
            item {
                game?.let { g ->
                    Row(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        AsyncImage(
                            model = g.coverUrl,
                            contentDescription = g.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(90.dp).clip(RoundedCornerShape(10.dp))
                        )
                        Column(modifier = Modifier.padding(start = 12.dp)) {
                            Text(Platforms.byId(g.platformId).displayName, style = MaterialTheme.typography.labelLarge)
                            g.summary?.let {
                                Text(it, style = MaterialTheme.typography.bodyMedium, maxLines = 4)
                            }
                        }
                    }
                }
            }

            item {
                NeonPanel(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp)) {
                    MonthCalendar(
                        yearMonth = currentMonth,
                        entriesByDay = entriesByDate,
                        onPrevMonth = { currentMonth = currentMonth.minusMonths(1) },
                        onNextMonth = { currentMonth = currentMonth.plusMonths(1) },
                        onDayClick = { date -> dialogDate = date }
                    )
                }
            }

            item {
                Text(
                    text = "DIARY",
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(start = 16.dp, top = 20.dp, bottom = 8.dp)
                )
            }

            if (entries.isEmpty()) {
                item {
                    Text(
                        "No sessions logged yet. Tap a day on the calendar to add one.",
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
            } else {
                items(entries.sortedByDescending { it.epochDay }, key = { it.id }) { entry: LogEntryEntity ->
                    DiaryRow(
                        entry = entry,
                        onClick = { dialogDate = LocalDate.ofEpochDay(entry.epochDay) },
                        onDelete = { entryPendingDelete = entry }
                    )
                }
            }
        }
    }

    dialogDate?.let { date ->
        val existing = entries.firstOrNull { LocalDate.ofEpochDay(it.epochDay) == date }
        AddEditEntryDialog(
            date = date,
            initialHours = existing?.hours,
            initialRating = existing?.rating,
            initialNotes = existing?.notes,
            onDismiss = { dialogDate = null },
            // Dismiss synchronously rather than after the suspend call finishes: while
            // that DB write is in flight the dialog (and its SAVE button) stayed on
            // screen and clickable, so a fast double-tap fired onSave twice and, for
            // a brand-new entry, inserted the session twice.
            onSave = { hours, rating, notes ->
                dialogDate = null
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                scope.launch {
                    if (existing != null) {
                        app.gameRepository.updateEntry(existing.copy(hours = hours, rating = rating, notes = notes))
                    } else {
                        app.gameRepository.logSession(gameId, date, hours, rating = rating, notes = notes)
                    }
                }
            },
            onDelete = existing?.let {
                {
                    dialogDate = null
                    entryPendingDelete = it
                }
            }
        )
    }

    entryPendingDelete?.let { entry ->
        val entryDate = LocalDate.ofEpochDay(entry.epochDay).format(DateTimeFormatter.ofPattern("MMM d, yyyy"))
        ConfirmDialog(
            title = "Delete session?",
            message = "This will permanently delete the ${entry.hours}h session logged on $entryDate. This can't be undone.",
            onConfirm = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                scope.launch { app.gameRepository.deleteEntry(entry) }
                entryPendingDelete = null
            },
            onDismiss = { entryPendingDelete = null }
        )
    }
}

@Composable
private fun DiaryRow(entry: LogEntryEntity, onClick: () -> Unit, onDelete: () -> Unit) {
    NeonPanel(modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.padding(end = 8.dp)) {
                Text(
                    LocalDate.ofEpochDay(entry.epochDay).format(DateTimeFormatter.ofPattern("EEE, MMM d yyyy")),
                    style = MaterialTheme.typography.titleMedium
                )
                Text("${entry.hours}h played", style = MaterialTheme.typography.bodyMedium, color = NeonBlue)
                entry.rating?.let {
                    val ratingText = if (it % 1f == 0f) it.toInt().toString() else it.toString()
                    Text("Rating: $ratingText / 10", style = MaterialTheme.typography.bodyMedium, color = NeonBlue)
                }
                entry.notes?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Delete entry", tint = TextSecondary)
            }
        }
    }
}
