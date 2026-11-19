package com.brainspresso.app.data.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.brainspresso.core.puzzle.Difficulty
import com.brainspresso.core.puzzle.PuzzleSettings
import com.brainspresso.core.puzzle.PuzzleType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore by preferencesDataStore(name = "brainspresso_settings")

/** Takes the [DataStore] directly (rather than a [Context]) so tests can inject an isolated instance. */
class SettingsRepository(private val dataStore: DataStore<Preferences>) {

    constructor(context: Context) : this(context.settingsDataStore)

    private object Keys {
        val ENABLED_TYPES = stringSetPreferencesKey("enabled_puzzle_types")
        val DIFFICULTY = stringPreferencesKey("difficulty")
        val REQUIRED_STREAK = intPreferencesKey("required_streak")
        val REQUIRE_WALK_TO_STOP = booleanPreferencesKey("require_walk_to_stop")
        val WALK_STEPS_REQUIRED = intPreferencesKey("walk_steps_required")
    }

    val settings: Flow<PuzzleSettings> = dataStore.data.map { prefs ->
        val defaults = PuzzleSettings()
        val enabledTypes = prefs[Keys.ENABLED_TYPES]
            ?.mapNotNull { runCatching { PuzzleType.valueOf(it) }.getOrNull() }
            ?.toSet()
            ?.ifEmpty { defaults.enabledTypes }
            ?: defaults.enabledTypes
        val difficulty = prefs[Keys.DIFFICULTY]
            ?.let { runCatching { Difficulty.valueOf(it) }.getOrNull() }
            ?: defaults.difficulty
        val requiredStreak = (prefs[Keys.REQUIRED_STREAK] ?: defaults.requiredStreak)
            .coerceIn(PuzzleSettings.MIN_STREAK, PuzzleSettings.MAX_STREAK)
        val requireWalkToStop = prefs[Keys.REQUIRE_WALK_TO_STOP] ?: defaults.requireWalkToStop
        val walkStepsRequired = (prefs[Keys.WALK_STEPS_REQUIRED] ?: defaults.walkStepsRequired)
            .coerceIn(PuzzleSettings.MIN_WALK_STEPS, PuzzleSettings.MAX_WALK_STEPS)
        PuzzleSettings(
            enabledTypes = enabledTypes,
            difficulty = difficulty,
            requiredStreak = requiredStreak,
            requireWalkToStop = requireWalkToStop,
            walkStepsRequired = walkStepsRequired,
        )
    }

    suspend fun setEnabledTypes(types: Set<PuzzleType>) {
        dataStore.edit { it[Keys.ENABLED_TYPES] = types.map(PuzzleType::name).toSet() }
    }

    suspend fun setDifficulty(difficulty: Difficulty) {
        dataStore.edit { it[Keys.DIFFICULTY] = difficulty.name }
    }

    suspend fun setRequiredStreak(streak: Int) {
        val clamped = streak.coerceIn(PuzzleSettings.MIN_STREAK, PuzzleSettings.MAX_STREAK)
        dataStore.edit { it[Keys.REQUIRED_STREAK] = clamped }
    }

    suspend fun setRequireWalkToStop(enabled: Boolean) {
        dataStore.edit { it[Keys.REQUIRE_WALK_TO_STOP] = enabled }
    }

    suspend fun setWalkStepsRequired(steps: Int) {
        val clamped = steps.coerceIn(PuzzleSettings.MIN_WALK_STEPS, PuzzleSettings.MAX_WALK_STEPS)
        dataStore.edit { it[Keys.WALK_STEPS_REQUIRED] = clamped }
    }
}
