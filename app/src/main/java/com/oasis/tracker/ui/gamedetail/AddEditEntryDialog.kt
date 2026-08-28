package com.oasis.tracker.ui.gamedetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.oasis.tracker.ui.theme.NeonBlue
import com.oasis.tracker.ui.theme.TextSecondary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.math.roundToInt

@Composable
fun AddEditEntryDialog(
    date: LocalDate,
    initialHours: Float?,
    initialRating: Float?,
    initialNotes: String?,
    onDismiss: () -> Unit,
    onSave: (hours: Float, rating: Float?, notes: String?) -> Unit,
    onDelete: (() -> Unit)?
) {
    var hoursText by remember { mutableStateOf(initialHours?.toString() ?: "") }
    var reviewText by remember { mutableStateOf(initialNotes ?: "") }
    var ratingValue by remember { mutableStateOf(initialRating ?: 5f) }
    var ratingSet by remember { mutableStateOf(initialRating != null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(date.format(DateTimeFormatter.ofPattern("EEEE, MMM d yyyy")), color = NeonBlue) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = hoursText,
                    onValueChange = { hoursText = it },
                    label = { Text("Hours played") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonBlue, cursorColor = NeonBlue)
                )

                Text(
                    if (ratingSet) "Rating: ${formatRating(ratingValue)} / 10" else "Rating: not rated",
                    color = TextSecondary
                )
                Slider(
                    value = ratingValue,
                    onValueChange = { newValue ->
                        ratingValue = (newValue * 2).roundToInt() / 2f
                        ratingSet = true
                    },
                    valueRange = 0f..10f,
                    // 20 half-point steps between 0 and 10 (0, 0.5, 1, ... 10)
                    steps = 19,
                    colors = SliderDefaults.colors(thumbColor = NeonBlue, activeTrackColor = NeonBlue),
                    modifier = Modifier.fillMaxWidth()
                )
                if (ratingSet) {
                    TextButton(onClick = { ratingSet = false }) { Text("CLEAR RATING") }
                }

                OutlinedTextField(
                    value = reviewText,
                    onValueChange = { reviewText = it },
                    label = { Text("Review (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonBlue, cursorColor = NeonBlue)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                // Accept comma as a decimal separator too (European decimal keypads emit ',').
                val hours = hoursText.trim().replace(',', '.').toFloatOrNull() ?: 0f
                onSave(hours, if (ratingSet) ratingValue else null, reviewText.ifBlank { null })
            }) { Text("SAVE") }
        },
        dismissButton = {
            Column {
                if (onDelete != null) {
                    TextButton(onClick = onDelete) { Text("DELETE") }
                }
                TextButton(onClick = onDismiss) { Text("CANCEL") }
            }
        }
    )
}

private fun formatRating(value: Float): String =
    if (value % 1f == 0f) value.toInt().toString() else value.toString()
