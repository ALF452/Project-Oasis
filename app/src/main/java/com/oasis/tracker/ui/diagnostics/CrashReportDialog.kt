package com.oasis.tracker.ui.diagnostics

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.oasis.tracker.ui.theme.NeonBlue
import com.oasis.tracker.ui.theme.TextSecondary

/**
 * Shown once after the app restarts following a crash, since a sideloaded
 * app has no Play Store crash reporting to surface this otherwise.
 */
@Composable
fun CrashReportDialog(report: String, onDismiss: () -> Unit) {
    val clipboard = LocalClipboardManager.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Oasis crashed last time", color = NeonBlue) },
        text = {
            Column {
                Text(
                    "Here's what happened. Copy it and share it if you'd like it fixed.",
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Box(modifier = Modifier.heightIn(max = 240.dp).verticalScroll(rememberScrollState())) {
                    Text(report, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { clipboard.setText(AnnotatedString(report)) }) { Text("COPY") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("DISMISS") }
        }
    )
}
