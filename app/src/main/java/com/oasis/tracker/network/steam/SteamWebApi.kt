package com.oasis.tracker.network.steam

import com.oasis.tracker.network.NetworkModule
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Query

interface SteamWebApi {
    @GET("ISteamUser/GetPlayerSummaries/v2/")
    suspend fun getPlayerSummaries(
        @Query("key") key: String,
        @Query("steamids") steamIds: String
    ): SteamPlayerSummariesResponse

    @GET("IPlayerService/GetOwnedGames/v1/")
    suspend fun getOwnedGames(
        @Query("key") key: String,
        @Query("steamid") steamId: String,
        @Query("include_appinfo") includeAppInfo: Int = 1,
        @Query("include_played_free_games") includePlayedFree: Int = 1
    ): SteamOwnedGamesResponse

    @GET("ISteamUserStats/GetPlayerAchievements/v1/")
    suspend fun getPlayerAchievements(
        @Query("key") key: String,
        @Query("steamid") steamId: String,
        @Query("appid") appId: Int,
        @Query("l") language: String = "english"
    ): SteamPlayerAchievementsResponse

    @GET("ISteamUserStats/GetSchemaForGame/v2/")
    suspend fun getSchemaForGame(
        @Query("key") key: String,
        @Query("appid") appId: Int
    ): SteamSchemaResponse

    companion object {
        fun create(): SteamWebApi = Retrofit.Builder()
            .baseUrl("https://api.steampowered.com/")
            .client(NetworkModule.sharedOkHttpClient)
            .addConverterFactory(NetworkModule.jsonConverter)
            .build()
            .create(SteamWebApi::class.java)
    }
}
