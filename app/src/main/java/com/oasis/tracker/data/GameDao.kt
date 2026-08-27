package com.oasis.tracker.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Insert
    suspend fun insert(game: GameEntity): Long

    @Delete
    suspend fun delete(game: GameEntity)

    @Query("SELECT * FROM games WHERE platformId = :platformId ORDER BY title ASC")
    fun observeGamesForPlatform(platformId: String): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE id = :id LIMIT 1")
    fun observeGame(id: Long): Flow<GameEntity?>

    @Query("SELECT * FROM games ORDER BY title ASC")
    fun observeAllGames(): Flow<List<GameEntity>>
}
