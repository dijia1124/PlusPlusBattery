package com.dijia1124.plusplusbattery.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
    val deviceInfo by batteryLogViewModel.deviceInfo.collectAsState()
    val logMap by batteryLogViewModel.latestLog.collectAsState()
    val scrollState = rememberScrollState()
    Column(Modifier
        .fillMaxSize()
        .padding(16.dp)
        .verticalScroll(scrollState)
    ) {
        // device info
        Text(
            stringResource(R.string.manufacturer, deviceInfo.manufacturer),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            stringResource(R.string.model, deviceInfo.model),
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(Modifier.padding(vertical = 8.dp))
        // logcat entries
        if (logMap.isNullOrEmpty()) {
            Text(
                stringResource(R.string.waiting_for_battery_data_from_logcat),
                style = MaterialTheme.typography.bodyMedium
            )
        } else {
            logMap!!.forEach { (key, value) ->
                Text("$key: $value", style = MaterialTheme.typography.bodyMedium)
            }
        }
    }
}