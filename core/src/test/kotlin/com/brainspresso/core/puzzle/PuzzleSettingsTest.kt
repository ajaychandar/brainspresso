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
