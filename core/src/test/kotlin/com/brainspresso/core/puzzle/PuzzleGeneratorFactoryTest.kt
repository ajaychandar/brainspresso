package com.brainspresso.core.puzzle

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PuzzleGeneratorFactoryTest {

    @Test
    fun `only generates enabled types`() {
        val enabled = setOf(PuzzleType.MATH, PuzzleType.TAP_PATTERN)
        val random = Random(7)
        repeat(200) {
            val puzzle = PuzzleGeneratorFactory.generate(enabled, Difficulty.EASY, random)
            assertTrue(puzzle.type in enabled, "generated disabled type ${puzzle.type}")
        }
    }

    @Test
    fun `eventually generates every enabled type given enough draws`() {
        val enabled = PuzzleType.entries.toSet()
        val random = Random(123)
        val seen = mutableSetOf<PuzzleType>()
        repeat(500) {
            seen += PuzzleGeneratorFactory.generate(enabled, Difficulty.EASY, random).type
        }
        assertEquals(enabled, seen)
    }

    @Test
    fun `falls back to math when no types are enabled`() {
        val puzzle = PuzzleGeneratorFactory.generate(emptySet(), Difficulty.EASY, Random(1))
        assertEquals(PuzzleType.MATH, puzzle.type)
    }

    @Test
    fun `direct type generation returns matching puzzle type`() {
        assertEquals(PuzzleType.MATH, PuzzleGeneratorFactory.generate(PuzzleType.MATH, Difficulty.EASY).type)
        assertEquals(
            PuzzleType.MEMORY_SEQUENCE,
            PuzzleGeneratorFactory.generate(PuzzleType.MEMORY_SEQUENCE, Difficulty.MEDIUM).type,
        )
        assertEquals(
            PuzzleType.TAP_PATTERN,
            PuzzleGeneratorFactory.generate(PuzzleType.TAP_PATTERN, Difficulty.HARD).type,
        )
    }
}
