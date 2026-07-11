package com.brainspresso.app.ui.navigation

sealed class Screen(val route: String) {
    object AlarmList : Screen("alarm_list")
    object Settings : Screen("settings")
    object EditAlarm : Screen("edit_alarm/{alarmId}") {
        const val ARG_ALARM_ID = "alarmId"
        const val NEW_ALARM_ID = -1L

        fun route(alarmId: Long = NEW_ALARM_ID) = "edit_alarm/$alarmId"
    }
}
