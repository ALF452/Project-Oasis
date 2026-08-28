package com.oasis.tracker.ui.steam

import android.annotation.SuppressLint
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.oasis.tracker.BuildConfig
import com.oasis.tracker.ui.rememberOasisApp
import kotlinx.coroutines.launch

/**
 * Steam's OpenID login has no third-party OAuth registration and rejects a
 * custom URL scheme as the return_to value ("Invalid return protocol"), so a
 * Custom Tab + deep link back into the app doesn't work here. Instead this
 * loads the login page in an in-app WebView and intercepts the moment it
 * tries to navigate to STEAM_RETURN_URL — that URL is never actually hosted
 * or loaded, just used as a recognizable prefix to catch the redirect.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun SteamLoginScreen(onBack: () -> Unit) {
    val app = rememberOasisApp()
    val scope = rememberCoroutineScope()
    val loginUrl = remember { app.steamRepository.loginUrl() }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                webViewClient = object : WebViewClient() {
                    override fun shouldOverrideUrlLoading(
                        view: WebView,
                        request: WebResourceRequest
                    ): Boolean {
                        val url = request.url
                        if (!url.toString().startsWith(BuildConfig.STEAM_RETURN_URL)) return false
                        scope.launch {
                            app.steamRepository.handleLoginCallback(url)
                            onBack()
                        }
                        return true
                    }
                }
                loadUrl(loginUrl)
            }
        },
        // WebView holds real native (Chromium engine) resources that Compose
        // won't release on its own — just detaching it from the hierarchy
        // when this screen is left isn't enough and leaks memory each time
        // the user opens the login screen.
        onRelease = { it.destroy() }
    )
}
