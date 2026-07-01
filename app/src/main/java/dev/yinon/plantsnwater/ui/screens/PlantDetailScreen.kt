package dev.yinon.plantsnwater.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.yinon.plantsnwater.ui.LocalAppContainer
import dev.yinon.plantsnwater.ui.PlantDetailViewModel
import dev.yinon.plantsnwater.ui.PlantViewModelFactory
import dev.yinon.plantsnwater.ui.wateringLabel

@Composable
fun PlantDetailScreen(plantId: Long, onPhotos: () -> Unit) {
    val container = LocalAppContainer.current
    val viewModel: PlantDetailViewModel = viewModel(factory = PlantViewModelFactory(container, plantId))
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var note by remember { mutableStateOf("") }
    val plant = state.plant

    LazyColumn {
        item {
            ScreenColumn {
                if (plant == null) {
                    EmptyState("Plant not found", "It may have been deleted.")
                } else {
                    SectionTitle(plant.name)
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Status: ${plant.status.name}")
                            Text("Next watering: ${plant.nextWateringAt.formatDate()} (${plant.wateringLabel()})")
                            plant.location?.let { Text("Location: $it") }
                            plant.species?.let { Text("Species: $it") }
                            plant.notes?.let { Text(it) }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = viewModel::markWatered) { Text("Watered") }
                                OutlinedButton(onClick = viewModel::skip) { Text("Skip") }
                                OutlinedButton(onClick = viewModel::postpone) { Text("+1 day") }
                            }
                        }
                    }
                    OutlinedButton(onClick = onPhotos) {
                        Text("Photo timeline (${state.photoCount})")
                    }
                    SectionTitle("Notes")
                    OutlinedTextField(note, { note = it }, label = { Text("Add note") }, modifier = Modifier.fillMaxWidth())
                    Button(onClick = { viewModel.addNote(note); note = "" }, enabled = note.isNotBlank()) {
                        Text("Save note")
                    }
                }
            }
        }
        items(state.notes) { saved ->
            ScreenColumn {
                Card { Text(saved, modifier = Modifier.padding(16.dp)) }
            }
        }
        item {
            ScreenColumn { SectionTitle("Watering history") }
        }
        items(state.events, key = { it.id }) { event ->
            ScreenColumn {
                Card {
                    Text(
                        "${event.status.name} · ${event.scheduledFor.formatDate()}",
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}
