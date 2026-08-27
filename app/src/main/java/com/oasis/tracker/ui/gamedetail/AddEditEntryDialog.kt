package com.oasis.tracker.ui.gamedetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun AddEditEntryDialog(
    date: LocalDate,
    initialHours: Float?,
    initialNotes: String?,
    onDismiss: () -> Unit,
    onSave: (hours: Float, notes: String?) -> Unit,
    onDelete: (() -> Unit)?
) {
    var hoursText by remember { mutableStateOf(initialHours?.toString() ?: "") }
    var notesText by remember { mutableStateOf(initialNotes ?: "") }

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
                OutlinedTextField(
                    value = notesText,
                    onValueChange = { notesText = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonBlue, cursorColor = NeonBlue)
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                // Accept comma as a decimal separator too (European decimal keypads emit ',').
                val hours = hoursText.trim().replace(',', '.').toFloatOrNull() ?: 0f
                onSave(hours, notesText.ifBlank { null })
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
