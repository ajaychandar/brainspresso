package com.brainspresso.app.ui.puzzle

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.unit.dp
import com.brainspresso.core.puzzle.TapPatternPuzzle
import kotlinx.coroutines.delay

private enum class Phase { MEMORIZE, RECALL }

@Composable
fun TapPatternPuzzleView(
    puzzle: TapPatternPuzzle,
    onSolved: () -> Unit,
    onFailed: () -> Unit,
) {
    var phase by remember(puzzle) { mutableStateOf(Phase.MEMORIZE) }
    var tapped by remember(puzzle) { mutableStateOf(setOf<Int>()) }

    LaunchedEffect(puzzle) {
        phase = Phase.MEMORIZE
        tapped = emptySet()
        delay(1400 + puzzle.pattern.size * 250L)
        phase = Phase.RECALL
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = when (phase) {
                Phase.MEMORIZE -> "Memorize the highlighted cells"
                Phase.RECALL -> "Tap the cells you memorized"
            },
            style = MaterialTheme.typography.titleMedium,
        )
        Spacer(modifier = Modifier.height(20.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(puzzle.gridSize),
            modifier = Modifier
                .width((puzzle.gridSize * 64).dp)
                .height((puzzle.gridSize * 64).dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(puzzle.cellCount) { cell ->
                val showHighlight = phase == Phase.MEMORIZE && cell in puzzle.pattern
                val showTapped = phase == Phase.RECALL && cell in tapped
                GridCellView(
                    highlighted = showHighlight || showTapped,
                    enabled = phase == Phase.RECALL && cell !in tapped,
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
    val color = if (highlighted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(color)
            .clickable(enabled = enabled, onClick = onClick),
    )
}
