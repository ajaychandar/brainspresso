package com.brainspresso.app.data.settings

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.test.core.app.ApplicationProvider
import com.brainspresso.core.puzzle.Difficulty
import com.brainspresso.core.puzzle.PuzzleSettings
import com.brainspresso.core.puzzle.PuzzleType
import java.util.UUID
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Each test gets its own uniquely-named DataStore file (rather than the app's fixed
 * `brainspresso_settings` name) so runs don't leak state into one another via a shared file
 * on disk within the same Robolectric process.
 */
@RunWith(RobolectricTestRunner::class)
class SettingsRepositoryTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val dataStore = PreferenceDataStoreFactory.create(
        scope = CoroutineScope(Dispatchers.Unconfined + Job()),
        produceFile = { context.preferencesDataStoreFile("test_settings_${UUID.randomUUID()}") },
    )
    private val repository = SettingsRepository(dataStore)

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

    @Test
    fun `setRequireWalkToStop round-trips`() = runTest {
        repository.setRequireWalkToStop(true)
        assertEquals(true, repository.settings.first().requireWalkToStop)

        repository.setRequireWalkToStop(false)
        assertEquals(false, repository.settings.first().requireWalkToStop)
    }

    @Test
    fun `setWalkStepsRequired clamps to the valid range`() = runTest {
        repository.setWalkStepsRequired(999)
        assertEquals(PuzzleSettings.MAX_WALK_STEPS, repository.settings.first().walkStepsRequired)

        repository.setWalkStepsRequired(0)
        assertEquals(PuzzleSettings.MIN_WALK_STEPS, repository.settings.first().walkStepsRequired)
    }

    @Test
    fun `setWalkStepsRequired persists an in-range value exactly`() = runTest {
        repository.setWalkStepsRequired(20)
        assertEquals(20, repository.settings.first().walkStepsRequired)
    }
}
