package com.example.plusplusbattery

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Scaffold
import androidx.compose.material.TopAppBar
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun Dashboard(historyInfoViewModel: HistoryInfoViewModel) {
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
                        text = stringResource(R.string.plus_plus_battery),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
            }
        },
        content = { innerPadding ->
            Column(modifier = Modifier.padding(innerPadding)) {
                // add a tab row
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    DashboardContent(historyInfoViewModel)
                }
            }
        }
    )
}

@Composable
fun DashboardContent(historyInfoViewModel: HistoryInfoViewModel) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    )
    {
        Column() {
            BatteryInfoUpdater(historyInfoViewModel)
        }
    }
}
@Composable
fun BatteryInfoUpdater(historyInfoViewModel: HistoryInfoViewModel) {
    val context = LocalContext.current
    val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
    val historyInfos = historyInfoViewModel.allHistoryInfos.collectAsState(emptyList())
    val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    val cycleCount = intent?.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, -1) ?: -1

    val currentTimestamp = System.currentTimeMillis()
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val dateString = formatter.format(Date(currentTimestamp))

    val historyInfo = HistoryInfo(
        date = currentTimestamp,
        dateString = dateString,
        cycleCount = cycleCount.toString()
    )
    val exists = historyInfoViewModel.existsHistoryInfo(dateString)
    if (!exists) {
        historyInfoViewModel.insertHistoryInfo(historyInfo)
    }


    val batteryInfoList = remember { mutableStateListOf<BatteryInfo>() }

    LaunchedEffect(Unit) {
        while (true) {
            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

            intent?.let {
                batteryInfoList.clear()
                batteryInfoList.addAll(
                    listOf(
                        BatteryInfo(context.getString(R.string.battery_level), "${it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)}%"),
                        BatteryInfo(context.getString(R.string.battery_temperature), "${it.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10}°C"),
                        BatteryInfo(context.getString(R.string.battery_voltage), "${it.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)} mV"),
                        BatteryInfo(context.getString(R.string.battery_status), getStatusString(it.getIntExtra(BatteryManager.EXTRA_STATUS, -1), context)),
                        BatteryInfo(context.getString(R.string.battery_health), getHealthString(it.getIntExtra(BatteryManager.EXTRA_HEALTH, 0), context)),
                        BatteryInfo(context.getString(R.string.battery_cycle_count), "${it.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, -1)}"),
                        BatteryInfo(context.getString(R.string.battery_current), "${batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)} mA")
                    )
                )

                val currentNow = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
                val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

                val fullChargeCapacityText = if (currentNow == 0 && batteryLevel == 100) {
                    val chargeCounter = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER)
                    val fullChargeCapacity = if (batteryLevel > 0)
                        (chargeCounter / (batteryLevel / 100.0)).toInt() / 1000 else -1
                    if (fullChargeCapacity != -1)
                        "$fullChargeCapacity mAh"
                    else
                        context.getString(R.string.full_charge_capacity_unavailable)
                } else {
                    if (currentNow != 0)
                        context.getString(R.string.estimating_full_charge_capacity)
                    else
                        context.getString(R.string.capacity_unavailable)
                }

                batteryInfoList.add(BatteryInfo(
                    context.getString(R.string.full_charge_capacity),
                    fullChargeCapacityText.toString()
                ))
            }

            delay(1000)
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {

        LazyColumn {
            items(batteryInfoList.size) { index ->
                val info = batteryInfoList[index]

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
                        Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                            Text(text = info.title, style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = info.value,
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

private fun getStatusString(status: Int, context: Context): String = when (status) {
    BatteryManager.BATTERY_STATUS_CHARGING -> context.getString(R.string.charging)
    BatteryManager.BATTERY_STATUS_DISCHARGING -> context.getString(R.string.discharging)
    BatteryManager.BATTERY_STATUS_FULL -> context.getString(R.string.full)
    else -> "Not Charging"
}

private fun getHealthString(health: Int, context: Context): String = when (health) {
    BatteryManager.BATTERY_HEALTH_GOOD -> context.getString(R.string.good)
    BatteryManager.BATTERY_HEALTH_OVERHEAT -> context.getString(R.string.overheat)
    BatteryManager.BATTERY_HEALTH_DEAD -> context.getString(R.string.dead)
    else -> "Unknown"
}

