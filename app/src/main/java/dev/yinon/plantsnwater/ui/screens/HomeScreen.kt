package dev.yinon.plantsnwater.ui.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.yinon.plantsnwater.R
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
                SectionTitle(stringResource(R.string.today))
                if (state.duePlants.isEmpty()) {
                    EmptyState(stringResource(R.string.nothing_due), stringResource(R.string.nothing_due_body))
                }
            }
        }
        items(state.duePlants, key = { it.id }) { plant ->
            ScreenColumn {
                PlantCard(
                    plant = plant,
                    latestPhoto = state.latestPhotos[plant.id],
                    onClick = { onPlantClick(plant.id) },
                    onWatered = { viewModel.markWatered(plant.id) },
                    onSkip = { viewModel.skip(plant.id) },
                    onPostpone = { viewModel.postpone(plant.id) }
                )
            }
        }
        item {
            ScreenColumn {
                SectionTitle(stringResource(R.string.upcoming))
                if (state.upcomingPlants.isEmpty() && state.duePlants.isNotEmpty()) {
                    EmptyState(stringResource(R.string.no_upcoming_tasks), stringResource(R.string.no_upcoming_tasks_body))
                }
            }
        }
        items(state.upcomingPlants, key = { it.id }) { plant ->
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
