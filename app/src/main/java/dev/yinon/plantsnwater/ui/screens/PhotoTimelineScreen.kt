package dev.yinon.plantsnwater.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import dev.yinon.plantsnwater.R
import dev.yinon.plantsnwater.data.local.GrowthStage
import dev.yinon.plantsnwater.data.local.PendingPhotoCapture
import dev.yinon.plantsnwater.data.local.PlantPhotoEntity
import dev.yinon.plantsnwater.ui.LocalAppContainer
import dev.yinon.plantsnwater.ui.PhotoTimelineViewModel
import dev.yinon.plantsnwater.ui.PlantViewModelFactory
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.launch

@Composable
fun PhotoTimelineScreen(plantId: Long) {
    val container = LocalAppContainer.current
    val viewModel: PhotoTimelineViewModel = viewModel(factory = PlantViewModelFactory(container, plantId))
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var message by remember { mutableStateOf<String?>(null) }
    var pendingCapture by remember { mutableStateOf<PendingPhotoCapture?>(null) }
    var pendingPhoto by remember { mutableStateOf<PendingPhotoSource?>(null) }
    var previewPhoto by remember { mutableStateOf<PlantPhotoEntity?>(null) }
    var editingPhoto by remember { mutableStateOf<PlantPhotoEntity?>(null) }

    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        val capture = pendingCapture
        pendingCapture = null
        if (success && capture != null) {
            pendingPhoto = PendingPhotoSource.Camera(capture.localReference)
        } else {
            capture?.let { viewModel.discardCapturedPhoto(it.localReference) }
            message = context.getString(R.string.camera_cancelled)
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
        if (granted) {
            scope.launch {
                val capture = viewModel.prepareCameraCapture()
                pendingCapture = capture
                cameraLauncher.launch(capture.contentUri)
            }
        } else {
            message = context.getString(R.string.camera_denied)
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri == null) {
            message = context.getString(R.string.gallery_cancelled)
        } else {
            pendingPhoto = PendingPhotoSource.Gallery(uri)
        }
    }

    fun startCamera() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            scope.launch {
                val capture = viewModel.prepareCameraCapture()
                pendingCapture = capture
                cameraLauncher.launch(capture.contentUri)
            }
        } else {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    LazyColumn {
        item {
            ScreenColumn {
                SectionTitle(state.plant?.let { stringResource(R.string.photos_for_plant, it.name) } ?: stringResource(R.string.photo_timeline))
                Text(
                    stringResource(R.string.photo_privacy_note),
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = ::startCamera) { Text(stringResource(R.string.take_photo)) }
                    OutlinedButton(
                        onClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    ) {
                        Text(stringResource(R.string.choose_photo))
                    }
                }
                message?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                if (state.photos.isEmpty()) {
                    EmptyState(
                        stringResource(R.string.no_growth_photos),
                        stringResource(R.string.no_growth_photos_body)
                    )
                }
            }
        }

        items(state.photos, key = { it.id }) { photo ->
            ScreenColumn {
                PhotoTimelineCard(
                    photo = photo,
                    imageUri = viewModel.photoUri(photo),
                    onOpen = { previewPhoto = photo },
                    onEdit = { editingPhoto = photo },
                    onDelete = { viewModel.deletePhoto(photo.id) }
                )
            }
        }
    }

    pendingPhoto?.let { source ->
        PhotoMetadataDialog(
            title = stringResource(R.string.add_photo_details),
            initialCreatedAt = Instant.now().toEpochMilli(),
            initialNote = "",
            initialGrowthStage = null,
            initialCustomStage = "",
            onDismiss = {
                if (source is PendingPhotoSource.Camera) viewModel.discardCapturedPhoto(source.localReference)
                pendingPhoto = null
            },
            onSave = { createdAt, note, growthStage, customStage ->
                when (source) {
                    is PendingPhotoSource.Camera -> viewModel.addCapturedPhoto(
                        source.localReference,
                        createdAt,
                        note,
                        growthStage,
                        customStage
                    )
                    is PendingPhotoSource.Gallery -> viewModel.addGalleryPhoto(
                        source.uri,
                        createdAt,
                        note,
                        growthStage,
                        customStage
                    )
                }
                pendingPhoto = null
            }
        )
    }

    editingPhoto?.let { photo ->
        PhotoMetadataDialog(
            title = stringResource(R.string.edit_photo_details),
            initialCreatedAt = photo.createdAt,
            initialNote = photo.note.orEmpty(),
            initialGrowthStage = photo.growthStage,
            initialCustomStage = photo.customGrowthStage.orEmpty(),
            onDismiss = { editingPhoto = null },
            onSave = { createdAt, note, growthStage, customStage ->
                viewModel.updatePhoto(photo.id, createdAt, note, growthStage, customStage)
                editingPhoto = null
            }
        )
    }

    previewPhoto?.let { photo ->
        AlertDialog(
            onDismissRequest = { previewPhoto = null },
            confirmButton = {
                TextButton(onClick = { previewPhoto = null }) { Text(stringResource(R.string.close)) }
            },
            title = { Text(photo.createdAt.formatDate()) },
            text = {
                AsyncImage(
                    model = viewModel.photoUri(photo),
                    contentDescription = stringResource(R.string.photo_from_date, photo.createdAt.formatDate()),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 520.dp),
                    contentScale = ContentScale.Fit
                )
            }
        )
    }
}

@Composable
private fun PhotoTimelineCard(
    photo: PlantPhotoEntity,
    imageUri: Uri,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AsyncImage(
                model = imageUri,
                contentDescription = stringResource(R.string.photo_from_date, photo.createdAt.formatDate()),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp, max = 280.dp)
                    .clickable(onClick = onOpen),
                contentScale = ContentScale.Crop
            )
            Text(photo.createdAt.formatDate(), style = MaterialTheme.typography.titleMedium)
            photo.stageLabelText()?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            photo.note?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onOpen) { Text(stringResource(R.string.preview)) }
                OutlinedButton(onClick = onEdit) { Text(stringResource(R.string.edit)) }
                TextButton(onClick = onDelete) { Text(stringResource(R.string.delete)) }
            }
        }
    }
}

@Composable
private fun PhotoMetadataDialog(
    title: String,
    initialCreatedAt: Long,
    initialNote: String,
    initialGrowthStage: GrowthStage?,
    initialCustomStage: String,
    onDismiss: () -> Unit,
    onSave: (createdAt: Long, note: String?, growthStage: GrowthStage?, customStage: String?) -> Unit
) {
    var dateText by remember { mutableStateOf(initialCreatedAt.toDateInput()) }
    var note by remember { mutableStateOf(initialNote) }
    var growthStage by remember { mutableStateOf(initialGrowthStage) }
    var customStage by remember { mutableStateOf(initialCustomStage) }
    var showDatePicker by remember { mutableStateOf(false) }
    val parsedDate = remember(dateText) { dateText.toEpochMillisOrNull() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text(stringResource(R.string.date)) },
                    supportingText = { Text(stringResource(R.string.use_yyyy_mm_dd)) },
                    isError = parsedDate == null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedButton(onClick = { showDatePicker = true }) {
                    Text(stringResource(R.string.change_date))
                }
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text(stringResource(R.string.note)) },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(stringResource(R.string.growth_stage), style = MaterialTheme.typography.labelLarge)
                GrowthStageChips(selected = growthStage, onSelected = { growthStage = it })
                if (growthStage == GrowthStage.Custom) {
                    OutlinedTextField(
                        value = customStage,
                        onValueChange = { customStage = it },
                        label = { Text(stringResource(R.string.custom_stage)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
        confirmButton = {
            Button(
                enabled = parsedDate != null,
                onClick = {
                    onSave(parsedDate ?: initialCreatedAt, note, growthStage, customStage)
                }
            ) {
                Text(stringResource(R.string.save))
            }
        }
    )

    if (showDatePicker) {
        LocalDatePickerDialog(
            initialDate = parsedDate?.toLocalDate() ?: LocalDate.now(),
            onDismiss = { showDatePicker = false },
            onDateSelected = { dateText = it.toString() }
        )
    }
}

@Composable
private fun GrowthStageChips(
    selected: GrowthStage?,
    onSelected: (GrowthStage?) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        (listOf<GrowthStage?>(null) + GrowthStage.entries).chunked(2).forEach { rowStages ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                rowStages.forEach { stage ->
                    FilterChip(
                        selected = selected == stage,
                        onClick = { onSelected(stage) },
                        label = { Text(stage?.labelText() ?: stringResource(R.string.none)) }
                    )
                }
            }
        }
    }
}

private sealed interface PendingPhotoSource {
    data class Camera(val localReference: String) : PendingPhotoSource
    data class Gallery(val uri: Uri) : PendingPhotoSource
}

@Composable
private fun PlantPhotoEntity.stageLabelText(): String? =
    if (growthStage == GrowthStage.Custom) customGrowthStage?.takeIf { it.isNotBlank() } else growthStage?.labelText()

@Composable
private fun GrowthStage.labelText(): String = when (this) {
    GrowthStage.Seed -> stringResource(R.string.stage_seed)
    GrowthStage.Sprout -> stringResource(R.string.stage_sprout)
    GrowthStage.YoungPlant -> stringResource(R.string.stage_young_plant)
    GrowthStage.MaturePlant -> stringResource(R.string.stage_mature_plant)
    GrowthStage.Flowering -> stringResource(R.string.stage_flowering)
    GrowthStage.Fruiting -> stringResource(R.string.stage_fruiting)
    GrowthStage.Recovering -> stringResource(R.string.stage_recovering)
    GrowthStage.Custom -> stringResource(R.string.stage_custom)
}

private fun Long.toDateInput(): String =
    Instant.ofEpochMilli(this).atZone(ZoneId.systemDefault()).toLocalDate().toString()

private fun String.toEpochMillisOrNull(): Long? =
    runCatching {
        LocalDate.parse(this)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
    }.getOrNull()
