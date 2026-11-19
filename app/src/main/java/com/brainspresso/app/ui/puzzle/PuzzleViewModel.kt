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
import com.brainspresso.core.puzzle.WalkProgress
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
    val isWalking: Boolean = false,
    val stepsWalked: Int = 0,
    val stepsRequired: Int = 0,
)

class PuzzleViewModel(
    application: Application,
    private val alarmId: Long,
) : AndroidViewModel(application) {

    private val app = application as BrainspressoApp
    private lateinit var settings: PuzzleSettings
    private lateinit var progress: PuzzleChainProgress

    /** Non-null only when this alarm's settings require walking after the puzzles are solved. */
    private var walkProgress: WalkProgress? = null

    private val _uiState = MutableStateFlow(PuzzleUiState())
    val uiState: StateFlow<PuzzleUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            settings = app.settingsRepository.settings.first()
            progress = PuzzleChainProgress(settings.requiredStreak)
            if (settings.requireWalkToStop) {
                walkProgress = WalkProgress(settings.walkStepsRequired)
            }
            val label = app.alarmRepository.getById(alarmId)?.label.orEmpty()
            _uiState.value = PuzzleUiState(
                puzzle = nextPuzzle(),
                alarmLabel = label,
                solvedCount = 0,
                required = settings.requiredStreak,
                stepsRequired = settings.walkStepsRequired,
            )
        }
    }

    private fun nextPuzzle(): Puzzle =
        PuzzleGeneratorFactory.generate(settings.enabledTypes, settings.difficulty, Random.Default)

    fun onPuzzleSolved() {
        val puzzlesComplete = progress.recordSolved()
        if (!puzzlesComplete) {
            _uiState.value = _uiState.value.copy(
                solvedCount = progress.solvedCount,
                justFailed = false,
                puzzle = nextPuzzle(),
            )
            return
        }

        val walk = walkProgress
        _uiState.value = if (walk == null) {
            _uiState.value.copy(solvedCount = progress.solvedCount, justFailed = false, isChainComplete = true)
        } else {
            _uiState.value.copy(solvedCount = progress.solvedCount, justFailed = false, isWalking = true)
        }
    }

    fun onPuzzleFailed() {
        progress.recordFailed()
        _uiState.value = _uiState.value.copy(
            solvedCount = progress.solvedCount,
            justFailed = true,
            puzzle = nextPuzzle(),
        )
    }

    /** Called from the Activity's step-sensor listener while in the walking phase. */
    fun onStepsDetected(count: Int) {
        val walk = walkProgress ?: return
        val complete = walk.recordSteps(count)
        _uiState.value = _uiState.value.copy(stepsWalked = walk.stepsWalked, isChainComplete = complete)
    }

    /**
     * Called by the Activity if the step sensor or permission turns out to be unavailable once
     * the walking phase starts, so a device that can't satisfy the requirement never traps the
     * user behind it.
     */
    fun skipWalkRequirement() {
        if (_uiState.value.isWalking) {
            _uiState.value = _uiState.value.copy(isChainComplete = true)
        }
    }

    class Factory(private val application: Application, private val alarmId: Long) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            PuzzleViewModel(application, alarmId) as T
    }
}
