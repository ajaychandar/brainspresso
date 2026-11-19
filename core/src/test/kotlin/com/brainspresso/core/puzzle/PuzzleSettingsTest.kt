package com.brainspresso.core.puzzle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class PuzzleSettingsTest {

    @Test
    fun `requiredStreak must be between 1 and 5`() {
        PuzzleSettings(requiredStreak = 1)
        PuzzleSettings(requiredStreak = 5)
        assertFailsWith<IllegalArgumentException> { PuzzleSettings(requiredStreak = 0) }
        assertFailsWith<IllegalArgumentException> { PuzzleSettings(requiredStreak = 6) }
    }

    @Test
    fun `walkStepsRequired must be between 5 and 50`() {
        PuzzleSettings(walkStepsRequired = 5)
        PuzzleSettings(walkStepsRequired = 50)
        assertFailsWith<IllegalArgumentException> { PuzzleSettings(walkStepsRequired = 4) }
        assertFailsWith<IllegalArgumentException> { PuzzleSettings(walkStepsRequired = 51) }
    }

    @Test
    fun `requireWalkToStop defaults to false`() {
        assertFalse(PuzzleSettings().requireWalkToStop)
    }
}

class PuzzleChainProgressTest {

    @Test
    fun `completes only after required consecutive solves`() {
        val progress = PuzzleChainProgress(required = 3)
        assertFalse(progress.isComplete)
        assertEquals(3, progress.remaining)

        assertFalse(progress.recordSolved())
        assertEquals(2, progress.remaining)

        assertFalse(progress.recordSolved())
        assertEquals(1, progress.remaining)

        assertTrue(progress.recordSolved())
        assertEquals(0, progress.remaining)
        assertTrue(progress.isComplete)
    }

    @Test
    fun `single required streak completes on first solve`() {
        val progress = PuzzleChainProgress(required = 1)
        assertTrue(progress.recordSolved())
    }

    @Test
    fun `recording past completion is a no-op`() {
        val progress = PuzzleChainProgress(required = 1)
        progress.recordSolved()
        assertTrue(progress.recordSolved())
        assertEquals(1, progress.solvedCount)
    }

    @Test
    fun `rejects out-of-range required streak`() {
        assertFailsWith<IllegalArgumentException> { PuzzleChainProgress(required = 0) }
        assertFailsWith<IllegalArgumentException> { PuzzleChainProgress(required = 6) }
    }

    @Test
    fun `a failure resets the streak back to zero`() {
        val progress = PuzzleChainProgress(required = 3)
        progress.recordSolved()
        progress.recordSolved()
        assertEquals(1, progress.remaining)

        progress.recordFailed()
        assertEquals(3, progress.remaining)
        assertFalse(progress.isComplete)

        progress.recordSolved()
        progress.recordSolved()
        assertTrue(progress.recordSolved())
    }
}

class WalkProgressTest {

    @Test
    fun `completes once accumulated steps reach the requirement`() {
        val progress = WalkProgress(required = 15)
        assertFalse(progress.isComplete)
        assertEquals(15, progress.remaining)

        assertFalse(progress.recordSteps(10))
        assertEquals(5, progress.remaining)

        assertTrue(progress.recordSteps(5))
        assertEquals(0, progress.remaining)
        assertTrue(progress.isComplete)
    }

    @Test
    fun `overshooting the requirement in one burst does not overshoot the counter`() {
        val progress = WalkProgress(required = 10)
        assertTrue(progress.recordSteps(100))
        assertEquals(10, progress.stepsWalked)
    }

    @Test
    fun `recording past completion stays capped at the requirement`() {
        val progress = WalkProgress(required = 5)
        progress.recordSteps(5)
        progress.recordSteps(3)
        assertEquals(5, progress.stepsWalked)
    }

    @Test
    fun `rejects a negative step count`() {
        val progress = WalkProgress(required = 10)
        assertFailsWith<IllegalArgumentException> { progress.recordSteps(-1) }
    }

    @Test
    fun `rejects out-of-range required steps`() {
        assertFailsWith<IllegalArgumentException> { WalkProgress(required = 4) }
        assertFailsWith<IllegalArgumentException> { WalkProgress(required = 51) }
    }
}
