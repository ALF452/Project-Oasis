package com.oasis.tracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BacklogDao {
    @Insert
    suspend fun insert(entry: BacklogEntity): Long

    /** Preserves each entry's original id — used to restore a backup. */
    @Insert
    suspend fun insertAll(entries: List<BacklogEntity>)

    @Delete
    suspend fun delete(entry: BacklogEntity)

    @Query("DELETE FROM backlog_games")
    suspend fun deleteAll()

    @Query("SELECT * FROM backlog_games ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<BacklogEntity>>
}
