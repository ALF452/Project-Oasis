package com.oasis.tracker.data

import android.content.Context

/**
 * Persists the user's drag-to-reorder tile order per main-menu section as a
 * comma-joined list of platform ids in SharedPreferences.
 */
class PlatformOrderStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /** Returns the saved order restricted/extended to match [defaultOrder]'s current members. */
    fun loadOrder(key: String, defaultOrder: List<String>): List<String> {
        val saved = prefs.getString(key, null)?.split(",")?.filter { it.isNotBlank() }
            ?: return defaultOrder
        val kept = saved.filter { it in defaultOrder }
        val savedSet = saved.toSet()
        val newlyAdded = defaultOrder.filter { it !in savedSet }
        return kept + newlyAdded
    }

    fun saveOrder(key: String, order: List<String>) {
        prefs.edit().putString(key, order.joinToString(",")).apply()
    }

    companion object {
        private const val PREFS_NAME = "oasis_platform_order"
        const val KEY_MODERN = "modern_order"
        const val KEY_RETRO = "retro_order"
    }
}
