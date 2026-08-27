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
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.oasis.tracker.data.PlatformOrderStore
import com.oasis.tracker.data.Platforms
import com.oasis.tracker.ui.components.NeonPanel
import com.oasis.tracker.ui.components.ReorderableTileGrid
import com.oasis.tracker.ui.rememberOasisApp
import com.oasis.tracker.ui.theme.NeonBlue
import com.oasis.tracker.ui.theme.TextSecondary
import com.oasis.tracker.ui.update.UpdateBanner
import com.oasis.tracker.update.UpdateState
import kotlinx.coroutines.launch

@Composable
fun MainMenuScreen(
    onOpenMonthlyTracker: () -> Unit,
    onOpenYearlyTracker: () -> Unit,
    onOpenPlatform: (String) -> Unit
) {
    val app = rememberOasisApp()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val updateState by app.updateManager.state.collectAsState()

    val orderStore = remember { PlatformOrderStore(context) }
    val modernPlatforms = remember {
        orderStore.loadOrder(PlatformOrderStore.KEY_MODERN, Platforms.MODERN_PLATFORMS.map { it.id })
            .map { Platforms.byId(it) }
    }
    val retroPlatforms = remember {
        orderStore.loadOrder(PlatformOrderStore.KEY_RETRO, Platforms.RETRO_PLATFORMS.map { it.id })
            .map { Platforms.byId(it) }
    }

    // Re-check on cold start, and again any time the user returns to this
    // screen (e.g. after backing out of an update install that didn't finish).
    LaunchedEffect(Unit) {
        app.updateManager.checkForUpdate()
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                scope.launch { app.updateManager.checkForUpdate() }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier.fillMaxSize()
    ) {
        item(span = { GridItemSpan(2) }) {
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

        item(span = { GridItemSpan(1) }) {
            MenuTile(label = "MONTHLY", sublabel = "Tracker", onClick = onOpenMonthlyTracker)
        }
        item(span = { GridItemSpan(1) }) {
            MenuTile(label = "YEARLY", sublabel = "Tracker", onClick = onOpenYearlyTracker)
        }

        item(span = { GridItemSpan(2) }) {
            Text(
                text = "PLATFORMS",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        item(span = { GridItemSpan(2) }) {
            ReorderableTileGrid(
                items = modernPlatforms,
                itemId = { it.id },
                onOrderChanged = { newOrder ->
                    orderStore.saveOrder(PlatformOrderStore.KEY_MODERN, newOrder.map { it.id })
                },
                onItemClick = { platform -> onOpenPlatform(platform.id) }
            ) { platform ->
                MenuTile(label = platform.glyph, sublabel = platform.displayName)
            }
        }

        item(span = { GridItemSpan(2) }) {
            Text(
                text = "RETRO",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        item(span = { GridItemSpan(2) }) {
            ReorderableTileGrid(
                items = retroPlatforms,
                itemId = { it.id },
                onOrderChanged = { newOrder ->
                    orderStore.saveOrder(PlatformOrderStore.KEY_RETRO, newOrder.map { it.id })
                },
                onItemClick = { platform -> onOpenPlatform(platform.id) }
            ) { platform ->
                MenuTile(label = platform.glyph, sublabel = platform.displayName)
            }
        }

        if (updateState != UpdateState.Idle && updateState != UpdateState.Checking) {
            item(span = { GridItemSpan(2) }) {
                UpdateBanner(
                    state = updateState,
                    onDownload = { (updateState as? UpdateState.Available)?.let { app.updateManager.startDownload(it.info) } },
                    onInstall = {
                        val ready = updateState as? UpdateState.ReadyToInstall ?: return@UpdateBanner
                        if (app.updateManager.canInstallPackages()) {
                            context.startActivity(app.updateManager.installApkIntent(ready.apkFile))
                        } else {
                            context.startActivity(app.updateManager.installPermissionSettingsIntent())
                        }
                    },
                    onDismiss = { app.updateManager.dismiss() }
                )
            }
        }
    }
}

@Composable
private fun MenuTile(label: String, sublabel: String, onClick: (() -> Unit)? = null) {
    val tileModifier = Modifier.fillMaxWidth().height(96.dp)
    NeonPanel(modifier = if (onClick != null) tileModifier.clickable(onClick = onClick) else tileModifier) {
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
