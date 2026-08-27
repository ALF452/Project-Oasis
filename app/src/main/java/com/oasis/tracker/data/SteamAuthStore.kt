package com.oasis.tracker.data

import android.content.Context

/** Persists the connected Steam account (just the SteamID64; profile details are re-fetched live). */
class SteamAuthStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var steamId64: String?
        get() = prefs.getString(KEY_STEAM_ID, null)
        set(value) {
            prefs.edit().putString(KEY_STEAM_ID, value).apply()
        }

    fun disconnect() {
        prefs.edit().remove(KEY_STEAM_ID).apply()
    }

    companion object {
        private const val PREFS_NAME = "oasis_steam_auth"
        private const val KEY_STEAM_ID = "steam_id_64"
    }
}
