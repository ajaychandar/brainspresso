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
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.material3.AlertDialog
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.saveable.rememberSaveable
import com.brainspresso.app.ui.splash.SplashScreen
import kotlinx.coroutines.delay
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlarmOn
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.brainspresso.app.ui.theme.DialogSurface
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
                var showSplash by rememberSaveable { mutableStateOf(true) }
                LaunchedEffect(Unit) {
                    delay(SPLASH_DURATION_MS)
                    showSplash = false
                }
                Crossfade(targetState = showSplash, animationSpec = tween(400), label = "splash") { splash ->
                    if (splash) {
                        SplashScreen()
                    } else {
                        ExactAlarmPermissionGate(
                            canScheduleExactAlarms = { alarmScheduler.canScheduleExactAlarms() },
                            onOpenSettings = ::openExactAlarmSettings,
                        )
                        BrainspressoNavHost()
                    }
                }
            }
        }
    }

    private companion object {
        const val SPLASH_DURATION_MS = 1_800L
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
            containerColor = DialogSurface,
            shape = RoundedCornerShape(28.dp),
            icon = {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        Icons.Filled.AlarmOn,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp),
                    )
                }
            },
            title = {
                Text(
                    text = "Allow exact alarms",
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Android needs the \"Alarms & reminders\" permission so Brainspresso can " +
                            "ring at the exact time you set.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    PermissionOutcomeRow(
                        icon = Icons.Filled.Close,
                        tint = MaterialTheme.colorScheme.error,
                        title = "Without it",
                        body = "Alarms may be delayed or may never go off.",
                    )
                    PermissionOutcomeRow(
                        icon = Icons.Filled.Check,
                        tint = MaterialTheme.colorScheme.secondary,
                        title = "With it",
                        body = "Alarms ring at the exact time you set.",
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onOpenSettings,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    ),
                ) { Text("Allow exact alarms", style = MaterialTheme.typography.labelLarge) }
            },
            dismissButton = {
                TextButton(onClick = { dismissedThisSession = true }) {
                    Text("Not now", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
        )
    }
}

@Composable
private fun PermissionOutcomeRow(icon: ImageVector, tint: Color, title: String, body: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(12.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(18.dp))
        Column(modifier = Modifier.padding(start = 10.dp)) {
            Text(title, style = MaterialTheme.typography.labelMedium, color = tint)
            Text(body, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}
