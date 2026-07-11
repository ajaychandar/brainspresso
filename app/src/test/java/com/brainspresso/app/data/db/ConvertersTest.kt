package com.brainspresso.app.data.db

import java.time.DayOfWeek
import org.junit.Assert.assertEquals
import org.junit.Test

class ConvertersTest {

    private val converters = Converters()

    @Test
    fun `empty set round-trips through mask 0`() {
        assertEquals(0, converters.fromDayOfWeekSet(emptySet()))
        assertEquals(emptySet<DayOfWeek>(), converters.toDayOfWeekSet(0))
    }

    @Test
    fun `every day round-trips through mask 127`() {
        val allDays = DayOfWeek.entries.toSet()
        assertEquals(127, converters.fromDayOfWeekSet(allDays))
        assertEquals(allDays, converters.toDayOfWeekSet(127))
    }

    @Test
    fun `arbitrary weekday subset round-trips`() {
        val days = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY)
        val mask = converters.fromDayOfWeekSet(days)
        assertEquals(days, converters.toDayOfWeekSet(mask))
    }

    @Test
    fun `single day masks are distinct and decode correctly`() {
        DayOfWeek.entries.forEach { day ->
            val mask = converters.fromDayOfWeekSet(setOf(day))
            assertEquals(setOf(day), converters.toDayOfWeekSet(mask))
        }
    }
}
