package com.brainspresso.app.data.settings

import androidx.test.core.app.ApplicationProvider
import com.brainspresso.core.puzzle.Difficulty
import com.brainspresso.core.puzzle.PuzzleSettings
import com.brainspresso.core.puzzle.PuzzleType
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SettingsRepositoryTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val repository = SettingsRepository(context)

    @Test
    fun `defaults match PuzzleSettings defaults`() = runTest {
        val settings = repository.settings.first()
        assertEquals(PuzzleSettings(), settings)
    }

    @Test
    fun `setEnabledTypes round-trips`() = runTest {
        val types = setOf(PuzzleType.MATH, PuzzleType.TAP_PATTERN)
        repository.setEnabledTypes(types)
        assertEquals(types, repository.settings.first().enabledTypes)
    }

    @Test
    fun `setDifficulty round-trips`() = runTest {
        repository.setDifficulty(Difficulty.HARD)
        assertEquals(Difficulty.HARD, repository.settings.first().difficulty)
    }

    @Test
    fun `setRequiredStreak clamps to the valid range`() = runTest {
        repository.setRequiredStreak(99)
        assertEquals(PuzzleSettings.MAX_STREAK, repository.settings.first().requiredStreak)

        repository.setRequiredStreak(-3)
        assertEquals(PuzzleSettings.MIN_STREAK, repository.settings.first().requiredStreak)
    }

    @Test
    fun `setRequiredStreak persists an in-range value exactly`() = runTest {
        repository.setRequiredStreak(3)
        assertEquals(3, repository.settings.first().requiredStreak)
    }
}
