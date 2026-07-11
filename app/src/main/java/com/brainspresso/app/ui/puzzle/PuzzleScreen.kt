package com.brainspresso.app.ui.puzzle

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.brainspresso.core.puzzle.MathPuzzle
import com.brainspresso.core.puzzle.MemorySequencePuzzle
import com.brainspresso.core.puzzle.TapPatternPuzzle

@Composable
fun PuzzleScreen(
    state: PuzzleUiState,
    onSolved: () -> Unit,
    onFailed: () -> Unit,
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Alarm,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = if (state.alarmLabel.isBlank()) "Wake up!" else state.alarmLabel,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }

            Text(
                text = "Solve the puzzle to stop the alarm",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )

            if (state.required > 1) {
                StreakDots(solved = state.solvedCount, required = state.required)
            }

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                AnimatedContent(
                    targetState = state.puzzle,
                    transitionSpec = { fadeIn(tween(200)) togetherWith fadeOut(tween(120)) },
                    label = "puzzle-content",
                ) { puzzle ->
                    when (puzzle) {
                        is MathPuzzle -> MathPuzzleView(puzzle = puzzle, onSolved = onSolved, onFailed = onFailed)
                        is MemorySequencePuzzle -> MemorySequencePuzzleView(puzzle = puzzle, onSolved = onSolved, onFailed = onFailed)
                        is TapPatternPuzzle -> TapPatternPuzzleView(puzzle = puzzle, onSolved = onSolved, onFailed = onFailed)
                        null -> Text(
                            text = "Loading puzzle…",
                            style = MaterialTheme.typography.bodyLarge,
                        )
                    }
                }
            }

            if (state.justFailed) {
                Text(
                    text = "Not quite — here's a fresh one.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun StreakDots(solved: Int, required: Int) {
    Row(
        modifier = Modifier.padding(top = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        repeat(required) { index ->
            val filled = index < solved
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(
                        if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    ),
            )
        }
    }
}
