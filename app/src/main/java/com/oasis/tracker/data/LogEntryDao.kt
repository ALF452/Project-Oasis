package com.oasis.tracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface LogEntryDao {
    @Insert
    suspend fun insert(entry: LogEntryEntity): Long

    @Update
    suspend fun update(entry: LogEntryEntity)

    @Delete
    suspend fun delete(entry: LogEntryEntity)

    @Query("SELECT * FROM log_entries WHERE gameId = :gameId ORDER BY epochDay DESC")
    fun observeEntriesForGame(gameId: Long): Flow<List<LogEntryEntity>>

    @Query(
        """
        SELECT log_entries.id AS id, log_entries.gameId AS gameId, log_entries.epochDay AS epochDay,
               log_entries.hours AS hours, log_entries.rating AS rating, log_entries.notes AS notes,
               games.title AS gameTitle, games.platformId AS platformId, games.coverUrl AS coverUrl
        FROM log_entries
        INNER JOIN games ON games.id = log_entries.gameId
        WHERE log_entries.epochDay BETWEEN :startDay AND :endDay
        ORDER BY log_entries.epochDay ASC, log_entries.id ASC
        """
    )
    fun observeEntriesWithGameInRange(startDay: Long, endDay: Long): Flow<List<LogEntryWithGame>>

    @Query(
        """
        SELECT log_entries.id AS id, log_entries.gameId AS gameId, log_entries.epochDay AS epochDay,
               log_entries.hours AS hours, log_entries.rating AS rating, log_entries.notes AS notes,
               games.title AS gameTitle, games.platformId AS platformId, games.coverUrl AS coverUrl
        FROM log_entries
        INNER JOIN games ON games.id = log_entries.gameId
        WHERE log_entries.epochDay = :day
        ORDER BY log_entries.id ASC
        """
    )
    fun observeEntriesWithGameForDay(day: Long): Flow<List<LogEntryWithGame>>
}
