package com.oasis.tracker.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.SystemClock
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.oasis.tracker.BuildConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.File

sealed interface UpdateState {
    data object Idle : UpdateState
    data object Checking : UpdateState
    data class Available(val info: UpdateInfo) : UpdateState
    data class Downloading(val info: UpdateInfo) : UpdateState
    data class ReadyToInstall(val apkFile: File) : UpdateState
    data class Error(val message: String) : UpdateState
}

/**
 * Drives the sideload update flow: check GitHub's latest release, download the
 * APK asset with [DownloadManager], then hand it to the system installer via a
 * FileProvider URI so the running app is upgraded in place (same signing key
 * keeps the Room database and preferences intact).
 */
class ApkUpdateManager(private val appContext: Context) {

    private val checker = UpdateChecker()
    private val prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val state: StateFlow<UpdateState> = _state.asStateFlow()

    private var enqueuedDownloadId: Long? = null
    private var receiver: BroadcastReceiver? = null
    private var lastCheckedAtElapsedMs: Long = 0L

    /** Whether the UI has already auto-launched the install prompt for the current
     *  ready-to-install download, so returning to the main menu (or any other
     *  recomposition) doesn't keep popping the system install dialog again. */
    private var autoInstallPromptShown = false

    /** versionCode the on-disk [apkFile] was downloaded for, so a leftover file from an
     *  update that's already been installed isn't mistaken for one still pending. */
    private var pendingApkVersionCode: Int
        get() = prefs.getInt(KEY_PENDING_VERSION_CODE, -1)
        set(value) = prefs.edit().putInt(KEY_PENDING_VERSION_CODE, value).apply()

    suspend fun checkForUpdate() {
        if (apkFile().exists()) {
            if (pendingApkVersionCode > BuildConfig.VERSION_CODE) {
                _state.value = UpdateState.ReadyToInstall(apkFile())
                return
            }
            // Stale leftover from an update that's already installed (or from a
            // build downgrade) — clear it so it can't get stuck offering forever.
            apkFile().delete()
        }

        // The GitHub REST API is unauthenticated here and rate-limited to 60
        // requests/hour per IP; re-checking on every app resume can burn through
        // that fast, so skip re-checks within a short window of the last one.
        val now = SystemClock.elapsedRealtime()
        if (lastCheckedAtElapsedMs != 0L && now - lastCheckedAtElapsedMs < MIN_RECHECK_INTERVAL_MS) {
            return
        }
        lastCheckedAtElapsedMs = now

        _state.value = UpdateState.Checking
        val info = checker.checkForUpdate()
        _state.value = if (info != null) UpdateState.Available(info) else UpdateState.Idle
    }

    fun startDownload(info: UpdateInfo) {
        apkFile().delete()
        pendingApkVersionCode = info.versionCode
        autoInstallPromptShown = false
        val downloadManager = appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(info.downloadUrl))
            .setTitle("Oasis update ${info.versionName}")
            // VISIBLE rather than VISIBLE_NOTIFY_COMPLETED: the app itself now opens the
            // installer automatically the moment the download finishes (see
            // consumeAutoInstallPrompt), so a separate "download complete, tap to open"
            // system notification would just be a second, redundant, easy-to-tap-instead
            // path into the exact same install flow.
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setDestinationInExternalFilesDir(appContext, "updates", APK_FILE_NAME)
            .setAllowedOverMetered(true)
            .setMimeType("application/vnd.android.package-archive")

        enqueuedDownloadId = downloadManager.enqueue(request)
        _state.value = UpdateState.Downloading(info)
        registerCompletionReceiver()
    }

    private fun registerCompletionReceiver() {
        if (receiver != null) return
        val newReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                val completedId = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                if (completedId == -1L || completedId != enqueuedDownloadId) return
                unregisterCompletionReceiver()
                _state.value = resolveDownloadResult(completedId)
            }
        }
        ContextCompat.registerReceiver(
            appContext,
            newReceiver,
            IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )
        receiver = newReceiver
    }

    /**
     * The completed broadcast fires whether the download succeeded or failed —
     * checking [apkFile] for mere existence isn't enough, since a failed or
     * partial download can still leave a (truncated) file behind. Ask
     * DownloadManager for the real terminal status instead.
     */
    private fun resolveDownloadResult(downloadId: Long): UpdateState {
        val downloadManager = appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        downloadManager.query(DownloadManager.Query().setFilterById(downloadId)).use { cursor ->
            if (!cursor.moveToFirst()) {
                apkFile().delete()
                return UpdateState.Error("Download failed. Check your connection and try again.")
            }
            val status = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_STATUS))
            if (status == DownloadManager.STATUS_SUCCESSFUL && apkFile().exists()) {
                return UpdateState.ReadyToInstall(apkFile())
            }
            apkFile().delete()
            val reason = cursor.getInt(cursor.getColumnIndexOrThrow(DownloadManager.COLUMN_REASON))
            return UpdateState.Error("Download failed (code $reason). Check your connection and try again.")
        }
    }

    private fun unregisterCompletionReceiver() {
        receiver?.let { runCatching { appContext.unregisterReceiver(it) } }
        receiver = null
    }

    /** Returns true the first time this is called for the current ready-to-install
     *  download, false on every call after — lets the UI auto-launch the system
     *  install prompt exactly once instead of every time it recomposes. */
    fun consumeAutoInstallPrompt(): Boolean {
        if (autoInstallPromptShown) return false
        autoInstallPromptShown = true
        return true
    }

    fun apkFile(): File = File(appContext.getExternalFilesDir("updates"), APK_FILE_NAME)

    fun canInstallPackages(): Boolean = appContext.packageManager.canRequestPackageInstalls()

    fun installPermissionSettingsIntent(): Intent =
        Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${appContext.packageName}"))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    fun installApkIntent(apkFile: File): Intent {
        val uri = FileProvider.getUriForFile(appContext, "${appContext.packageName}.fileprovider", apkFile)
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    fun dismiss() {
        _state.value = UpdateState.Idle
    }

    companion object {
        private const val APK_FILE_NAME = "oasis-update.apk"
        private const val PREFS_NAME = "oasis_update_manager"
        private const val KEY_PENDING_VERSION_CODE = "pending_apk_version_code"
        private const val MIN_RECHECK_INTERVAL_MS = 10 * 60 * 1000L
    }
}
