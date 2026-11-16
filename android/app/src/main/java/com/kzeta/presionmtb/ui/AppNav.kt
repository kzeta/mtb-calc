package com.kzeta.presionmtb.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.kzeta.presionmtb.PresionApp
import com.kzeta.presionmtb.ui.bikes.BikeEditScreen
import com.kzeta.presionmtb.ui.bikes.BikesScreen
import com.kzeta.presionmtb.ui.calculator.CalculatorScreen
import com.kzeta.presionmtb.ui.history.HistoryScreen
import com.kzeta.presionmtb.ui.profile.ProfileScreen
import com.kzeta.presionmtb.ui.theme.LocalPalette
import com.kzeta.presionmtb.ui.theme.Manrope

private data class Tab(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val tabs = listOf(
    Tab("calculator", "Calcular", Icons.Filled.Speed),
    Tab("bikes", "Bicis", Icons.AutoMirrored.Filled.DirectionsBike),
    Tab("history", "Historial", Icons.Filled.History),
    Tab("profile", "Perfil", Icons.Filled.Person),
)

@Composable
fun AppNav(app: PresionApp) {
    val nav = rememberNavController()
    val palette = LocalPalette.current
    val entry by nav.currentBackStackEntryAsState()
    val route = entry?.destination?.route
    val showBar = route in tabs.map { it.route }
    Scaffold(
        containerColor = palette.bg,
        bottomBar = {
            if (showBar) {
                NavigationBar(containerColor = palette.surface) {
                    tabs.forEach { tab ->
                        NavigationBarItem(
                            selected = route == tab.route,
                            onClick = {
                                nav.navigate(tab.route) {
                                    popUpTo(nav.graph.findStartDestination().id) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                            icon = { Icon(tab.icon, contentDescription = tab.label) },
                            label = { Text(tab.label, fontFamily = Manrope) },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = palette.onLime,
                                selectedTextColor = palette.accent,
                                indicatorColor = palette.lime,
                                unselectedIconColor = palette.muted,
                                unselectedTextColor = palette.muted,
                            ),
                        )
                    }
                }
            }
        },
    ) { padding ->
        NavHost(nav, startDestination = "calculator", modifier = Modifier.padding(padding)) {
            composable("calculator") { CalculatorScreen(app) }
            composable("bikes") { BikesScreen(app) { id -> nav.navigate("bike/$id") } }
            composable("history") { HistoryScreen(app) }
            composable("profile") { ProfileScreen(app) }
            composable("bike/{bikeId}") { entry ->
                BikeEditScreen(
                    app = app,
                    bikeId = entry.arguments?.getString("bikeId") ?: "new",
                    onDone = { nav.popBackStack() },
                )
            }
        }
    }
}
