package com.example.plusplusbattery

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun History(historyInfoViewModel: HistoryInfoViewModel) {
    Scaffold(
        topBar = {
            androidx.compose.material3.TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                ),
                title = {
                    Text(
                        text = stringResource(R.string.history),
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
            )
        },
        content = { innerPadding ->
            Column(modifier = Modifier.padding(top = innerPadding.calculateTopPadding())) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    HistoryContent(historyInfoViewModel)
                }
            }
        }
    )
}

@Composable
fun HistoryContent(historyInfoViewModel: HistoryInfoViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    )
    {
        Column() {
            HistoryInfoScreen(historyInfoViewModel)
        }
    }
}

@Composable
fun HistoryInfoScreen(historyInfoViewModel: HistoryInfoViewModel) {
    val historyInfos by historyInfoViewModel.allHistoryInfos.collectAsState(emptyList())
    Column (modifier = Modifier.padding(horizontal = 16.dp)){
        LazyColumn {
            items(historyInfos.size) { index ->

                OutlinedCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, Color.LightGray),
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val date = Date(historyInfos[historyInfos.size - index -1].date)
                        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                        val dateString = formatter.format(date)

                        Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                            Text(text = dateString, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = stringResource(R.string.cycle_counts) + historyInfos[historyInfos.size - index -1].cycleCount,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}