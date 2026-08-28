package com.oasis.tracker.diagnostics

import android.os.Build
import com.oasis.tracker.BuildConfig
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Installs a [Thread.UncaughtExceptionHandler] that records the crash to
 * [CrashLogStore] before delegating to whatever handler was already in place
 * (the system default, which shows "Oasis has stopped" and kills the
 * process) — this only adds a durable record for the user to inspect on the
 * next launch, it doesn't change crash behavior itself.
 */
object CrashHandler {
    fun install(store: CrashLogStore) {
        val previousHandler = Thread.getDefaultUncaughtExceptionHandler()
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            runCatching { store.write(buildReport(throwable)) }
            previousHandler?.uncaughtException(thread, throwable)
        }
    }

    private fun buildReport(throwable: Throwable): String {
        val stackTrace = StringWriter().also { throwable.printStackTrace(PrintWriter(it)) }.toString()
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        return buildString {
            appendLine("Oasis crash — $timestamp")
            appendLine("App version: ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})")
            appendLine("Device: ${Build.MANUFACTURER} ${Build.MODEL}, Android ${Build.VERSION.RELEASE} (SDK ${Build.VERSION.SDK_INT})")
            appendLine()
            append(stackTrace)
        }
    }
}
