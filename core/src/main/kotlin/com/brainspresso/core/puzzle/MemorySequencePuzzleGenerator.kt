package com.brainspresso.core.puzzle

import kotlin.random.Random

/** Simon-style memory puzzle: a sequence of pad indices to watch, then repeat in order. */
object MemorySequencePuzzleGenerator {

    fun generate(difficulty: Difficulty, random: Random = Random.Default): MemorySequencePuzzle {
        val (length, padSize) = when (difficulty) {
            Difficulty.EASY -> 3 to 4
            Difficulty.MEDIUM -> 5 to 6
            Difficulty.HARD -> 7 to 8
        }
        val sequence = List(length) { random.nextInt(padSize) }
        return MemorySequencePuzzle(sequence = sequence, padSize = padSize)
    }
}
