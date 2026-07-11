package com.brainspresso.app.ui.puzzle

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import com.brainspresso.core.puzzle.MemorySequencePuzzle
import kotlinx.coroutines.delay

private val padPalette = listOf(
    Color(0xFFD97F4C), // amber
    Color(0xFF6B4226), // espresso
    Color(0xFF8C6A4F), // latte
    Color(0xFF3E7C59), // sage
    Color(0xFF4C6FD9), // blueberry
    Color(0xFFB03A5B), // berry
    Color(0xFFC79A3F), // caramel
    Color(0xFF5B4B8A), // plum
)

private enum class MemoryPhase { WATCH, REPEAT }

@Composable
fun MemorySequencePuzzleView(
    puzzle: MemorySequencePuzzle,
    onSolved: () -> Unit,
    onFailed: () -> Unit,
) {
    var phase by remember(puzzle) { mutableStateOf(MemoryPhase.WATCH) }
    var highlightedIndex by remember(puzzle) { mutableStateOf(-1) }
    var attempt by remember(puzzle) { mutableStateOf(listOf<Int>()) }

    LaunchedEffect(puzzle) {
        phase = MemoryPhase.WATCH
        attempt = emptyList()
        delay(500)
        for (step in puzzle.sequence) {
            highlightedIndex = step
            delay(450)
            highlightedIndex = -1
            delay(200)
        }
        phase = MemoryPhase.REPEAT
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = when (phase) {
                MemoryPhase.WATCH -> "Watch closely…"
                MemoryPhase.REPEAT -> "Now repeat it"
            },
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(24.dp))

        val columns = if (puzzle.padSize <= 4) 2 else 3
        val rows = (puzzle.padSize + columns - 1) / columns
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            for (row in 0 until rows) {
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    for (col in 0 until columns) {
                        val index = row * columns + col
                        if (index < puzzle.padSize) {
                            PadCell(
                                color = padPalette[index % padPalette.size],
                                isHighlighted = index == highlightedIndex,
                                enabled = phase == MemoryPhase.REPEAT,
                                onClick = {
                                    val next = attempt + index
                                    if (puzzle.isPrefixCorrect(next)) {
                                        attempt = next
                                        if (puzzle.isComplete(next)) onSolved()
                                    } else {
                                        onFailed()
                                    }
                                },
                            )
                        } else {
                            Spacer(modifier = Modifier.size(72.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PadCell(
    color: Color,
    isHighlighted: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val brightness by animateFloatAsState(
        targetValue = if (isHighlighted) 1f else 0f,
        animationSpec = tween(150),
        label = "pad-brightness",
    )
    val displayColor = lerp(color, Color.White, brightness * 0.6f)
    Box(
        modifier = Modifier
            .size(72.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(displayColor)
            .clickable(enabled = enabled) { onClick() },
    )
}
