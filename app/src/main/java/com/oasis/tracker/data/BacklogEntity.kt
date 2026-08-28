package com.oasis.tracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * A game the user wants to play next but hasn't started tracking yet — no
 * platform or hours committed. Deliberately separate from [GameEntity]/
 * [LogEntryEntity] rather than a status flag on them, since a backlog item
 * carries none of that tracking data yet.
 */
@Entity(tableName = "backlog_games")
data class BacklogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val coverUrl: String? = null,
    val sourceUrl: String? = null,
    val summary: String? = null,
    val addedAt: Long = System.currentTimeMillis()
)
