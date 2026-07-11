package com.brainspresso.core.puzzle

import kotlin.random.Random

/** Grid cells are flashed briefly; the user must tap exactly those cells afterwards. */
object TapPatternPuzzleGenerator {

    fun generate(difficulty: Difficulty, random: Random = Random.Default): TapPatternPuzzle {
        val (gridSize, patternCount) = when (difficulty) {
            Difficulty.EASY -> 3 to 3
            Difficulty.MEDIUM -> 4 to 5
            Difficulty.HARD -> 5 to 7
        }
        val cellCount = gridSize * gridSize
        val pattern = generateSequence { random.nextInt(cellCount) }
            .distinct()
            .take(patternCount)
            .toSet()
        return TapPatternPuzzle(gridSize = gridSize, pattern = pattern)
    }
}
