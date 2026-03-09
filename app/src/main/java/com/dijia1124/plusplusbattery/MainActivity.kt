package com.dijia1124.plusplusbattery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.dijia1124.plusplusbattery.ui.theme.PlusPlusBatteryTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.navigation.NavController
import com.dijia1124.plusplusbattery.ui.nav.NavRoute
import com.dijia1124.plusplusbattery.ui.screen.About
import com.dijia1124.plusplusbattery.ui.screen.BatteryMonitor
import com.dijia1124.plusplusbattery.ui.screen.BatteryMonitorSettings
import com.dijia1124.plusplusbattery.ui.screen.Dashboard
import com.dijia1124.plusplusbattery.ui.screen.History
import com.dijia1124.plusplusbattery.ui.screen.Settings
import com.dijia1124.plusplusbattery.ui.screen.FloatingWindowSettings
import com.dijia1124.plusplusbattery.ui.screen.UniversalSupportLogcat
import com.dijia1124.plusplusbattery.vm.BatteryInfoViewModel
import com.dijia1124.plusplusbattery.vm.BatteryLogViewModel
import com.dijia1124.plusplusbattery.vm.BatteryMonitorSettingsViewModel
import com.dijia1124.plusplusbattery.vm.FloatingWindowSettingsViewModel
import com.dijia1124.plusplusbattery.vm.HistoryInfoViewModel
import com.dijia1124.plusplusbattery.vm.SettingsViewModel
import com.topjohnwu.superuser.Shell
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    // This list contains the top-level routes that will show the bottom navigation bar
    private val topLevelRoutes = listOf(
        "dashboard", "battery_monitor", "history", "settings"
    )
    private val settingsViewModel: SettingsViewModel by viewModels()
    private val battMonViewModel: BatteryMonitorSettingsViewModel by viewModels()
    private val floatingWindowSettingsViewModel: FloatingWindowSettingsViewModel by viewModels()
    private val batteryInfoViewModel: BatteryInfoViewModel by viewModels()
    private val historyInfoViewModel: HistoryInfoViewModel by viewModels()
    private val batteryLogViewModel: BatteryLogViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Request Shizuku permission on app start if available but not granted yet
        if (com.dijia1124.plusplusbattery.data.util.ShizukuUtils.isShizukuAvailable() && !com.dijia1124.plusplusbattery.data.util.ShizukuUtils.hasShizukuPermission()) {
            com.dijia1124.plusplusbattery.data.util.ShizukuUtils.requestShizukuPermission(1)
        }

        setContent {
            //        Shell.enableVerboseLogging = true  // Enable verbose logging for debugging
            Shell.getShell()

            val useDarkTheme = (application as MainApplication).useDarkTheme
            PlusPlusBatteryTheme(darkTheme = useDarkTheme) {
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val hasRoot by settingsViewModel.hasRoot.collectAsState()
                Scaffold(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    // Only show the top bar when on a top-level screen
                    bottomBar = {
                        if (currentRoute in topLevelRoutes) {
                            BottomNavigationBar(navController)
                        }
                    }
                )
                { paddingValues ->
                    NavHost(
                        navController = navController,
                        startDestination = "dashboard",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(bottom = paddingValues.calculateBottomPadding())
                    ) {
                        composable("dashboard") {
                            Dashboard(
                                hasRoot,
                                stringResource(R.string.app_name),
                                batteryInfoViewModel,
                                settingsViewModel
                            )
                        }
                        composable("history") {
                            History(
                                historyInfoViewModel,
                                stringResource(R.string.history)
                            )
                        }
                        composable("settings")  {
                            Settings(
                                currentTitle = stringResource(R.string.settings),
                                navController = navController,
                                hasRoot = hasRoot,
                                batteryVM = batteryInfoViewModel,
                                settingsVM = settingsViewModel
                            )
                        }
                        composable("about")     { About(stringResource(R.string.about), navController) }
                        composable("batt_mon_settings") {
                            BatteryMonitorSettings(
                                navController,
                                battMonViewModel,
                                stringResource(R.string.battery_monitor_entry_settings)
                            )
                        }
                        composable("floating_window_settings") {
                            FloatingWindowSettings(
                                stringResource(R.string.floating_window_settings),
                                navController,
                                floatingWindowSettingsViewModel
                            )
                        }
                        composable("battery_monitor") {
                            BatteryMonitor(
                                stringResource(R.string.battery_monitor),
                                navController,
                                battMonViewModel
                            )
                        }
                        composable("battery_logcat_experiment") {
                            UniversalSupportLogcat(
                                navController,
                                batteryLogViewModel,
                                stringResource(R.string.get_from_logcat_title)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(navController: NavController) {
    // Define the list of navigation routes using the data class
    val navRoutes = listOf(
        NavRoute(
            "dashboard",
            selectedIcon = ImageVector.vectorResource(id = R.drawable.home_24dp_1f1f1f_fill1_wght400_grad0_opsz24),
            unselectedIcon = ImageVector.vectorResource(id = R.drawable.home_24dp_1f1f1f_fill0_wght400_grad0_opsz24),
            label = stringResource(R.string.nav_dashboard)
        ),
        NavRoute(
            "battery_monitor",
            selectedIcon = ImageVector.vectorResource(id = R.drawable.speed_24dp_1f1f1f_fill1_wght400_grad0_opsz24),
            unselectedIcon = ImageVector.vectorResource(id = R.drawable.speed_24dp_1f1f1f_fill0_wght400_grad0_opsz24),
            label = stringResource(R.string.monitor)
        ),
        NavRoute(
            "history",
            selectedIcon = ImageVector.vectorResource(id = R.drawable.library_books_24dp_1f1f1f_fill1_wght400_grad0_opsz24),
            unselectedIcon = ImageVector.vectorResource(id = R.drawable.library_books_24dp_1f1f1f_fill0_wght400_grad0_opsz24),
            label = stringResource(R.string.nav_history)
        ),
        NavRoute(
            "settings",
            selectedIcon = ImageVector.vectorResource(id = R.drawable.settings_24dp_1f1f1f_fill1_wght400_grad0_opsz24),
            unselectedIcon = ImageVector.vectorResource(id = R.drawable.settings_24dp_1f1f1f_fill0_wght400_grad0_opsz24),
            label = stringResource(R.string.settings)
        ),
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    NavigationBar()
    {
        navRoutes.forEach { navRoute ->
            val selected = currentDestination?.route == navRoute.route
            NavigationBarItem(
                alwaysShowLabel = false,
                icon = {
                    Icon(
                        imageVector = if (selected) navRoute.selectedIcon else navRoute.unselectedIcon,
                        contentDescription = navRoute.label
                    )
                },
                label = { Text(navRoute.label) },
                selected = selected,
                onClick = {
                    navController.navigate(navRoute.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            inclusive = false
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}
