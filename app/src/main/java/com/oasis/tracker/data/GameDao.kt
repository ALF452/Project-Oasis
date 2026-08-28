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

    /** Preserves each game's original id — used to restore a backup, where log
     *  entries and the favorites/ranking lists already reference those ids. */
    @Insert
    suspend fun insertAll(games: List<GameEntity>)

    @Delete
    suspend fun delete(game: GameEntity)

    @Query("DELETE FROM games")
    suspend fun deleteAll()

    @Query("SELECT * FROM games WHERE platformId = :platformId ORDER BY title ASC")
    fun observeGamesForPlatform(platformId: String): Flow<List<GameEntity>>

    @Query("SELECT * FROM games WHERE id = :id LIMIT 1")
    fun observeGame(id: Long): Flow<GameEntity?>

    @Query("SELECT * FROM games ORDER BY title ASC")
    fun observeAllGames(): Flow<List<GameEntity>>

    @Query("SELECT steamAppId FROM games WHERE steamAppId IS NOT NULL")
    suspend fun getImportedSteamAppIds(): List<Int>
}
