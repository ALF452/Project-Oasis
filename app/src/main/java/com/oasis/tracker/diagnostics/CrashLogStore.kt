package com.oasis.tracker.diagnostics

import android.content.Context
import java.io.File

/**
 * Persists the most recent uncaught crash's details to a single file so the
 * user can view or copy it after the app restarts. A sideloaded app has no
 * Play Store crash reporting behind it, so this file is the only record of
 * what went wrong. Only the latest crash is kept — this is a debugging aid,
 * not a log archive.
 */
class CrashLogStore(context: Context) {
    private val file = File(context.filesDir, "last_crash.txt")

    fun write(report: String) {
        runCatching { file.writeText(report) }
    }

    fun read(): String? = if (file.exists()) runCatching { file.readText() }.getOrNull() else null

    fun clear() {
        file.delete()
    }
}
