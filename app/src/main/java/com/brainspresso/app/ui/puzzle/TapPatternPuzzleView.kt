package com.brainspresso.app.ui.puzzle

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.unit.dp
import com.brainspresso.core.puzzle.TapPatternPuzzle
import kotlinx.coroutines.delay

private enum class TapPhase { MEMORIZE, RECALL }

@Composable
fun TapPatternPuzzleView(
    puzzle: TapPatternPuzzle,
    onSolved: () -> Unit,
    onFailed: () -> Unit,
) {
    var phase by remember(puzzle) { mutableStateOf(TapPhase.MEMORIZE) }
    var tapped by remember(puzzle) { mutableStateOf(setOf<Int>()) }

    LaunchedEffect(puzzle) {
        phase = TapPhase.MEMORIZE
        tapped = emptySet()
        delay(1400 + puzzle.pattern.size * 250L)
        phase = TapPhase.RECALL
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        PuzzleStatusBar(
            text = when (phase) {
                TapPhase.MEMORIZE -> "Memorize the highlighted cells"
                TapPhase.RECALL -> "Tap the cells you memorized"
            },
        )
        Spacer(modifier = Modifier.height(20.dp))

        // Sized as a fraction of available width (capped so it doesn't grow huge on tablets)
        // rather than a fixed per-cell dp, so HARD difficulty's 5x5 grid never overflows a
        // narrow phone screen.
        LazyVerticalGrid(
            columns = GridCells.Fixed(puzzle.gridSize),
            modifier = Modifier
                .fillMaxWidth()
                .widthIn(max = 340.dp)
                .aspectRatio(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            userScrollEnabled = false,
        ) {
            items(puzzle.cellCount) { cell ->
                val showHighlight = phase == TapPhase.MEMORIZE && cell in puzzle.pattern
                val showTapped = phase == TapPhase.RECALL && cell in tapped
                GridCellView(
                    highlighted = showHighlight || showTapped,
                    enabled = phase == TapPhase.RECALL && cell !in tapped,
                    onClick = {
                        if (puzzle.isValidTap(cell)) {
                            val next = tapped + cell
                            tapped = next
                            if (puzzle.isComplete(next)) onSolved()
                        } else {
                            onFailed()
                        }
                    },
                )
            }
        }
    }
}

@Composable
private fun GridCellView(
    highlighted: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val glow = MaterialTheme.colorScheme.primaryContainer
    val color = if (highlighted) glow else MaterialTheme.colorScheme.surfaceContainerHighest
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .shadow(elevation = if (highlighted) 10.dp else 0.dp, shape = shape, ambientColor = glow, spotColor = glow)
            .clip(shape)
            .background(color)
            .clickable(enabled = enabled, onClick = onClick),
    )
}
