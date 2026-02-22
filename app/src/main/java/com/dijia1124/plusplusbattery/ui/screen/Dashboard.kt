package com.dijia1124.plusplusbattery.ui.screen

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.TextField
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import com.dijia1124.plusplusbattery.data.model.BatteryInfo
import com.dijia1124.plusplusbattery.vm.BatteryInfoViewModel
import com.dijia1124.plusplusbattery.R
import com.dijia1124.plusplusbattery.data.model.BatteryInfoType
import com.dijia1124.plusplusbattery.data.model.CustomEntry
import com.dijia1124.plusplusbattery.data.util.getBoolString
import com.dijia1124.plusplusbattery.data.util.readTermCoeff
import com.dijia1124.plusplusbattery.ui.components.AppScaffold
import com.dijia1124.plusplusbattery.ui.components.CardWithPowerChart
import com.dijia1124.plusplusbattery.ui.components.PowerDataPoint
import com.dijia1124.plusplusbattery.ui.components.getListItemShape
import com.dijia1124.plusplusbattery.ui.components.showRootDeniedToast
import com.dijia1124.plusplusbattery.vm.SettingsViewModel
import kotlinx.coroutines.launch
import java.text.NumberFormat

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Dashboard(hasRoot: Boolean, currentTitle: String, batteryInfoViewModel: BatteryInfoViewModel, settingsViewModel: SettingsViewModel) {
    AppScaffold(title = currentTitle, isTopLevel = true)
    {
        DashBoardContent(hasRoot, batteryInfoViewModel, settingsViewModel)
    }
}

@Composable
fun NormalBatteryCard(info: BatteryInfo) {
    Column(modifier = Modifier.padding(horizontal = 4.dp)) {
        Text(text = info.customTitle ?: stringResource(info.type.titleRes), style = MaterialTheme.typography.bodyMedium)
        Text(
            text = info.value,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold
        )
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BatteryCardWithButton(
    info: BatteryInfo,
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
        ) {
            Text(
                text = info.customTitle ?: stringResource(info.type.titleRes),
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = info.value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
        Box(modifier = Modifier.padding(horizontal = 4.dp)) {
            FilledTonalIconButton(
                onClick = onClick,
                modifier = Modifier.size(36.dp),
                shapes = IconButtonDefaults.shapes()
            ) {
                Icon(icon, contentDescription = contentDescription, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
fun BatteryCardWithInfo(
    info: BatteryInfo,
    onShowInfo: () -> Unit
) {
    BatteryCardWithButton(
        info = info,
        icon = ImageVector.vectorResource(id = R.drawable.info_24dp_1f1f1f_fill0_wght400_grad0_opsz24),
        contentDescription = "Show Info",
        onClick = onShowInfo
    )
}

@Composable
fun BatteryCardWithSwap(
    info: BatteryInfo,
    onSwap: () -> Unit
) {
    BatteryCardWithButton(
        info = info,
        icon = ImageVector.vectorResource(id = R.drawable.swap_horiz_24dp_1f1f1f_fill0_wght400_grad0_opsz24),
        contentDescription = "Swap Unit",
        onClick = onSwap
    )
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BatteryCardWithCalibration(
    info: BatteryInfo,
    isDualBatt: Boolean,
    isRootMode: Boolean,
    context: Context,
    onToggleDualBat: () -> Unit,
    onShowMultiplierDialog: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.padding(horizontal = 4.dp)) {
            Text(text = stringResource(id = info.type.titleRes), style = MaterialTheme.typography.bodyMedium)
            Text(text = info.value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
        if (isRootMode) {
            Spacer(modifier = Modifier.weight(1f))
        }
        Box(modifier = Modifier.padding(horizontal = 4.dp)) {
            FilledTonalIconButton(
                onClick = onShowMultiplierDialog,
                modifier = Modifier.size(36.dp),
                shapes = IconButtonDefaults.shapes()
            ) {
                Icon(
                    ImageVector.vectorResource(id = R.drawable.edit_24dp_1f1f1f_fill0_wght400_grad0_opsz24),
                    contentDescription = "Calibrate",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        if (!isRootMode) {
            Spacer(modifier = Modifier.weight(1f))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(modifier = Modifier.padding(horizontal = 4.dp)) {
                    Text(text = stringResource(R.string.dual_cell), style = MaterialTheme.typography.bodyMedium)
                    Text(
                        text = getBoolString(isDualBatt, context),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                Box(modifier = Modifier.padding(horizontal = 4.dp)) {
                    FilledTonalIconButton(
                        onClick = onToggleDualBat,
                        modifier = Modifier.size(36.dp),
                        shapes = IconButtonDefaults.shapes()
                    ) {
                        Icon(
                            ImageVector.vectorResource(id = R.drawable.swap_horiz_24dp_1f1f1f_fill0_wght400_grad0_opsz24),
                            contentDescription = "Toggle Dual Battery",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun BatteryCardWithCoeffTable(
    info: BatteryInfo,
    onShowInfo: () -> Unit
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.padding(horizontal = 4.dp)) {
            Text(text = stringResource(id = info.type.titleRes), style = MaterialTheme.typography.bodyMedium)
            Text(text = info.value, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Bold)
        }
        Spacer(modifier = Modifier.weight(1f))
        Box(modifier = Modifier.padding(horizontal = 4.dp)) {
            FilledTonalIconButton(
                onClick = onShowInfo,
                modifier = Modifier.size(36.dp),
                shapes = IconButtonDefaults.shapes()
            ) {
                Icon(
                    ImageVector.vectorResource(id = R.drawable.info_24dp_1f1f1f_fill0_wght400_grad0_opsz24),
                    contentDescription = "Show TermCoeff Table",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

@Composable
fun CycleCountDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.battery_cycle_count)) },
        text = {
            Text(
                text = stringResource(R.string.battery_cycle_count_info),
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
fun EstFccInfoDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.estimated_fcc_title)) },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Text(
                    text = stringResource(R.string.estimated_fcc_info),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        },
        confirmButton = {
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        }
    )
}

@Composable
fun WrongVoltageDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.battery_voltage)) },
        text = {
            Text(
                text = stringResource(R.string.wrong_battery_voltage_info),
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
fun DashBoardContent(hasRoot: Boolean, batteryInfoViewModel: BatteryInfoViewModel, settingsViewModel: SettingsViewModel) {
    val listState = rememberLazyListState()
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isRootMode by batteryInfoViewModel.isRootMode.collectAsState()
    val isMultiply by batteryInfoViewModel.isMultiply.collectAsState()
    val isDualBatt by batteryInfoViewModel.isDualBatt.collectAsState()
    val refreshInterval by settingsViewModel.refreshInterval.collectAsState()
    val isPowerChartExpanded by settingsViewModel.isPowerChartExpanded.collectAsState()
    val selectedMagnitude by batteryInfoViewModel.selectedMagnitude.collectAsState()
    val showSwitch by batteryInfoViewModel.showSwitchOnDashboard.collectAsState()
    val isCelsius by settingsViewModel.isCelsius.collectAsState()
    var showCoeffDialog by remember { mutableStateOf(false) }
    var showMultiplierDialog by remember { mutableStateOf(false) }
    var showCycleCountDialog by remember { mutableStateOf(false) }
    var showEstFccDialog by remember { mutableStateOf(false) }
    var showWrongVoltageDialog by remember { mutableStateOf(false) }
    var coeffDialogText by remember { mutableStateOf(context.getString(R.string.unknown)) }
    val batteryInfoList = remember { mutableStateListOf<BatteryInfo>() }
    val lifecycleOwner = LocalLifecycleOwner.current
    val powerDataPoints = remember { mutableStateListOf<PowerDataPoint>() }
    var chartStartTime by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(isRootMode, hasRoot, lifecycleOwner, isCelsius) {
        if (!hasRoot && isRootMode) {
            batteryInfoViewModel.setRootMode(false)
        }

        lifecycleOwner.lifecycle
            .repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Reset power chart data when entering foreground
                powerDataPoints.clear()
                chartStartTime = System.currentTimeMillis()

                while (true) {
                    val displayList = batteryInfoViewModel.getDisplayBatteryInfo()

                    // Collect power data for chart
                    collectPowerDataForChart(displayList, powerDataPoints, chartStartTime)

                    batteryInfoList.clear()
                    batteryInfoList.addAll(displayList)
                    delay(refreshInterval.toLong())
                }
            }
    }

    LaunchedEffect(Unit) {
        batteryInfoViewModel.saveCycleCount()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.padding(horizontal = 8.dp)) {
            LazyColumn (
                state = listState,
                modifier = Modifier.fillMaxWidth()
            ){
                items(batteryInfoList.size) { index ->
                    val info = batteryInfoList[index]
                    val cardShape = getListItemShape(index = index, size = batteryInfoList.size)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 1.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        shape = cardShape
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            when (batteryInfoList[index].type) {
                                BatteryInfoType.CYCLE_COUNT -> BatteryCardWithInfo(
                                    info = info,
                                    onShowInfo = { showCycleCountDialog = true}
                                )
                                BatteryInfoType.TEMP -> BatteryCardWithSwap(
                                    info = info,
                                    onSwap = {
                                        settingsViewModel.setIsCelsius(!isCelsius)
                                    }
                                )
                                BatteryInfoType.POWER -> CardWithPowerChart(
                                    info = info,
                                    powerData = powerDataPoints.toList(),
                                    onResetData = {
                                        powerDataPoints.clear()
                                        chartStartTime = System.currentTimeMillis()
                                    },
                                    isExpanded = isPowerChartExpanded,
                                    isCelsius = isCelsius,
                                    onChartExpand = {
                                        settingsViewModel.setPowerChartExpanded(!isPowerChartExpanded)
                                    }
                                )
                                BatteryInfoType.CURRENT -> BatteryCardWithCalibration(
                                    info = info,
                                    isDualBatt = isDualBatt,
                                    isRootMode = isRootMode,
                                    context = context,
                                    onToggleDualBat = { batteryInfoViewModel.setDualBatt(!isDualBatt) },
                                    onShowMultiplierDialog = { showMultiplierDialog = true }
                                )
                                BatteryInfoType.VOLTAGE -> BatteryCardWithInfo(
                                    info = info,
                                    onShowInfo = { showWrongVoltageDialog = true }
                                )
                                BatteryInfoType.OPLUS_RAW_FCC, BatteryInfoType.OPLUS_RAW_SOH -> BatteryCardWithCoeffTable(
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
                                                append(context.getString(R.string.unknown_due_to_fcc_equals_design_capacity))
                                            }
                                            showCoeffDialog = true
                                        }
                                    }
                                )
                                BatteryInfoType.EST_FCC -> BatteryCardWithInfo(
                                    info = info,
                                    onShowInfo = { showEstFccDialog = true }
                                )
                                else -> NormalBatteryCard(info)
                            }
                        }
                    }
                }

                // Add extra space to avoid overlapping
                if (showSwitch) {
                    item {
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }

        if (showSwitch) {
            ExpandableFab(
                hasRoot = hasRoot,
                isRootMode = isRootMode,
                context = context,
                batteryInfoViewModel = batteryInfoViewModel,
                onToggleRootMode = { batteryInfoViewModel.setRootMode(it) },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .windowInsetsPadding(WindowInsets.navigationBars)
            )
        }
    }

    if (showCycleCountDialog) {
        CycleCountDialog {
            showCycleCountDialog = false
        }
    }

    if (showEstFccDialog) {
        EstFccInfoDialog {
            showEstFccDialog = false
        }
    }

    if (showCoeffDialog) {
        CoeffTableDialog(infoText = coeffDialogText) {
            showCoeffDialog = false
        }
    }

    if (showWrongVoltageDialog) {
        WrongVoltageDialog {
            showWrongVoltageDialog = false
        }
    }

    if (showMultiplierDialog) {
        AlertDialog(
            onDismissRequest = { showMultiplierDialog = false },
            title = { Text(stringResource(R.string.calibrate_via_multiplier)) },
            text = {
                Column (modifier = Modifier.verticalScroll(rememberScrollState())) {
                    Text(stringResource(R.string.battery_current_info))
                    MultiplierSelector(
                        isMultiply = isMultiply,
                        onMultiplyChange = { batteryInfoViewModel.setMultiplierPrefs(it, selectedMagnitude) },
                        selectedMagnitude = selectedMagnitude,
                        onMagnitudeChange = { batteryInfoViewModel.setMultiplierPrefs(isMultiply, it) }
                    )
                }
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

@Composable
fun AddFieldDialog(
    existingPaths: Set<String>,
    onAdd: (String,String,String,Int) -> Unit,
    onDismiss: () -> Unit,
    defaultPath:  String = "",
    defaultTitle: String = "",
    defaultUnit:  String = "",
    defaultScale: Int    = 0
) {
    var path  by rememberSaveable { mutableStateOf(defaultPath)  }
    var title by rememberSaveable { mutableStateOf(defaultTitle) }
    var unit  by rememberSaveable { mutableStateOf(defaultUnit)  }
    var scale by rememberSaveable { mutableIntStateOf(defaultScale) }

    val editing         = defaultPath.isNotEmpty()
    val pathIsDuplicate = path in existingPaths && path != defaultPath
    val scaleOptions = listOf(0, -3, 3)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editing) stringResource(R.string.edit_custom_entry) else stringResource(R.string.add_custom_entry)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(path,  { path = it }, label = { Text(stringResource(R.string.path_sys)) })
                OutlinedTextField(title, { title = it }, label = { Text(stringResource(R.string.title)) })
                OutlinedTextField(unit,  { unit  = it }, label = { Text(stringResource(R.string.unit_optional)) })

                Spacer(Modifier.height(12.dp))
                Text(stringResource(R.string.scale), style = MaterialTheme.typography.bodyMedium)
                Row {
                    scaleOptions.forEach {
                        FilterChip(
                            selected = (scale == it),
                            onClick  = { scale = it },
                            label    = { Text("10^$it") }
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                }
            }
        },
        confirmButton = {
            Button(
                enabled = path.isNotBlank() && !pathIsDuplicate,
                onClick = {
                    onAdd(path, title.ifBlank { path.substringAfterLast('/') }, unit, scale)
                    onDismiss()
                }
            ) { Text(if (pathIsDuplicate) stringResource(R.string.existed) else stringResource(R.string.save)) }
        },
        dismissButton = { Button(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun ManageEntriesDialog(
    viewModel: BatteryInfoViewModel,
    onDismiss: () -> Unit
) {
    var context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val entries by viewModel.customEntries.collectAsState()
    var showAdd by remember { mutableStateOf(false) }
    var entryEditing by remember { mutableStateOf<CustomEntry?>(null) }
    val presets = listOf("generic", "empty")
    var expanded by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                coroutineScope.launch {
                    try {
                        viewModel.importJsonFromUri(it)
                        Toast.makeText(context,
                            context.getString(R.string.imported), Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context,
                            context.getString(R.string.import_failed, e.message), Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        dismissButton = {
            Button(
                onClick = {
                    viewModel.exportEntries(context) { uri ->
                        if (uri != Uri.EMPTY)
                            Toast.makeText(context,
                                context.getString(R.string.saved_to_downloads), Toast.LENGTH_SHORT).show()
                        else
                            Toast.makeText(context,
                                context.getString(R.string.export_failed), Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text(
                    text = stringResource(R.string.export_profile))
            }
            Button(onClick = onDismiss) {
                Text(stringResource(R.string.close))
            }
        },
        confirmButton = {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { launcher.launch(arrayOf("application/json")) }
            ) {
                Text(stringResource(R.string.import_profile))
            }
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = { showAdd = true }
            ) {
                Text(stringResource(R.string.add))
            }
        },
        title = { Text(stringResource(R.string.manage_custom_entries)) },
        text = {
            Column {
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        value = stringResource(R.string.choose_from_presets),
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        presets.forEach { preset ->
                            DropdownMenuItem(
                                text = { Text(preset.replaceFirstChar(Char::titlecase)) },
                                onClick = {
                                    expanded = false
                                    coroutineScope.launch {
                                        viewModel.importPreset(preset)
                                        Toast.makeText(
                                            context,
                                            context.getString(R.string.preset_imported, preset),
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            )
                        }
                    }
                }

                Spacer(Modifier.height(8.dp))
                LazyColumn {
                    items(count = entries.size, key = { entries[it].path }) { index ->
                        val entry = entries[index]
                        Row(
                            Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(entry.title, style = MaterialTheme.typography.bodyMedium)
                                Text(entry.path, style = MaterialTheme.typography.bodySmall)
                            }
                            FilledTonalIconButton(
                                onClick = { entryEditing = entry },
                                shapes = IconButtonDefaults.shapes()
                            ) {
                                Icon(ImageVector.vectorResource(id = R.drawable.edit_24dp_1f1f1f_fill0_wght400_grad0_opsz24), "Edit")
                            }
                            FilledTonalIconButton(
                                onClick = {
                                    coroutineScope.launch {
                                        viewModel.removeCustomEntry(entry.path)
                                    }
                                },
                                shapes = IconButtonDefaults.shapes()
                            ) {
                                Icon(ImageVector.vectorResource(id = R.drawable.delete_24dp_1f1f1f_fill0_wght400_grad0_opsz24), contentDescription = "Remove")
                            }
                        }
                    }
                    if (entries.isEmpty()) {
                        item { Text(stringResource(R.string.no_custom_entries)) }
                    }
                }
            }
        }
    )
    if (showAdd || entryEditing != null) {
        val init = entryEditing
        AddFieldDialog(
            existingPaths = entries.mapTo(mutableSetOf()) { it.path },
            onAdd = { path, title, unit, scale ->
                coroutineScope.launch {
                    viewModel.addCustomEntry(CustomEntry(path, title, unit, scale))
                }
            },
            onDismiss = {
                showAdd = false
                entryEditing = null},
            defaultPath  = init?.path  ?: "",
            defaultTitle = init?.title ?: "",
            defaultUnit  = init?.unit  ?: "",
            defaultScale = init?.scale ?: 0
        )
    }
}

private fun collectPowerDataForChart(
    displayList: List<BatteryInfo>,
    powerDataPoints: MutableList<PowerDataPoint>,
    chartStartTime: Long,
    maxDataPoints: Int = 36000
) {
    val powerInfo = displayList.find { it.type == BatteryInfoType.POWER }
    val tempInfo = displayList.find { it.type == BatteryInfoType.TEMP }

    powerInfo?.let { power ->
        try {
            val nf = NumberFormat.getInstance()
            val powerValue = nf.parse(power.value)?.toFloat() ?: 0f
            val tempValue = tempInfo?.value?.let { nf.parse(it)?.toFloat() } ?: 0f
            val currentTime = System.currentTimeMillis()

            powerDataPoints.add(PowerDataPoint(
                timestamp = currentTime - chartStartTime,
                power = kotlin.math.abs(powerValue),
                temperature = tempValue
            ))

            // Keep only last N data points
            if (powerDataPoints.size > maxDataPoints) {
                powerDataPoints.removeAt(0)
            }
        } catch (e: Exception) {
            // Ignore parsing errors
        }
    }
}

@OptIn(ExperimentalMaterial3ExpressiveApi::class, ExperimentalAnimationApi::class)
@Composable
fun ExpandableFab(
    hasRoot: Boolean,
    isRootMode: Boolean,
    context: Context,
    batteryInfoViewModel: BatteryInfoViewModel,
    onToggleRootMode: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    var showMgr by remember { mutableStateOf(false) }
    var isExpanded by remember { mutableStateOf(false) }

    val fabContainerColor by animateColorAsState(
        targetValue = if (isExpanded) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "fab_container_color"
    )
    val fabContentColor by animateColorAsState(
        targetValue = if (isExpanded) MaterialTheme.colorScheme.onPrimary else contentColorFor(MaterialTheme.colorScheme.secondaryContainer),
        animationSpec = MaterialTheme.motionScheme.fastEffectsSpec(),
        label = "fab_content_color"
    )
    val fabCornerRadius by animateDpAsState(
        targetValue = if (isExpanded) 28.dp else 16.dp,
        animationSpec = MaterialTheme.motionScheme.fastSpatialSpec(),
        label = "fab_corner_radius"
    )

    if (isExpanded) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null
                ) { isExpanded = false }
        )
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.End,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandHorizontally(
                animationSpec = MaterialTheme.motionScheme.fastEffectsSpec()
            ),
            exit = fadeOut(
                animationSpec = MaterialTheme.motionScheme.fastEffectsSpec()
            )
        ) {
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ExtendedFloatingActionButton(
                    onClick = {
                        showMgr = true
                        isExpanded = false
                    },
                    elevation = FloatingActionButtonDefaults.elevation(0.dp),
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    icon = { Icon(ImageVector.vectorResource(R.drawable.battery_profile_24dp_1f1f1f_fill0_wght400_grad0_opsz24), null) },
                    text = {
                        Text(
                            text = stringResource(R.string.manage_custom_entries)
                        )
                    }
                )
                // Root Mode Extended FAB
                ExtendedFloatingActionButton(
                    onClick = {
                        if (!isRootMode) {
                            if (hasRoot) onToggleRootMode(true) else context.showRootDeniedToast()
                        } else {
                            onToggleRootMode(false)
                        }
                        isExpanded = false
                    },
                    elevation = FloatingActionButtonDefaults.elevation(0.dp),
                    containerColor = if (isRootMode) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondaryContainer,
                    icon = { Icon(ImageVector.vectorResource(R.drawable.numbers_24dp_1f1f1f_fill0_wght400_grad0_opsz24), null) },
                    text = {
                        Text(
                            text = if (isRootMode) stringResource(R.string.disable_root_mode) else stringResource(R.string.enable_root_mode)
                        )
                    }
                )
            }
        }

        // Main FAB
        FloatingActionButton(
            onClick = { isExpanded = !isExpanded },
            shape = RoundedCornerShape(fabCornerRadius),
            containerColor = fabContainerColor,
        ) {
            AnimatedContent(
                targetState = isExpanded,
                label = "fab_icon_content"
            ) { expanded ->
                Icon(
                    imageVector = if (expanded) 
                                    ImageVector.vectorResource(id = R.drawable.close_24dp_1f1f1f_fill0_wght400_grad0_opsz24) 
                                else 
                                    ImageVector.vectorResource(id = R.drawable.build_24dp_1f1f1f_fill0_wght400_grad0_opsz24),
                    contentDescription = if (expanded) "Close menu" else "Open menu",
                    tint = fabContentColor
                )
            }
        }

        if (showMgr) {
            if (!hasRoot || !isRootMode) {
                context.showRootDeniedToast()
                showMgr = false
            }
            else {
                ManageEntriesDialog(
                    viewModel = batteryInfoViewModel,
                    onDismiss = { showMgr = false }
                )
            }
        }
    }
}