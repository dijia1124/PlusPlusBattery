package com.example.plusplusbattery

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.TextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Info
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.rememberCoroutineScope
import com.example.plusplusbattery.ui.components.AppScaffold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val BATTERY_INFO_LIST_ROOT_SIZE = 19

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dashboard(hasRoot: Boolean, currentTitle: String, batteryInfoViewModel: BatteryInfoViewModel) {
    AppScaffold(currentTitle) {
        DashBoardContent(hasRoot, batteryInfoViewModel)
    }
}

@Composable
fun NormalBatteryCard(info: BatteryInfo) {
    Column(modifier = Modifier.padding(horizontal = 4.dp)) {
        Text(text = info.title, style = MaterialTheme.typography.bodyMedium)
        Text(
            text = info.value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun BatteryCardWithCalibration(
    info: BatteryInfo,
    isDualBatt: Boolean,
    isRootMode: Boolean,
    context: Context,
    onToggleDualBat: () -> Unit,
    onShowMultiplierDialog: () -> Unit
) {
    Row {
        Column(modifier = Modifier.padding(horizontal = 4.dp)) {
            Text(text = info.title, style = MaterialTheme.typography.bodyMedium)
            Text(text = info.value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
        if (isRootMode) {
            Spacer(modifier = Modifier.weight(1f))
        }
        IconButton(onClick = onShowMultiplierDialog, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Create, contentDescription = "Calibrate", modifier = Modifier.size(18.dp))
        }
        if (!isRootMode) {
            Spacer(modifier = Modifier.weight(1f))
            Row {
                Column {
                    Text(text = stringResource(R.string.dual_battery), style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = getBoolString(isDualBatt, context),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onToggleDualBat, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Create, contentDescription = "Toggle Dual Battery", modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
fun BatteryCardWithCoeffTable(
    info: BatteryInfo,
    onShowInfo: () -> Unit
) {
    Row {
        Column(modifier = Modifier.padding(horizontal = 4.dp)) {
            Text(text = info.title, style = MaterialTheme.typography.bodyMedium)
            Text(text = info.value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onShowInfo, modifier = Modifier.size(36.dp)) {
            Icon(Icons.Default.Info, contentDescription = "Show TermCoeff Table", modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
fun CoeffTableDialog(infoText: String, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.fcc_soh_offset_table)) },
        text = {
            Text(
                text = infoText,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@Composable
fun DashBoardContent(hasRoot: Boolean, batteryInfoViewModel: BatteryInfoViewModel) {
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isRootMode by batteryInfoViewModel.isRootMode.collectAsState()
    val isMultiply by batteryInfoViewModel.isMultiply.collectAsState()
    val isDualBatt by batteryInfoViewModel.isDualBatt.collectAsState()
    val selectedMagnitude by batteryInfoViewModel.selectedMagnitude.collectAsState()
    val showSwitch by batteryInfoViewModel.showSwitchOnDashboard.collectAsState()
    var showCoeffDialog by remember { mutableStateOf(false) }
    var showMultiplierDialog by remember { mutableStateOf(false) }
    var coeffDialogText by remember { mutableStateOf(context.getString(R.string.unknown)) }
    val batteryInfoList = remember { mutableStateListOf<BatteryInfo>() }
    var lastSize by remember { mutableStateOf(BATTERY_INFO_LIST_ROOT_SIZE) }

    LaunchedEffect(isRootMode, hasRoot) {
        if (!hasRoot && isRootMode) {
            batteryInfoViewModel.setRootMode(false)
        }
        while (true) {
            val basicList = batteryInfoViewModel.refreshBatteryInfo()
            val displayList  = mutableListOf<BatteryInfo>().apply { addAll(basicList) }

            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))

            intent?.let {
                if (isRootMode) {
                    val rootList = batteryInfoViewModel.refreshBatteryInfoWithRoot()
                    displayList.addAll(rootList)
                }
                else {
                    // use system battery manager api if root access is not available
                    val nonRootVCPList = batteryInfoViewModel.refreshNonRootVoltCurrPwr()
                    displayList.addAll(nonRootVCPList)
                    val fccInfo = batteryInfoViewModel.getEstimatedFcc()
                    displayList.add(fccInfo)
                    lastSize = displayList.size
                }
                batteryInfoList.clear()
                batteryInfoList.addAll(displayList)
                // scroll to bottom if root mode is enabled
                if (isRootMode &&
                    batteryInfoList.size == BATTERY_INFO_LIST_ROOT_SIZE &&
                    batteryInfoList.size != lastSize
                ) {
                    listState.scrollToItem(batteryInfoList.lastIndex)
                }
                lastSize = batteryInfoList.size
            }
            delay(1000)
        }
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
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
                        when (index) {
                            6 -> BatteryCardWithCalibration(
                                info = info,
                                isDualBatt = isDualBatt,
                                isRootMode = isRootMode,
                                context = context,
                                onToggleDualBat = { batteryInfoViewModel.setDualBat(!isDualBatt) },
                                onShowMultiplierDialog = { showMultiplierDialog = true }
                            )
                            10 -> BatteryCardWithCoeffTable(
                                info = info,
                                onShowInfo = {
                                    coroutineScope.launch{
                                        val list = readTermCoeff(context)
                                        coeffDialogText = buildString {
                                            append(context.getString(R.string.raw_fcc_soh_calc_intro))
                                            append(context.getString(R.string.vbatuv_mv_fccoffset_mah_sohoffset))
                                            list.forEach {
                                                append("${it.first}, ${it.second}, ${it.third}\n")
                                            }
                                            if (list.isEmpty()) {
                                                append(context.getString(R.string.offset_table_not_found))
                                            }
                                        }
                                        showCoeffDialog = true
                                    }
                                }
                            )
                            else -> NormalBatteryCard(info)
                        }
                    }
                }
            }
        }
        if (showSwitch){
            RootSwitch(hasRoot, isRootMode , context, onToggle = {
                batteryInfoViewModel.setRootMode(it)
            })
        }
    }

    if (showCoeffDialog) {
        CoeffTableDialog(infoText = coeffDialogText) {
            showCoeffDialog = false
        }
    }

    if (showMultiplierDialog) {
        AlertDialog(
            onDismissRequest = { showMultiplierDialog = false },
            title = { Text(stringResource(R.string.calibrate_via_multiplier)) },
            text = {
                MultiplierSelector(
                    isMultiply = isMultiply,
                    onMultiplyChange = { batteryInfoViewModel.setMultiplierPrefs(it, selectedMagnitude) },
                    selectedMagnitude = selectedMagnitude,
                    onMagnitudeChange = { batteryInfoViewModel.setMultiplierPrefs(isMultiply, it) }
                )
            },
            confirmButton = {
                Button(onClick = { showMultiplierDialog = false }) {
                    Text(stringResource(R.string.close))
                }
            },
            dismissButton = {
                Button(onClick = {
                    batteryInfoViewModel.setMultiplierPrefs(true, 0)
                    showMultiplierDialog = false
                }) {
                    Text(stringResource(R.string.reset))
                }

            }
        )
    }
}

@Composable
fun RootSwitch(hasRoot: Boolean, isRootMode: Boolean, context: Context, onToggle: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        OutlinedCard(
            modifier = Modifier
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, Color.LightGray),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.use_root_mode),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = isRootMode,
                    onCheckedChange = { desired ->
                        if (desired) {
                            if (hasRoot) {
                                onToggle(true)
                            } else {
                                Toast.makeText(
                                    context,
                                    context.getString(R.string.root_access_denied),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            onToggle(false)
                        }
                    }
                )
            }
        }
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

suspend fun safeRootReadInt(
    context: Context,
    path: String,
    index: Int,
    fallback: () -> Int,
    onFallback: () -> Unit
): Int = withContext(Dispatchers.IO) {
    try {
        val valueStr = readBatteryInfo(path, index)
        val parsed = valueStr?.toIntOrNull()
        if (parsed != null) {
            parsed
        } else {
            onFallback()
            fallback()
        }
    } catch (e: Exception) {
        onFallback()
        fallback()
    }
}