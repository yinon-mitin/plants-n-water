package dev.yinon.plantsnwater.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.yinon.plantsnwater.data.local.PlantEntity
import dev.yinon.plantsnwater.data.local.PlantPhotoEntity
import dev.yinon.plantsnwater.ui.LocalAppContainer
import dev.yinon.plantsnwater.ui.wateringLabel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val DateFormatter = DateTimeFormatter.ofPattern("MMM d")

@Composable
fun ScreenColumn(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        content()
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(text = text, style = MaterialTheme.typography.titleLarge)
}

@Composable
fun EmptyState(title: String, body: String) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(body, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun PlantCard(
    plant: PlantEntity,
    latestPhoto: PlantPhotoEntity? = null,
    onClick: () -> Unit,
    onWatered: (() -> Unit)? = null,
    onSkip: (() -> Unit)? = null,
    onPostpone: (() -> Unit)? = null
) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Row(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    latestPhoto?.let { PlantThumbnail(plant.name, it) }
                    Column {
                        Text(plant.name, style = MaterialTheme.typography.titleMedium)
                        val subtitle = listOfNotNull(plant.species, plant.location).joinToString(" · ")
                        if (subtitle.isNotBlank()) Text(subtitle, style = MaterialTheme.typography.bodyMedium)
                    }
                }
                AssistChip(onClick = {}, label = { Text(plant.wateringLabel()) })
            }
            Text("Every ${plant.wateringIntervalDays} day${if (plant.wateringIntervalDays == 1) "" else "s"}")
            if (onWatered != null || onSkip != null || onPostpone != null) {
                Spacer(Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (onWatered != null) Button(onClick = onWatered) { Text("Watered") }
                    if (onSkip != null) OutlinedButton(onClick = onSkip) { Text("Skip") }
                    if (onPostpone != null) OutlinedButton(onClick = onPostpone) { Text("+1 day") }
                }
            }
        }
    }
}

@Composable
private fun PlantThumbnail(plantName: String, photo: PlantPhotoEntity) {
    val container = LocalAppContainer.current
    AsyncImage(
        model = container.plantRepository.photoUri(photo),
        contentDescription = "Latest photo of $plantName",
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(8.dp)),
        contentScale = ContentScale.Crop
    )
}

fun Long.formatDate(): String = Instant.ofEpochMilli(this)
    .atZone(ZoneId.systemDefault())
    .toLocalDate()
    .format(DateFormatter)
