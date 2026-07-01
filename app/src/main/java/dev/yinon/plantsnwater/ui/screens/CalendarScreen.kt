package dev.yinon.plantsnwater.ui.screens

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.yinon.plantsnwater.ui.CalendarViewModel
import dev.yinon.plantsnwater.ui.LocalAppContainer
import dev.yinon.plantsnwater.ui.PlantViewModelFactory

@Composable
fun CalendarScreen(onPlantClick: (Long) -> Unit) {
    val container = LocalAppContainer.current
    val viewModel: CalendarViewModel = viewModel(factory = PlantViewModelFactory(container))
    val plants by viewModel.plants.collectAsStateWithLifecycle()
    val grouped = plants.groupBy { it.nextWateringAt.formatDate() }

    LazyColumn {
        item {
            ScreenColumn {
                SectionTitle("Calendar")
                if (plants.isEmpty()) EmptyState("No schedule yet", "Add a plant to create watering tasks.")
            }
        }
        grouped.forEach { (date, datePlants) ->
            item { ScreenColumn { SectionTitle(date) } }
            items(datePlants, key = { it.id }) { plant ->
                ScreenColumn { PlantCard(plant, onClick = { onPlantClick(plant.id) }) }
            }
        }
    }
}
