package com.oasis.tracker.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.oasis.tracker.ui.backlog.BacklogScreen
import com.oasis.tracker.ui.diary.DiaryFeedScreen
import com.oasis.tracker.ui.gamedetail.GameDetailScreen
import com.oasis.tracker.ui.mainmenu.MainMenuScreen
import com.oasis.tracker.ui.platform.PlatformDetailScreen
import com.oasis.tracker.ui.search.GameSearchMode
import com.oasis.tracker.ui.search.GameSearchScreen
import com.oasis.tracker.ui.steam.SteamGameAchievementsScreen
import com.oasis.tracker.ui.steam.SteamLoginScreen
import com.oasis.tracker.ui.steam.SteamScreen
import com.oasis.tracker.ui.topranking.Top250Screen
import com.oasis.tracker.ui.tracker.MonthlyTrackerScreen
import com.oasis.tracker.ui.tracker.YearlyTrackerScreen

@Composable
fun OasisRoot() {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = OasisDestinations.MAIN_MENU,
        modifier = Modifier.fillMaxSize().systemBarsPadding()
    ) {
        composable(OasisDestinations.MAIN_MENU) {
            MainMenuScreen(
                onOpenMonthlyTracker = { navController.navigate(OasisDestinations.MONTHLY_TRACKER) },
                onOpenYearlyTracker = { navController.navigate(OasisDestinations.YEARLY_TRACKER) },
                onOpenPlatform = { platformId -> navController.navigate(OasisDestinations.platformDetail(platformId)) },
                onOpenSteam = { navController.navigate(OasisDestinations.STEAM) },
                onOpenTopRanking = { navController.navigate(OasisDestinations.TOP_RANKING) },
                onOpenDiary = { navController.navigate(OasisDestinations.DIARY) },
                onOpenBacklog = { navController.navigate(OasisDestinations.BACKLOG) },
                onOpenFavoritesPicker = { navController.navigate(OasisDestinations.FAVORITES_ADD) },
                onOpenGame = { gameId -> navController.navigate(OasisDestinations.gameDetail(gameId)) }
            )
        }
        composable(OasisDestinations.FAVORITES_ADD) {
            GameSearchScreen(
                mode = GameSearchMode.AddToFavorites,
                onBack = { navController.popBackStack() },
                onGameAdded = { navController.popBackStack() }
            )
        }
        composable(OasisDestinations.DIARY) {
            DiaryFeedScreen(
                onBack = { navController.popBackStack() },
                onOpenGame = { gameId -> navController.navigate(OasisDestinations.gameDetail(gameId)) }
            )
        }
        composable(OasisDestinations.BACKLOG) {
            BacklogScreen(
                onBack = { navController.popBackStack() },
                onAddGame = { navController.navigate(OasisDestinations.BACKLOG_ADD) }
            )
        }
        composable(OasisDestinations.BACKLOG_ADD) {
            GameSearchScreen(
                mode = GameSearchMode.AddToBacklog,
                onBack = { navController.popBackStack() },
                onGameAdded = { navController.popBackStack() }
            )
        }
        composable(OasisDestinations.STEAM) {
            SteamScreen(
                onBack = { navController.popBackStack() },
                onOpenLogin = { navController.navigate(OasisDestinations.STEAM_LOGIN) },
                onOpenGameAchievements = { appId -> navController.navigate(OasisDestinations.steamAchievements(appId)) }
            )
        }
        composable(OasisDestinations.STEAM_LOGIN) {
            SteamLoginScreen(onBack = { navController.popBackStack() })
        }
        composable(OasisDestinations.TOP_RANKING) {
            Top250Screen(
                onBack = { navController.popBackStack() },
                onOpenGame = { gameId -> navController.navigate(OasisDestinations.gameDetail(gameId)) },
                onAddGame = { navController.navigate(OasisDestinations.TOP_RANKING_ADD) }
            )
        }
        composable(OasisDestinations.TOP_RANKING_ADD) {
            GameSearchScreen(
                mode = GameSearchMode.AddToTopRanking,
                onBack = { navController.popBackStack() },
                onGameAdded = { navController.popBackStack() }
            )
        }
        composable(
            route = OasisDestinations.STEAM_ACHIEVEMENTS,
            arguments = listOf(navArgument(OasisDestinations.ARG_APP_ID) { type = NavType.IntType })
        ) { backStackEntry ->
            val appId = backStackEntry.arguments?.getInt(OasisDestinations.ARG_APP_ID) ?: 0
            SteamGameAchievementsScreen(appId = appId, onBack = { navController.popBackStack() })
        }
        composable(OasisDestinations.MONTHLY_TRACKER) {
            MonthlyTrackerScreen(onBack = { navController.popBackStack() })
        }
        composable(OasisDestinations.YEARLY_TRACKER) {
            YearlyTrackerScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = OasisDestinations.PLATFORM_DETAIL,
            arguments = listOf(navArgument(OasisDestinations.ARG_PLATFORM_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val platformId = backStackEntry.arguments?.getString(OasisDestinations.ARG_PLATFORM_ID).orEmpty()
            PlatformDetailScreen(
                platformId = platformId,
                onBack = { navController.popBackStack() },
                onAddGame = { navController.navigate(OasisDestinations.gameSearch(platformId)) },
                onOpenGame = { gameId -> navController.navigate(OasisDestinations.gameDetail(gameId)) }
            )
        }
        composable(
            route = OasisDestinations.GAME_SEARCH,
            arguments = listOf(navArgument(OasisDestinations.ARG_PLATFORM_ID) { type = NavType.StringType })
        ) { backStackEntry ->
            val platformId = backStackEntry.arguments?.getString(OasisDestinations.ARG_PLATFORM_ID).orEmpty()
            GameSearchScreen(
                mode = GameSearchMode.AddToLibrary(platformId),
                onBack = { navController.popBackStack() },
                onGameAdded = { navController.popBackStack() }
            )
        }
        composable(
            route = OasisDestinations.GAME_DETAIL,
            arguments = listOf(navArgument(OasisDestinations.ARG_GAME_ID) { type = NavType.LongType })
        ) { backStackEntry ->
            val gameId = backStackEntry.arguments?.getLong(OasisDestinations.ARG_GAME_ID) ?: 0L
            GameDetailScreen(gameId = gameId, onBack = { navController.popBackStack() })
        }
    }
}
