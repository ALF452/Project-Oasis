package com.oasis.tracker.ui.update

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.oasis.tracker.ui.components.NeonPanel
import com.oasis.tracker.ui.theme.NeonBlue
import com.oasis.tracker.update.UpdateState

/**
 * Persistent strip shown above the current screen whenever an update is
 * available, downloading, or ready to install. Idle/Checking render nothing.
 */
@Composable
fun UpdateBanner(
    state: UpdateState,
    onDownload: () -> Unit,
    onInstall: () -> Unit,
    onDismiss: () -> Unit
) {
    when (state) {
        is UpdateState.Available -> NeonPanel(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("UPDATE AVAILABLE", style = MaterialTheme.typography.labelLarge, color = NeonBlue)
                    Text("Oasis ${state.info.versionName}", style = MaterialTheme.typography.bodyMedium)
                }
                TextButton(onClick = onDownload) { Text("DOWNLOAD") }
            }
        }

        is UpdateState.Downloading -> NeonPanel(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("DOWNLOADING ${state.info.versionName}…", style = MaterialTheme.typography.labelLarge)
                CircularProgressIndicator(modifier = Modifier.padding(start = 8.dp), color = NeonBlue)
            }
        }

        is UpdateState.ReadyToInstall -> NeonPanel(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("UPDATE READY TO INSTALL", style = MaterialTheme.typography.labelLarge)
                TextButton(onClick = onInstall) { Text("INSTALL") }
            }
        }

        is UpdateState.Error -> NeonPanel(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(state.message, style = MaterialTheme.typography.bodyMedium)
                TextButton(onClick = onDismiss) { Text("DISMISS") }
            }
        }

        UpdateState.Idle, UpdateState.Checking -> Unit
    }
}
