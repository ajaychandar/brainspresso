package com.brainspresso.app.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.lifecycleScope
import com.brainspresso.app.BrainspressoApp
import com.brainspresso.app.R
import com.brainspresso.app.data.db.AlarmEntity
import com.brainspresso.app.ui.puzzle.PuzzleActivity
import kotlinx.coroutines.launch

/**
 * Foreground service that rings/vibrates on loop until [PuzzleActivity] stops it after the
 * user solves the required puzzle chain. Nothing else in this app calls [stopSelf] on it.
 */
class AlarmRingingService : LifecycleService() {

    private var mediaPlayer: MediaPlayer? = null
    private var vibrator: Vibrator? = null

    // If a second alarm fires while the first is still ringing, onStartCommand runs again and
    // launches a second coroutine. Without this guard, whichever DB lookup finishes last wins,
    // which could let a stale (first) coroutine overwrite the second alarm's sound/notification
    // after the fact. Only the most recently requested alarm id is allowed to take effect.
    private var activeAlarmId: Long = -1L

    override fun onCreate() {
        super.onCreate()
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            (getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager).defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        val alarmId = intent?.getLongExtra(EXTRA_ALARM_ID, -1L) ?: -1L
        if (alarmId == -1L) {
            stopSelf()
            return START_NOT_STICKY
        }

        activeAlarmId = alarmId

        // Show the foreground notification immediately; details are filled in once loaded.
        startForegroundWith(alarmId = alarmId, label = getString(R.string.alarm_ringing_default_label))

        lifecycleScope.launch {
            val app = application as BrainspressoApp
            val alarm = app.alarmRepository.getById(alarmId)
            if (alarm != null && alarmId == activeAlarmId) {
                startForegroundWith(alarmId = alarmId, label = alarm.label.ifBlank { getString(R.string.alarm_ringing_default_label) })
                startRinging(alarm)
            }
        }

        return START_STICKY
    }

    private fun startForegroundWith(alarmId: Long, label: String) {
        val notification = buildNotification(alarmId, label)
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            notification,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            } else {
                0
            },
        )
    }

    private fun buildNotification(alarmId: Long, label: String): Notification {
        val fullScreenIntent = PendingIntent.getActivity(
            this,
            alarmId.toInt(),
            PuzzleActivity.buildIntent(this, alarmId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

        return NotificationCompat.Builder(this, NotificationChannels.ALARM_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_alarm_notification)
            .setContentTitle(getString(R.string.alarm_ringing_title))
            .setContentText(label)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setOngoing(true)
            .setAutoCancel(false)
            .setOnlyAlertOnce(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setFullScreenIntent(fullScreenIntent, true)
            .setContentIntent(fullScreenIntent)
            // Deliberately no action buttons: solving the puzzle in PuzzleActivity is the
            // only way to stop the alarm.
            .build()
    }

    private fun startRinging(alarm: AlarmEntity) {
        playSound(alarm.soundUri)
        if (alarm.vibrate) startVibrating()
    }

    private fun playSound(soundUri: String?) {
        stopSound()
        val requestedUri = soundUri?.let(Uri::parse)
        val fallbackUri = defaultAlarmUri()

        val player = MediaPlayer()
        mediaPlayer = player

        val playedRequested = requestedUri != null && player.tryStart(requestedUri)
        if (!playedRequested && fallbackUri != null && fallbackUri != requestedUri) {
            player.tryStart(fallbackUri)
        }
    }

    private fun MediaPlayer.tryStart(uri: Uri): Boolean = runCatching {
        reset()
        setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ALARM)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build(),
        )
        isLooping = true
        setDataSource(this@AlarmRingingService, uri)
        prepare()
        start()
    }.isSuccess

    private fun defaultAlarmUri(): Uri? =
        RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM)
            ?: RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

    private fun startVibrating() {
        val pattern = longArrayOf(0, 800, 400, 800, 400)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator?.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            @Suppress("DEPRECATION")
            vibrator?.vibrate(pattern, 0)
        }
    }

    private fun stopSound() {
        mediaPlayer?.apply {
            runCatching { if (isPlaying) stop() }
            release()
        }
        mediaPlayer = null
    }

    private fun stopVibrating() {
        vibrator?.cancel()
    }

    override fun onDestroy() {
        stopSound()
        stopVibrating()
        super.onDestroy()
    }

    companion object {
        private const val NOTIFICATION_ID = 4242
        private const val EXTRA_ALARM_ID = "extra_alarm_id"

        fun buildIntent(context: Context, alarmId: Long): Intent =
            Intent(context, AlarmRingingService::class.java).putExtra(EXTRA_ALARM_ID, alarmId)

        /** Called only from [PuzzleActivity] once the required puzzle chain is solved. */
        fun stop(context: Context) {
            context.stopService(Intent(context, AlarmRingingService::class.java))
        }
    }
}
