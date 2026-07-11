package com.brainspresso.core.alarm

import java.time.DayOfWeek
import java.time.ZonedDateTime

/** Framework-agnostic computation of when an alarm should next fire. */
object AlarmTimeCalculator {

    /**
     * Returns the next [ZonedDateTime] strictly after [now] at which the alarm should ring.
     *
     * @param repeatDays empty for a one-time alarm (next occurrence of [hour]:[minute],
     *   today if still upcoming, otherwise tomorrow); non-empty for a weekly repeating alarm
     *   that only fires on the given days.
     */
    fun nextTrigger(
        hour: Int,
        minute: Int,
        repeatDays: Set<DayOfWeek>,
        now: ZonedDateTime,
    ): ZonedDateTime {
        require(hour in 0..23) { "hour must be in 0..23, was $hour" }
        require(minute in 0..59) { "minute must be in 0..59, was $minute" }

        if (repeatDays.isEmpty()) {
            val todayCandidate = now.toLocalDate().atTime(hour, minute).atZone(now.zone)
            return if (todayCandidate.isAfter(now)) todayCandidate else todayCandidate.plusDays(1)
        }

        for (offset in 0L..7L) {
            val candidateDate = now.toLocalDate().plusDays(offset)
            if (candidateDate.dayOfWeek !in repeatDays) continue
            val candidate = candidateDate.atTime(hour, minute).atZone(now.zone)
            if (candidate.isAfter(now)) return candidate
        }
        error("No matching day found within one week for repeatDays=$repeatDays")
    }
}
