package com.oasis.tracker.data

import android.content.Context

/**
 * Persists up to [MAX_FAVORITES] pinned games, shown at the top of the main
 * menu, as a comma-joined ordered list of game ids in SharedPreferences.
 */
class FavoritesStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Returns the saved favorites, dropping any ids that no longer exist in [existingGameIds]. */
    fun loadFavorites(existingGameIds: Set<Long>): List<Long> = rawFavorites().filter { it in existingGameIds }

    fun saveFavorites(favorites: List<Long>) {
        prefs.edit().putString(KEY_FAVORITES, favorites.joinToString(",")).apply()
    }

    /**
     * Doesn't need the caller's full game-id set: a stale id left behind by a
     * deleted game is harmless here since every read goes through
     * [loadFavorites], which always re-filters against the current games.
     */
    fun addFavorite(gameId: Long) {
        val current = rawFavorites()
        if (gameId in current || current.size >= MAX_FAVORITES) return
        saveFavorites(current + gameId)
    }

    fun removeFavorite(gameId: Long) {
        saveFavorites(rawFavorites().filter { it != gameId })
    }

    private fun rawFavorites(): List<Long> =
        prefs.getString(KEY_FAVORITES, null)?.split(",")?.mapNotNull { it.toLongOrNull() } ?: emptyList()

    companion object {
        private const val PREFS_NAME = "oasis_favorites"
        private const val KEY_FAVORITES = "favorites"
        const val MAX_FAVORITES = 5
    }
}
