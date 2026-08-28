package com.oasis.tracker

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.oasis.tracker.data.GameRepository
import com.oasis.tracker.data.OasisDatabase
import com.oasis.tracker.data.SteamAuthStore
import com.oasis.tracker.diagnostics.CrashHandler
import com.oasis.tracker.diagnostics.CrashLogStore
import com.oasis.tracker.network.GameSearchRepository
import com.oasis.tracker.network.NetworkModule
import com.oasis.tracker.network.steam.SteamRepository
import com.oasis.tracker.update.ApkUpdateManager

/** Minimal hand-rolled service locator; the app is small enough not to need a DI framework. */
class OasisApp : Application(), ImageLoaderFactory {

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

    /**
     * Without this, Coil builds its own default OkHttpClient for image loads — one that
     * doesn't carry the custom User-Agent NetworkModule.sharedOkHttpClient sets for API
     * calls. Wikimedia's API already required that header to work at all; its image CDN
     * (upload.wikimedia.org) appears to require it too, since cover URLs that resolve
     * correctly from the API still failed to load as images without it.
     */
    override fun newImageLoader(): ImageLoader =
        ImageLoader.Builder(this)
            .okHttpClient(NetworkModule.sharedOkHttpClient)
            .build()
}
