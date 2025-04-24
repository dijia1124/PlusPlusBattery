package com.example.plusplusbattery

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.plusplusbattery.ui.theme.PlusPlusBatteryTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
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
import androidx.compose.ui.res.stringResource
import com.topjohnwu.superuser.Shell

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PlusPlusBatteryTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column {
                        BottomNavigationBar(HistoryInfoViewModel(application), modifier = Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(historyInfoViewModel: HistoryInfoViewModel, modifier: Modifier) {
    var hasRoot by remember { mutableStateOf(false) }
    hasRoot = hasRootAccess()
// Define the list of navigation routes using the data class
    val navRoutes = listOf(
        NavRoute("dashboard", Icons.Filled.Home, stringResource(R.string.nav_dashboard)),
        NavRoute("history", Icons.Filled.Star, stringResource(R.string.nav_history)),
        NavRoute("about", Icons.Filled.Info, stringResource(R.string.nav_about))
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
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("dashboard") { Dashboard(historyInfoViewModel, hasRoot) }
            composable("history") { History(historyInfoViewModel) }
            composable("about") { About() }
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
