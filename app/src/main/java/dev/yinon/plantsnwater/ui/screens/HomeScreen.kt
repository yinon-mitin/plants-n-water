package dev.yinon.plantsnwater.ui.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.yinon.plantsnwater.ui.HomeViewModel
import dev.yinon.plantsnwater.ui.LocalAppContainer
import dev.yinon.plantsnwater.ui.PlantViewModelFactory

@Composable
fun HomeScreen(onPlantClick: (Long) -> Unit) {
    val container = LocalAppContainer.current
    val viewModel: HomeViewModel = viewModel(factory = PlantViewModelFactory(container))
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn {
        item {
            ScreenColumn {
                SectionTitle("Today")
                if (state.duePlants.isEmpty()) {
                    EmptyState("Nothing due", "Your plants are on track. Upcoming watering tasks are below.")
                }
            }
        }
        items(state.duePlants, key = { it.id }) { plant ->
            ScreenColumn {
                PlantCard(
                    plant = plant,
                    onClick = { onPlantClick(plant.id) },
                    onWatered = { viewModel.markWatered(plant.id) },
                    onSkip = { viewModel.skip(plant.id) },
                    onPostpone = { viewModel.postpone(plant.id) }
                )
            }
        }
        item {
            ScreenColumn {
                SectionTitle("Upcoming")
                if (state.upcomingPlants.isEmpty() && state.duePlants.isNotEmpty()) {
                    EmptyState("No upcoming tasks", "Watered plants will appear here after their next schedule is calculated.")
                }
            }
        }
        items(state.upcomingPlants, key = { it.id }) { plant ->
            ScreenColumn {
                PlantCard(plant = plant, onClick = { onPlantClick(plant.id) })
            }
        }
    }
}
