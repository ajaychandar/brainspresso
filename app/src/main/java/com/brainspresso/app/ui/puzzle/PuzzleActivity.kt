package com.brainspresso.app.ui.puzzle

import android.Manifest
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.content.ContextCompat
import com.brainspresso.app.service.AlarmRingingService
import com.brainspresso.app.ui.theme.BrainspressoTheme

/**
 * Full-screen puzzle gate. Shows over the lock screen, blocks the back button, and is the
 * only place in the app that stops [AlarmRingingService].
 */
class PuzzleActivity : ComponentActivity() {

    private val alarmId: Long by lazy { intent.getLongExtra(EXTRA_ALARM_ID, -1L) }
    private var chainSolved = false

    private val viewModel: PuzzleViewModel by viewModels {
        PuzzleViewModel.Factory(application, alarmId)
    }

    private val sensorManager by lazy { getSystemService(SENSOR_SERVICE) as SensorManager }
    private var stepListener: SensorEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showOverLockScreen()

        onBackPressedDispatcher.addCallback(
            this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    // Consciously does nothing: back cannot dismiss the alarm.
                }
            },
        )

        setContent {
            BrainspressoTheme {
                val state by viewModel.uiState.collectAsState()

                DisposableEffect(state.isChainComplete) {
                    if (state.isChainComplete && !chainSolved) {
                        chainSolved = true
                        onChainSolved()
                    }
                    onDispose { }
                }

                DisposableEffect(state.isWalking) {
                    if (state.isWalking && !state.isChainComplete) {
                        startStepTrackingOrSkip()
                    }
                    onDispose { stopStepTracking() }
                }

                PuzzleScreen(
                    state = state,
                    onSolved = viewModel::onPuzzleSolved,
                    onFailed = viewModel::onPuzzleFailed,
                )
            }
        }
    }

    /** Starts counting steps toward the walk requirement, or skips it if the device can't support it. */
    private fun startStepTrackingOrSkip() {
        val hasPermission = Build.VERSION.SDK_INT < Build.VERSION_CODES.Q || ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACTIVITY_RECOGNITION,
        ) == PackageManager.PERMISSION_GRANTED
        val stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR)

        if (!hasPermission || stepSensor == null) {
            // Never trap the user behind a requirement this device can't actually check.
            viewModel.skipWalkRequirement()
            return
        }

        val listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                // TYPE_STEP_DETECTOR reports exactly one step per event, value always 1.0.
                viewModel.onStepsDetected(1)
            }

            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
        }
        stepListener = listener
        sensorManager.registerListener(listener, stepSensor, SensorManager.SENSOR_DELAY_NORMAL)
    }

    private fun stopStepTracking() {
        stepListener?.let { sensorManager.unregisterListener(it) }
        stepListener = null
    }

    private fun onChainSolved() {
        stopStepTracking()
        AlarmRingingService.stop(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(KeyguardManager::class.java)?.requestDismissKeyguard(this, null)
        }
        Handler(Looper.getMainLooper()).postDelayed({ finish() }, SOLVED_DISMISS_DELAY_MS)
    }

    private fun showOverLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                    WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                    WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    /**
     * Best-effort defense against the user leaving via the home button/recents before the
     * puzzle chain is solved. Android does not let apps truly block the home button/gesture,
     * so this is a nudge, not a guarantee: it re-requests the foreground shortly after being
     * backgrounded, which works within the OS's brief post-foreground grace window on most
     * (not all) OEM builds.
     */
    override fun onStop() {
        super.onStop()
        if (!chainSolved) {
            Handler(Looper.getMainLooper()).postDelayed({
                if (!chainSolved && !isFinishing) {
                    val relaunch = buildIntent(this, alarmId).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
                    }
                    runCatching { startActivity(relaunch) }
                }
            }, RELAUNCH_DELAY_MS)
        }
    }

    companion object {
        private const val EXTRA_ALARM_ID = "extra_alarm_id"
        private const val RELAUNCH_DELAY_MS = 400L
        private const val SOLVED_DISMISS_DELAY_MS = 600L

        fun buildIntent(context: Context, alarmId: Long): Intent =
            Intent(context, PuzzleActivity::class.java)
                .putExtra(EXTRA_ALARM_ID, alarmId)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
