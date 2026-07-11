package com.brainspresso.core.puzzle

import kotlin.random.Random

object PuzzleGeneratorFactory {

    /**
     * Picks a random enabled puzzle type and generates a fresh instance.
     * Falls back to [PuzzleType.MATH] if [enabledTypes] is empty, so a misconfigured
     * settings state can never produce zero puzzles.
     */
    fun generate(
        enabledTypes: Set<PuzzleType>,
        difficulty: Difficulty,
        random: Random = Random.Default,
    ): Puzzle {
        val pool = enabledTypes.ifEmpty { setOf(PuzzleType.MATH) }
        val type = pool.toList()[random.nextInt(pool.size)]
        return generate(type, difficulty, random)
    }

    fun generate(type: PuzzleType, difficulty: Difficulty, random: Random = Random.Default): Puzzle {
        return when (type) {
            PuzzleType.MATH -> MathPuzzleGenerator.generate(difficulty, random)
            PuzzleType.MEMORY_SEQUENCE -> MemorySequencePuzzleGenerator.generate(difficulty, random)
            PuzzleType.TAP_PATTERN -> TapPatternPuzzleGenerator.generate(difficulty, random)
        }
    }
}
