package com.brainspresso.core.puzzle

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class TapPatternPuzzleGeneratorTest {

    @Test
    fun `difficulty scales grid size and pattern count`() {
        val easy = TapPatternPuzzleGenerator.generate(Difficulty.EASY, Random(1))
        val medium = TapPatternPuzzleGenerator.generate(Difficulty.MEDIUM, Random(1))
        val hard = TapPatternPuzzleGenerator.generate(Difficulty.HARD, Random(1))

        assertTrue(easy.gridSize < medium.gridSize)
        assertTrue(medium.gridSize < hard.gridSize)
        assertTrue(easy.pattern.size < medium.pattern.size)
        assertTrue(medium.pattern.size < hard.pattern.size)
    }

    @Test
    fun `pattern cells are unique and within grid bounds`() {
        repeat(200) { seed ->
            val puzzle = TapPatternPuzzleGenerator.generate(Difficulty.HARD, Random(seed))
            assertEquals(puzzle.pattern.size, puzzle.pattern.toSet().size)
            puzzle.pattern.forEach { cell -> assertTrue(cell in 0 until puzzle.cellCount) }
        }
    }

    @Test
    fun `isComplete requires the exact set regardless of order`() {
        val puzzle = TapPatternPuzzle(gridSize = 3, pattern = setOf(0, 4, 8))
        assertTrue(puzzle.isComplete(setOf(8, 0, 4)))
        assertFalse(puzzle.isComplete(setOf(0, 4)))
        assertFalse(puzzle.isComplete(setOf(0, 4, 8, 1)))
    }

    @Test
    fun `isValidTap flags taps outside the pattern`() {
        val puzzle = TapPatternPuzzle(gridSize = 3, pattern = setOf(0, 4, 8))
        assertTrue(puzzle.isValidTap(4))
        assertFalse(puzzle.isValidTap(1))
    }
}
