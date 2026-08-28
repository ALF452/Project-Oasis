package com.oasis.tracker

import android.app.Application
import com.oasis.tracker.data.GameRepository
import com.oasis.tracker.data.OasisDatabase
import com.oasis.tracker.data.SteamAuthStore
import com.oasis.tracker.diagnostics.CrashHandler
import com.oasis.tracker.diagnostics.CrashLogStore
import com.oasis.tracker.network.GameSearchRepository
import com.oasis.tracker.network.steam.SteamRepository
import com.oasis.tracker.update.ApkUpdateManager

/** Minimal hand-rolled service locator; the app is small enough not to need a DI framework. */
class OasisApp : Application() {

    lateinit var gameRepository: GameRepository
        private set
    lateinit var searchRepository: GameSearchRepository
        private set
    lateinit var updateManager: ApkUpdateManager
        private set
    lateinit var steamRepository: SteamRepository
        private set
    lateinit var crashLogStore: CrashLogStore
        private set

    override fun onCreate() {
        super.onCreate()
        crashLogStore = CrashLogStore(this)
        CrashHandler.install(crashLogStore)
        gameRepository = GameRepository(OasisDatabase.getInstance(this))
        searchRepository = GameSearchRepository()
        updateManager = ApkUpdateManager(this)
        steamRepository = SteamRepository(SteamAuthStore(this))
    }
}
