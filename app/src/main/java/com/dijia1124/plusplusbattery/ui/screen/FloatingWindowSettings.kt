package com.dijia1124.plusplusbattery.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ListItem
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
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
import com.dijia1124.plusplusbattery.vm.FloatingWindowSettingsViewModel

@Composable
fun FloatingWindowSettings(
    currentTitle: String,
    navController: NavController,
    floatingWindowSettingsViewModel: FloatingWindowSettingsViewModel
) {
    AppScaffold(
        currentTitle,
        navigationIcon = { BackIcon(navController) }
    ) {
        FloatingWindowSettingsContent(floatingWindowSettingsViewModel)
    }
}

@Composable
fun FloatingWindowSettingsContent(floatingWindowSettingsViewModel: FloatingWindowSettingsViewModel) {
    val alpha by floatingWindowSettingsViewModel.floatingWindowAlpha.collectAsState()
    val size by floatingWindowSettingsViewModel.floatingWindowSize.collectAsState()
    val touchable by floatingWindowSettingsViewModel.floatingWindowTouchable.collectAsState()
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp)
            .verticalScroll(rememberScrollState())
    ) {
        ListItem(
            headlineContent = { Text(text = stringResource(
                R.string.opacity,
                String.format("%.2f", alpha)
            )) },
            supportingContent = {
                Slider(
                    value = alpha,
                    onValueChange = { newAlpha ->
                        floatingWindowSettingsViewModel.setFloatingWindowAlpha(newAlpha)
                    },
                    valueRange = 0.0f..1.0f
                )
            }
        )
        ListItem(
            headlineContent = { Text(text = stringResource(
                R.string.size,
                String.format("%.2f", size)
            )) },
            supportingContent = {
                Slider(
                    value = size,
                    onValueChange = { newSize ->
                        floatingWindowSettingsViewModel.setFloatingWindowSize(newSize)
                    },
                    valueRange = 0.5f..2.0f
                )
            }
        )
        ListItem(
            headlineContent = { Text(text = stringResource(R.string.enable_touch)) },
            trailingContent = {
                Switch(
                    checked = touchable,
                    onCheckedChange = { newTouchable ->
                        floatingWindowSettingsViewModel.setFloatingWindowTouchable(newTouchable)
                    }
                )
            }
        )
        ListItem(
            headlineContent = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { floatingWindowSettingsViewModel.resetFloatingWindowSettings() }) {
                        Text(text = stringResource(R.string.reset))
                    }
                }
            }
        )
    }
}
