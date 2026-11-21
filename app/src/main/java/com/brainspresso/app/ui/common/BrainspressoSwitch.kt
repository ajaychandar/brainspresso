package com.brainspresso.app.ui.common

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

/** Material switch restyled to the design: amber thumb with a check when on, muted with a cross when off. */
@Composable
fun BrainspressoSwitch(
    checked: Boolean,
    onCheckedChange: ((Boolean) -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        modifier = modifier,
        thumbContent = {
            Icon(
                imageVector = if (checked) Icons.Filled.Check else Icons.Filled.Close,
                contentDescription = null,
                modifier = Modifier.size(SwitchDefaults.IconSize),
            )
        },
        colors = SwitchDefaults.colors(
            checkedThumbColor = colors.primaryContainer,
            checkedTrackColor = colors.surfaceContainerHighest,
            checkedBorderColor = Color.Transparent,
            checkedIconColor = colors.onPrimaryContainer,
            uncheckedThumbColor = colors.surfaceContainerHighest,
            uncheckedTrackColor = colors.surfaceContainer,
            uncheckedBorderColor = Color.Transparent,
            uncheckedIconColor = colors.onSurfaceVariant,
        ),
    )
}
