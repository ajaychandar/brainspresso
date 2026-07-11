package com.brainspresso.app.ui.navigation

import android.app.Application
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import com.brainspresso.app.ui.alarmlist.AlarmListScreen
import com.brainspresso.app.ui.alarmlist.AlarmListViewModel
import com.brainspresso.app.ui.editalarm.EditAlarmScreen
import com.brainspresso.app.ui.editalarm.EditAlarmViewModel
import com.brainspresso.app.ui.settings.SettingsScreen
import com.brainspresso.app.ui.settings.SettingsViewModel

@Composable
fun BrainspressoNavHost(navController: NavHostController = rememberNavController()) {
    val application = LocalContext.current.applicationContext as Application

    NavHost(navController = navController, startDestination = Screen.AlarmList.route) {
        composable(Screen.AlarmList.route) {
            val viewModel: AlarmListViewModel = viewModel(factory = AlarmListViewModel.Factory(application))
            AlarmListScreen(
                viewModel = viewModel,
                onAddAlarm = { navController.navigate(Screen.EditAlarm.route()) },
                onEditAlarm = { id -> navController.navigate(Screen.EditAlarm.route(id)) },
                onOpenSettings = { navController.navigate(Screen.Settings.route) },
            )
        }
        composable(Screen.Settings.route) {
            val viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory(application))
            SettingsScreen(viewModel = viewModel, onBack = { navController.popBackStack() })
        }
        composable(
            route = Screen.EditAlarm.route,
            arguments = listOf(navArgument(Screen.EditAlarm.ARG_ALARM_ID) { type = NavType.LongType }),
        ) { backStackEntry ->
            val alarmId = backStackEntry.arguments?.getLong(Screen.EditAlarm.ARG_ALARM_ID) ?: Screen.EditAlarm.NEW_ALARM_ID
            val viewModel: EditAlarmViewModel = viewModel(
                factory = EditAlarmViewModel.Factory(application, alarmId),
                key = "edit_alarm_$alarmId",
            )
            EditAlarmScreen(viewModel = viewModel, onDone = { navController.popBackStack() })
        }
    }
}
