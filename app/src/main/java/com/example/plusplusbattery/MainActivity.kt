package com.example.plusplusbattery

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.plusplusbattery.ui.theme.PlusPlusBatteryTheme
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.topjohnwu.superuser.Shell

class MainActivity : ComponentActivity() {
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

        setContent {
            //        Shell.enableVerboseLogging = true  // Enable verbose logging for debugging
            Shell.getShell()

            val darkModeEnabled   by settingsViewModel.darkModeEnabled.collectAsState()
            val followSystemTheme by settingsViewModel.followSystemTheme.collectAsState()
            val sysDark           = isSystemInDarkTheme()

            val useDarkTheme = if (followSystemTheme) sysDark else darkModeEnabled

            PlusPlusBatteryTheme(darkTheme = useDarkTheme) {
                BottomNavigationBar(HistoryInfoViewModel(application), application, settingsViewModel, battMonViewModel)
            }
        }
    }
}

@Composable
fun BottomNavigationBar(
    historyInfoViewModel: HistoryInfoViewModel,
    application: Application,
    settingsViewModel: SettingsViewModel,
    batteryMonitorSettingsViewModel: BatteryMonitorSettingsViewModel
) {
    val hasRoot by settingsViewModel.hasRoot.collectAsState()
    val prefsRepo = remember { PrefsRepository(application) }
    val historyRepo = remember { HistoryInfoRepository(application) }
    val batteryInfoRepository = remember { BatteryInfoRepository(application) }
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)
    val batteryInfoViewModel = remember {
        ViewModelProvider(
            viewModelStoreOwner,
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return BatteryInfoViewModel(application, batteryInfoRepository, prefsRepo, historyRepo) as T
                }
            }
        )[BatteryInfoViewModel::class.java]
    }

    // Define the list of navigation routes using the data class
    val navRoutes = listOf(
        NavRoute("dashboard", Icons.Filled.Home, stringResource(R.string.nav_dashboard)),
        NavRoute("battery_monitor", ImageVector.vectorResource(id = R.drawable.speed_24dp_1f1f1f_fill1_wght400_grad0_opsz24),
            stringResource(R.string.monitor)
        ),
        NavRoute("history", ImageVector.vectorResource(id = R.drawable.library_books_24dp_1f1f1f_fill1_wght400_grad0_opsz24), stringResource(R.string.nav_history)),
        NavRoute("settings", Icons.Filled.Settings, stringResource(R.string.settings)),
    )
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            NavigationBar()
            {
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentDestination = navBackStackEntry?.destination

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
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            composable("dashboard") { Dashboard(hasRoot, stringResource(R.string.app_name), batteryInfoViewModel) }
            composable("history") { History(historyInfoViewModel, stringResource(R.string.history)) }
            composable("settings")  {
                Settings(
                    currentTitle = stringResource(R.string.settings),
                    navController = navController,
                    hasRoot = hasRoot,
                    batteryVM  = batteryInfoViewModel,
                    settingsVM = settingsViewModel
                )
            }
            composable("about")     { About(stringResource(R.string.about)) }
            composable("batt_mon_settings") {
                BatteryMonitorSettings(batteryMonitorSettingsViewModel, stringResource(R.string.battery_monitor_entry_settings))
            }
            composable("battery_monitor") { BatteryMonitor(stringResource(R.string.battery_monitor), navController) }
        }
    }
}
