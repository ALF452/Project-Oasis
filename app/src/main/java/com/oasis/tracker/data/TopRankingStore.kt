package com.oasis.tracker.data

import android.content.Context

/**
 * Persists the user's custom Top 250 game ranking as a comma-joined,
 * ordered list of game ids in SharedPreferences. New games are never
 * auto-added here — only games the user explicitly ranks.
 */
class TopRankingStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Returns the saved ranking, dropping any ids that no longer exist in [existingGameIds]. */
    fun loadRanking(existingGameIds: Set<Long>): List<Long> {
        val saved = prefs.getString(KEY_RANKING, null)
            ?.split(",")
            ?.mapNotNull { it.toLongOrNull() }
            ?: return emptyList()
        return saved.filter { it in existingGameIds }
    }

    fun saveRanking(ranking: List<Long>) {
        prefs.edit().putString(KEY_RANKING, ranking.joinToString(",")).apply()
    }

    /** Number of games currently ranked, stale ids included — good enough for a UI default. */
    fun count(): Int = rawRanking().size

    /**
     * Doesn't need the caller's full game-id set: a stale id left behind by a
     * deleted game is harmless here since every read goes through
     * [loadRanking], which always re-filters against the current games.
     *
     * [atRank] inserts at that 1-based position, shifting every game from
     * there down one rank; omitted (or out of range), it appends to the end.
     */
    fun addGame(gameId: Long, atRank: Int? = null) {
        val current = rawRanking()
        if (gameId in current || current.size >= MAX_RANKED) return
        val index = (atRank?.minus(1) ?: current.size).coerceIn(0, current.size)
        saveRanking(current.toMutableList().apply { add(index, gameId) })
    }

    fun removeGame(gameId: Long) {
        saveRanking(rawRanking().filter { it != gameId })
    }

    private fun rawRanking(): List<Long> =
        prefs.getString(KEY_RANKING, null)?.split(",")?.mapNotNull { it.toLongOrNull() } ?: emptyList()

    companion object {
        private const val PREFS_NAME = "oasis_top_ranking"
        private const val KEY_RANKING = "ranking"
        const val MAX_RANKED = 250
    }
}
