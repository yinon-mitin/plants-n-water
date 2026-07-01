package dev.yinon.plantsnwater.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import dev.yinon.plantsnwater.R
import dev.yinon.plantsnwater.data.local.PlantEntity
import dev.yinon.plantsnwater.data.local.PlantPhotoEntity
import dev.yinon.plantsnwater.domain.WateringSchedule
import dev.yinon.plantsnwater.domain.WateringState
import dev.yinon.plantsnwater.ui.LocalAppContainer
import dev.yinon.plantsnwater.ui.nextWateringLocalDate
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

private val DateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM)

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
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp)
    ) {
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
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(8.dp)) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
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
                AssistChip(onClick = {}, label = { Text(plant.wateringLabelText()) })
            }
            Text(pluralStringResource(R.plurals.interval_days, plant.wateringIntervalDays, plant.wateringIntervalDays))
            if (onWatered != null || onSkip != null || onPostpone != null) {
                Spacer(Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (onWatered != null) Button(onClick = onWatered) { Text(stringResource(R.string.watered)) }
                    if (onSkip != null) OutlinedButton(onClick = onSkip) { Text(stringResource(R.string.skip)) }
                    if (onPostpone != null) OutlinedButton(onClick = onPostpone) { Text(stringResource(R.string.postpone_one_day)) }
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
        contentDescription = stringResource(R.string.latest_photo_of, plantName),
        modifier = Modifier
            .size(56.dp)
            .clip(RoundedCornerShape(8.dp)),
        contentScale = ContentScale.Crop
    )
}

@Composable
fun PlantEntity.wateringLabelText(): String {
    val state = WateringSchedule.classify(nextWateringLocalDate())
    return when (state) {
        WateringState.DueToday -> stringResource(R.string.watering_today)
        is WateringState.Overdue -> stringResource(R.string.watering_overdue, state.days)
        is WateringState.Upcoming -> stringResource(R.string.watering_upcoming, state.days)
    }
}

fun Long.formatDate(): String = Instant.ofEpochMilli(this)
    .atZone(ZoneId.systemDefault())
    .toLocalDate()
    .format(DateFormatter)

fun LocalDate.formatDate(): String = format(DateFormatter)

fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this)
    .atZone(ZoneId.systemDefault())
    .toLocalDate()

fun LocalDate.startOfDayMillis(): Long = atStartOfDay(ZoneId.systemDefault())
    .toInstant()
    .toEpochMilli()

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalDatePickerDialog(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit
) {
    val state = rememberDatePickerState(initialSelectedDateMillis = initialDate.startOfDayMillis())
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    state.selectedDateMillis?.let { onDateSelected(it.toLocalDate()) }
                    onDismiss()
                }
            ) {
                Text(stringResource(R.string.save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        }
    ) {
        DatePicker(state = state)
    }
}
