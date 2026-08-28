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

    @Delete
    suspend fun delete(entry: BacklogEntity)

    @Query("SELECT * FROM backlog_games ORDER BY addedAt DESC")
    fun observeAll(): Flow<List<BacklogEntity>>
}
