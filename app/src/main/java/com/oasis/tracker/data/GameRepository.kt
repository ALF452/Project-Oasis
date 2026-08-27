package com.oasis.tracker.data

import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.YearMonth

class GameRepository(private val db: OasisDatabase) {

    fun gamesForPlatform(platformId: String): Flow<List<GameEntity>> =
        db.gameDao().observeGamesForPlatform(platformId)

    fun allGames(): Flow<List<GameEntity>> = db.gameDao().observeAllGames()

    fun game(id: Long): Flow<GameEntity?> = db.gameDao().observeGame(id)

    suspend fun addGame(
        platformId: String,
        title: String,
        coverUrl: String?,
        sourceUrl: String?,
        summary: String?
    ): Long = db.gameDao().insert(
        GameEntity(
            platformId = platformId,
            title = title,
            coverUrl = coverUrl,
            sourceUrl = sourceUrl,
            summary = summary
        )
    )

    suspend fun removeGame(game: GameEntity) = db.gameDao().delete(game)

    fun entriesForGame(gameId: Long): Flow<List<LogEntryEntity>> =
        db.logEntryDao().observeEntriesForGame(gameId)

    fun entriesForDay(date: LocalDate): Flow<List<LogEntryWithGame>> =
        db.logEntryDao().observeEntriesWithGameForDay(date.toEpochDay())

    fun entriesForMonth(yearMonth: YearMonth): Flow<List<LogEntryWithGame>> {
        val start = yearMonth.atDay(1).toEpochDay()
        val end = yearMonth.atEndOfMonth().toEpochDay()
        return db.logEntryDao().observeEntriesWithGameInRange(start, end)
    }

    fun entriesForYear(year: Int): Flow<List<LogEntryWithGame>> {
        val start = LocalDate.of(year, 1, 1).toEpochDay()
        val end = LocalDate.of(year, 12, 31).toEpochDay()
        return db.logEntryDao().observeEntriesWithGameInRange(start, end)
    }

    suspend fun logSession(gameId: Long, date: LocalDate, hours: Float, notes: String?) {
        db.logEntryDao().insert(
            LogEntryEntity(gameId = gameId, epochDay = date.toEpochDay(), hours = hours, notes = notes)
        )
    }

    suspend fun updateEntry(entry: LogEntryEntity) = db.logEntryDao().update(entry)

    suspend fun deleteEntry(entry: LogEntryEntity) = db.logEntryDao().delete(entry)
}
