package com.brainspresso.core.puzzle

import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class MemorySequencePuzzleGeneratorTest {

    @Test
    fun `difficulty scales sequence length and pad size`() {
        val easy = MemorySequencePuzzleGenerator.generate(Difficulty.EASY, Random(1))
        val medium = MemorySequencePuzzleGenerator.generate(Difficulty.MEDIUM, Random(1))
        val hard = MemorySequencePuzzleGenerator.generate(Difficulty.HARD, Random(1))

        assertTrue(easy.sequence.size < medium.sequence.size)
        assertTrue(medium.sequence.size < hard.sequence.size)
        assertTrue(easy.padSize <= medium.padSize)
        assertTrue(medium.padSize <= hard.padSize)
    }

    @Test
    fun `every sequence index is within pad bounds`() {
        repeat(200) { seed ->
            val puzzle = MemorySequencePuzzleGenerator.generate(Difficulty.HARD, Random(seed))
            puzzle.sequence.forEach { index ->
                assertTrue(index in 0 until puzzle.padSize)
            }
        }
    }

    @Test
    fun `isComplete requires exact order match`() {
        val puzzle = MemorySequencePuzzle(sequence = listOf(0, 1, 2), padSize = 4)
        assertTrue(puzzle.isComplete(listOf(0, 1, 2)))
        assertFalse(puzzle.isComplete(listOf(0, 2, 1)))
        assertFalse(puzzle.isComplete(listOf(0, 1)))
    }

    @Test
    fun `isPrefixCorrect validates partial attempts as they are entered`() {
        val puzzle = MemorySequencePuzzle(sequence = listOf(3, 1, 2), padSize = 4)
        assertTrue(puzzle.isPrefixCorrect(emptyList()))
        assertTrue(puzzle.isPrefixCorrect(listOf(3)))
        assertTrue(puzzle.isPrefixCorrect(listOf(3, 1)))
        assertFalse(puzzle.isPrefixCorrect(listOf(3, 2)))
        assertFalse(puzzle.isPrefixCorrect(listOf(3, 1, 2, 0)))
    }

    @Test
    fun `same seed produces same sequence for reproducibility`() {
        val a = MemorySequencePuzzleGenerator.generate(Difficulty.MEDIUM, Random(99))
        val b = MemorySequencePuzzleGenerator.generate(Difficulty.MEDIUM, Random(99))
        assertEquals(a, b)
    }
}
