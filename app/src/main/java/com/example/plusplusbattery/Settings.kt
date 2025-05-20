package com.example.plusplusbattery

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavController
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.plusplusbattery.ui.components.AppScaffold

@Composable
fun Settings(currentTitle: String, navController: NavController, hasRoot: Boolean, batteryVM: BatteryInfoViewModel, settingsVM: SettingsViewModel) {
    AppScaffold(currentTitle) {
        SettingsContent(navController, hasRoot, batteryVM, settingsVM)
    }
}

@Composable
fun SettingsContent(
    navController: NavController,
    hasRoot: Boolean,
    batteryVM: BatteryInfoViewModel,
    settingsVM: SettingsViewModel,
) {
    val isRootMode by batteryVM.isRootMode.collectAsState()
    val showOnDash by batteryVM.showSwitchOnDashboard.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    val darkModeEnabled by settingsVM.darkModeEnabled.collectAsState()
    val followSystemTheme by settingsVM.followSystemTheme.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .verticalScroll(scrollState)
    ) {
        ListItem(
            headlineContent = { Text(text = stringResource(R.string.use_root_mode), style = MaterialTheme.typography.bodyLarge) },
            trailingContent = {
                Switch(
                    checked = isRootMode,
                    onCheckedChange = { desired ->
                        if (desired) {
                            if (hasRoot) batteryVM.setRootMode(true)
                            else Toast.makeText(
                                context,
                                context.getString(R.string.root_access_denied),
                                Toast.LENGTH_SHORT
                            ).show()
                        } else batteryVM.setRootMode(false)
                    }
                )
            }
        )
        ListItem(
            headlineContent = { Text(text = stringResource(R.string.show_root_switch_on_dashboard), style = MaterialTheme.typography.bodyLarge) },
            trailingContent = {
                Switch(
                    checked = showOnDash,
                    onCheckedChange = { batteryVM.setShowSwitchOnDashboard(it) }
                )
            }
        )
        ListItem(
            headlineContent = {
                Text(text = stringResource(R.string.follow_system_theme), style = MaterialTheme.typography.bodyLarge)
            },
            trailingContent = {
                Switch(
                    checked = followSystemTheme,
                    onCheckedChange = { settingsVM.setFollowSystem(it) }
                )
            }
        )

        ListItem(
            headlineContent = {
                Text(text = stringResource(R.string.enable_dark_mode), style = MaterialTheme.typography.bodyLarge)
            },
            trailingContent = {
                Switch(
                    checked = darkModeEnabled,
                    onCheckedChange = { settingsVM.setDarkMode(it) },
                    enabled = !followSystemTheme
                )
            }
        )
        ListItem(
            modifier = Modifier.clickable { navController.navigate("about") },
            headlineContent = { Text(text = stringResource(R.string.about), style = MaterialTheme.typography.bodyLarge) }
        )
    }
}


@Composable
fun BatteryMonitorButton() {
    val context = LocalContext.current
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            val intent = Intent(context, BatteryMonitorService::class.java)
            context.startForegroundService(intent)
        } else {
            Toast.makeText(context,
                context.getString(R.string.notification_permission_denied), Toast.LENGTH_SHORT).show()
        }
    }

    Button(
        modifier = Modifier.fillMaxWidth(),
        onClick = {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                launcher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            } else {
                val intent = Intent(context, BatteryMonitorService::class.java)
                context.startForegroundService(intent)
            }
        } else {
            val intent = Intent(context, BatteryMonitorService::class.java)
            context.startForegroundService(intent)
        }
    }) {
        Text("Launch Battery Monitor")
    }
}
