package com.oasis.tracker

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
        // Launching via a home-screen shortcut (long-press the app icon) carries the
        // target route as a plain extra — read only here, not in onNewIntent, since a
        // shortcut always starts a fresh launch of this Activity in practice.
        val shortcutDestination = intent?.getStringExtra(SHORTCUT_DESTINATION_EXTRA)
        setContent {
            OasisTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    OasisRoot(initialDestination = shortcutDestination)
                }
            }
        }
    }

    companion object {
        const val SHORTCUT_DESTINATION_EXTRA = "shortcut_destination"
    }
}
