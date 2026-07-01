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
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
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
            message = "Camera cancelled."
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
            message = "Camera permission denied. You can still choose an existing photo."
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri: Uri? ->
        if (uri == null) {
            message = "Gallery selection cancelled."
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
                SectionTitle(state.plant?.let { "${it.name} photos" } ?: "Photo timeline")
                Text(
                    "Track growth with local photos. Photos stay on this device unless you export them.",
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = ::startCamera) { Text("Take photo") }
                    OutlinedButton(
                        onClick = {
                            galleryLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    ) {
                        Text("Choose photo")
                    }
                }
                message?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
                if (state.photos.isEmpty()) {
                    EmptyState(
                        "No growth photos yet",
                        "Add photos from the camera or gallery to build this plant's timeline."
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
            title = "Add photo details",
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
            title = "Edit photo details",
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
                TextButton(onClick = { previewPhoto = null }) { Text("Close") }
            },
            title = { Text(photo.createdAt.formatDate()) },
            text = {
                AsyncImage(
                    model = viewModel.photoUri(photo),
                    contentDescription = "Photo from ${photo.createdAt.formatDate()}",
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
                contentDescription = "Plant photo from ${photo.createdAt.formatDate()}",
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp, max = 280.dp)
                    .clickable(onClick = onOpen),
                contentScale = ContentScale.Crop
            )
            Text(photo.createdAt.formatDate(), style = MaterialTheme.typography.titleMedium)
            photo.stageLabel()?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            photo.note?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onOpen) { Text("Preview") }
                OutlinedButton(onClick = onEdit) { Text("Edit") }
                TextButton(onClick = onDelete) { Text("Delete") }
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
    val parsedDate = remember(dateText) { dateText.toEpochMillisOrNull() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = dateText,
                    onValueChange = { dateText = it },
                    label = { Text("Date") },
                    supportingText = { Text("Use YYYY-MM-DD") },
                    isError = parsedDate == null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note") },
                    minLines = 2,
                    modifier = Modifier.fillMaxWidth()
                )
                Text("Growth stage", style = MaterialTheme.typography.labelLarge)
                GrowthStageChips(selected = growthStage, onSelected = { growthStage = it })
                if (growthStage == GrowthStage.Custom) {
                    OutlinedTextField(
                        value = customStage,
                        onValueChange = { customStage = it },
                        label = { Text("Custom stage") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        },
        confirmButton = {
            Button(
                enabled = parsedDate != null,
                onClick = {
                    onSave(parsedDate ?: initialCreatedAt, note, growthStage, customStage)
                }
            ) {
                Text("Save")
            }
        }
    )
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
                        label = { Text(stage?.label() ?: "None") }
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

private fun PlantPhotoEntity.stageLabel(): String? =
    if (growthStage == GrowthStage.Custom) customGrowthStage?.takeIf { it.isNotBlank() } else growthStage?.label()

private fun GrowthStage.label(): String = when (this) {
    GrowthStage.Seed -> "Seed"
    GrowthStage.Sprout -> "Sprout"
    GrowthStage.YoungPlant -> "Young plant"
    GrowthStage.MaturePlant -> "Mature plant"
    GrowthStage.Flowering -> "Flowering"
    GrowthStage.Fruiting -> "Fruiting"
    GrowthStage.Recovering -> "Recovering"
    GrowthStage.Custom -> "Custom"
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
