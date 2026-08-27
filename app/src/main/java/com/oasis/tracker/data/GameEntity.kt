package com.oasis.tracker.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "games")
data class GameEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val platformId: String,
    val title: String,
    val coverUrl: String? = null,
    val sourceUrl: String? = null,
    val summary: String? = null,
    val addedAt: Long = System.currentTimeMillis(),
    /** Set when this game was imported from a Steam library, for import dedup. */
    val steamAppId: Int? = null
)
