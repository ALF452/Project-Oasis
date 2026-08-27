package com.oasis.tracker.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.foundation.layout.* // weight() as a single named import collides with an internal same-named property in this Compose version
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.oasis.tracker.ui.gamedetail.GameDetailScreen
import com.oasis.tracker.ui.mainmenu.MainMenuScreen
import com.oasis.tracker.ui.platform.PlatformDetailScreen
import com.oasis.tracker.ui.rememberOasisApp
import com.oasis.tracker.ui.search.GameSearchScreen
import com.oasis.tracker.ui.tracker.MonthlyTrackerScreen
import com.oasis.tracker.ui.tracker.YearlyTrackerScreen
import com.oasis.tracker.ui.update.UpdateBanner
import com.oasis.tracker.update.UpdateState

@Composable
fun OasisRoot() {
    val app = rememberOasisApp()
    val context = LocalContext.current
    val updateState by app.updateManager.state.collectAsState()

    LaunchedEffect(Unit) {
        app.updateManager.checkForUpdate()
    }

    Column(modifier = Modifier.fillMaxSize().systemBarsPadding()) {
        UpdateBanner(
            state = updateState,
            onDownload = { (updateState as? UpdateState.Available)?.let { app.updateManager.startDownload(it.info) } },
            onInstall = {
                val ready = updateState as? UpdateState.ReadyToInstall ?: return@UpdateBanner
                if (app.updateManager.canInstallPackages()) {
                    context.startActivity(app.updateManager.installApkIntent(ready.apkFile))
                } else {
                    context.startActivity(app.updateManager.installPermissionSettingsIntent())
                }
            },
            onDismiss = { app.updateManager.dismiss() }
        )

        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = OasisDestinations.MAIN_MENU,
            modifier = Modifier.weight(1f)
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
}
