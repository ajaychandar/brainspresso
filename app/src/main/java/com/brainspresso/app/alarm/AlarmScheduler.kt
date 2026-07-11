package com.brainspresso.app.alarm

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import com.brainspresso.app.data.db.AlarmEntity
import com.brainspresso.app.ui.MainActivity
import com.brainspresso.core.alarm.AlarmTimeCalculator
import java.time.ZonedDateTime

/**
 * Thin wrapper around [AlarmManager]. Uses `setAlarmClock`, which is exempt from the
 * exact-alarm permission gate on Android 12+ because this app's core function *is* alarms.
 */
class AlarmScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun schedule(alarm: AlarmEntity) {
        val triggerAt = AlarmTimeCalculator.nextTrigger(
            hour = alarm.hour,
            minute = alarm.minute,
            repeatDays = alarm.repeatDays,
            now = ZonedDateTime.now(),
        ).toInstant().toEpochMilli()

        val showIntent = PendingIntent.getActivity(
            context,
            alarm.id.toInt(),
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        val firePendingIntent = PendingIntent.getBroadcast(
            context,
            alarm.id.toInt(),
            AlarmReceiver.buildIntent(context, alarm.id),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        alarmManager.setAlarmClock(
            AlarmManager.AlarmClockInfo(triggerAt, showIntent),
            firePendingIntent,
        )
    }

    fun cancel(alarmId: Long) {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            AlarmReceiver.buildIntent(context, alarmId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }
}
