package com.brainspresso.app.ui.editalarm

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.brainspresso.app.BrainspressoApp
import com.brainspresso.app.data.db.AlarmEntity
import com.brainspresso.app.ui.navigation.Screen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek

data class EditAlarmUiState(
    val alarmId: Long = Screen.EditAlarm.NEW_ALARM_ID,
    val hour: Int = 7,
    val minute: Int = 0,
    val label: String = "",
    val repeatDays: Set<DayOfWeek> = emptySet(),
    val soundUri: String? = null,
    val soundName: String = "Default alarm sound",
    val vibrate: Boolean = true,
    val enabled: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val isLoaded: Boolean = false,
    val isNew: Boolean = true,
    val saved: Boolean = false,
)

class EditAlarmViewModel(
    application: Application,
    private val alarmId: Long,
) : AndroidViewModel(application) {

    private val app = application as BrainspressoApp

    private val _uiState = MutableStateFlow(EditAlarmUiState(alarmId = alarmId, isNew = alarmId == Screen.EditAlarm.NEW_ALARM_ID))
    val uiState: StateFlow<EditAlarmUiState> = _uiState.asStateFlow()

    init {
        if (alarmId != Screen.EditAlarm.NEW_ALARM_ID) {
            viewModelScope.launch {
                app.alarmRepository.getById(alarmId)?.let { alarm ->
                    _uiState.value = _uiState.value.copy(
                        hour = alarm.hour,
                        minute = alarm.minute,
                        label = alarm.label,
                        repeatDays = alarm.repeatDays,
                        soundUri = alarm.soundUri,
                        soundName = resolveSoundName(alarm.soundUri),
                        vibrate = alarm.vibrate,
                        enabled = alarm.enabled,
                        createdAt = alarm.createdAt,
                        isLoaded = true,
                    )
                }
            }
        } else {
            _uiState.value = _uiState.value.copy(isLoaded = true)
        }
    }

    private fun resolveSoundName(uri: String?): String {
        if (uri == null) return "Default alarm sound"
        return runCatching {
            android.media.RingtoneManager.getRingtone(app, android.net.Uri.parse(uri))?.getTitle(app)
        }.getOrNull() ?: "Custom sound"
    }

    fun setTime(hour: Int, minute: Int) {
        _uiState.value = _uiState.value.copy(hour = hour, minute = minute)
    }

    fun setLabel(label: String) {
        _uiState.value = _uiState.value.copy(label = label)
    }

    fun toggleDay(day: DayOfWeek) {
        val current = _uiState.value.repeatDays
        val next = if (day in current) current - day else current + day
        _uiState.value = _uiState.value.copy(repeatDays = next)
    }

    fun setSound(uri: String?, name: String) {
        _uiState.value = _uiState.value.copy(soundUri = uri, soundName = name)
    }

    fun setVibrate(vibrate: Boolean) {
        _uiState.value = _uiState.value.copy(vibrate = vibrate)
    }

    fun save() {
        val state = _uiState.value
        viewModelScope.launch {
            app.alarmRepository.save(
                AlarmEntity(
                    id = if (state.isNew) 0 else state.alarmId,
                    hour = state.hour,
                    minute = state.minute,
                    label = state.label.trim(),
                    repeatDays = state.repeatDays,
                    enabled = state.enabled,
                    soundUri = state.soundUri,
                    vibrate = state.vibrate,
                    createdAt = state.createdAt,
                ),
            )
            _uiState.value = _uiState.value.copy(saved = true)
        }
    }

    fun delete() {
        val state = _uiState.value
        if (state.isNew) return
        viewModelScope.launch {
            app.alarmRepository.getById(state.alarmId)?.let { app.alarmRepository.delete(it) }
            _uiState.value = _uiState.value.copy(saved = true)
        }
    }

    class Factory(private val application: Application, private val alarmId: Long) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            EditAlarmViewModel(application, alarmId) as T
    }
}
