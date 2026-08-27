package com.oasis.tracker.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.oasis.tracker.OasisApp

@Composable
fun rememberOasisApp(): OasisApp {
    val context = LocalContext.current.applicationContext
    return context as OasisApp
}
