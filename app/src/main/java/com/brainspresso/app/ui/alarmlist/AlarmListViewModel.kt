package com.brainspresso.app.ui.alarmlist

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.brainspresso.app.BrainspressoApp
import com.brainspresso.app.data.db.AlarmEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AlarmListViewModel(application: Application) : AndroidViewModel(application) {

    private val app = application as BrainspressoApp

    val alarms: StateFlow<List<AlarmEntity>> = app.alarmRepository.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun setEnabled(alarm: AlarmEntity, enabled: Boolean) {
        viewModelScope.launch { app.alarmRepository.setEnabled(alarm, enabled) }
    }

    fun delete(alarm: AlarmEntity) {
        viewModelScope.launch { app.alarmRepository.delete(alarm) }
    }

    class Factory(private val application: Application) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T = AlarmListViewModel(application) as T
    }
}
