package com.oasis.tracker.ui.navigation

object OasisDestinations {
    const val MAIN_MENU = "main_menu"
    const val MONTHLY_TRACKER = "monthly_tracker"
    const val YEARLY_TRACKER = "yearly_tracker"
    const val PLATFORM_DETAIL = "platform/{platformId}"
    const val GAME_SEARCH = "search/{platformId}"
    const val GAME_DETAIL = "game/{gameId}"
    const val STEAM = "steam"
    const val STEAM_LOGIN = "steam/login"
    const val STEAM_ACHIEVEMENTS = "steam/achievements/{appId}"
    const val TOP_RANKING = "top_ranking"
    const val TOP_RANKING_ADD = "top_ranking/add"
    const val DIARY = "diary"
    const val BACKLOG = "backlog"
    const val BACKLOG_ADD = "backlog/add"
    const val FAVORITES_ADD = "favorites/add"

    const val ARG_PLATFORM_ID = "platformId"
    const val ARG_GAME_ID = "gameId"
    const val ARG_APP_ID = "appId"

    fun platformDetail(platformId: String) = "platform/$platformId"
    fun gameSearch(platformId: String) = "search/$platformId"
    fun gameDetail(gameId: Long) = "game/$gameId"
    fun steamAchievements(appId: Int) = "steam/achievements/$appId"
}
