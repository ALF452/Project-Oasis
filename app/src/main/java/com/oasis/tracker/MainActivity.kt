package com.oasis.tracker

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.oasis.tracker.ui.navigation.OasisRoot
import com.oasis.tracker.ui.theme.OasisTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIntent(intent)
        setContent {
            OasisTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    OasisRoot()
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIntent(intent)
    }

    /** Steam's OpenID login redirects back here via the oasis://steamcallback deep link. */
    private fun handleIntent(intent: Intent) {
        val uri = intent.data ?: return
        if (uri.scheme == "oasis" && uri.host == "steamcallback") {
            (applicationContext as OasisApp).postSteamLoginCallback(uri)
        }
    }
}
