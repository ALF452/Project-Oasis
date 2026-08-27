package com.oasis.tracker.ui.mainmenu

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.oasis.tracker.data.PlatformDef
import com.oasis.tracker.data.Platforms
import com.oasis.tracker.ui.components.NeonPanel
import com.oasis.tracker.ui.theme.NeonBlue
import com.oasis.tracker.ui.theme.TextSecondary

@Composable
fun MainMenuScreen(
    onOpenMonthlyTracker: () -> Unit,
    onOpenYearlyTracker: () -> Unit,
    onOpenPlatform: (String) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
            Column(modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)) {
                Text(
                    text = "OASIS",
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "GAME TRACKER",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(1) }) {
            MenuTile(label = "MONTHLY", sublabel = "Tracker", onClick = onOpenMonthlyTracker)
        }
        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(1) }) {
            MenuTile(label = "YEARLY", sublabel = "Tracker", onClick = onOpenYearlyTracker)
        }

        item(span = { androidx.compose.foundation.lazy.grid.GridItemSpan(2) }) {
            Text(
                text = "PLATFORMS",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        items(Platforms.ALL) { platform: PlatformDef ->
            MenuTile(label = platform.glyph, sublabel = platform.displayName, onClick = { onOpenPlatform(platform.id) })
        }
    }
}

@Composable
private fun MenuTile(label: String, sublabel: String, onClick: () -> Unit) {
    NeonPanel(modifier = Modifier.fillMaxWidth().height(96.dp).clickable(onClick = onClick)) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.headlineMedium, color = NeonBlue)
            Text(text = sublabel, style = MaterialTheme.typography.bodyMedium)
        }
    }
}
