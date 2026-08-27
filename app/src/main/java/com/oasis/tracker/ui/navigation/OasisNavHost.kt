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
import com.oasis.tracker.ui.gamedetail.GameDetailScreen
import com.oasis.tracker.ui.mainmenu.MainMenuScreen
import com.oasis.tracker.ui.platform.PlatformDetailScreen
import com.oasis.tracker.ui.search.GameSearchScreen
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
                onOpenPlatform = { platformId -> navController.navigate(OasisDestinations.platformDetail(platformId)) }
            )
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
                platformId = platformId,
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
