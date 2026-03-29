package com.stickytimer.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.stickytimer.app.ui.settings.StickySettingsScreen
import com.stickytimer.app.ui.theme.BedtimeStickyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BedtimeStickyTheme {
                StickySettingsScreen(appContainer = (application as StickyTimerApp).appContainer)
            }
        }
    }
}
