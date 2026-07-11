package com.brainspresso.app.data

import com.brainspresso.app.alarm.AlarmScheduler
import com.brainspresso.app.data.db.AlarmDao
import com.brainspresso.app.data.db.AlarmEntity
import kotlinx.coroutines.flow.Flow

/**
 * Single source of truth for alarms. Keeps the database and the AlarmManager
 * scheduling state in sync so callers never have to remember to do both.
 */
class AlarmRepository(
    private val dao: AlarmDao,
    private val scheduler: AlarmScheduler,
) {
    fun observeAll(): Flow<List<AlarmEntity>> = dao.observeAll()

    suspend fun getById(id: Long): AlarmEntity? = dao.getById(id)

    suspend fun save(alarm: AlarmEntity): Long {
        val id = dao.upsert(alarm)
        val saved = alarm.copy(id = if (alarm.id == 0L) id else alarm.id)
        applyScheduling(saved)
        return id
    }

    suspend fun delete(alarm: AlarmEntity) {
        scheduler.cancel(alarm.id)
        dao.delete(alarm)
    }

    suspend fun setEnabled(alarm: AlarmEntity, enabled: Boolean) {
        dao.setEnabled(alarm.id, enabled)
        applyScheduling(alarm.copy(enabled = enabled))
    }

    /** Re-schedules every enabled alarm; used after boot since AlarmManager forgets on reboot. */
    suspend fun rescheduleAllEnabled() {
        dao.getAllEnabled().forEach { applyScheduling(it) }
    }

    private fun applyScheduling(alarm: AlarmEntity) {
        if (alarm.enabled) scheduler.schedule(alarm) else scheduler.cancel(alarm.id)
    }
}
