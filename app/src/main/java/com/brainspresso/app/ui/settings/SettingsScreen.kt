package com.brainspresso.app.ui.settings

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.Grain
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.brainspresso.app.BuildConfig
import com.brainspresso.app.ui.common.BrainspressoSwitch
import com.brainspresso.core.puzzle.Difficulty
import com.brainspresso.core.puzzle.PuzzleSettings
import com.brainspresso.core.puzzle.PuzzleType

private const val WALK_STEPS_INCREMENT = 5
private const val PRIVACY_POLICY_URL = "https://ajaychandar.github.io/brainspresso/privacy-policy.html"
private const val TERMS_URL = "https://ajaychandar.github.io/brainspresso/terms.html"

private fun PuzzleType.displayName() = when (this) {
    PuzzleType.MATH -> "Math puzzles"
    PuzzleType.MEMORY_SEQUENCE -> "Memory sequence"
    PuzzleType.TAP_PATTERN -> "Tap the pattern"
}

private fun PuzzleType.description() = when (this) {
    PuzzleType.MATH -> "Arithmetic that scales with difficulty"
    PuzzleType.MEMORY_SEQUENCE -> "Simon-style sequences to repeat back"
    PuzzleType.TAP_PATTERN -> "Recall flashed grid positions"
}

private fun PuzzleType.icon(): ImageVector = when (this) {
    PuzzleType.MATH -> Icons.Filled.Calculate
    PuzzleType.MEMORY_SEQUENCE -> Icons.Filled.Grain
    PuzzleType.TAP_PATTERN -> Icons.Filled.GridView
}

private fun Difficulty.displayName() = when (this) {
    Difficulty.EASY -> "Easy"
    Difficulty.MEDIUM -> "Medium"
    Difficulty.HARD -> "Hard"
}

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
) {
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    val activityRecognitionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
    ) { granted ->
        if (granted) viewModel.setRequireWalkToStop(true)
    }

    fun onToggleRequireWalk(enabled: Boolean) {
        if (!enabled) {
            viewModel.setRequireWalkToStop(false)
            return
        }
        val needsRuntimePermission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        val alreadyGranted = !needsRuntimePermission || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACTIVITY_RECOGNITION,
        ) == PackageManager.PERMISSION_GRANTED
        if (alreadyGranted) {
            viewModel.setRequireWalkToStop(true)
        } else {
            activityRecognitionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Column(modifier = Modifier.padding(start = 4.dp)) {
                    Text(
                        text = "Settings",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "MISSION LAB",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconBadge(Icons.Filled.Tune, size = 48, tint = MaterialTheme.colorScheme.primary)
                Column(modifier = Modifier.padding(start = 16.dp)) {
                    Text(
                        text = "Mission Lab",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Text(
                        text = "Tune how you have to wake up",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            SettingsSection(
                icon = Icons.Filled.Psychology,
                title = "Wake-up challenges",
                trailing = "${settings.enabledTypes.size} active",
            ) {
                Text(
                    text = "At least one must stay enabled.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                PuzzleType.entries.forEach { type ->
                    val selected = type in settings.enabledTypes
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerLow)
                            .selectable(
                                selected = selected,
                                onClick = { viewModel.toggleType(type) },
                                role = Role.Checkbox,
                            )
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CheckTile(checked = selected)
                        Column(modifier = Modifier
                            .weight(1f)
                            .padding(horizontal = 12.dp)) {
                            Text(
                                text = type.displayName(),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Text(
                                text = type.description(),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                        Icon(
                            imageVector = type.icon(),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp),
                        )
                    }
                }

                Text(
                    text = "DIFFICULTY",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Difficulty.entries.forEach { difficulty ->
                        val selected = settings.difficulty == difficulty
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(CircleShape)
                                .background(if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                .selectable(
                                    selected = selected,
                                    onClick = { viewModel.setDifficulty(difficulty) },
                                    role = Role.RadioButton,
                                )
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(
                                text = difficulty.displayName(),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }

                StepperRow(
                    title = "Puzzles in a row",
                    subtitle = "Must be solved consecutively",
                    valueLabel = if (settings.requiredStreak == 1) "1 puzzle" else "${settings.requiredStreak} puzzles",
                    tint = MaterialTheme.colorScheme.primary,
                    onMinus = { viewModel.setRequiredStreak(settings.requiredStreak - 1) },
                    onPlus = { viewModel.setRequiredStreak(settings.requiredStreak + 1) },
                    minusEnabled = settings.requiredStreak > PuzzleSettings.MIN_STREAK,
                    plusEnabled = settings.requiredStreak < PuzzleSettings.MAX_STREAK,
                )
            }

            SettingsSection(icon = Icons.Filled.DirectionsWalk, title = "Physical movement mission") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconBadge(Icons.Filled.DirectionsWalk, size = 40, tint = MaterialTheme.colorScheme.secondary)
                    Column(modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 12.dp)) {
                        Text(
                            text = "Walk to stop",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "After solving, walk a few steps with the phone before the alarm stops",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    BrainspressoSwitch(
                        checked = settings.requireWalkToStop,
                        onCheckedChange = ::onToggleRequireWalk,
                    )
                }
                StepperRow(
                    title = "Steps required",
                    subtitle = "Range ${PuzzleSettings.MIN_WALK_STEPS} to ${PuzzleSettings.MAX_WALK_STEPS}",
                    valueLabel = "${settings.walkStepsRequired} steps",
                    tint = MaterialTheme.colorScheme.secondary,
                    onMinus = { viewModel.setWalkStepsRequired(settings.walkStepsRequired - WALK_STEPS_INCREMENT) },
                    onPlus = { viewModel.setWalkStepsRequired(settings.walkStepsRequired + WALK_STEPS_INCREMENT) },
                    minusEnabled = settings.requireWalkToStop && settings.walkStepsRequired > PuzzleSettings.MIN_WALK_STEPS,
                    plusEnabled = settings.requireWalkToStop && settings.walkStepsRequired < PuzzleSettings.MAX_WALK_STEPS,
                    modifier = Modifier.alpha(if (settings.requireWalkToStop) 1f else 0.4f),
                )
            }

            SettingsSection(icon = Icons.Filled.Info, title = "About & legal") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Brainspresso ${BuildConfig.VERSION_NAME}",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Runs entirely on this device",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            Icons.Filled.LocalCafe,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp),
                        )
                        Text(
                            text = "By Ajay Chandar",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(start = 6.dp),
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainerLow)
                        .padding(12.dp),
                    verticalAlignment = Alignment.Top,
                ) {
                    Icon(
                        Icons.Filled.VerifiedUser,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(18.dp),
                    )
                    Text(
                        text = "Provided \"as is,\" without warranty of any kind. Not a substitute for a " +
                            "backup alarm when something truly can't be missed (medication, flights, " +
                            "exams). The developer is not liable for missed alarms or any damages " +
                            "arising from its use. No network access, no accounts, no data collection.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 8.dp),
                    )
                }
                LinkRow(icon = Icons.Filled.PrivacyTip, label = "Privacy policy") { uriHandler.openUri(PRIVACY_POLICY_URL) }
                LinkRow(icon = Icons.Filled.Description, label = "Terms of use") { uriHandler.openUri(TERMS_URL) }
                Text(
                    text = "Fonts: Space Grotesk and Plus Jakarta Sans, SIL Open Font License 1.1.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    icon: ImageVector,
    title: String,
    trailing: String? = null,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
            Text(
                text = title.uppercase(),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 6.dp),
            )
            if (trailing != null) {
                Text(
                    text = trailing,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                        .padding(horizontal = 8.dp, vertical = 2.dp),
                )
            }
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surfaceContainer)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            content()
        }
    }
}

@Composable
private fun IconBadge(icon: ImageVector, size: Int, tint: Color) {
    Box(
        modifier = Modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceContainerHigh),
        contentAlignment = Alignment.Center,
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size((size / 2).dp))
    }
}

@Composable
private fun CheckTile(checked: Boolean) {
    val shape = RoundedCornerShape(8.dp)
    Box(
        modifier = Modifier
            .size(24.dp)
            .then(
                if (checked) {
                    Modifier.shadow(
                        elevation = 8.dp,
                        shape = shape,
                        ambientColor = MaterialTheme.colorScheme.primaryContainer,
                        spotColor = MaterialTheme.colorScheme.primaryContainer,
                    )
                } else {
                    Modifier
                },
            )
            .clip(shape)
            .background(if (checked) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceContainerHighest),
        contentAlignment = Alignment.Center,
    ) {
        if (checked) {
            Icon(
                Icons.Filled.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun StepperRow(
    title: String,
    subtitle: String,
    valueLabel: String,
    tint: Color,
    onMinus: () -> Unit,
    onPlus: () -> Unit,
    minusEnabled: Boolean,
    plusEnabled: Boolean,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Row(
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StepperButton(Icons.Filled.Remove, "Decrease", tint, onMinus, minusEnabled)
            Text(
                text = valueLabel,
                style = MaterialTheme.typography.headlineSmall,
                color = tint,
                modifier = Modifier.padding(horizontal = 10.dp),
            )
            StepperButton(Icons.Filled.Add, "Increase", tint, onPlus, plusEnabled)
        }
    }
}

@Composable
private fun StepperButton(
    icon: ImageVector,
    description: String,
    tint: Color,
    onClick: () -> Unit,
    enabled: Boolean,
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.size(36.dp),
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = tint,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
            disabledContentColor = tint.copy(alpha = 0.35f),
        ),
    ) {
        Icon(icon, contentDescription = description, modifier = Modifier.size(18.dp))
    }
}

@Composable
private fun LinkRow(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.secondary,
            modifier = Modifier
                .weight(1f)
                .padding(start = 8.dp),
        )
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
    }
}
