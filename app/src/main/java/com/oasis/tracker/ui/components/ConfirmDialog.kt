package com.oasis.tracker.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import com.oasis.tracker.ui.theme.NeonBlue

/** Generic yes/no confirmation for a destructive action, e.g. deleting a logged session. */
@Composable
fun ConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String = "DELETE",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = NeonBlue) },
        text = { Text(message) },
        confirmButton = { TextButton(onClick = onConfirm) { Text(confirmLabel) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("CANCEL") } }
    )
}
