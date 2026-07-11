package com.brainspresso.core.puzzle

/** A single puzzle instance the user must solve to stop the alarm. */
sealed interface Puzzle {
    val type: PuzzleType
}

data class MathPuzzle(
    val expression: String,
    val correctAnswer: Int,
) : Puzzle {
    override val type: PuzzleType get() = PuzzleType.MATH

    fun isCorrect(selected: Int): Boolean = selected == correctAnswer
}

/** Simon-style: the sequence is flashed to the user, who must repeat it in order. */
data class MemorySequencePuzzle(
    val sequence: List<Int>,
    val padSize: Int,
) : Puzzle {
    override val type: PuzzleType get() = PuzzleType.MEMORY_SEQUENCE

    /** Checks a partial or full attempt against the sequence prefix-wise. */
    fun isPrefixCorrect(attempt: List<Int>): Boolean {
        if (attempt.size > sequence.size) return false
        return attempt.indices.all { attempt[it] == sequence[it] }
    }

    fun isComplete(attempt: List<Int>): Boolean = attempt == sequence
}

/** Grid cells are flashed briefly; the user must tap exactly those cells (any order). */
data class TapPatternPuzzle(
    val gridSize: Int,
    val pattern: Set<Int>,
) : Puzzle {
    override val type: PuzzleType get() = PuzzleType.TAP_PATTERN

    val cellCount: Int get() = gridSize * gridSize

    fun isValidTap(cell: Int): Boolean = cell in pattern

    fun isComplete(tapped: Set<Int>): Boolean = tapped == pattern
}
