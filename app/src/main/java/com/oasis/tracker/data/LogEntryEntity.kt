package com.oasis.tracker.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * One calendar-day diary entry for a game, Letterboxd-style: a date plus how
 * many hours were played that day. [epochDay] is java.time.LocalDate.toEpochDay().
 */
@Entity(
    tableName = "log_entries",
    foreignKeys = [
        ForeignKey(
            entity = GameEntity::class,
            parentColumns = ["id"],
            childColumns = ["gameId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("gameId"), Index("epochDay")]
)
data class LogEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val gameId: Long,
    val epochDay: Long,
    val hours: Float,
    val notes: String? = null
)

data class LogEntryWithGame(
    val id: Long,
    val gameId: Long,
    val epochDay: Long,
    val hours: Float,
    val notes: String?,
    val gameTitle: String,
    val platformId: String,
    val coverUrl: String?
)
