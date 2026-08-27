package com.oasis.tracker.ui.navigation

object OasisDestinations {
    const val MAIN_MENU = "main_menu"
    const val MONTHLY_TRACKER = "monthly_tracker"
    const val YEARLY_TRACKER = "yearly_tracker"
    const val PLATFORM_DETAIL = "platform/{platformId}"
    const val GAME_SEARCH = "search/{platformId}"
    const val GAME_DETAIL = "game/{gameId}"

    const val ARG_PLATFORM_ID = "platformId"
    const val ARG_GAME_ID = "gameId"

    fun platformDetail(platformId: String) = "platform/$platformId"
    fun gameSearch(platformId: String) = "search/$platformId"
    fun gameDetail(gameId: Long) = "game/$gameId"
}
