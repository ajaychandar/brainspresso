package com.brainspresso.app.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.brainspresso.app.BrainspressoApp
import com.brainspresso.core.puzzle.Difficulty
import com.brainspresso.core.puzzle.PuzzleSettings
import com.brainspresso.core.puzzle.PuzzleType
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as BrainspressoApp

    val settings: StateFlow<PuzzleSettings> = app.settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), PuzzleSettings())

    /** Toggling the last remaining enabled type is a no-op: at least one must stay on. */
    fun toggleType(type: PuzzleType) {
        val current = settings.value.enabledTypes
        val next = if (type in current) current - type else current + type
        if (next.isEmpty()) return
        viewModelScope.launch { app.settingsRepository.setEnabledTypes(next) }
    }

    fun setDifficulty(difficulty: Difficulty) {
        viewModelScope.launch { app.settingsRepository.setDifficulty(difficulty) }
    }

    fun setRequiredStreak(streak: Int) {
        viewModelScope.launch { app.settingsRepository.setRequiredStreak(streak) }
    }

    fun setRequireWalkToStop(enabled: Boolean) {
        viewModelScope.launch { app.settingsRepository.setRequireWalkToStop(enabled) }
    }

    fun setWalkStepsRequired(steps: Int) {
        viewModelScope.launch { app.settingsRepository.setWalkStepsRequired(steps) }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = SettingsViewModel(application) as T
    }
}
