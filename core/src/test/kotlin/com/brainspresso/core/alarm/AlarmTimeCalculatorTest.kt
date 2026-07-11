package com.brainspresso.core.alarm

import java.time.DayOfWeek
import java.time.ZoneId
import java.time.ZonedDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class AlarmTimeCalculatorTest {

    private val zone = ZoneId.of("UTC")

    // Wednesday 2026-07-08 08:30:00 UTC
    private fun wednesdayMorning(): ZonedDateTime =
        ZonedDateTime.of(2026, 7, 8, 8, 30, 0, 0, zone)

    @Test
    fun `one-time alarm later today fires today`() {
        val now = wednesdayMorning()
        val next = AlarmTimeCalculator.nextTrigger(hour = 9, minute = 0, repeatDays = emptySet(), now = now)
        assertEquals(now.toLocalDate(), next.toLocalDate())
        assertEquals(9, next.hour)
    }

    @Test
    fun `one-time alarm earlier today rolls to tomorrow`() {
        val now = wednesdayMorning()
        val next = AlarmTimeCalculator.nextTrigger(hour = 8, minute = 0, repeatDays = emptySet(), now = now)
        assertEquals(now.toLocalDate().plusDays(1), next.toLocalDate())
    }

    @Test
    fun `one-time alarm at the exact current minute rolls to tomorrow`() {
        val now = wednesdayMorning()
        val next = AlarmTimeCalculator.nextTrigger(hour = 8, minute = 30, repeatDays = emptySet(), now = now)
        assertEquals(now.toLocalDate().plusDays(1), next.toLocalDate())
    }

    @Test
    fun `repeating alarm picks the nearest matching day later this week`() {
        val now = wednesdayMorning() // Wednesday
        val next = AlarmTimeCalculator.nextTrigger(
            hour = 7,
            minute = 0,
            repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY),
            now = now,
        )
        assertEquals(DayOfWeek.FRIDAY, next.dayOfWeek)
        assertEquals(now.toLocalDate().plusDays(2), next.toLocalDate())
    }

    @Test
    fun `repeating alarm on today but time already passed rolls to next week`() {
        val now = wednesdayMorning() // Wednesday 08:30
        val next = AlarmTimeCalculator.nextTrigger(
            hour = 7,
            minute = 0,
            repeatDays = setOf(DayOfWeek.WEDNESDAY),
            now = now,
        )
        assertEquals(now.toLocalDate().plusDays(7), next.toLocalDate())
    }

    @Test
    fun `repeating alarm on today with time still upcoming fires today`() {
        val now = wednesdayMorning() // Wednesday 08:30
        val next = AlarmTimeCalculator.nextTrigger(
            hour = 20,
            minute = 0,
            repeatDays = setOf(DayOfWeek.WEDNESDAY),
            now = now,
        )
        assertEquals(now.toLocalDate(), next.toLocalDate())
    }

    @Test
    fun `every day repeating alarm always lands within 24 hours`() {
        val now = wednesdayMorning()
        val next = AlarmTimeCalculator.nextTrigger(
            hour = 8,
            minute = 29,
            repeatDays = DayOfWeek.entries.toSet(),
            now = now,
        )
        assertEquals(now.toLocalDate().plusDays(1), next.toLocalDate())
    }

    @Test
    fun `rejects invalid hour or minute`() {
        val now = wednesdayMorning()
        assertFailsWith<IllegalArgumentException> {
            AlarmTimeCalculator.nextTrigger(hour = 24, minute = 0, repeatDays = emptySet(), now = now)
        }
        assertFailsWith<IllegalArgumentException> {
            AlarmTimeCalculator.nextTrigger(hour = 0, minute = 60, repeatDays = emptySet(), now = now)
        }
    }
}
