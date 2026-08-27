package com.oasis.tracker

import android.app.Application
import android.net.Uri
import com.oasis.tracker.data.GameRepository
import com.oasis.tracker.data.OasisDatabase
import com.oasis.tracker.data.SteamAuthStore
import com.oasis.tracker.network.GameSearchRepository
import com.oasis.tracker.network.steam.SteamRepository
import com.oasis.tracker.update.ApkUpdateManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

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

    // MainActivity pushes the Steam OpenID redirect Uri here (from onCreate's
    // initial intent or onNewIntent); the Steam screen observes and consumes it.
    private val _steamLoginCallback = MutableStateFlow<Uri?>(null)
    val steamLoginCallback = _steamLoginCallback.asStateFlow()

    fun postSteamLoginCallback(uri: Uri) {
        _steamLoginCallback.value = uri
    }

    fun consumeSteamLoginCallback() {
        _steamLoginCallback.value = null
    }

    override fun onCreate() {
        super.onCreate()
        gameRepository = GameRepository(OasisDatabase.getInstance(this))
        searchRepository = GameSearchRepository()
        updateManager = ApkUpdateManager(this)
        steamRepository = SteamRepository(SteamAuthStore(this))
    }
}
