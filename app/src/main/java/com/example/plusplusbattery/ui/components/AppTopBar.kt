package com.example.plusplusbattery.ui.components

import android.content.pm.PackageManager
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(title: String) {
    val isWatch = LocalContext.current.packageManager
        .hasSystemFeature(PackageManager.FEATURE_WATCH)

    TopAppBar(
        title = {
            if (isWatch) {
                Text(text = title, style = MaterialTheme.typography.titleSmall)
            }
            else {
                Text(text = title, style = MaterialTheme.typography.headlineSmall)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    )
}