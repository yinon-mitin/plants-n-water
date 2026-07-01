package dev.yinon.plantsnwater.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import dev.yinon.plantsnwater.R
import dev.yinon.plantsnwater.ui.screens.AddPlantScreen
import dev.yinon.plantsnwater.ui.screens.CalendarScreen
import dev.yinon.plantsnwater.ui.screens.HomeScreen
import dev.yinon.plantsnwater.ui.screens.ImportExportScreen
import dev.yinon.plantsnwater.ui.screens.PhotoTimelineScreen
import dev.yinon.plantsnwater.ui.screens.PlantDetailScreen
import dev.yinon.plantsnwater.ui.screens.PlantListScreen
import dev.yinon.plantsnwater.ui.screens.SettingsScreen

private sealed class Route(val value: String) {
    data object Home : Route("home")
    data object Plants : Route("plants")
    data object AddPlant : Route("addPlant")
    data object Calendar : Route("calendar")
    data object Settings : Route("settings")
    data object Detail : Route("plant/{plantId}") {
        fun create(plantId: Long) = "plant/$plantId"
    }
    data object Photos : Route("plant/{plantId}/photos") {
        fun create(plantId: Long) = "plant/$plantId/photos"
    }
    data object ImportExport : Route("importExport")
}

@Composable
fun PlantsNWaterApp() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route
    val topLevel = listOf(Route.Home, Route.Plants, Route.Calendar, Route.Settings)

    Scaffold(
        bottomBar = {
            NavigationBar {
                topLevel.forEach { route ->
                    val icon = when (route) {
                        Route.Home -> Icons.Default.Home
                        Route.Plants -> Icons.AutoMirrored.Filled.List
                        Route.Calendar -> Icons.Default.DateRange
                        else -> Icons.Default.Settings
                    }
                    val label = when (route) {
                        Route.Home -> stringResource(R.string.nav_home)
                        Route.Plants -> stringResource(R.string.nav_plants)
                        Route.Calendar -> stringResource(R.string.nav_calendar)
                        else -> stringResource(R.string.nav_settings)
                    }
                    NavigationBarItem(
                        selected = currentRoute == route.value,
                        onClick = {
                            navController.navigate(route.value) {
                                popUpTo(Route.Home.value)
                                launchSingleTop = true
                            }
                        },
                        icon = { Icon(icon, contentDescription = null) },
                        label = { Text(label) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (currentRoute == Route.Home.value || currentRoute == Route.Plants.value) {
                FloatingActionButton(onClick = { navController.navigate(Route.AddPlant.value) }) {
                    Icon(Icons.Default.Add, contentDescription = stringResource(R.string.add_plant))
                }
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Route.Home.value,
            modifier = Modifier.padding(padding)
        ) {
            composable(Route.Home.value) {
                HomeScreen(onPlantClick = { navController.navigate(Route.Detail.create(it)) })
            }
            composable(Route.Plants.value) {
                PlantListScreen(onPlantClick = { navController.navigate(Route.Detail.create(it)) })
            }
            composable(Route.AddPlant.value) {
                AddPlantScreen(onDone = { navController.navigate(Route.Detail.create(it)) { popUpTo(Route.Home.value) } })
            }
            composable(Route.Calendar.value) {
                CalendarScreen(onPlantClick = { navController.navigate(Route.Detail.create(it)) })
            }
            composable(Route.Settings.value) {
                SettingsScreen(
                    onImportExport = { navController.navigate(Route.ImportExport.value) }
                )
            }
            composable(Route.ImportExport.value) {
                ImportExportScreen()
            }
            composable(
                route = Route.Detail.value,
                arguments = listOf(navArgument("plantId") { type = NavType.LongType })
            ) {
                val plantId = requireNotNull(it.arguments?.getLong("plantId"))
                PlantDetailScreen(
                    plantId = plantId,
                    onPhotos = { navController.navigate(Route.Photos.create(plantId)) }
                )
            }
            composable(
                route = Route.Photos.value,
                arguments = listOf(navArgument("plantId") { type = NavType.LongType })
            ) {
                PhotoTimelineScreen(plantId = requireNotNull(it.arguments?.getLong("plantId")))
            }
        }
    }
}
