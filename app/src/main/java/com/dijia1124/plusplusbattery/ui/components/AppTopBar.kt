package com.dijia1124.plusplusbattery.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(title: String,
              navigationIcon: (@Composable () -> Unit)? = null,
              actions: @Composable RowScope.() -> Unit = {},) {
    TopAppBar(
        title = {
            Text(text = title, style = MaterialTheme.typography.headlineSmall)
        },
        navigationIcon = {
            // only show the navigation icon if it's not null
            navigationIcon?.invoke()
        },
        actions = actions,
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}