package com.stickytimer.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.stickytimer.app.ui.settings.StickySettingsScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StickySettingsScreen(appContainer = (application as StickyTimerApp).appContainer)
        }
    }
}
