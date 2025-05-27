package com.dijia1124.plusplusbattery.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun AppScaffold(
    title: String,
    content: @Composable () -> Unit
) {
    Scaffold(
        topBar = { AppTopBar(title) },
        content = { innerPadding ->
            Column(
                modifier = Modifier.padding(top = innerPadding.calculateTopPadding())
            ) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    content()
                }
            }
        }
    )
}