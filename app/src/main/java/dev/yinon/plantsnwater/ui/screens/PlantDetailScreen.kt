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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.yinon.plantsnwater.R
import dev.yinon.plantsnwater.data.local.PlantEntity
import dev.yinon.plantsnwater.data.local.PlantStatus
import dev.yinon.plantsnwater.ui.LocalAppContainer
import dev.yinon.plantsnwater.ui.PlantDetailViewModel
import dev.yinon.plantsnwater.ui.PlantViewModelFactory
import java.time.LocalDate

@Composable
fun PlantDetailScreen(plantId: Long, onPhotos: () -> Unit) {
    val container = LocalAppContainer.current
    val viewModel: PlantDetailViewModel = viewModel(factory = PlantViewModelFactory(container, plantId))
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var note by remember { mutableStateOf("") }
    var editingPlant by remember { mutableStateOf<PlantEntity?>(null) }
    val plant = state.plant

    LazyColumn {
        item {
            ScreenColumn {
                if (plant == null) {
                    EmptyState(stringResource(R.string.plant_not_found), stringResource(R.string.plant_not_found_body))
                } else {
                    SectionTitle(plant.name)
                    Card {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(stringResource(R.string.status_label, plant.status.name))
                            Text(stringResource(R.string.next_watering_label, plant.nextWateringAt.formatDate(), plant.wateringLabelText()))
                            plant.location?.let { Text(stringResource(R.string.location_label, it)) }
                            plant.species?.let { Text(stringResource(R.string.species_label, it)) }
                            plant.notes?.let { Text(it) }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = viewModel::markWatered) { Text(stringResource(R.string.watered)) }
                                OutlinedButton(onClick = viewModel::skip) { Text(stringResource(R.string.skip)) }
                                OutlinedButton(onClick = viewModel::postpone) { Text(stringResource(R.string.postpone_one_day)) }
                                OutlinedButton(onClick = { editingPlant = plant }) { Text(stringResource(R.string.edit)) }
                            }
                        }
                    }
                    OutlinedButton(onClick = onPhotos) {
                        Text(stringResource(R.string.photo_timeline_count, state.photoCount))
                    }
                    SectionTitle(stringResource(R.string.notes))
                    OutlinedTextField(note, { note = it }, label = { Text(stringResource(R.string.add_note)) }, modifier = Modifier.fillMaxWidth())
                    Button(onClick = { viewModel.addNote(note); note = "" }, enabled = note.isNotBlank()) {
                        Text(stringResource(R.string.save_note))
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
            ScreenColumn { SectionTitle(stringResource(R.string.watering_history)) }
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

    editingPlant?.let { current ->
        EditPlantDialog(
            plant = current,
            onDismiss = { editingPlant = null },
            onSave = { name, interval, lastWateredAt, species, location, notes, care, status ->
                viewModel.updatePlant(name, interval, lastWateredAt, species, location, notes, care, status)
                editingPlant = null
            }
        )
    }
}

@Composable
private fun EditPlantDialog(
    plant: PlantEntity,
    onDismiss: () -> Unit,
    onSave: (String, Int, Long?, String?, String?, String?, String?, PlantStatus) -> Unit
) {
    var name by remember { mutableStateOf(plant.name) }
    var interval by remember { mutableStateOf(plant.wateringIntervalDays.toString()) }
    var species by remember { mutableStateOf(plant.species.orEmpty()) }
    var location by remember { mutableStateOf(plant.location.orEmpty()) }
    var notes by remember { mutableStateOf(plant.notes.orEmpty()) }
    var care by remember { mutableStateOf(plant.careInstructions.orEmpty()) }
    var status by remember { mutableStateOf(plant.status) }
    var lastWatered by remember { mutableStateOf(plant.lastWateredAt?.toLocalDate()) }
    var showDatePicker by remember { mutableStateOf(false) }

    androidx.compose.material3.AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.edit_plant)) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, label = { Text(stringResource(R.string.plant_name)) }, singleLine = true)
                OutlinedTextField(interval, { interval = it.filter(Char::isDigit) }, label = { Text(stringResource(R.string.water_every_n_days)) }, singleLine = true)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(stringResource(R.string.last_watered), modifier = Modifier.weight(1f))
                    TextButton(onClick = { showDatePicker = true }) {
                        Text(lastWatered?.toString() ?: stringResource(R.string.last_watered_not_sure))
                    }
                }
                OutlinedTextField(species, { species = it }, label = { Text(stringResource(R.string.species_or_type)) })
                OutlinedTextField(location, { location = it }, label = { Text(stringResource(R.string.location)) })
                OutlinedTextField(notes, { notes = it }, label = { Text(stringResource(R.string.notes)) })
                OutlinedTextField(care, { care = it }, label = { Text(stringResource(R.string.care_instructions)) })
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    PlantStatus.entries.take(3).forEach {
                        FilterChip(selected = status == it, onClick = { status = it }, label = { Text(it.name) })
                    }
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) } },
        confirmButton = {
            Button(
                enabled = name.isNotBlank() && (interval.toIntOrNull() ?: 0) > 0,
                onClick = {
                    onSave(
                        name,
                        interval.toInt(),
                        lastWatered?.startOfDayMillis(),
                        species,
                        location,
                        notes,
                        care,
                        status
                    )
                }
            ) { Text(stringResource(R.string.save)) }
        }
    )

    if (showDatePicker) {
        LocalDatePickerDialog(
            initialDate = lastWatered ?: LocalDate.now(),
            onDismiss = { showDatePicker = false },
            onDateSelected = { lastWatered = it }
        )
    }
}
