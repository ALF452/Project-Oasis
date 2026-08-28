package com.oasis.tracker.data

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

/**
 * Everything in this app lives only in this install's local Room database and
 * SharedPreferences — uninstalling, clearing app data, or moving to a new
 * phone loses it all with no recovery path. This lets the user write a full
 * snapshot out to a file they choose (Downloads, Drive, wherever) and load it
 * back in later, via the system's own document picker rather than any
 * app-managed storage — no extra permissions needed.
 */
class BackupManager(
    private val context: Context,
    private val gameRepository: GameRepository,
    private val favoritesStore: FavoritesStore,
    private val topRankingStore: TopRankingStore
) {
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    suspend fun exportTo(uri: Uri) {
        val games = gameRepository.allGames().first()
        val entries = gameRepository.allEntriesWithGame().first()
        val backlog = gameRepository.backlog().first()
        val gameIds = games.map { it.id }.toSet()

        val backup = OasisBackup(
            exportedAtEpochMillis = System.currentTimeMillis(),
            games = games.map { it.toBackup() },
            logEntries = entries.map { it.toBackup() },
            backlog = backlog.map { it.toBackup() },
            favoriteGameIds = favoritesStore.loadFavorites(gameIds),
            topRankingGameIds = topRankingStore.loadRanking(gameIds)
        )

        val bytes = json.encodeToString(backup).toByteArray(Charsets.UTF_8)
        withContext(Dispatchers.IO) {
            val stream = context.contentResolver.openOutputStream(uri)
                ?: error("Couldn't open the selected location for writing.")
            stream.use { it.write(bytes) }
        }
    }

    suspend fun importFrom(uri: Uri) {
        val text = withContext(Dispatchers.IO) {
            val stream = context.contentResolver.openInputStream(uri)
                ?: error("Couldn't open the selected file for reading.")
            stream.use { it.readBytes().toString(Charsets.UTF_8) }
        }
        val backup = json.decodeFromString<OasisBackup>(text)

        gameRepository.restoreFromBackup(
            games = backup.games.map { it.toEntity() },
            logEntries = backup.logEntries.map { it.toEntity() },
            backlog = backup.backlog.map { it.toEntity() }
        )

        // Restored ids are trusted (they came from this same restore), but drop
        // anything that didn't actually make it into the games table so a
        // corrupt or hand-edited backup file can't leave a dangling reference.
        val restoredGameIds = backup.games.map { it.id }.toSet()
        favoritesStore.saveFavorites(backup.favoriteGameIds.filter { it in restoredGameIds })
        topRankingStore.saveRanking(backup.topRankingGameIds.filter { it in restoredGameIds })
    }
}
