package com.brainspresso.app.ui.puzzle

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.brainspresso.app.BrainspressoApp
import com.brainspresso.core.puzzle.Puzzle
import com.brainspresso.core.puzzle.PuzzleChainProgress
import com.brainspresso.core.puzzle.PuzzleGeneratorFactory
import com.brainspresso.core.puzzle.PuzzleSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlin.random.Random

data class PuzzleUiState(
    val puzzle: Puzzle? = null,
    val alarmLabel: String = "",
    val solvedCount: Int = 0,
    val required: Int = 1,
    val isChainComplete: Boolean = false,
    val justFailed: Boolean = false,
)

class PuzzleViewModel(
    application: Application,
    private val alarmId: Long,
) : AndroidViewModel(application) {

    private val app = application as BrainspressoApp
    private lateinit var settings: PuzzleSettings
    private lateinit var progress: PuzzleChainProgress

    private val _uiState = MutableStateFlow(PuzzleUiState())
    val uiState: StateFlow<PuzzleUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settings = app.settingsRepository.settings.first()
            progress = PuzzleChainProgress(settings.requiredStreak)
            val label = app.alarmRepository.getById(alarmId)?.label.orEmpty()
            _uiState.value = PuzzleUiState(
                puzzle = nextPuzzle(),
                alarmLabel = label,
                solvedCount = 0,
                required = settings.requiredStreak,
            )
        }
    }

    private fun nextPuzzle(): Puzzle =
        PuzzleGeneratorFactory.generate(settings.enabledTypes, settings.difficulty, Random.Default)

    fun onPuzzleSolved() {
        val complete = progress.recordSolved()
        _uiState.value = _uiState.value.copy(
            solvedCount = progress.solvedCount,
            isChainComplete = complete,
            justFailed = false,
            puzzle = if (complete) _uiState.value.puzzle else nextPuzzle(),
        )
    }

    fun onPuzzleFailed() {
        progress.recordFailed()
        _uiState.value = _uiState.value.copy(
            solvedCount = progress.solvedCount,
            justFailed = true,
            puzzle = nextPuzzle(),
        )
    }

    class Factory(private val application: Application, private val alarmId: Long) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PuzzleViewModel(application, alarmId) as T
    }
}
