package com.brainspresso.app.alarm

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.ContextCompat
import com.brainspresso.app.BrainspressoApp
import com.brainspresso.app.service.AlarmRingingService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra(EXTRA_ALARM_ID, -1L)
        if (alarmId == -1L) return

        val app = context.applicationContext as BrainspressoApp
        val pendingResult = goAsync()

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val alarm = app.alarmRepository.getById(alarmId)
                if (alarm != null && alarm.enabled) {
                    runCatching {
                        ContextCompat.startForegroundService(
                            context,
                            AlarmRingingService.buildIntent(context, alarm.id),
                        )
                    }.onFailure { e ->
                        Log.e("AlarmReceiver", "Failed to start ringing service for alarm ${alarm.id}", e)
                    }
                    if (alarm.isRepeating) {
                        app.alarmScheduler.schedule(alarm)
                    } else {
                        app.alarmRepository.setEnabled(alarm, enabled = false)
                    }
                }
            } finally {
                pendingResult.finish()
            }
        }
    }

    companion object {
        private const val EXTRA_ALARM_ID = "extra_alarm_id"

        fun buildIntent(context: Context, alarmId: Long): Intent =
            Intent(context, AlarmReceiver::class.java).putExtra(EXTRA_ALARM_ID, alarmId)
    }
}
