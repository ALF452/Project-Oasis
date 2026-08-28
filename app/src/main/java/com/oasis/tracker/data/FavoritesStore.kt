package com.oasis.tracker.data

import android.content.Context

/**
 * Persists up to [MAX_FAVORITES] pinned games, shown at the top of the main
 * menu, as a comma-joined ordered list of game ids in SharedPreferences.
 */
class FavoritesStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Returns the saved favorites, dropping any ids that no longer exist in [existingGameIds]. */
    fun loadFavorites(existingGameIds: Set<Long>): List<Long> {
        val saved = prefs.getString(KEY_FAVORITES, null)
            ?.split(",")
            ?.mapNotNull { it.toLongOrNull() }
            ?: return emptyList()
        return saved.filter { it in existingGameIds }
    }

    fun saveFavorites(favorites: List<Long>) {
        prefs.edit().putString(KEY_FAVORITES, favorites.joinToString(",")).apply()
    }

    fun addFavorite(gameId: Long, existingGameIds: Set<Long>) {
        val current = loadFavorites(existingGameIds)
        if (gameId in current || current.size >= MAX_FAVORITES) return
        saveFavorites(current + gameId)
    }

    fun removeFavorite(gameId: Long, existingGameIds: Set<Long>) {
        saveFavorites(loadFavorites(existingGameIds).filter { it != gameId })
    }

    companion object {
        private const val PREFS_NAME = "oasis_favorites"
        private const val KEY_FAVORITES = "favorites"
        const val MAX_FAVORITES = 5
    }
}
