package com.oasis.tracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.weight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.oasis.tracker.ui.theme.CharcoalSurfaceRaised
import com.oasis.tracker.ui.theme.NeonBlue
import com.oasis.tracker.ui.theme.TextSecondary
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

/**
 * Letterboxd-style diary calendar for a single month. Days that have a log
 * entry get a neon-highlighted cell; tapping any day reports it via [onDayClick].
 */
@Composable
fun MonthCalendar(
    yearMonth: YearMonth,
    entriesByDay: Map<LocalDate, Float>,
    onPrevMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onDayClick: (LocalDate) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrevMonth) {
                Icon(Icons.Filled.ChevronLeft, contentDescription = "Previous month", tint = NeonBlue)
            }
            Text(
                text = "${yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${yearMonth.year}",
                style = MaterialTheme.typography.titleMedium
            )
            IconButton(onClick = onNextMonth) {
                Icon(Icons.Filled.ChevronRight, contentDescription = "Next month", tint = NeonBlue)
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
            DayOfWeek.values().forEach { day ->
                Text(
                    text = day.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        val firstOfMonth = yearMonth.atDay(1)
        val leadingBlanks = firstOfMonth.dayOfWeek.value % 7 // Monday=1..Sunday=7 -> Sunday-first grid offset
        val daysInMonth = yearMonth.lengthOfMonth()
        val totalCells = leadingBlanks + daysInMonth
        val rows = (totalCells + 6) / 7

        for (row in 0 until rows) {
            Row(modifier = Modifier.fillMaxWidth()) {
                for (col in 0 until 7) {
                    val cellIndex = row * 7 + col
                    val dayNumber = cellIndex - leadingBlanks + 1
                    Box(modifier = Modifier.weight(1f).aspectRatio(1f).padding(2.dp)) {
                        if (dayNumber in 1..daysInMonth) {
                            val date = yearMonth.atDay(dayNumber)
                            val hours = entriesByDay[date]
                            DayCell(dayNumber = dayNumber, hours = hours, onClick = { onDayClick(date) })
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCell(dayNumber: Int, hours: Float?, onClick: () -> Unit) {
    val hasEntry = hours != null
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (hasEntry) CharcoalSurfaceRaised else androidx.compose.ui.graphics.Color.Transparent)
            .then(if (hasEntry) Modifier.neonGlowBorder(cornerRadius = 6.dp, strokeWidth = 1.dp, glowRadius = 6.dp) else Modifier)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = dayNumber.toString(),
                style = MaterialTheme.typography.bodyMedium,
                color = if (hasEntry) NeonBlue else TextSecondary
            )
            if (hasEntry) {
                Text(
                    text = "${hours}h",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeonBlue
                )
            }
        }
    }
}
