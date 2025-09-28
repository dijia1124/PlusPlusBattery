package com.dijia1124.plusplusbattery

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.dijia1124.plusplusbattery.ui.theme.PlusPlusBatteryTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
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

class MainActivity : ComponentActivity() {

    // This list contains the top-level routes that will show the bottom navigation bar
    private val topLevelRoutes = listOf(
        "dashboard", "battery_monitor", "history", "settings"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val settingsViewModel by lazy {
            ViewModelProvider(
                this,
                object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T =
                        SettingsViewModel(application) as T
                }
            )[SettingsViewModel::class.java]
        }

        val battMonViewModel by lazy{
            ViewModelProvider(
                this,
                object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T =
                        BatteryMonitorSettingsViewModel(application) as T
                }
            )[BatteryMonitorSettingsViewModel::class.java]
        }

        val floatingWindowSettingsViewModel by lazy {
            ViewModelProvider(
                this,
                object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T =
                        FloatingWindowSettingsViewModel(application) as T
                }
            )[FloatingWindowSettingsViewModel::class.java]
        }

        val batteryInfoViewModel by lazy {
            ViewModelProvider(
                this,
                object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return BatteryInfoViewModel(application) as T
                    }
                }
            )[BatteryInfoViewModel::class.java]
        }

        val historyInfoViewModel by lazy {
            ViewModelProvider(
                this,
                object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return HistoryInfoViewModel(application) as T
                    }
                }
            )[HistoryInfoViewModel::class.java]
        }

        val batteryLogViewModel by lazy {
            ViewModelProvider(
                this,
                object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        return BatteryLogViewModel(application) as T
                    }
                }
            )[BatteryLogViewModel::class.java]
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
        NavRoute("dashboard", Icons.Filled.Home, stringResource(R.string.nav_dashboard)),
        NavRoute(
            "battery_monitor",
            ImageVector.vectorResource(id = R.drawable.speed_24dp_1f1f1f_fill1_wght400_grad0_opsz24),
            stringResource(R.string.monitor)
        ),
        NavRoute(
            "history",
            ImageVector.vectorResource(id = R.drawable.library_books_24dp_1f1f1f_fill1_wght400_grad0_opsz24),
            stringResource(R.string.nav_history)
        ),
        NavRoute("settings", Icons.Filled.Settings, stringResource(R.string.settings)),
    )
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    NavigationBar()
    {
        navRoutes.forEach { navRoute ->
            NavigationBarItem(
                alwaysShowLabel = false,
                icon = { Icon(navRoute.icon, contentDescription =
                    navRoute.label) },
                label = { Text(navRoute.label) },
                selected = currentDestination?.route == navRoute.route,
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
