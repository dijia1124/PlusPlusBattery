package com.example.plusplusbattery.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.plusplusbattery.R
import com.example.plusplusbattery.ui.components.AppScaffold


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun About(currentTitle: String) {
    AppScaffold(currentTitle) {
        AboutScreen()
    }
}

@Composable
fun AboutScreen() {
    val context = LocalContext.current
    val packageInfo = context.packageManager.getPackageInfo(context.packageName, 0)
    val versionName = packageInfo.versionName
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(stringResource(R.string.version, versionName.toString()))
        Text(stringResource(R.string.instructions_content).trimIndent())
    }
}
