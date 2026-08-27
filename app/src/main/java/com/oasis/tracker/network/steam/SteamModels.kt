package com.oasis.tracker.network.steam

import kotlinx.serialization.Serializable

@Serializable
data class SteamPlayerSummariesResponse(val response: SteamPlayerSummariesInner = SteamPlayerSummariesInner())

@Serializable
data class SteamPlayerSummariesInner(val players: List<SteamPlayerSummaryDto> = emptyList())

@Serializable
data class SteamPlayerSummaryDto(
    val steamid: String = "",
    val personaname: String = "",
    val avatarfull: String? = null,
    val profileurl: String? = null,
    val communityvisibilitystate: Int = 1
)

@Serializable
data class SteamOwnedGamesResponse(val response: SteamOwnedGamesInner = SteamOwnedGamesInner())

@Serializable
data class SteamOwnedGamesInner(val game_count: Int = 0, val games: List<SteamOwnedGameDto> = emptyList())

@Serializable
data class SteamOwnedGameDto(
    val appid: Int = 0,
    val name: String = "",
    val playtime_forever: Int = 0,
    val img_icon_url: String? = null,
    val has_community_visible_stats: Boolean = false
)

@Serializable
data class SteamPlayerAchievementsResponse(val playerstats: SteamPlayerAchievementsInner = SteamPlayerAchievementsInner())

@Serializable
data class SteamPlayerAchievementsInner(
    val steamID: String? = null,
    val gameName: String? = null,
    val achievements: List<SteamPlayerAchievementDto>? = null,
    val success: Boolean = false,
    val error: String? = null
)

@Serializable
data class SteamPlayerAchievementDto(
    val apiname: String = "",
    val achieved: Int = 0,
    val unlocktime: Long = 0
)

@Serializable
data class SteamSchemaResponse(val game: SteamSchemaGame = SteamSchemaGame())

@Serializable
data class SteamSchemaGame(val availableGameStats: SteamSchemaStats? = null)

@Serializable
data class SteamSchemaStats(val achievements: List<SteamSchemaAchievementDto>? = null)

@Serializable
data class SteamSchemaAchievementDto(
    val name: String = "",
    val displayName: String = "",
    val description: String? = null,
    val icon: String? = null,
    val icongray: String? = null,
    val hidden: Int = 0
)
