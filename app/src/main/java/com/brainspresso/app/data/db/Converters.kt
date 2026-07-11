package com.brainspresso.app.data.db

import androidx.room.TypeConverter
import java.time.DayOfWeek

/** Persists a [Set] of [DayOfWeek] as a 7-bit mask (bit 0 = Monday, bit 6 = Sunday). */
class Converters {

    @TypeConverter
    fun fromDayOfWeekSet(days: Set<DayOfWeek>): Int =
        days.fold(0) { mask, day -> mask or (1 shl (day.value - 1)) }

    @TypeConverter
    fun toDayOfWeekSet(mask: Int): Set<DayOfWeek> =
        DayOfWeek.entries.filterTo(mutableSetOf()) { day -> (mask shr (day.value - 1)) and 1 == 1 }
}
