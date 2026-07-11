package com.brainspresso.app.data

import com.brainspresso.app.data.db.AlarmDao
import com.brainspresso.app.data.db.AlarmEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/** In-memory [AlarmDao] for tests, avoiding the need for a real Room/SQLite instance. */
class FakeAlarmDao : AlarmDao {

    private var nextId = 1L
    private val state = MutableStateFlow<List<AlarmEntity>>(emptyList())

    override fun observeAll(): StateFlow<List<AlarmEntity>> = state

    override suspend fun getById(id: Long): AlarmEntity? = state.value.find { it.id == id }

    override suspend fun getAllEnabled(): List<AlarmEntity> = state.value.filter { it.enabled }

    override suspend fun upsert(alarm: AlarmEntity): Long {
        val id = if (alarm.id == 0L) nextId++ else alarm.id
        val saved = alarm.copy(id = id)
        state.value = state.value.filterNot { it.id == id } + saved
        return id
    }

    override suspend fun update(alarm: AlarmEntity) {
        state.value = state.value.map { if (it.id == alarm.id) alarm else it }
    }

    override suspend fun delete(alarm: AlarmEntity) {
        state.value = state.value.filterNot { it.id == alarm.id }
    }

    override suspend fun setEnabled(id: Long, enabled: Boolean) {
        state.value = state.value.map { if (it.id == id) it.copy(enabled = enabled) else it }
    }
}
