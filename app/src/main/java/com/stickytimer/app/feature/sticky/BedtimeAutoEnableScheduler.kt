package com.stickytimer.app.feature.sticky

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.stickytimer.app.core.settings.StickySettings
import com.stickytimer.app.platform.alarm.BedtimeAutoEnableReceiver
import java.time.ZonedDateTime

class BedtimeAutoEnableScheduler(context: Context) {
    private val appContext = context.applicationContext

    private val alarmManager: AlarmManager
        get() = appContext.getSystemService(AlarmManager::class.java)

    fun sync(settings: StickySettings) {
        val minutesOfDay = settings.autoEnableTimeMinutesOfDay
        if (!settings.hasAutoEnableTime) {
            cancel()
            return
        }
        schedule(minutesOfDay)
    }

    fun schedule(minutesOfDay: Int) {
        val clamped = minutesOfDay.coerceIn(0, StickySettings.MINUTES_PER_DAY - 1)
        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            nextTriggerTimeMs(clamped),
            alarmIntent()
        )
    }

    fun cancel() {
        alarmManager.cancel(alarmIntent())
    }

    private fun alarmIntent(): PendingIntent {
        val intent = Intent(appContext, BedtimeAutoEnableReceiver::class.java)
            .setAction(ACTION_AUTO_ENABLE_ALARM)
        return PendingIntent.getBroadcast(
            appContext,
            REQUEST_CODE,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun nextTriggerTimeMs(minutesOfDay: Int): Long {
        val hour = minutesOfDay / 60
        val minute = minutesOfDay % 60
        val now = ZonedDateTime.now()
        var trigger = now.withHour(hour).withMinute(minute).withSecond(0).withNano(0)
        if (!trigger.isAfter(now)) {
            trigger = trigger.plusDays(1)
        }
        return trigger.toInstant().toEpochMilli()
    }

    companion object {
        const val ACTION_AUTO_ENABLE_ALARM = "com.stickytimer.app.action.AUTO_ENABLE_ALARM"
        private const val REQUEST_CODE = 0xB741
    }
}
