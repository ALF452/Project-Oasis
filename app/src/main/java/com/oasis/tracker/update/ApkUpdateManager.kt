package com.oasis.tracker.update

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
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
    private val _state = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val state: StateFlow<UpdateState> = _state.asStateFlow()

    private var enqueuedDownloadId: Long? = null
    private var receiver: BroadcastReceiver? = null

    suspend fun checkForUpdate() {
        _state.value = UpdateState.Checking
        val info = checker.checkForUpdate()
        _state.value = if (info != null) UpdateState.Available(info) else UpdateState.Idle
    }

    fun startDownload(info: UpdateInfo) {
        apkFile().delete()
        val downloadManager = appContext.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
        val request = DownloadManager.Request(Uri.parse(info.downloadUrl))
            .setTitle("Oasis update ${info.versionName}")
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
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
                _state.value = if (apkFile().exists()) {
                    UpdateState.ReadyToInstall(apkFile())
                } else {
                    UpdateState.Error("Download failed. Check your connection and try again.")
                }
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

    private fun unregisterCompletionReceiver() {
        receiver?.let { runCatching { appContext.unregisterReceiver(it) } }
        receiver = null
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
    }
}
