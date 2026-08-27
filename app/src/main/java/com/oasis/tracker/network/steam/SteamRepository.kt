package com.oasis.tracker.network.steam

import android.net.Uri
import com.oasis.tracker.BuildConfig
import com.oasis.tracker.data.SteamAuthStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SteamProfile(
    val steamId: String,
    val personaName: String,
    val avatarUrl: String?,
    val profileUrl: String?,
    val isPublic: Boolean
)

data class SteamGameSummary(
    val appId: Int,
    val name: String,
    val iconUrl: String?,
    val playtimeMinutes: Int,
    val hasStats: Boolean
)

data class SteamAchievement(
    val apiName: String,
    val displayName: String,
    val description: String?,
    val iconUrl: String?,
    val unlocked: Boolean,
    val unlockedAt: Long?
)

data class SteamGameAchievements(
    val gameName: String?,
    val achievements: List<SteamAchievement>,
    val unavailableReason: String? = null
)

sealed interface SteamConnectionState {
    data object Disconnected : SteamConnectionState
    data object Connecting : SteamConnectionState
    data class Connected(val profile: SteamProfile) : SteamConnectionState
    data class Error(val message: String) : SteamConnectionState
}

class SteamRepository(
    private val authStore: SteamAuthStore,
    private val api: SteamWebApi = SteamWebApi.create()
) {
    private val _connectionState = MutableStateFlow<SteamConnectionState>(SteamConnectionState.Disconnected)
    val connectionState: StateFlow<SteamConnectionState> = _connectionState.asStateFlow()

    val isConfigured: Boolean get() = BuildConfig.STEAM_API_KEY.isNotBlank()

    suspend fun restoreSession() {
        val steamId = authStore.steamId64 ?: return
        loadProfile(steamId)
    }

    fun loginUrl(): String = SteamOpenId.buildLoginUrl()

    suspend fun handleLoginCallback(uri: Uri) {
        _connectionState.value = SteamConnectionState.Connecting
        val steamId = SteamOpenId.verifyCallback(uri)
        if (steamId == null) {
            _connectionState.value = SteamConnectionState.Error("Steam sign-in couldn't be verified. Try again.")
            return
        }
        authStore.steamId64 = steamId
        loadProfile(steamId)
    }

    fun disconnect() {
        authStore.disconnect()
        _connectionState.value = SteamConnectionState.Disconnected
    }

    private suspend fun loadProfile(steamId: String) {
        val player = runCatching { api.getPlayerSummaries(BuildConfig.STEAM_API_KEY, steamId) }
            .getOrNull()?.response?.players?.firstOrNull()
        if (player == null) {
            _connectionState.value = SteamConnectionState.Error(
                "Couldn't load your Steam profile. Check your connection and try again."
            )
            return
        }
        _connectionState.value = SteamConnectionState.Connected(
            SteamProfile(
                steamId = player.steamid,
                personaName = player.personaname,
                avatarUrl = player.avatarfull,
                profileUrl = player.profileurl,
                isPublic = player.communityvisibilitystate >= 3
            )
        )
    }

    suspend fun getOwnedGames(steamId: String): List<SteamGameSummary> {
        val response = runCatching { api.getOwnedGames(BuildConfig.STEAM_API_KEY, steamId) }.getOrNull()
        return response?.response?.games.orEmpty()
            .map { game ->
                SteamGameSummary(
                    appId = game.appid,
                    name = game.name,
                    iconUrl = game.img_icon_url?.takeIf { it.isNotBlank() }?.let { hash ->
                        "https://media.steampowered.com/steamcommunity/public/images/apps/${game.appid}/$hash.jpg"
                    },
                    playtimeMinutes = game.playtime_forever,
                    hasStats = game.has_community_visible_stats
                )
            }
            .sortedByDescending { it.playtimeMinutes }
    }

    suspend fun getAchievements(steamId: String, appId: Int): SteamGameAchievements {
        val playerStats = runCatching { api.getPlayerAchievements(BuildConfig.STEAM_API_KEY, steamId, appId) }
            .getOrNull()?.playerstats

        if (playerStats == null || !playerStats.success) {
            val reason = playerStats?.error ?: "This game has no public achievement data."
            return SteamGameAchievements(gameName = null, achievements = emptyList(), unavailableReason = reason)
        }

        val schema = runCatching { api.getSchemaForGame(BuildConfig.STEAM_API_KEY, appId) }
            .getOrNull()?.game?.availableGameStats?.achievements.orEmpty()
            .associateBy { it.name }

        val achievements = playerStats.achievements.orEmpty().map { achieved ->
            val meta = schema[achieved.apiname]
            SteamAchievement(
                apiName = achieved.apiname,
                displayName = meta?.displayName?.takeIf { it.isNotBlank() } ?: achieved.apiname,
                description = meta?.description,
                iconUrl = if (achieved.achieved == 1) meta?.icon else meta?.icongray,
                unlocked = achieved.achieved == 1,
                unlockedAt = achieved.unlocktime.takeIf { it > 0 }
            )
        }.sortedByDescending { it.unlocked }

        return SteamGameAchievements(gameName = playerStats.gameName, achievements = achievements)
    }
}
