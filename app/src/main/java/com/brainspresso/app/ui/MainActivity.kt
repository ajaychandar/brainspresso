package com.brainspresso.app.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.brainspresso.app.BrainspressoApp
import com.brainspresso.app.ui.navigation.BrainspressoNavHost
import com.brainspresso.app.ui.theme.BrainspressoTheme

class MainActivity : ComponentActivity() {

    private val alarmScheduler by lazy { (application as BrainspressoApp).alarmScheduler }

    private val notificationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestNotificationPermissionIfNeeded()

        setContent {
            BrainspressoTheme {
                ExactAlarmPermissionGate(
                    canScheduleExactAlarms = { alarmScheduler.canScheduleExactAlarms() },
                    onOpenSettings = ::openExactAlarmSettings,
                )
                BrainspressoNavHost()
            }
        }
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.POST_NOTIFICATIONS,
        ) == PackageManager.PERMISSION_GRANTED
        if (!granted) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private fun openExactAlarmSettings() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) return
        runCatching {
            startActivity(
                Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                    data = Uri.parse("package:$packageName")
                },
            )
        }
    }
}

/**
 * `setAlarmClock()` is documented as exempt from the exact-alarm permission gate, but some
 * devices enforce it anyway, in which case scheduling silently fails -- the alarm saves fine
 * and the list shows it enabled, but AlarmManager never actually arms it. This surfaces that
 * as a visible prompt instead of letting the user discover it by an alarm quietly not ringing.
 */
@Composable
private fun ExactAlarmPermissionGate(
    canScheduleExactAlarms: () -> Boolean,
    onOpenSettings: () -> Unit,
) {
    var granted by remember { mutableStateOf(canScheduleExactAlarms()) }
    var dismissedThisSession by remember { mutableStateOf(false) }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                granted = canScheduleExactAlarms()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    if (!granted && !dismissedThisSession) {
        AlertDialog(
            onDismissRequest = { dismissedThisSession = true },
            title = { Text("Allow exact alarms") },
            text = {
                Text(
                    "Brainspresso needs the \"Alarms & reminders\" permission to ring at the " +
                        "exact time you set. Without it, your alarms may silently never go off.",
                )
            },
            confirmButton = {
                TextButton(onClick = onOpenSettings) { Text("Open settings") }
            },
            dismissButton = {
                TextButton(onClick = { dismissedThisSession = true }) { Text("Not now") }
            },
        )
    }
}
