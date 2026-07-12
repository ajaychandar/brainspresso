package com.brainspresso.app.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.brainspresso.core.puzzle.Difficulty
import com.brainspresso.core.puzzle.PuzzleSettings
import com.brainspresso.core.puzzle.PuzzleType

private fun PuzzleType.displayName() = when (this) {
    PuzzleType.MATH -> "Math"
    PuzzleType.MEMORY_SEQUENCE -> "Memory sequence"
    PuzzleType.TAP_PATTERN -> "Tap the pattern"
}

private fun Difficulty.displayName() = when (this) {
    Difficulty.EASY -> "Easy"
    Difficulty.MEDIUM -> "Medium"
    Difficulty.HARD -> "Hard"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBack: () -> Unit,
) {
    val settings by viewModel.settings.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
        ) {
            SectionTitle("Puzzle types")
            Text(
                "At least one must stay enabled.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            PuzzleType.entries.forEach { type ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = type in settings.enabledTypes,
                            onClick = { viewModel.toggleType(type) },
                            role = Role.Checkbox,
                        )
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Checkbox(checked = type in settings.enabledTypes, onCheckedChange = { viewModel.toggleType(type) })
                    Text(type.displayName(), modifier = Modifier.padding(start = 8.dp))
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp))

            SectionTitle("Difficulty")
            Difficulty.entries.forEach { difficulty ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .selectable(
                            selected = settings.difficulty == difficulty,
                            onClick = { viewModel.setDifficulty(difficulty) },
                            role = Role.RadioButton,
                        )
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = settings.difficulty == difficulty, onClick = { viewModel.setDifficulty(difficulty) })
                    Text(difficulty.displayName(), modifier = Modifier.padding(start = 8.dp))
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 20.dp))

            SectionTitle("Puzzles required to stop the alarm")
            Text(
                "Solve this many puzzles in a row before the alarm stops.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(
                    onClick = { viewModel.setRequiredStreak(settings.requiredStreak - 1) },
                    enabled = settings.requiredStreak > PuzzleSettings.MIN_STREAK,
                ) { Icon(Icons.Filled.Remove, contentDescription = "Decrease") }

                Text(
                    text = settings.requiredStreak.toString(),
                    style = MaterialTheme.typography.displaySmall,
                    modifier = Modifier.padding(horizontal = 24.dp),
                )

                IconButton(
                    onClick = { viewModel.setRequiredStreak(settings.requiredStreak + 1) },
                    enabled = settings.requiredStreak < PuzzleSettings.MAX_STREAK,
                ) { Icon(Icons.Filled.Add, contentDescription = "Increase") }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(bottom = 8.dp),
    )
}
