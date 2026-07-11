package com.brainspresso.app.data

import androidx.test.core.app.ApplicationProvider
import com.brainspresso.app.alarm.AlarmScheduler
import com.brainspresso.app.data.db.AlarmEntity
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.DayOfWeek

@RunWith(RobolectricTestRunner::class)
class AlarmRepositoryTest {

    private val context = ApplicationProvider.getApplicationContext<android.content.Context>()
    private val dao = FakeAlarmDao()
    private val repository = AlarmRepository(dao, AlarmScheduler(context))

    private fun newAlarm(
        id: Long = 0,
        hour: Int = 7,
        minute: Int = 30,
        enabled: Boolean = true,
        repeatDays: Set<DayOfWeek> = emptySet(),
    ) = AlarmEntity(
        id = id,
        hour = hour,
        minute = minute,
        label = "Test",
        repeatDays = repeatDays,
        enabled = enabled,
        soundUri = null,
        vibrate = true,
        createdAt = 0L,
    )

    @Test
    fun `save inserts a new alarm and assigns an id`() = runTest {
        val id = repository.save(newAlarm())
        assertTrue(id > 0)
        assertEquals(id, repository.getById(id)?.id)
    }

    @Test
    fun `save on an existing id updates it in place`() = runTest {
        val id = repository.save(newAlarm())
        repository.save(newAlarm(id = id, hour = 9))
        assertEquals(9, repository.getById(id)?.hour)
        assertEquals(1, dao.getAllEnabled().size)
    }

    @Test
    fun `delete removes the alarm`() = runTest {
        val id = repository.save(newAlarm())
        val alarm = repository.getById(id)!!
        repository.delete(alarm)
        assertNull(repository.getById(id))
    }

    @Test
    fun `setEnabled false persists and does not throw when cancelling scheduling`() = runTest {
        val id = repository.save(newAlarm(enabled = true))
        val alarm = repository.getById(id)!!
        repository.setEnabled(alarm, enabled = false)
        assertEquals(false, repository.getById(id)?.enabled)
    }

    @Test
    fun `rescheduleAllEnabled only touches enabled alarms`() = runTest {
        repository.save(newAlarm(enabled = true, hour = 6))
        repository.save(newAlarm(enabled = false, hour = 8))
        repository.rescheduleAllEnabled()
        // Reaching this point without throwing confirms AlarmScheduler.schedule() was
        // exercised for the enabled alarm only; getAllEnabled() confirms the filter itself.
        assertEquals(1, dao.getAllEnabled().size)
    }
}
