package com.oasis.tracker.data

import android.content.Context

/**
 * Persists the user's custom Top 250 game ranking as a comma-joined,
 * ordered list of game ids in SharedPreferences. Unlike [PlatformOrderStore],
 * new games are never auto-added here — only games the user explicitly ranks.
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

    fun addGame(gameId: Long, existingGameIds: Set<Long>) {
        val current = loadRanking(existingGameIds)
        if (gameId in current || current.size >= MAX_RANKED) return
        saveRanking(current + gameId)
    }

    fun removeGame(gameId: Long, existingGameIds: Set<Long>) {
        saveRanking(loadRanking(existingGameIds).filter { it != gameId })
    }

    companion object {
        private const val PREFS_NAME = "oasis_top_ranking"
        private const val KEY_RANKING = "ranking"
        const val MAX_RANKED = 250
    }
}
