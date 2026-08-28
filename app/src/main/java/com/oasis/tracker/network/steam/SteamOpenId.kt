package com.oasis.tracker.network.steam

import android.net.Uri
import com.oasis.tracker.BuildConfig
import com.oasis.tracker.network.NetworkModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.FormBody
import okhttp3.Request

private const val STEAM_OPENID_ENDPOINT = "https://steamcommunity.com/openid/login"

/**
 * Steam has no OAuth app-registration flow for third parties — "Sign in
 * through Steam" is OpenID 2.0, which requires an http(s) return_to URL (a
 * custom scheme is rejected outright). There's no server-side callback route
 * here (the app has no backend), so [BuildConfig.STEAM_RETURN_URL] is a
 * placeholder https URL that's never actually hosted — the login WebView
 * intercepts navigation to it directly instead of letting it load.
 */
object SteamOpenId {

    fun buildLoginUrl(): String {
        val returnTo = BuildConfig.STEAM_RETURN_URL
        return Uri.parse(STEAM_OPENID_ENDPOINT).buildUpon()
            .appendQueryParameter("openid.ns", "http://specs.openid.net/auth/2.0")
            .appendQueryParameter("openid.mode", "checkid_setup")
            .appendQueryParameter("openid.return_to", returnTo)
            .appendQueryParameter("openid.realm", returnTo)
            .appendQueryParameter("openid.identity", "http://specs.openid.net/auth/2.0/identifier_select")
            .appendQueryParameter("openid.claimed_id", "http://specs.openid.net/auth/2.0/identifier_select")
            .build()
            .toString()
    }

    /**
     * Re-posts the callback params back to Steam with mode=check_authentication
     * to confirm the redirect is genuine — required by the OpenID spec, since
     * otherwise anyone could hand the app a fake callback claiming any SteamID.
     * Returns the verified SteamID64, or null if verification fails.
     */
    suspend fun verifyCallback(callbackUri: Uri): String? = withContext(Dispatchers.IO) {
        val mode = callbackUri.getQueryParameter("openid.mode")
        val claimedId = callbackUri.getQueryParameter("openid.claimed_id")
        if (mode != "id_res" || claimedId.isNullOrBlank()) return@withContext null

        val body = FormBody.Builder().apply {
            for (name in callbackUri.queryParameterNames) {
                val value = callbackUri.getQueryParameter(name) ?: continue
                add(name, if (name == "openid.mode") "check_authentication" else value)
            }
        }.build()

        val request = Request.Builder()
            .url(STEAM_OPENID_ENDPOINT)
            .post(body)
            .build()

        val responseBody = runCatching {
            NetworkModule.sharedOkHttpClient.newCall(request).execute().use { it.body?.string() }
        }.getOrNull() ?: return@withContext null

        val isValid = responseBody.lineSequence().any { it.trim() == "is_valid:true" }
        if (!isValid) return@withContext null

        // claimedId looks like https://steamcommunity.com/openid/id/76561197960287930
        claimedId.substringAfterLast('/').takeIf { id -> id.isNotEmpty() && id.all(Char::isDigit) }
    }
}
