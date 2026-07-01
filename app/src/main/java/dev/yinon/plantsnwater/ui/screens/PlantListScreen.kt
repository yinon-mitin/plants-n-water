package dev.yinon.plantsnwater.ui.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.yinon.plantsnwater.ui.LocalAppContainer
import dev.yinon.plantsnwater.ui.PlantListViewModel
import dev.yinon.plantsnwater.ui.PlantViewModelFactory

@Composable
fun PlantListScreen(onPlantClick: (Long) -> Unit) {
    val container = LocalAppContainer.current
    val viewModel: PlantListViewModel = viewModel(factory = PlantViewModelFactory(container))
    val plants by viewModel.plants.collectAsStateWithLifecycle()

    LazyColumn {
        item {
            ScreenColumn {
                SectionTitle("Plants")
                if (plants.isEmpty()) {
                    EmptyState("Add your first plant", "A plant only needs a name and watering interval.")
                }
            }
        }
        items(plants, key = { it.id }) { plant ->
            ScreenColumn {
                PlantCard(plant = plant, onClick = { onPlantClick(plant.id) })
            }
        }
    }
}
