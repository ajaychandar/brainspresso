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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.unit.dp
import com.brainspresso.core.puzzle.MemorySequencePuzzle
import kotlinx.coroutines.delay

private val padPalette = listOf(
    Color(0xFFFE9C02), // amber
    Color(0xFF1EA19C), // teal
    Color(0xFFFFC383), // crema
    Color(0xFFFF9592), // coral
    Color(0xFF67D8D3), // aqua
    Color(0xFFFFDCBC), // peach
    Color(0xFF85F5EF), // cyan
    Color(0xFFFFB86A), // gold
)

private val PadSize = 88.dp
private val PadGap = 12.dp

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

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        PuzzleStatusBar(
            text = when (phase) {
                MemoryPhase.WATCH -> "Watch closely…"
                MemoryPhase.REPEAT -> "Now repeat it"
            },
        )
        Spacer(modifier = Modifier.height(20.dp))

        val columns = if (puzzle.padSize <= 4) 2 else 3
        val rows = (puzzle.padSize + columns - 1) / columns
        Column(verticalArrangement = Arrangement.spacedBy(PadGap)) {
            for (row in 0 until rows) {
                Row(horizontalArrangement = Arrangement.spacedBy(PadGap)) {
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
                            Spacer(modifier = Modifier.size(PadSize))
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
    val resting = lerp(MaterialTheme.colorScheme.surfaceContainerHighest, color, 0.4f)
    val displayColor = lerp(resting, color, brightness)
    val shape = RoundedCornerShape(24.dp)
    Box(
        modifier = Modifier
            .size(PadSize)
            .shadow(elevation = (18 * brightness).dp, shape = shape, ambientColor = color, spotColor = color)
            .clip(shape)
            .background(displayColor)
            .clickable(enabled = enabled) { onClick() },
    )
}
