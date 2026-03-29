package com.stickytimer.app.ui.settings

import android.app.TimePickerDialog
import android.content.Context
import android.text.format.DateFormat
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.Dp
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
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

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

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(text = context.getString(R.string.settings_title)) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.86f),
                    titleContentColor = MaterialTheme.colorScheme.primary
                ),
                windowInsets = WindowInsets.statusBars
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF050913),
                            Color(0xFF091427),
                            Color(0xFF06101E)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(horizontal = 16.dp, vertical = 14.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                NeonCard {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            text = context.getString(R.string.settings_subtitle),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = context.getString(R.string.setting_fade_duration_help),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = context.getString(R.string.toggle_title),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = phaseLabel(mode.phase, context),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.secondary
                                )
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
                    }
                }

                if (!hasAccess) {
                    NeonCard(borderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(
                                text = context.getString(R.string.notification_access_required),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    description = context.getString(R.string.setting_session_duration_help),
                    secondsValue = settings.sessionDurationSec,
                    minSeconds = 60,
                    maxSeconds = 1800,
                    stepSeconds = 60,
                    onPersist = { sec ->
                        ioScope.launch(Dispatchers.IO) {
                            appContainer.settingsRepository.updateSessionDurationSec(sec)
                        }
                    }
                )

                AutoEnableTimeCard(
                    settings = settings,
                    onPick = { selectedMinutes ->
                        ioScope.launch(Dispatchers.IO) {
                            appContainer.settingsRepository.updateAutoEnableTimeMinutesOfDay(selectedMinutes)
                        }
                    }
                )

                DurationSlider(
                    title = context.getString(R.string.setting_max_window),
                    description = context.getString(R.string.setting_max_window_help),
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

@Composable
private fun NeonCard(
    modifier: Modifier = Modifier,
    borderColor: Color = MaterialTheme.colorScheme.primary.copy(alpha = 0.28f),
    cornerRadius: Dp = 16.dp,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.74f)
        ),
        border = BorderStroke(1.dp, borderColor),
        shape = RoundedCornerShape(cornerRadius)
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            content()
        }
    }
}

@Composable
private fun AutoEnableTimeCard(
    settings: StickySettings,
    onPick: (Int?) -> Unit
) {
    val context = LocalContext.current
    val selectedTimeLabel = if (settings.hasAutoEnableTime) {
        formatTimeOfDay(settings.autoEnableTimeMinutesOfDay, context)
    } else {
        context.getString(R.string.setting_auto_enable_time_off)
    }

    NeonCard(borderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.34f)) {
        Text(
            text = context.getString(R.string.setting_auto_enable_time).uppercase(),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = context.getString(R.string.setting_auto_enable_time_help),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = selectedTimeLabel,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = {
                showTimePicker(
                    context = context,
                    initialMinutesOfDay = settings.autoEnableTimeMinutesOfDay,
                    onPicked = { onPick(it) }
                )
            }) {
                Text(text = context.getString(R.string.setting_auto_enable_set_time))
            }

            if (settings.hasAutoEnableTime) {
                TextButton(onClick = { onPick(null) }) {
                    Text(text = context.getString(R.string.setting_auto_enable_clear_time))
                }
            }
        }
    }
}

private fun showTimePicker(
    context: Context,
    initialMinutesOfDay: Int,
    onPicked: (Int) -> Unit
) {
    val initial = if (initialMinutesOfDay in 0 until StickySettings.MINUTES_PER_DAY) {
        initialMinutesOfDay
    } else {
        22 * 60
    }
    val initialHour = initial / 60
    val initialMinute = initial % 60
    val dialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            onPicked(hourOfDay * 60 + minute)
        },
        initialHour,
        initialMinute,
        DateFormat.is24HourFormat(context)
    )
    dialog.show()
}

private fun formatTimeOfDay(minutesOfDay: Int, context: Context): String {
    if (minutesOfDay !in 0 until StickySettings.MINUTES_PER_DAY) {
        return context.getString(R.string.setting_auto_enable_time_off)
    }
    val hour = minutesOfDay / 60
    val minute = minutesOfDay % 60
    val formatter = if (DateFormat.is24HourFormat(context)) {
        DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    } else {
        DateTimeFormatter.ofPattern("h:mm a", Locale.getDefault())
    }
    return LocalTime.of(hour, minute).format(formatter)
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
    description: String,
    secondsValue: Int,
    minSeconds: Int,
    maxSeconds: Int,
    stepSeconds: Int,
    onPersist: (Int) -> Unit
) {
    var sliderPosition by remember(secondsValue, minSeconds, maxSeconds) {
        mutableFloatStateOf(secondsValue.coerceIn(minSeconds, maxSeconds).toFloat())
    }
    val valueRange = minSeconds.toFloat()..maxSeconds.toFloat()
    val steps = ((maxSeconds - minSeconds) / stepSeconds).coerceAtLeast(1) - 1
    NeonCard(borderColor = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.3f)) {
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = formatDuration(sliderPosition.toInt()),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
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
            steps = steps,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.35f)
            )
        )
    }
    Spacer(modifier = Modifier.height(2.dp))
}

private fun formatDuration(totalSeconds: Int): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (seconds == 0) "${minutes}m" else "${minutes}m ${seconds}s"
}
