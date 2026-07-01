package dev.yinon.plantsnwater.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import dev.yinon.plantsnwater.data.local.PlantStatus
import dev.yinon.plantsnwater.ui.AddPlantViewModel
import dev.yinon.plantsnwater.ui.LocalAppContainer
import dev.yinon.plantsnwater.ui.PlantViewModelFactory

@Composable
fun AddPlantScreen(onDone: (Long) -> Unit) {
    val container = LocalAppContainer.current
    val viewModel: AddPlantViewModel = viewModel(factory = PlantViewModelFactory(container))
    var name by remember { mutableStateOf("") }
    var interval by remember { mutableIntStateOf(2) }
    var species by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }
    var notes by remember { mutableStateOf("") }
    var care by remember { mutableStateOf("") }
    var status by remember { mutableStateOf(PlantStatus.Healthy) }
    var advanced by remember { mutableStateOf(false) }

    ScreenColumn {
        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            SectionTitle("Add plant")
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Plant name") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = interval.toString(),
                onValueChange = { interval = it.toIntOrNull()?.coerceAtLeast(1) ?: interval },
                label = { Text("Water every N days") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(1, 2, 3, 7).forEach { days ->
                    FilterChip(
                        selected = interval == days,
                        onClick = { interval = days },
                        label = { Text("$days d") }
                    )
                }
            }
            TextButton(onClick = { advanced = !advanced }) {
                Text(if (advanced) "Hide advanced fields" else "Advanced fields")
            }
            AnimatedVisibility(advanced) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(species, { species = it }, label = { Text("Species or type") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(location, { location = it }, label = { Text("Location") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(notes, { notes = it }, label = { Text("Notes") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                    OutlinedTextField(care, { care = it }, label = { Text("Care instructions") }, modifier = Modifier.fillMaxWidth(), minLines = 2)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        PlantStatus.entries.take(3).forEach {
                            FilterChip(selected = status == it, onClick = { status = it }, label = { Text(it.name) })
                        }
                    }
                }
            }
            Button(
                onClick = {
                    viewModel.addPlant(name, interval, species, location, notes, care, status, onDone)
                },
                enabled = name.isNotBlank(),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Create plant")
            }
        }
    }
}
