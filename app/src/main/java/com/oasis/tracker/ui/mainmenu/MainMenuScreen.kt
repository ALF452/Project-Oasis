package com.oasis.tracker.ui.mainmenu

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.* // weight() as a single named import collides with an internal same-named property in this Compose version
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import coil.compose.AsyncImage
import com.oasis.tracker.data.FavoritesStore
import com.oasis.tracker.data.GameEntity
import com.oasis.tracker.data.Platforms
import com.oasis.tracker.ui.components.NeonPanel
import com.oasis.tracker.ui.diagnostics.CrashReportDialog
import com.oasis.tracker.ui.rememberOasisApp
import com.oasis.tracker.ui.theme.CharcoalBackground
import com.oasis.tracker.ui.theme.NeonBlue
import com.oasis.tracker.ui.theme.NeonPurple
import com.oasis.tracker.ui.theme.TextSecondary
import com.oasis.tracker.ui.update.UpdateBanner
import com.oasis.tracker.update.UpdateState
import kotlinx.coroutines.launch

@Composable
fun MainMenuScreen(
    onOpenMonthlyTracker: () -> Unit,
    onOpenYearlyTracker: () -> Unit,
    onOpenPlatform: (String) -> Unit,
    onOpenSteam: () -> Unit,
    onOpenTopRanking: () -> Unit,
    onOpenDiary: () -> Unit,
    onOpenBacklog: () -> Unit,
    onOpenFavoritesPicker: () -> Unit,
    onOpenGame: (Long) -> Unit
) {
    val app = rememberOasisApp()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val updateState by app.updateManager.state.collectAsState()

    var crashReport by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(Unit) {
        crashReport = app.crashLogStore.read()
    }

    val favoritesStore = remember { FavoritesStore(context) }
    val allGames by app.gameRepository.allGames().collectAsState(initial = null)
    var favoriteIds by remember { mutableStateOf<List<Long>>(emptyList()) }
    LaunchedEffect(allGames) {
        val games = allGames ?: return@LaunchedEffect
        favoriteIds = favoritesStore.loadFavorites(games.map { it.id }.toSet())
    }
    val favoriteGames = remember(favoriteIds, allGames) {
        val byId = allGames?.associateBy { it.id }.orEmpty()
        favoriteIds.mapNotNull { byId[it] }
    }

    // Tapping any main-menu tile bleeds its glow from blue to purple before
    // the actual navigation happens, rather than cutting away instantly.
    val tileGlowFraction = remember { Animatable(0f) }
    var transitioningTileId by remember { mutableStateOf<String?>(null) }
    fun handleTileClick(tileId: String, navigate: () -> Unit) {
        if (transitioningTileId != null) return
        transitioningTileId = tileId
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        scope.launch {
            tileGlowFraction.snapTo(0f)
            tileGlowFraction.animateTo(1f, animationSpec = tween(450))
            navigate()
            transitioningTileId = null
            tileGlowFraction.snapTo(0f)
        }
    }
    fun tileBorderColor(tileId: String): Color =
        if (tileId == transitioningTileId) lerp(NeonBlue, NeonPurple, tileGlowFraction.value) else NeonBlue

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

        item(span = { GridItemSpan(2) }) {
            FavoritesRow(
                favorites = favoriteGames,
                onOpenGame = { gameId ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onOpenGame(gameId)
                },
                onRemove = { gameId ->
                    favoritesStore.removeFavorite(gameId)
                    favoriteIds = favoriteIds - gameId
                },
                onAddSlot = onOpenFavoritesPicker
            )
        }

        item(span = { GridItemSpan(1) }) {
            MenuTile(
                label = "MONTHLY",
                sublabel = "Tracker",
                onClick = { handleTileClick("monthly", onOpenMonthlyTracker) },
                borderColor = tileBorderColor("monthly")
            )
        }
        item(span = { GridItemSpan(1) }) {
            MenuTile(
                label = "YEARLY",
                sublabel = "Tracker",
                onClick = { handleTileClick("yearly", onOpenYearlyTracker) },
                borderColor = tileBorderColor("yearly")
            )
        }

        item(span = { GridItemSpan(2) }) {
            Text(
                text = "RANKINGS",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        item(span = { GridItemSpan(2) }) {
            MenuTile(
                label = "TOP 250",
                sublabel = "Your custom ranking",
                onClick = { handleTileClick("top_ranking", onOpenTopRanking) },
                borderColor = tileBorderColor("top_ranking")
            )
        }

        item(span = { GridItemSpan(2) }) {
            Text(
                text = "ACCOUNTS",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        item(span = { GridItemSpan(2) }) {
            MenuTile(
                label = "STEAM",
                sublabel = "Library & achievements",
                onClick = { handleTileClick("steam", onOpenSteam) },
                borderColor = tileBorderColor("steam")
            )
        }

        item(span = { GridItemSpan(2) }) {
            Text(
                text = "ACTIVITY",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        item(span = { GridItemSpan(2) }) {
            MenuTile(
                label = "DIARY",
                sublabel = "Every session, most recent first",
                onClick = { handleTileClick("diary", onOpenDiary) },
                borderColor = tileBorderColor("diary")
            )
        }

        item(span = { GridItemSpan(2) }) {
            Text(
                text = "BACKLOG",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        item(span = { GridItemSpan(2) }) {
            MenuTile(
                label = "BACKLOG",
                sublabel = "What to play next",
                onClick = { handleTileClick("backlog", onOpenBacklog) },
                borderColor = tileBorderColor("backlog")
            )
        }

        item(span = { GridItemSpan(2) }) {
            Text(
                text = "PLATFORMS",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        items(Platforms.MODERN_PLATFORMS, key = { it.id }) { platform ->
            MenuTile(
                label = platform.glyph,
                sublabel = platform.displayName,
                onClick = { handleTileClick(platform.id) { onOpenPlatform(platform.id) } },
                borderColor = tileBorderColor(platform.id)
            )
        }

        item(span = { GridItemSpan(2) }) {
            Text(
                text = "RETRO",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        items(Platforms.RETRO_PLATFORMS, key = { it.id }) { platform ->
            MenuTile(
                label = platform.glyph,
                sublabel = platform.displayName,
                onClick = { handleTileClick(platform.id) { onOpenPlatform(platform.id) } },
                borderColor = tileBorderColor(platform.id)
            )
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

    crashReport?.let { report ->
        CrashReportDialog(
            report = report,
            onDismiss = {
                app.crashLogStore.clear()
                crashReport = null
            }
        )
    }
}

@Composable
private fun MenuTile(
    label: String,
    sublabel: String,
    onClick: (() -> Unit)? = null,
    borderColor: Color = NeonBlue
) {
    val tileModifier = Modifier.fillMaxWidth().height(96.dp)
    NeonPanel(
        modifier = if (onClick != null) tileModifier.clickable(onClick = onClick) else tileModifier,
        borderColor = borderColor
    ) {
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

/** Up to [FavoritesStore.MAX_FAVORITES] pinned games shown as cover art, Letterboxd-favorites-style. */
@Composable
private fun FavoritesRow(
    favorites: List<GameEntity>,
    onOpenGame: (Long) -> Unit,
    onRemove: (Long) -> Unit,
    onAddSlot: () -> Unit
) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(FavoritesStore.MAX_FAVORITES) { index ->
            val game = favorites.getOrNull(index)
            Box(modifier = Modifier.weight(1f).aspectRatio(2f / 3f)) {
                if (game != null) {
                    FilledFavoriteSlot(game = game, onClick = { onOpenGame(game.id) }, onRemove = { onRemove(game.id) })
                } else {
                    EmptyFavoriteSlot(onClick = onAddSlot)
                }
            }
        }
    }
}

@Composable
private fun FilledFavoriteSlot(game: GameEntity, onClick: () -> Unit, onRemove: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        AsyncImage(
            model = game.coverUrl,
            contentDescription = game.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(8.dp))
                .clickable(onClick = onClick)
        )
        IconButton(
            onClick = onRemove,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(2.dp)
                .size(22.dp)
                .background(CharcoalBackground.copy(alpha = 0.75f), CircleShape)
        ) {
            Icon(
                Icons.Filled.Close,
                contentDescription = "Remove favorite",
                tint = TextSecondary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun EmptyFavoriteSlot(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, TextSecondary, RoundedCornerShape(8.dp))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Filled.Add, contentDescription = "Add favorite", tint = TextSecondary)
    }
}
