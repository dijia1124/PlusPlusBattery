package com.example.plusplusbattery

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.plusplusbattery.ui.components.AppScaffold

@Composable
fun BatteryMonitor(currentTitle: String, navController: NavController) {
    AppScaffold(currentTitle) {
        BatteryMonitorContent(navController)
    }
}

@Composable
fun BatteryMonitorContent(
    navController: NavController,
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .verticalScroll(scrollState)
    ) {
        ListItem(
            modifier = Modifier.clickable { navController.navigate("batt_mon_settings") },
            headlineContent = { Text(text = stringResource(R.string.battery_monitor_entry_settings), style = MaterialTheme.typography.bodyLarge) }
        )
        BatteryMonitorButton()
        //todo: testing
    }
}