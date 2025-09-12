package com.dijia1124.plusplusbattery.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import com.dijia1124.plusplusbattery.vm.HistoryInfoViewModel
import com.dijia1124.plusplusbattery.R
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.dijia1124.plusplusbattery.ui.components.AppScaffold
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun History(historyInfoViewModel: HistoryInfoViewModel, currentTitle: String) {
    var showHelpDialog by remember { mutableStateOf(false) }

    AppScaffold(
        title = currentTitle,
        actions = {
            IconButton(onClick = { showHelpDialog = true }) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "Help"
                )
            }
        }
    ) {
        HistoryInfoScreen(historyInfoViewModel)
    }

    if (showHelpDialog) {
        AlertDialog(
            onDismissRequest = { showHelpDialog = false },
            title = { Text(text = stringResource(R.string.history)) },
            text = {
                Text(
                    text = stringResource(R.string.workmanager_for_charge_cycle)
                )
            },
            confirmButton = {
                Button(onClick = { showHelpDialog = false }) {
                    Text("OK")
                }
            }
        )
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