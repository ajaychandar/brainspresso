package com.brainspresso.app

import android.app.Application
import com.brainspresso.app.alarm.AlarmScheduler
import com.brainspresso.app.data.AlarmRepository
import com.brainspresso.app.data.db.AppDatabase
import com.brainspresso.app.data.settings.SettingsRepository
import com.brainspresso.app.service.NotificationChannels

class BrainspressoApp : Application() {

    val database by lazy { AppDatabase.getInstance(this) }
    val alarmScheduler by lazy { AlarmScheduler(this) }
    val alarmRepository by lazy { AlarmRepository(database.alarmDao(), alarmScheduler) }
    val settingsRepository by lazy { SettingsRepository(this) }

    override fun onCreate() {
        super.onCreate()
        NotificationChannels.ensureCreated(this)
    }
}
