package com.example.plusplusbattery

import android.os.Bundle
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
// Define the list of navigation routes using the data class
    val navRoutes = listOf(
        NavRoute("dashboard", Icons.Filled.Home, "Dashboard"),
        NavRoute("history", Icons.Filled.Star, "History"),
        NavRoute("about", Icons.Filled.Info, "About")
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
            composable("dashboard") { Dashboard(historyInfoViewModel) }
            composable("history") { History(historyInfoViewModel) }
            composable("about") { About() }
        }
    }
}