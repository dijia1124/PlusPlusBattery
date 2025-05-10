package com.example.plusplusbattery

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
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
import androidx.navigation.NavController
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.plusplusbattery.ui.components.AppScaffold

@Composable
fun Settings(currentTitle: String, navController: NavController, hasRoot: Boolean, batteryVM: BatteryInfoViewModel) {
    AppScaffold(currentTitle) {
        SettingsContent(navController, hasRoot, batteryVM)
    }
}

@Composable
fun SettingsContent(
    navController: NavController,
    hasRoot: Boolean,
    batteryVM: BatteryInfoViewModel
) {
    val isRootMode by batteryVM.isRootMode.collectAsState()
    val showOnDash by batteryVM.showSwitchOnDashboard.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .verticalScroll(scrollState)
    ) {
        ListItem(
            headlineContent = { Text(text = stringResource(R.string.use_root_mode), style = MaterialTheme.typography.bodyMedium) },
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
            headlineContent = { Text(text = stringResource(R.string.show_root_switch_on_dashboard), style = MaterialTheme.typography.bodyMedium) },
            trailingContent = {
                Switch(
                    checked = showOnDash,
                    onCheckedChange = { batteryVM.setShowSwitchOnDashboard(it) }
                )
            }
        )
        ListItem(
            modifier = Modifier.clickable { navController.navigate("about") },
            headlineContent = { Text(text = stringResource(R.string.about), style = MaterialTheme.typography.bodyMedium) }
        )
    }
}