package com.example.plusplusbattery

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material.Scaffold
import androidx.compose.material3.TextField
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.RadioButton
import kotlin.math.pow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


@Composable
fun Dashboard(historyInfoViewModel: HistoryInfoViewModel, hasRoot: Boolean) {
    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = MaterialTheme.colorScheme.surface,
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
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    DashboardContent(historyInfoViewModel, hasRoot)
                }
            }
        }
    )
}

@Composable
fun DashboardContent(historyInfoViewModel: HistoryInfoViewModel, hasRoot: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    )
    {
        Column() {
            BatteryInfoUpdater(historyInfoViewModel, hasRoot)
        }
    }
}
@Composable
fun BatteryInfoUpdater(historyInfoViewModel: HistoryInfoViewModel, hasRoot: Boolean) {
    val listState = rememberLazyListState()
    var isRootMode by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val batteryManager = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
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
    val dualBatFlow = remember {
        context.dataStore.data.map { prefs -> prefs[DUAL_BATTERY_KEY] ?: false }
    }
    val isDualBat by dualBatFlow.collectAsState(initial = false)

    val scope = rememberCoroutineScope()

    fun setDualBat(newVal: Boolean) {
        scope.launch {
            context.dataStore.edit { prefs ->
                prefs[DUAL_BATTERY_KEY] = newVal
            }
        }
    }

    val dualBatMultiplier = if (isDualBat) 2 else 1

    val multiplyFlow = remember { context.dataStore.data.map { prefs -> prefs[MULTIPLY_KEY] != false } }
    val savedIsMultiply by multiplyFlow.collectAsState(initial = true)
    val magnitudeFlow = remember { context.dataStore.data.map { prefs -> prefs[MULTIPLIER_MAGNITUDE_KEY] ?: 0 } }
    val savedMagnitude by magnitudeFlow.collectAsState(initial = 0)

    var showMultiplierDialog by remember { mutableStateOf(false) }

    val isMultiply = savedIsMultiply
    val selectedMagnitude = savedMagnitude

    fun setMultiplierPrefs(isMultiplyNew: Boolean, magnitudeNew: Int) {
        scope.launch {
            context.dataStore.edit { prefs ->
                prefs[MULTIPLY_KEY] = isMultiplyNew
                prefs[MULTIPLIER_MAGNITUDE_KEY] = magnitudeNew
            }
        }
    }

    val calibMultiplier = if (isMultiply)
        10.0.pow(selectedMagnitude.toDouble())
    else
        1 / 10.0.pow(selectedMagnitude.toDouble())
    
    var rm by remember {mutableStateOf(context.getString(R.string.unknown))}
    var fcc by remember {mutableStateOf(context.getString(R.string.unknown))}
    var soh by remember {mutableStateOf(context.getString(R.string.unknown))}
    var vbatUv by remember {mutableStateOf(context.getString(R.string.unknown))}
    var sn by remember { mutableStateOf(context.getString(R.string.unknown)) }
    var batManDate by remember { mutableStateOf(context.getString(R.string.unknown)) }
    var rawSoh by remember { mutableStateOf(context.getString(R.string.unknown)) }
    var rawFcc by remember { mutableStateOf(context.getString(R.string.unknown)) }

        LaunchedEffect(isRootMode) {
            scope.launch {
                historyInfoViewModel.insertOrUpdateHistoryInfo(historyInfo)
                if (isRootMode) {
                    withContext(Dispatchers.IO) {
                        rm = readBatteryInfo("battery_rm", context)
                        fcc = readBatteryInfo("battery_fcc", context)
                        soh = readBatteryInfo("battery_soh", context)
                        vbatUv = readBatteryInfo("vbat_uv", context)
                        sn = readBatteryInfo("battery_sn", context)
                        batManDate = readBatteryInfo("battery_manu_date", context)
                        rawSoh = calcRawSoh(soh.toInt(),vbatUv.toInt(),readTermCoeff()).toString()
                        rawFcc = calcRawFcc(fcc.toInt(),rawSoh.toFloat(),vbatUv.toInt(),readTermCoeff()).toString()
                    }
                }
            }
        }

    val batteryInfoList = remember { mutableStateListOf<BatteryInfo>() }

    LaunchedEffect(dualBatMultiplier, calibMultiplier) {
        while (true) {
            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

            intent?.let {
                batteryInfoList.clear()
                batteryInfoList.addAll(
                    listOf(
                        BatteryInfo(context.getString(R.string.battery_level), "${it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)}%"),
                        BatteryInfo(context.getString(R.string.battery_temperature), "${it.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) / 10}°C"),
                        BatteryInfo(context.getString(R.string.battery_status), getStatusString(it.getIntExtra(BatteryManager.EXTRA_STATUS, -1), context)),
                        BatteryInfo(context.getString(R.string.battery_health), getHealthString(it.getIntExtra(BatteryManager.EXTRA_HEALTH, 0), context)),
                        BatteryInfo(context.getString(R.string.battery_cycle_count), "${it.getIntExtra(BatteryManager.EXTRA_CYCLE_COUNT, -1)}"),
                        BatteryInfo(context.getString(R.string.battery_voltage), "${it.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)} mV"),
                        BatteryInfo(context.getString(R.string.battery_current), "${batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) * dualBatMultiplier * calibMultiplier} mA"),
                        BatteryInfo(context.getString(R.string.power),
                            String.format("%.2f W",(batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW) * dualBatMultiplier * calibMultiplier * it.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0) / 1000000.0))
                        ),

                    )
                )
                if (isRootMode) {
                    batteryInfoList.addAll(
                        listOf(
                            BatteryInfo(context.getString(R.string.remaining_charge_counter), "$rm mAh"),
                            BatteryInfo(context.getString(R.string.full_charge_capacity_battery_fcc), "$fcc mAh"),
                            BatteryInfo(context.getString(R.string.raw_full_charge_capacity_before_compensation), "$rawFcc mAh"),
                            BatteryInfo(context.getString(R.string.battery_health_battery_soh), "$soh %"),
                            BatteryInfo(context.getString(R.string.raw_battery_health_before_compensation), "$rawSoh %"),
                            BatteryInfo(context.getString(R.string.battery_under_voltage_threshold_vbat_uv), "$vbatUv mV"),
                            BatteryInfo(context.getString(R.string.battery_serial_number_battery_sn), sn),
                            BatteryInfo(context.getString(R.string.battery_manufacture_date_battery_manu_date), batManDate),
                        )
                    )
                }
                else {

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

            }

            delay(1000)
        }
    }


    LaunchedEffect(isRootMode, batteryInfoList.size) {
        if (isRootMode) {
            scope.launch {
                listState.animateScrollToItem(index = batteryInfoList.size - 1)
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        LazyColumn (
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ){
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
                        if (index == 6) {
                            Row {
                                Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                                    Text(text = info.title, style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        text = info.value,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                IconButton(
                                    onClick = { showMultiplierDialog = true
                                        },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Create,
                                        contentDescription = "Calibrate Battery Current",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.weight(1f))
                                    Row {
                                        Column {
                                            Text(text = stringResource(R.string.dual_battery), style = MaterialTheme.typography.bodyMedium)
                                            Text(text = getBoolString(isDualBat, context),
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Bold)
                                        }
                                        IconButton(
                                            onClick = { setDualBat(!isDualBat) },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Create,
                                                contentDescription = "Switch On/Off Dual Battery",
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                            }
                        }
                        else {
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
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { if (hasRoot) {
                isRootMode = !isRootMode}
                else {
                    Toast.makeText(context,
                        context.getString(R.string.root_access_denied), Toast.LENGTH_SHORT).show()
            }
            }) {
            Text(if (isRootMode) stringResource(R.string.use_basic_mode) else stringResource(R.string.use_root_mode))
        }

    }

    if (showMultiplierDialog) {
        AlertDialog(
            onDismissRequest = { showMultiplierDialog = false },
            title = { Text(stringResource(R.string.calibrate_via_multiplier)) },
            text = {
                MultiplierSelector(
                    isMultiply = isMultiply,
                    onMultiplyChange = { setMultiplierPrefs(it, selectedMagnitude) },
                    selectedMagnitude = selectedMagnitude,
                    onMagnitudeChange = { setMultiplierPrefs(isMultiply, it) }
                )
            },
            confirmButton = {
                Button(onClick = { showMultiplierDialog = false }) {
                    Text(stringResource(R.string.close))
                }
            },
            dismissButton = {
                Button(onClick = {
                    setMultiplierPrefs(true, 0)
                    showMultiplierDialog = false
                }) {
                    Text(stringResource(R.string.reset))
                }

            }
        )
    }


}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MultiplierSelector(
    isMultiply: Boolean,
    onMultiplyChange: (Boolean) -> Unit,
    selectedMagnitude: Int,
    onMagnitudeChange: (Int) -> Unit
) {
    val magnitudeOptions = listOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9) // 10, 100, 1000, 10000 etc
    val label = if (isMultiply) stringResource(R.string.multiply) else stringResource(R.string.divide)
    var isExpanded by remember { mutableStateOf(false) }

    Column {
        // Multiply/Divide RadioButton
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(selected = isMultiply, onClick = { onMultiplyChange(true) })
            Text(stringResource(R.string.multiply))
            Spacer(Modifier.width(16.dp))
            RadioButton(selected = !isMultiply, onClick = { onMultiplyChange(false) })
            Text(stringResource(R.string.divide))
        }
        Spacer(modifier = Modifier.height(12.dp))

        // Choose multiplier in dropdown menu
        ExposedDropdownMenuBox(
            expanded = isExpanded,
            onExpandedChange = { isExpanded = it }
        ) {
            TextField(
                modifier = Modifier
                    .menuAnchor()
                    .fillMaxWidth(),
                readOnly = true,
                value = "10^$selectedMagnitude",
                onValueChange = {},
                label = { Text(stringResource(R.string.multiplier, label)) },
                trailingIcon = {
                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = isExpanded)
                },
            )
            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false }
            ) {
                magnitudeOptions.forEach { option ->
                    DropdownMenuItem(
                        text = { Text("10^$option") },
                        onClick = {
                            onMagnitudeChange(option)
                            isExpanded = false
                        },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                    )
                }
            }
        }
    }
}

