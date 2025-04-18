package com.example.plusplusbattery

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp


@Composable
fun About() {
    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colorScheme.surfaceContainer,
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),

                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = stringResource(R.string.about),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
        },
        content = { innerPadding ->
            Column(modifier = Modifier
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    AboutScreen()
                }
            }
        }
    )
}

@Composable
fun AboutScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(stringResource(R.string.instructions_content).trimIndent())
    }
}
