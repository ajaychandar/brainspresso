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
