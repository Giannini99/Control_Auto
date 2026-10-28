package br.com.sensorauto.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import br.com.sensorauto.ui.screen.config.equipment.EquipmentConfigScreen
import br.com.sensorauto.ui.screen.config.sensors.SensorConfigScreen
import br.com.sensorauto.ui.screen.files.RecordingDetailScreen
import br.com.sensorauto.ui.screen.files.RecordingFilesScreen
import br.com.sensorauto.ui.screen.start.ActiveStartScreen
import br.com.sensorauto.ui.screen.config.bubble.BubbleLevelScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SensorAutoNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val bottomItems = listOf(
        BottomNavItem("Iniciar",     Icons.Default.RadioButtonChecked, Screen.ActiveSession),
        BottomNavItem("Equipamentos", Icons.Default.DirectionsCar,      Screen.EquipmentConfig),
        BottomNavItem("Sensores",    Icons.Default.Sensors,            Screen.SensorConfig),
        BottomNavItem("Histórico",   Icons.Default.VideoLibrary,       Screen.RecordingFiles),
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "ROADIFY LOGGER",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar {
                bottomItems.forEach { item ->
                    NavigationBarItem(
                        selected = currentDestination?.hierarchy?.any {
                            it.hasRoute(item.route::class)
                        } == true,
                        onClick = {
                            navController.navigate(item.route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.EquipmentConfig,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<Screen.ActiveSession> {
                ActiveStartScreen()
            }
            composable<Screen.EquipmentConfig> {
                EquipmentConfigScreen()
            }
            composable<Screen.SensorConfig> {
                SensorConfigScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToBubbleLevel = { navController.navigate(Screen.BubbleLevel) }
                )
            }
            composable<Screen.BubbleLevel> {
                BubbleLevelScreen(
                    onBack = { navController.popBackStack() }
                )
            }
            composable<Screen.RecordingFiles> {
                RecordingFilesScreen(
                    onOpenDetail = { id, name ->
                        navController.navigate(Screen.RecordingDetail(id = id, name = name))
                    }
                )
            }
            composable<Screen.RecordingDetail> { backStackEntry ->
                val route = backStackEntry.toRoute<Screen.RecordingDetail>()
                RecordingDetailScreen(
                    recordingId = route.id,
                    recordingName = route.name,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

private data class BottomNavItem(
    val label: String,
    val icon: ImageVector,
    val route: Screen
)
