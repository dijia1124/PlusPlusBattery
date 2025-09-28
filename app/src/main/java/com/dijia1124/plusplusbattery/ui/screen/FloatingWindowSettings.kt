package com.dijia1124.plusplusbattery.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
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
    val textColorKey by floatingWindowSettingsViewModel.floatingWindowTextColor.collectAsState()
    val backgroundColorKey by floatingWindowSettingsViewModel.floatingWindowBackgroundColor.collectAsState()
    val fontWeight by floatingWindowSettingsViewModel.floatingWindowFontWeight.collectAsState()

    val textColorOptions = mapOf(
        "auto" to stringResource(R.string.color_auto),
        "primary" to stringResource(R.string.color_primary),
        "secondary" to stringResource(R.string.color_secondary),
        "error" to stringResource(R.string.color_error)
    )

    val backgroundColorOptions = mapOf(
        "auto" to stringResource(R.string.color_auto),
        "primaryContainer" to stringResource(R.string.color_primary_container),
        "secondaryContainer" to stringResource(R.string.color_secondary_container),
        "errorContainer" to stringResource(R.string.color_error_container)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        ListItem(
            modifier = Modifier.padding(horizontal = 10.dp),
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
            modifier = Modifier.padding(horizontal = 10.dp),
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
            modifier = Modifier.padding(horizontal = 10.dp),
            headlineContent = { Text(text = stringResource(R.string.font_weight, fontWeight)) },
            supportingContent = {
                Slider(
                    value = fontWeight.toFloat(),
                    onValueChange = { newFontWeight ->
                        floatingWindowSettingsViewModel.setFloatingWindowFontWeight(newFontWeight.toInt())
                    },
                    valueRange = 100f..900f,
                    steps = 7
                )
            }
        )
        Text(
            text = stringResource(R.string.text_color),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 26.dp, top = 16.dp, end = 26.dp, bottom = 8.dp)
        )
        LazyRow(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 26.dp)
        ) {
            items(textColorOptions.toList()) { (key, name) ->
                val isSelected = key == textColorKey
                OutlinedCard(
                    onClick = { floatingWindowSettingsViewModel.setFloatingWindowTextColor(key) },
                    border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else CardDefaults.outlinedCardBorder(),
                    colors = if (isSelected) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else CardDefaults.outlinedCardColors(),
                ) {
                    Text(
                        text = name,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        Text(
            text = stringResource(R.string.background_color),
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(start = 26.dp, top = 16.dp, end = 26.dp, bottom = 8.dp)
        )
        LazyRow(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 26.dp)
        ) {
            items(backgroundColorOptions.toList()) { (key, name) ->
                val isSelected = key == backgroundColorKey
                OutlinedCard(
                    onClick = { floatingWindowSettingsViewModel.setFloatingWindowBackgroundColor(key) },
                    border = if (isSelected) BorderStroke(2.dp, MaterialTheme.colorScheme.primary) else CardDefaults.outlinedCardBorder(),
                    colors = if (isSelected) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer) else CardDefaults.outlinedCardColors(),
                ) {
                    Text(
                        text = name,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        ListItem(
            modifier = Modifier.padding(horizontal = 10.dp),
            headlineContent = { Text(text = stringResource(R.string.text_shadow)) },
            trailingContent = {
                val textShadowEnabled by floatingWindowSettingsViewModel.floatingWindowTextShadowEnabled.collectAsState()
                Switch(
                    checked = textShadowEnabled,
                    onCheckedChange = { floatingWindowSettingsViewModel.setFloatingWindowTextShadowEnabled(it) }
                )
            }
        )
        ListItem(
            modifier = Modifier.padding(horizontal = 10.dp),
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
            modifier = Modifier.padding(horizontal = 10.dp),
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
