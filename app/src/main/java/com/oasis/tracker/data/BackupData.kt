package com.oasis.tracker.data

import kotlinx.serialization.Serializable

/**
 * The full contents of the app in one portable snapshot: every tracked game,
 * every logged session, the backlog, and the favorites/Top 250 orderings
 * (which otherwise live only in SharedPreferences, tied to this install).
 * All ids are preserved on export so log entries and the ordered id lists
 * still point at the right game after a restore.
 */
@Serializable
data class OasisBackup(
    val exportedAtEpochMillis: Long,
    val games: List<BackupGame>,
    val logEntries: List<BackupLogEntry>,
    val backlog: List<BackupBacklogEntry>,
    val favoriteGameIds: List<Long>,
    val topRankingGameIds: List<Long>
)

@Serializable
data class BackupGame(
    val id: Long,
    val platformId: String,
    val title: String,
    val coverUrl: String? = null,
    val sourceUrl: String? = null,
    val summary: String? = null,
    val addedAt: Long,
    val steamAppId: Int? = null
)

@Serializable
data class BackupLogEntry(
    val id: Long,
    val gameId: Long,
    val epochDay: Long,
    val hours: Float,
    val rating: Float? = null,
    val notes: String? = null
)

@Serializable
data class BackupBacklogEntry(
    val id: Long,
    val title: String,
    val coverUrl: String? = null,
    val sourceUrl: String? = null,
    val summary: String? = null,
    val addedAt: Long
)

fun GameEntity.toBackup() = BackupGame(id, platformId, title, coverUrl, sourceUrl, summary, addedAt, steamAppId)
fun BackupGame.toEntity() = GameEntity(id, platformId, title, coverUrl, sourceUrl, summary, addedAt, steamAppId)

fun LogEntryWithGame.toBackup() = BackupLogEntry(id, gameId, epochDay, hours, rating, notes)
fun BackupLogEntry.toEntity() = LogEntryEntity(id, gameId, epochDay, hours, rating, notes)

fun BacklogEntity.toBackup() = BackupBacklogEntry(id, title, coverUrl, sourceUrl, summary, addedAt)
fun BackupBacklogEntry.toEntity() = BacklogEntity(id, title, coverUrl, sourceUrl, summary, addedAt)
