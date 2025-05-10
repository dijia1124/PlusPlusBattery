package com.example.plusplusbattery

import android.app.Application
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.navigation.NavHostController
import com.topjohnwu.superuser.Shell

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PlusPlusBatteryTheme {
                BottomNavigationBar(HistoryInfoViewModel(application), application)
            }
        }
    }
}

@Composable
fun WearNavigationBar(
    navRoutes: List<NavRoute>,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    Surface(
        tonalElevation = 3.dp,
        shadowElevation = 3.dp,
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            navRoutes.forEach { navRoute ->
                IconToggleButton(
                    checked = currentDestination?.route == navRoute.route,
                    onCheckedChange = {
                        navController.navigate(navRoute.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                inclusive = false
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                ) {
                    Icon(
                        imageVector = navRoute.icon,
                        contentDescription = navRoute.label,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}


@Composable
fun BottomNavigationBar(historyInfoViewModel: HistoryInfoViewModel, application: Application) {
    val historyRepo = remember { HistoryInfoRepository(application) }
    val viewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current)
    val batteryInfoViewModel = remember {
        ViewModelProvider(
            viewModelStoreOwner,
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return BatteryInfoViewModel(application, historyRepo) as T
                }
            }
        )[BatteryInfoViewModel::class.java]
    }
    val isWatch = LocalContext.current.packageManager
        .hasSystemFeature(PackageManager.FEATURE_WATCH)

    var hasRoot by remember { mutableStateOf(false) }
    hasRoot = hasRootAccess()
    // Define the list of navigation routes using the data class
    val navRoutes = listOf(
        NavRoute("dashboard", Icons.Filled.Home, stringResource(R.string.nav_dashboard)),
        NavRoute("history", Icons.Filled.Star, stringResource(R.string.nav_history)),
        NavRoute("settings", Icons.Filled.Settings, stringResource(R.string.settings))
    )
    val navController = rememberNavController()
    Scaffold(
        bottomBar = {
            if (isWatch) {
                WearNavigationBar(
                    navRoutes = navRoutes,
                    navController = navController
                )
            } else {
                NavigationBar()
                {
                    val navBackStackEntry by navController.currentBackStackEntryAsState()
                    val currentDestination = navBackStackEntry?.destination

                    navRoutes.forEach { navRoute ->
                        NavigationBarItem(
                            alwaysShowLabel = false,
                            icon = {
                                Icon(
                                    navRoute.icon, contentDescription =
                                        navRoute.label
                                )
                            },
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
        }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = paddingValues.calculateBottomPadding())
        ) {
            composable("dashboard") { Dashboard(hasRoot, stringResource(R.string.app_name),batteryInfoViewModel) }
            composable("history") { History(historyInfoViewModel, stringResource(R.string.history)) }
            composable("settings")  {
                Settings(
                    currentTitle = stringResource(R.string.settings),
                    navController = navController,
                    hasRoot = hasRoot,
                    batteryVM  = batteryInfoViewModel
                )
            }
            composable("about")     { About(stringResource(R.string.about)) }
        }
    }
}

fun hasRootAccess(): Boolean {
    return try {
        val result = Shell.cmd("su -c whoami").exec()
        result.isSuccess
    } catch (e: Exception) {
        false
    }
}
