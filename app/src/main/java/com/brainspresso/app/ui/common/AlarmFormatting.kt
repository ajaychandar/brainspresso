package com.brainspresso.app.ui.common

import java.time.DayOfWeek
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

fun formatAlarmTime(hour: Int, minute: Int): String =
    LocalTime.of(hour, minute).format(timeFormatter)

fun formatRepeatDays(days: Set<DayOfWeek>): String = when {
    days.isEmpty() -> "One-time"
    days.size == 7 -> "Every day"
    days == setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY) -> "Weekends"
    days == DayOfWeek.entries.filterNot { it == DayOfWeek.SATURDAY || it == DayOfWeek.SUNDAY }.toSet() -> "Weekdays"
    else -> DayOfWeek.entries
        .filter { it in days }
        .joinToString(", ") { it.getDisplayName(TextStyle.SHORT, Locale.getDefault()) }
}

private val clockFormatter = DateTimeFormatter.ofPattern("h:mm")
private val meridiemFormatter = DateTimeFormatter.ofPattern("a")

/** Splits a time into its clock digits ("6:30") and AM/PM marker so they can be styled separately. */
fun formatAlarmTimeParts(hour: Int, minute: Int): Pair<String, String> {
    val time = LocalTime.of(hour, minute)
    return time.format(clockFormatter) to time.format(meridiemFormatter)
}

/** "7h 45m" / "45m" / "Under a minute" for the time left until an alarm rings. */
fun formatTimeRemaining(totalMinutes: Long): String {
    val minutes = totalMinutes.coerceAtLeast(0)
    val days = minutes / (24 * 60)
    val hours = (minutes % (24 * 60)) / 60
    val mins = minutes % 60
    return when {
        days > 0 -> "${days}d ${hours}h"
        hours > 0 -> "${hours}h ${mins}m"
        mins > 0 -> "${mins}m"
        else -> "Under a minute"
    }
}
