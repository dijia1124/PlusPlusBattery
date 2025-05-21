package com.example.plusplusbattery

import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.plusplusbattery.ui.components.AppScaffold
import androidx.compose.runtime.getValue

@Composable
fun BatteryMonitor(currentTitle: String, navController: NavController, battMonVM: BatteryMonitorSettingsViewModel) {
    AppScaffold(currentTitle) {
        BatteryMonitorContent(navController, battMonVM)
    }
}

@Composable
fun BatteryMonitorContent(
    navController: NavController,
    battMonVM: BatteryMonitorSettingsViewModel
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
        ListItem(
            headlineContent = {BatteryMonitorSwitch(battMonVM)}
        )
        ListItem(
            headlineContent = {
                Text(text = stringResource(R.string.disable_battery_optimization), style = MaterialTheme.typography.bodySmall)
            }
        )
    }
}

@Composable
fun BatteryMonitorSwitch(battMonVM: BatteryMonitorSettingsViewModel) {
    val context = LocalContext.current
    val isMonitoring by battMonVM.isMonitoring.collectAsState()

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        if (granted) {
            battMonVM.startMonitor()
        } else {
            Toast.makeText(
                context,
                context.getString(R.string.notification_permission_denied),
                Toast.LENGTH_SHORT
            ).show()
            battMonVM.stopMonitor()
        }
    }

    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = context.getString(R.string.battery_monitor), modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyLarge)
        Switch(
            checked = isMonitoring,
            onCheckedChange = { on ->
                if (on) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        when {
                            ContextCompat.checkSelfPermission(
                                context,
                                android.Manifest.permission.POST_NOTIFICATIONS
                            ) == PackageManager.PERMISSION_GRANTED -> {
                                battMonVM.startMonitor()
                            }
                            else -> {
                                permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                            }
                        }
                    } else {
                        battMonVM.startMonitor()
                    }
                } else {
                    battMonVM.stopMonitor()
                }
            }
        )
    }
}
