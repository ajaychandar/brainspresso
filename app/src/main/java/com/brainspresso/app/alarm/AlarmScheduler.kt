package com.brainspresso.app.alarm

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
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

    /**
     * Whether the OS will currently allow this app to schedule alarms that fire at the exact
     * time requested. Android docs say `setAlarmClock()` is exempt from this gate, but real
     * devices (particularly some OEM builds) have been observed enforcing it anyway, in which
     * case `setAlarmClock()` silently fails to actually arm the alarm. Checked proactively by
     * the UI so the user can be told to grant "Alarms & reminders" rather than an alarm just
     * quietly never firing.
     */
    fun canScheduleExactAlarms(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || alarmManager.canScheduleExactAlarms()

    // Lint's ScheduleExactAlarm check flags setAlarmClock() too, but per Android's own docs
    // that API is exempt from the SCHEDULE_EXACT_ALARM permission gate for alarm-clock apps.
    @SuppressLint("ScheduleExactAlarm")
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

        // A scheduling failure (e.g. a SecurityException if some OEM enforces the exact-alarm
        // permission more strictly than stock Android's documented setAlarmClock exemption)
        // must never crash the app -- the alarm is already saved in the database at this point,
        // and the user can still see and re-save it. Fail safe, not fatal.
        runCatching {
            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(triggerAt, showIntent),
                firePendingIntent,
            )
        }.onFailure { e ->
            Log.e(TAG, "Failed to schedule alarm ${alarm.id}", e)
        }
    }

    fun cancel(alarmId: Long) {
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            alarmId.toInt(),
            AlarmReceiver.buildIntent(context, alarmId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        runCatching {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }.onFailure { e ->
            Log.e(TAG, "Failed to cancel alarm $alarmId", e)
        }
    }

    private companion object {
        const val TAG = "AlarmScheduler"
    }
}
