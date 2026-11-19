package com.brainspresso.core.puzzle

data class PuzzleSettings(
    val enabledTypes: Set<PuzzleType> = setOf(PuzzleType.MATH, PuzzleType.MEMORY_SEQUENCE, PuzzleType.TAP_PATTERN),
    val difficulty: Difficulty = Difficulty.EASY,
    val requiredStreak: Int = 1,
    val requireWalkToStop: Boolean = false,
    val walkStepsRequired: Int = 15,
) {
    init {
        require(requiredStreak in MIN_STREAK..MAX_STREAK) {
            "requiredStreak must be between $MIN_STREAK and $MAX_STREAK, was $requiredStreak"
        }
        require(walkStepsRequired in MIN_WALK_STEPS..MAX_WALK_STEPS) {
            "walkStepsRequired must be between $MIN_WALK_STEPS and $MAX_WALK_STEPS, was $walkStepsRequired"
        }
    }

    companion object {
        const val MIN_STREAK = 1
        const val MAX_STREAK = 5
        const val MIN_WALK_STEPS = 5
        const val MAX_WALK_STEPS = 50
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

/** Tracks progress toward the number of steps that must be walked (phone in hand) to stop the alarm. */
class WalkProgress(private val required: Int) {
    init {
        require(required in PuzzleSettings.MIN_WALK_STEPS..PuzzleSettings.MAX_WALK_STEPS)
    }

    var stepsWalked: Int = 0
        private set

    val remaining: Int get() = (required - stepsWalked).coerceAtLeast(0)

    val isComplete: Boolean get() = stepsWalked >= required

    /** Call with however many steps the step sensor just reported. Returns [isComplete]. */
    fun recordSteps(count: Int): Boolean {
        require(count >= 0) { "count must not be negative, was $count" }
        stepsWalked = (stepsWalked + count).coerceAtMost(required)
        return isComplete
    }
}
