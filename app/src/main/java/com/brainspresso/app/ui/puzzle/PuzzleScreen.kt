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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
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
                text = if (state.isWalking) "Walk to fully stop the alarm" else "Solve the puzzle to stop the alarm",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp),
            )

            if (state.required > 1 && !state.isWalking) {
                StreakDots(solved = state.solvedCount, required = state.required)
            }

            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                if (state.isWalking) {
                    WalkToStopView(stepsWalked = state.stepsWalked, stepsRequired = state.stepsRequired)
                } else {
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
            }

            if (state.justFailed && !state.isWalking) {
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
private fun WalkToStopView(stepsWalked: Int, stepsRequired: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = Icons.Filled.DirectionsWalk,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(56.dp),
        )
        Text(
            text = "$stepsWalked / $stepsRequired steps",
            style = MaterialTheme.typography.displaySmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 16.dp),
        )
        Text(
            text = "Keep the phone with you and keep moving.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp),
        )
        LinearProgressIndicator(
            progress = { (stepsWalked.toFloat() / stepsRequired.toFloat()).coerceIn(0f, 1f) },
            modifier = Modifier
                .padding(top = 20.dp)
                .fillMaxWidth(0.7f),
        )
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
                    .size(14.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        if (filled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    ),
            )
        }
    }
}
