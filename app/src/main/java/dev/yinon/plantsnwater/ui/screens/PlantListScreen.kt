package dev.yinon.plantsnwater.ui.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.yinon.plantsnwater.R
import dev.yinon.plantsnwater.ui.LocalAppContainer
import dev.yinon.plantsnwater.ui.PlantListViewModel
import dev.yinon.plantsnwater.ui.PlantViewModelFactory

@Composable
fun PlantListScreen(onPlantClick: (Long) -> Unit) {
    val container = LocalAppContainer.current
    val viewModel: PlantListViewModel = viewModel(factory = PlantViewModelFactory(container))
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LazyColumn {
        item {
            ScreenColumn {
                SectionTitle(stringResource(R.string.plants))
                if (state.plants.isEmpty()) {
                    EmptyState(stringResource(R.string.add_first_plant), stringResource(R.string.add_first_plant_body))
                }
            }
        }
        items(state.plants, key = { it.id }) { plant ->
            ScreenColumn {
                PlantCard(
                    plant = plant,
                    latestPhoto = state.latestPhotos[plant.id],
                    onClick = { onPlantClick(plant.id) }
                )
            }
        }
    }
}
