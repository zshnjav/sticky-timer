package com.stickytimer.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.stickytimer.app.AppContainer
import com.stickytimer.app.R
import com.stickytimer.app.core.settings.StickySettings
import com.stickytimer.app.core.timer.StickyPhase
import com.stickytimer.app.feature.sticky.StickyForegroundService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StickySettingsScreen(appContainer: AppContainer) {
    val context = LocalContext.current
    val settings by appContainer.settingsRepository.settings.collectAsState(initial = StickySettings())
    val mode by appContainer.statusStore.state.collectAsState()
    val lifecycleOwner = LocalLifecycleOwner.current
    val ioScope = rememberCoroutineScope()

    var hasAccess by remember { mutableStateOf(appContainer.notificationAccessManager.hasAccess()) }

    DisposableEffect(lifecycleOwner) {
        val lifecycle = lifecycleOwner.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasAccess = appContainer.notificationAccessManager.hasAccess()
            }
        }
        lifecycle.addObserver(observer)
        onDispose { lifecycle.removeObserver(observer) }
    }

    MaterialTheme {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = context.getString(R.string.settings_title)) },
                    windowInsets = WindowInsets.statusBars
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(text = context.getString(R.string.settings_subtitle), style = MaterialTheme.typography.bodyMedium)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = context.getString(R.string.toggle_title), style = MaterialTheme.typography.titleMedium)
                        val statusText = if (mode.isEnabled) R.string.status_enabled else R.string.status_disabled
                        Text(text = context.getString(statusText), style = MaterialTheme.typography.bodyMedium)
                        Text(text = phaseLabel(mode.phase, context), style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(
                        checked = mode.isEnabled,
                        onCheckedChange = { enabled ->
                            if (enabled) {
                                if (!hasAccess) {
                                    context.startActivity(appContainer.notificationAccessManager.accessSettingsIntent())
                                } else {
                                    StickyForegroundService.startEnable(context)
                                }
                            } else {
                                StickyForegroundService.requestDisable(context)
                            }
                        }
                    )
                }

                if (!hasAccess) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = context.getString(R.string.notification_access_required),
                                style = MaterialTheme.typography.bodyMedium
                            )
                            Button(onClick = {
                                context.startActivity(appContainer.notificationAccessManager.accessSettingsIntent())
                            }) {
                                Text(text = context.getString(R.string.open_notification_access))
                            }
                        }
                    }
                }

                DurationSlider(
                    title = context.getString(R.string.setting_session_duration),
                    secondsValue = settings.sessionDurationSec,
                    minSeconds = 10,
                    maxSeconds = 1800,
                    stepSeconds = 5,
                    onPersist = { sec ->
                        ioScope.launch(Dispatchers.IO) {
                            appContainer.settingsRepository.updateSessionDurationSec(sec)
                        }
                    }
                )

                DurationSlider(
                    title = context.getString(R.string.setting_fade_duration),
                    secondsValue = settings.fadeDurationSec,
                    minSeconds = 0,
                    maxSeconds = 60,
                    stepSeconds = 1,
                    onPersist = { sec ->
                        ioScope.launch(Dispatchers.IO) {
                            appContainer.settingsRepository.updateFadeDurationSec(sec)
                        }
                    }
                )

                DurationSlider(
                    title = context.getString(R.string.setting_reengagement_window),
                    secondsValue = settings.reengagementWindowSec,
                    minSeconds = 5,
                    maxSeconds = 180,
                    stepSeconds = 5,
                    onPersist = { sec ->
                        ioScope.launch(Dispatchers.IO) {
                            appContainer.settingsRepository.updateReengagementWindowSec(sec)
                        }
                    }
                )

                DurationSlider(
                    title = context.getString(R.string.setting_max_window),
                    secondsValue = settings.maxActiveWindowMin * 60,
                    minSeconds = 15 * 60,
                    maxSeconds = 300 * 60,
                    stepSeconds = 5 * 60,
                    onPersist = { sec ->
                        ioScope.launch(Dispatchers.IO) {
                            appContainer.settingsRepository.updateMaxActiveWindowMin(sec / 60)
                        }
                    }
                )
            }
        }
    }
}

private fun phaseLabel(phase: StickyPhase, context: android.content.Context): String {
    return when (phase) {
        StickyPhase.OFF -> context.getString(R.string.phase_off)
        StickyPhase.ON_IDLE -> context.getString(R.string.phase_on_idle)
        StickyPhase.SESSION_RUNNING -> context.getString(R.string.phase_session_running)
        StickyPhase.FADING -> context.getString(R.string.phase_fading)
        StickyPhase.STOPPED_RECENTLY -> context.getString(R.string.phase_stopped_recently)
        StickyPhase.EXPIRED -> context.getString(R.string.phase_expired)
    }
}

@Composable
private fun DurationSlider(
    title: String,
    secondsValue: Int,
    minSeconds: Int,
    maxSeconds: Int,
    stepSeconds: Int,
    onPersist: (Int) -> Unit
) {
    var sliderPosition by remember(secondsValue) { mutableFloatStateOf(secondsValue.toFloat()) }
    val valueRange = minSeconds.toFloat()..maxSeconds.toFloat()
    val steps = ((maxSeconds - minSeconds) / stepSeconds).coerceAtLeast(1) - 1
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(text = title, style = MaterialTheme.typography.titleSmall)
        Text(
            text = formatDuration(sliderPosition.toInt()),
            style = MaterialTheme.typography.bodyMedium
        )
        Slider(
            value = sliderPosition,
            onValueChange = {
                val snapped = (it / stepSeconds).toInt() * stepSeconds
                sliderPosition = snapped.toFloat().coerceIn(valueRange)
            },
            onValueChangeFinished = {
                onPersist(sliderPosition.toInt())
            },
            valueRange = valueRange,
            steps = steps
        )
    }
    Spacer(modifier = Modifier.height(2.dp))
}

private fun formatDuration(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (seconds == 0) "${minutes}m" else "${minutes}m ${seconds}s"
}
