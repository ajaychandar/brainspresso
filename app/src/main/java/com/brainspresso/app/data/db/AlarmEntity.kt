package com.brainspresso.app.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.DayOfWeek

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val label: String,
    val repeatDays: Set<DayOfWeek>,
    val enabled: Boolean,
    val soundUri: String?,
    val vibrate: Boolean,
    val createdAt: Long,
) {
    val isRepeating: Boolean get() = repeatDays.isNotEmpty()
}
