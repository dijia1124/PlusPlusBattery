package com.dijia1124.plusplusbattery.ui.screen

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.dijia1124.plusplusbattery.vm.BatteryInfoViewModel
import com.dijia1124.plusplusbattery.R
import com.dijia1124.plusplusbattery.vm.SettingsViewModel
import com.dijia1124.plusplusbattery.ui.components.AppScaffold
import androidx.core.net.toUri
import com.dijia1124.plusplusbattery.ui.components.showRootDeniedToast
import kotlin.math.roundToInt

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
    val refreshInterval by settingsVM.refreshInterval.collectAsState()
    val showOplusFields by settingsVM.showOplusFields.collectAsState()
    var showMgr by remember { mutableStateOf(false) }

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
            headlineContent = {
                Text(text = stringResource(R.string.enable_oplus_fields), style = MaterialTheme.typography.bodyLarge)
            },
            trailingContent = {
                Switch(
                    checked = showOplusFields,
                    onCheckedChange = { settingsVM.setShowOplusFields(it) }
                )
            }
        )
        ListItem(
            modifier = Modifier.clickable { showMgr = true },
            headlineContent = {
                Text(
                    text = stringResource(R.string.manage_custom_entries),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        )
        if (showMgr) {
            if (isRootMode) {
                ManageEntriesDialog(
                    viewModel = batteryVM,
                    onDismiss = { showMgr = false }
                )
            }
            else {
                context.showRootDeniedToast()
                showMgr = false
            }
        }
        RefreshIntervalListItem(
            refreshInterval = refreshInterval,
            onIntervalChange = { newRate ->
                settingsVM.setRefreshInterval(newRate)
            }
        )
        ListItem(
            modifier = Modifier.clickable {
                if (isRootMode) {
                    navController.navigate("battery_logcat_experiment")
                }
                else context.showRootDeniedToast()},
            headlineContent = { Text(text = stringResource(R.string.get_from_logcat), style = MaterialTheme.typography.bodyLarge) }
        )
        ListItem(
            modifier = Modifier.clickable {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = "https://github.com/dijia1124/PlusPlusBattery/releases".toUri()
                }
                context.startActivity(intent)
            },
            headlineContent = {
                Text(
                    text = stringResource(R.string.check_for_updates),
                    style = MaterialTheme.typography.bodyLarge
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
fun RefreshIntervalListItem(
    refreshInterval: Int,
    onIntervalChange: (Int) -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    val tickValues = listOf(200, 500, 800, 1000, 2000, 3000, 5000, 10000)
    val defaultIndex = tickValues.indexOf(1000).coerceAtLeast(0)

    // current slider index
    var sliderIndex by remember(refreshInterval) {
        mutableIntStateOf(
            tickValues.indexOfFirst { it >= refreshInterval }
                .let { if (it == -1) tickValues.lastIndex else it }
        )
    }

    ListItem(
        modifier = Modifier
            .clickable { showDialog = true },
        headlineContent = {
            Text(
                text = stringResource(R.string.set_refresh_interval_ms),
                style = MaterialTheme.typography.bodyLarge
            )
        },
        supportingContent = {
            Text(
                text = "${tickValues[sliderIndex]}",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    )

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(stringResource(R.string.set_refresh_interval_ms))
            },
            text = {
                Column {
                    Text(
                        text = stringResource(R.string.current_value, tickValues[sliderIndex]),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.oem_refresh_interval_limitation_description),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = stringResource(R.string.refresh_interval_battery_drain_description),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(16.dp))
                    Slider(
                        value = sliderIndex.toFloat(),
                        onValueChange = { pos ->
                            val idx = pos.roundToInt().coerceIn(0, tickValues.lastIndex)
                            sliderIndex = idx
                            onIntervalChange(tickValues[idx])
                        },
                        valueRange = 0f..(tickValues.lastIndex).toFloat(),
                        steps = tickValues.size - 2
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showDialog = false }) {
                    Text(stringResource(R.string.close))
                }
            },
            dismissButton = {
                Button(onClick = {
                    sliderIndex = defaultIndex
                    onIntervalChange(tickValues[defaultIndex])
                    showDialog = false
                }) {
                    Text(stringResource(R.string.reset))
                }
            }
        )
    }
}
