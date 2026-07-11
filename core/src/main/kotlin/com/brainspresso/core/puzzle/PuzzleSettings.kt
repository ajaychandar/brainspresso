package com.brainspresso.core.puzzle

data class PuzzleSettings(
    val enabledTypes: Set<PuzzleType> = setOf(PuzzleType.MATH, PuzzleType.MEMORY_SEQUENCE, PuzzleType.TAP_PATTERN),
    val difficulty: Difficulty = Difficulty.EASY,
    val requiredStreak: Int = 1,
) {
    init {
        require(requiredStreak in MIN_STREAK..MAX_STREAK) {
            "requiredStreak must be between $MIN_STREAK and $MAX_STREAK, was $requiredStreak"
        }
    }

    companion object {
        const val MIN_STREAK = 1
        const val MAX_STREAK = 5
    }
}

/** Tracks progress through a chain of puzzles that must be solved consecutively. */
class PuzzleChainProgress(private val required: Int) {
    init {
        require(required in PuzzleSettings.MIN_STREAK..PuzzleSettings.MAX_STREAK)
    }

    var solvedCount: Int = 0
        private set

    val remaining: Int get() = (required - solvedCount).coerceAtLeast(0)

    val isComplete: Boolean get() = solvedCount >= required

    /** Call when the current puzzle is solved correctly. Returns [isComplete]. */
    fun recordSolved(): Boolean {
        if (!isComplete) solvedCount++
        return isComplete
    }

    /** Call when the current puzzle is answered incorrectly; breaks the "in a row" streak. */
    fun recordFailed() {
        solvedCount = 0
    }
}
