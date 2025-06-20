package com.dijia1124.plusplusbattery.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.dijia1124.plusplusbattery.R
import com.dijia1124.plusplusbattery.ui.components.AppScaffold
import com.dijia1124.plusplusbattery.ui.components.BackIcon
import com.dijia1124.plusplusbattery.vm.BatteryLogViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UniversalSupportLogcat(navController: NavController,
                           batteryLogViewModel: BatteryLogViewModel, currentTitle: String) {

    AppScaffold(currentTitle,
        navigationIcon = { BackIcon(navController)
        }
    ) {
        UniversalSupportLogcatScreen(batteryLogViewModel)
    }
}
@Composable
fun UniversalSupportLogcatScreen(
    batteryLogViewModel: BatteryLogViewModel
) {
    val logMap by batteryLogViewModel.latestLog.collectAsState()
    Column(Modifier
        .fillMaxSize()
        .padding(16.dp)) {
        logMap?.let { map ->
            map.forEach { (key, value) ->
                Text("$key: $value", style = MaterialTheme.typography.bodyMedium)
            }
        } ?: Text(
            stringResource(R.string.waiting_for_battery_data_from_logcat),
            style = MaterialTheme.typography.bodyMedium)
    }
}